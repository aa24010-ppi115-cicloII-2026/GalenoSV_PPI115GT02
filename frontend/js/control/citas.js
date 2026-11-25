/**
 * Citas armadas a partir de las tablas: procedimientos, pasos en orden con su registro,
 * órdenes de examen y las listas que usan la búsqueda del paciente y la agenda del empleado.
 */

import { normalizarTexto } from "../comun/texto.js";
import { leerObservaciones } from "../entity/observaciones.js";
import { buscarPorId, filasDonde, mismoId } from "./tablas.js";
import { documentoDePersona } from "./documentos.js";
import { edadDe, esRolPaciente, rolesDePersona } from "./personas.js";

/** Total de pasos y cuántos están completados; se usa en la agenda y en el registro de atención. */
export function conteoDePasos(pasos) {
  const completados = pasos.filter(({ estado }) => estado === "completado").length;
  return { total: pasos.length, completados };
}

/**
 * Estado general de una cita según su fecha de fin y sus pasos.
 * @param {object} cita resumen de la cita (ver resumenCita)
 * @returns {{clave: string, texto: string}} clave para estilos y texto para mostrar
 */
export function estadoDeCita(cita) {
  if (cita.consulta?.fechaFin) return { clave: "completada", texto: "Completada" };
  const pasos = cita.procedimientos.flatMap(({ pasos: lista }) => lista);
  const { total, completados } = conteoDePasos(pasos);
  if (total === 0) return { clave: "sin-pasos", texto: "Sin pasos" };
  if (completados === total) return { clave: "completada", texto: "Completada" };
  if (completados > 0 || pasos.some(({ registro }) => registro)) return { clave: "proceso", texto: "En proceso" };
  return { clave: "programada", texto: "Programada" };
}

function dentroDeRango(fecha, desde, hasta) {
  if (!fecha) return false;
  const valor = new Date(fecha).getTime();
  if (Number.isNaN(valor)) return false;
  if (desde) {
    const inicio = new Date(`${desde}T00:00:00`).getTime();
    if (Number.isNaN(inicio)) return false;
    if (valor < inicio) return false;
  }
  if (hasta) {
    const fin = new Date(`${hasta}T00:00:00`);
    if (Number.isNaN(fin.getTime())) return false;
    fin.setDate(fin.getDate() + 1);
    if (valor >= fin.getTime()) return false;
  }
  return true;
}

/**
 * Pasos de un procedimiento en el orden que marcan sus secuencias «después de».
 * @param {object} datos tablas de la API
 * @param {string} idProcedimiento
 * @returns {object[]}
 */
export function pasosDeProcedimiento(datos, idProcedimiento) {
  const pasos = filasDonde(datos.procedimientosPaso, "idProcedimiento", idProcedimiento);
  const ids = pasos.map((paso) => paso.idProcedimientoPaso);
  const secuencias = filasDonde(datos.procedimientosPasoSecuencia, "idProcedimientoPaso", ids).filter(
    (secuencia) =>
      ids.some((id) => mismoId(id, secuencia.idProcedimientoPasoReferencia)) &&
      normalizarTexto(secuencia.tipoSecuencia) === "despues_de"
  );
  const siguiente = [];
  const conPredecesor = [];
  for (const secuencia of secuencias) {
    siguiente.push({ actual: secuencia.idProcedimientoPasoReferencia, siguiente: secuencia.idProcedimientoPaso });
    conPredecesor.push(secuencia.idProcedimientoPaso);
  }
  const ordenados = [];
  const visitados = [];
  const visitar = (id) => {
    if (id === null || id === undefined || visitados.some((visitado) => mismoId(visitado, id))) return;
    visitados.push(id);
    const paso = pasos.find((item) => mismoId(item.idProcedimientoPaso, id));
    if (paso) ordenados.push(paso);
    visitar(siguiente.find((item) => mismoId(item.actual, id))?.siguiente);
  };
  for (const paso of pasos) {
    if (!conPredecesor.some((id) => mismoId(id, paso.idProcedimientoPaso))) visitar(paso.idProcedimientoPaso);
  }
  for (const paso of pasos) {
    if (!visitados.some((id) => mismoId(id, paso.idProcedimientoPaso))) visitar(paso.idProcedimientoPaso);
  }
  return ordenados;
}

