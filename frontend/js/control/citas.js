/**
 * Citas armadas a partir de las tablas: procedimientos, pasos en orden con su registro,
 * órdenes de examen y las listas que usan la búsqueda del paciente y la agenda del empleado.
 */

import { normalizarTexto } from "../comun/texto.js";
import { leerObservaciones } from "../entity/observaciones.js";
import { buscarPorId, mismoId } from "./tablas.js";
import { documentoDePersona } from "./documentos.js";
import { edadDe, rolesDePersona } from "./personas.js";

export function estadoDeCita(cita) {
  if (cita.consulta?.fechaFin) return { clave: "completada", texto: "Completada" };
  const pasos = cita.procedimientos.flatMap(({ pasos: lista }) => lista);
  if (pasos.length === 0) return { clave: "sin-pasos", texto: "Sin pasos" };
  const completados = pasos.filter(({ estado }) => estado === "completado").length;
  if (completados === pasos.length) return { clave: "completada", texto: "Completada" };
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

export function pasosDeProcedimiento(datos, idProcedimiento) {
  const pasos = datos.procedimientosPaso.filter((paso) => mismoId(paso.idProcedimiento, idProcedimiento));
  const ids = pasos.map((paso) => paso.idProcedimientoPaso);
  const secuencias = datos.procedimientosPasoSecuencia.filter(
    (secuencia) =>
      ids.some((id) => mismoId(id, secuencia.idProcedimientoPaso)) &&
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
    const paso = pasos.find((item) => String(item.idProcedimientoPaso) === String(id));
    if (paso) ordenados.push(paso);
    visitar(siguiente.find((item) => String(item.actual) === String(id))?.siguiente);
  };
  for (const paso of pasos) {
    if (!conPredecesor.some((id) => mismoId(id, paso.idProcedimientoPaso))) visitar(paso.idProcedimientoPaso);
  }
  for (const paso of pasos) {
    if (!visitados.some((id) => mismoId(id, paso.idProcedimientoPaso))) visitar(paso.idProcedimientoPaso);
  }
  return ordenados;
}

export function pasosConRegistros(datos, consultaProcedimiento, { finalizada = false } = {}) {
  const pasos = pasosDeProcedimiento(datos, consultaProcedimiento.idProcedimiento);
  const registros = datos.consultasProcedimientoPaso
    .filter((registro) => mismoId(registro.idConsultaProcedimiento, consultaProcedimiento.idConsultaProcedimiento))
    .sort((a, b) => new Date(a.fechaInicio ?? 0) - new Date(b.fechaInicio ?? 0));
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
  const registros = datos.consultasProcedimientoPaso
    .filter((registro) => mismoId(registro.idConsultaProcedimiento, consultaProcedimiento.idConsultaProcedimiento))
    .map((registro) => registro.idConsultaProcedimientoPaso);
  return datos.ordenesExamen
    .filter((orden) => registros.some((id) => mismoId(id, orden.idConsultaProcedimientoPaso)))
    .map((orden) => ({
      fecha: orden.fechaCreacion ?? null,
      texto: textoOrden(orden),
    }));
}

export function resumenCita(datos, consulta) {
  const personaRol = buscarPorId(datos, "personasRol", consulta.idPersonaRol);
  const paciente = personaRol ? buscarPorId(datos, "personas", personaRol.idPersona) : null;
  const documento = paciente ? documentoDePersona(datos, paciente.idPersona) : null;
  const clinica = personaRol ? buscarPorId(datos, "clinicas", personaRol.idClinica) : null;
  const procedimientos = datos.consultasProcedimiento
    .filter((consultaProcedimiento) => mismoId(consultaProcedimiento.idConsulta, consulta.idConsulta))
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

export function citasDePaciente(datos, { idPersona, idClinica = null, desde = null, hasta = null }) {
  const asignaciones = rolesDePersona(datos, idPersona).filter(
    ({ personaRol, rol }) =>
      normalizarTexto(rol?.nombre) === "paciente" &&
      rol?.activo !== false &&
      (!idClinica || String(personaRol.idClinica) === String(idClinica))
  );
  const ids = asignaciones.map(({ personaRol }) => personaRol.idPersonaRol);
  return datos.consultas
    .filter((consulta) => ids.some((id) => mismoId(id, consulta.idPersonaRol)) && dentroDeRango(consulta.fechaInicio, desde, hasta))
    .sort((a, b) => new Date(b.fechaInicio ?? 0) - new Date(a.fechaInicio ?? 0))
    .map((consulta) => resumenCita(datos, consulta));
}

export function citasDeEmpleado(datos, { idClinica, idRol, desde = null, hasta = null, idPaciente = null }) {
  return datos.consultas
    .filter((consulta) => {
      if (!dentroDeRango(consulta.fechaInicio, desde, hasta)) return false;
      const asignacion = buscarPorId(datos, "personasRol", consulta.idPersonaRol);
      if (!asignacion) return false;
      if (idClinica && String(asignacion.idClinica) !== String(idClinica)) return false;
      if (idPaciente && String(asignacion.idPersona) !== String(idPaciente)) return false;
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

export function consultaCompleta(datos, idConsulta) {
  const consulta = buscarPorId(datos, "consultas", idConsulta);
  if (!consulta) return null;
  return resumenCita(datos, consulta);
}

export function examenesAsignables(datos, idProcedimiento, idPaso = null) {
  const pasos = pasosDeProcedimiento(datos, idProcedimiento);
  const ids = pasos.map((paso) => paso.idProcedimientoPaso);
  const relaciones = datos.procedimientosPasoExamen.filter(
    (relacion) =>
      ids.some((id) => mismoId(id, relacion.idProcedimientoPaso)) &&
      (!idPaso || mismoId(relacion.idProcedimientoPaso, idPaso)) &&
      relacion.activo !== false
  );
  const unicos = [];
  for (const relacion of relaciones) {
    const examen = buscarPorId(datos, "examenes", relacion.idExamen);
    if (!examen || examen.activo === false) continue;
    const tipos = datos.examenesTipoExamen
      .filter((vinculo) => mismoId(vinculo.idExamen, examen.idExamen))
      .map((vinculo) => buscarPorId(datos, "tiposExamen", vinculo.idTipoExamen)?.nombre)
      .filter(Boolean);
    const existente = unicos.findIndex((item) => String(item.examen.idExamen) === String(examen.idExamen));
    const item = { examen, tipos };
    if (existente < 0) unicos.push(item);
    else unicos[existente] = item;
  }
  return unicos;
}