/**
 * Pasos de un procedimiento de la consulta, cada uno con su registro, quién lo hizo, sus notas
 * y su estado (completado, actual o pendiente).
 * @param {object} datos tablas de la API
 * @param {object} consultaProcedimiento
 * @param {{finalizada?: boolean}} [opciones] con la consulta finalizada ningún paso queda actual
 * @returns {object[]}
 */
export function pasosConRegistros(datos, consultaProcedimiento, { finalizada = false } = {}) {
  const pasos = pasosDeProcedimiento(datos, consultaProcedimiento.idProcedimiento);
  const registros = filasDonde(
    datos.consultasProcedimientoPaso,
    "idConsultaProcedimiento",
    consultaProcedimiento.idConsultaProcedimiento
  ).sort((a, b) => new Date(a.fechaInicio ?? 0) - new Date(b.fechaInicio ?? 0));
  const observaciones = leerObservaciones(consultaProcedimiento.observaciones);
  let actualAsignado = false;
  return pasos.map((paso, indice) => {
    const registro = registros[indice] ?? null;
    const finalizado = registro ? normalizarTexto(registro.estado) === "finalizado" : false;
    let estado = "pendiente";
    if (finalizado) {
      estado = "completado";
    } else if (!actualAsignado && !finalizada) {
      // Solo el primer paso sin terminar está en curso. Un paso posterior puede conservar su
      // registro (se inició antes de retroceder) y se retoma al volver a avanzar.
      estado = "actual";
      actualAsignado = true;
    }
    const personaRol = registro ? buscarPorId(datos, "personasRol", registro.idPersonaRol) : null;
    const persona = personaRol ? buscarPorId(datos, "personas", personaRol.idPersona) : null;
    return {
      paso,
      registro,
      persona,
      indice,
      estado,
      notas: observaciones.pasos?.[paso.idProcedimientoPaso] ?? null,
      rol: buscarPorId(datos, "roles", paso.idRol),
    };
  });
}

function pasoActualDeCita(cita) {
  return cita.procedimientos
    .flatMap(({ pasos }) => pasos)
    .find(({ estado }) => estado === "actual") ?? null;
}

/** Texto de una orden de examen para mostrar: se guarda como «Examen: …», pero se lee sin repetir la palabra. */
export function textoOrden(orden) {
  return String(orden.indicaciones ?? "Sin indicaciones").replace(/^Examen:\s*/, "");
}

/** Órdenes de examen emitidas en los pasos de un procedimiento. */
function ordenesDeProcedimiento(datos, consultaProcedimiento) {
  const registros = filasDonde(
    datos.consultasProcedimientoPaso,
    "idConsultaProcedimiento",
    consultaProcedimiento.idConsultaProcedimiento
  ).map((registro) => registro.idConsultaProcedimientoPaso);
  return filasDonde(datos.ordenesExamen, "idConsultaProcedimientoPaso", registros)
    .map((orden) => ({
      fecha: orden.fechaCreacion ?? null,
      texto: textoOrden(orden),
    }));
}

/**
 * Todo lo que se muestra de una cita: paciente con documento y edad, clínica y procedimientos
 * con sus pasos, notas y órdenes de examen.
 * @param {object} datos tablas de la API
 * @param {object} consulta
 * @returns {object}
 */
export function resumenCita(datos, consulta) {
  const personaRol = buscarPorId(datos, "personasRol", consulta.idPersonaRol);
  const paciente = personaRol ? buscarPorId(datos, "personas", personaRol.idPersona) : null;
  const documento = paciente ? documentoDePersona(datos, paciente.idPersona) : null;
  const clinica = personaRol ? buscarPorId(datos, "clinicas", personaRol.idClinica) : null;
  const procedimientos = filasDonde(datos.consultasProcedimiento, "idConsulta", consulta.idConsulta)
    .map((consultaProcedimiento) => ({
      consultaProcedimiento,
      procedimiento: buscarPorId(datos, "procedimientos", consultaProcedimiento.idProcedimiento),
      notas: leerObservaciones(consultaProcedimiento.observaciones),
      pasos: pasosConRegistros(datos, consultaProcedimiento, { finalizada: Boolean(consulta.fechaFin) }),
      ordenes: ordenesDeProcedimiento(datos, consultaProcedimiento),
    }));
  return {
    consulta,
    paciente: paciente ? { ...paciente, documento, edad: edadDe(paciente) } : null,
    clinica,
    procedimientos,
  };
}

/**
 * Citas de un paciente, de la más reciente a la más antigua.
 * @param {object} datos tablas de la API
 * @param {{idPersona: string, idClinica?: string|null, desde?: string|null, hasta?: string|null}} filtro
 *   fechas en formato YYYY-MM-DD, ambas incluidas
 * @returns {object[]} resúmenes de cita
 */
export function citasDePaciente(datos, { idPersona, idClinica = null, desde = null, hasta = null }) {
  const asignaciones = rolesDePersona(datos, idPersona).filter(
    ({ personaRol, rol }) => esRolPaciente(rol) && (!idClinica || mismoId(personaRol.idClinica, idClinica))
  );
  const ids = asignaciones.map(({ personaRol }) => personaRol.idPersonaRol);
  return filasDonde(datos.consultas, "idPersonaRol", ids)
    .filter((consulta) => dentroDeRango(consulta.fechaInicio, desde, hasta))
    .sort((a, b) => new Date(b.fechaInicio ?? 0) - new Date(a.fechaInicio ?? 0))
    .map((consulta) => resumenCita(datos, consulta));
}

/**
 * Agenda de un empleado: citas de su clínica cuyo paso en curso le toca a su rol (o, ya finalizadas,
 * en las que participó ese rol), en orden de hora.
 * @param {object} datos tablas de la API
 * @param {{idClinica: string, idRol?: string, desde?: string|null, hasta?: string|null, idPaciente?: string|null}} filtro
 * @returns {object[]} resúmenes de cita
 */
export function citasDeEmpleado(datos, { idClinica, idRol, desde = null, hasta = null, idPaciente = null }) {
  return datos.consultas
    .filter((consulta) => {
      if (!dentroDeRango(consulta.fechaInicio, desde, hasta)) return false;
      const asignacion = buscarPorId(datos, "personasRol", consulta.idPersonaRol);
      if (!asignacion) return false;
      if (idClinica && !mismoId(asignacion.idClinica, idClinica)) return false;
      if (idPaciente && !mismoId(asignacion.idPersona, idPaciente)) return false;
      return true;
    })
    .map((consulta) => resumenCita(datos, consulta))
    .filter((cita) => {
      if (!idRol) return true;
      const actual = pasoActualDeCita(cita);
      if (actual) return mismoId(actual.paso.idRol, idRol);
      // Una consulta finalizada no tiene paso en curso; se conserva en la agenda del rol
      // que participa en sus procedimientos para poder revisar lo registrado.
      return cita.procedimientos.some(({ pasos }) =>
        pasos.some(({ paso }) => mismoId(paso.idRol, idRol))
      );
    })
    .sort((a, b) => new Date(a.consulta.fechaInicio ?? 0) - new Date(b.consulta.fechaInicio ?? 0));
}

/**
 * Resumen de una consulta por su id.
 * @param {object} datos tablas de la API
 * @param {string} idConsulta
 * @returns {object|null} null si no existe
 */
export function consultaCompleta(datos, idConsulta) {
  const consulta = buscarPorId(datos, "consultas", idConsulta);
  if (!consulta) return null;
  return resumenCita(datos, consulta);
}

/**
 * Exámenes activos que se pueden ordenar en un procedimiento (o en uno de sus pasos), con sus tipos.
 * @param {object} datos tablas de la API
 * @param {string} idProcedimiento
 * @param {string|null} [idPaso] limita a los exámenes de ese paso
 * @returns {{examen: object, tipos: string[]}[]}
 */
export function examenesAsignables(datos, idProcedimiento, idPaso = null) {
  const pasos = pasosDeProcedimiento(datos, idProcedimiento);
  const ids = pasos.map((paso) => paso.idProcedimientoPaso);
  const buscados = idPaso ? ids.filter((id) => mismoId(id, idPaso)) : ids;
  const relaciones = filasDonde(datos.procedimientosPasoExamen, "idProcedimientoPaso", buscados).filter(
    (relacion) => relacion.activo !== false
  );
  const unicos = [];
  for (const relacion of relaciones) {
    const examen = buscarPorId(datos, "examenes", relacion.idExamen);
    if (!examen || examen.activo === false) continue;
    const tipos = filasDonde(datos.examenesTipoExamen, "idExamen", examen.idExamen)
      .map((vinculo) => buscarPorId(datos, "tiposExamen", vinculo.idTipoExamen)?.nombre)
      .filter(Boolean);
    const existente = unicos.findIndex((item) => mismoId(item.examen.idExamen, examen.idExamen));
    const item = { examen, tipos };
    if (existente < 0) unicos.push(item);
    else unicos[existente] = item;
  }
  return unicos;
}
