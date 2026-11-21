import { listarTodo } from "./api.js";
import { listarPendientes } from "./pendientes.js";
import { FUENTES, CAMPO_ID, CLAVE_DE_RUTA } from "./recursos.js";
import { nombreCompleto, normalizarTexto } from "./texto.js";

/** Busca un registro en la lista recibida del backend usando su identificador. */
function buscarPorId(datos, clave, id) {
  if (id === null || id === undefined) return null;
  const campo = CAMPO_ID[clave];
  return (datos[clave] ?? []).find((item) => String(item[campo]) === String(id)) ?? null;
}

function mismoId(a, b) {
  return a !== null && a !== undefined && b !== null && b !== undefined && String(a) === String(b);
}

/**
 * Muestra sobre los datos del servidor los cambios guardados sin conexión, para que la pantalla
 * refleje lo hecho antes de sincronizar. Trabaja sobre copias: los datos originales no cambian.
 */
export function aplicarPendientes(datos, pendientes) {
  if (pendientes.length === 0) return datos;
  const tablas = { ...datos };
  for (const { metodo, ruta, cuerpo } of pendientes) {
    const [recurso, id] = ruta.split("/");
    const clave = CLAVE_DE_RUTA[recurso];
    if (!clave) continue;
    const campo = CAMPO_ID[clave];
    if (tablas[clave] === datos[clave]) tablas[clave] = [...(datos[clave] ?? [])];
    const lista = tablas[clave];
    const indice = lista.findIndex((item) => String(item[campo]) === id);
    // Un alta sin id del cliente no se puede referenciar todavía; se verá al sincronizar.
    if (metodo === "POST" && cuerpo?.[campo]) {
      const existente = lista.findIndex((item) => String(item[campo]) === String(cuerpo[campo]));
      if (existente >= 0) lista[existente] = { ...lista[existente], ...cuerpo };
      else lista.push({ ...cuerpo });
    }
    else if (metodo === "PUT" && indice >= 0) lista[indice] = { ...lista[indice], ...cuerpo };
    else if (metodo === "DELETE" && indice >= 0) lista.splice(indice, 1);
  }
  return tablas;
}

let cache = null;
let carga = null;
let revisionCache = 0;

export async function cargarTodo({ recargar = false } = {}) {
  if (recargar) {
    revisionCache += 1;
    cache = null;
    carga = null;
  }
  if (cache) return aplicarPendientes(cache, listarPendientes());
  if (!carga) {
    const revision = revisionCache;
    carga = Promise.all(
      Object.entries(FUENTES).map(async ([clave, ruta]) => [clave, await listarTodo(ruta)])
    ).then((entradas) => {
      const datos = Object.fromEntries(entradas);
      if (revision === revisionCache) cache = datos;
      return datos;
    });
  }
  const solicitud = carga;
  const revision = revisionCache;
  try {
    const datos = await solicitud;
    if (revision !== revisionCache) return cargarTodo();
    return aplicarPendientes(datos, listarPendientes());
  } finally {
    if (carga === solicitud) carga = null;
  }
}

export async function recargarTablas(claves) {
  const desconocidas = claves.filter((clave) => !Object.hasOwn(FUENTES, clave));
  if (desconocidas.length) throw new Error(`No se pueden recargar estos recursos: ${desconocidas.join(", ")}.`);
  if (claves.length === 0) return cargarTodo();
  if (!cache) await cargarTodo();

  const revision = revisionCache;
  const base = cache;
  const entradas = await Promise.all(
    claves.map(async (clave) => [clave, await listarTodo(FUENTES[clave])])
  );
  if (revision !== revisionCache) return cargarTodo();
  cache = { ...(cache ?? base), ...Object.fromEntries(entradas) };
  return aplicarPendientes(cache, listarPendientes());
}

export function limpiarCache() {
  revisionCache += 1;
  cache = null;
  carga = null;
}

export function documentoDe(datos, idTipoDocumento, valor) {
  return datos.documentos.find(
    (documento) =>
      mismoId(documento.idTipoDocumento, idTipoDocumento) &&
      normalizarTexto(documento.valor) === normalizarTexto(valor)
  );
}

export function tiposDocumentoActivos(datos) {
  return [...(datos.tiposDocumento ?? [])]
    .filter((tipo) => tipo.activo !== false)
    .sort((a, b) => String(a.nombre).localeCompare(String(b.nombre)));
}

export function tipoDocumentoElegido(datos, id) {
  return (datos?.tiposDocumento ?? []).find((tipo) => mismoId(tipo.idTipoDocumento, id)) ?? null;
}

export function indicacionesDocumento(tipo) {
  return {
    texto: tipo?.indicaciones || (tipo?.expresionRegular ? `Formato: ${tipo.expresionRegular}` : ""),
    ejemplo: tipo?.nombre === "DUI" ? "Ej. 00000000-0" : "",
  };
}

export function mensajeFormatoDocumento(tipo) {
  const detalle = tipo?.indicaciones || tipo?.expresionRegular || "el formato del documento";
  return `El número${tipo?.nombre ? ` de ${tipo.nombre}` : ""} no tiene el formato esperado: ${detalle}.`;
}

/**
 * Valida el número contra la expresión regular del tipo de documento.
 * Un tipo sin expresión, o con una mal escrita en el catálogo, no bloquea al usuario.
 */
export function cumpleFormato(tipo, valor) {
  if (!tipo?.expresionRegular) return true;
  try {
    return new RegExp(tipo.expresionRegular).test(valor);
  } catch {
    return true;
  }
}

export function errorDeFormatoDocumento(tipo, valor) {
  return cumpleFormato(tipo, valor) ? "" : mensajeFormatoDocumento(tipo);
}

export function personaDeDocumento(datos, idTipoDocumento, valor) {
  const documento = documentoDe(datos, idTipoDocumento, valor);
  return documento ? buscarPorId(datos, "personas", documento.idPersona) : null;
}

function rolesDePersona(datos, idPersona) {
  return datos.personasRol
    .filter((personaRol) => mismoId(personaRol.idPersona, idPersona))
    .map((personaRol) => ({
      personaRol,
      rol: buscarPorId(datos, "roles", personaRol.idRol),
      clinica: buscarPorId(datos, "clinicas", personaRol.idClinica),
    }));
}

export function clinicasDePaciente(datos, idPersona) {
  const asignaciones = rolesDePersona(datos, idPersona);
  const pacientes = asignaciones.filter(
    ({ rol }) => normalizarTexto(rol?.nombre) === "paciente" && rol?.activo !== false
  );
  const unicas = [];
  for (const { clinica } of pacientes) {
    if (clinica && !unicas.some((item) => String(item.idClinica) === String(clinica.idClinica))) unicas.push(clinica);
  }
  return unicas;
}

export function empleadoDePersona(datos, idPersona) {
  return rolesDePersona(datos, idPersona).filter(
    ({ rol }) => rol && normalizarTexto(rol.nombre) !== "paciente" && rol.activo !== false
  );
}

export function estadoDeCita(cita) {
  if (cita.consulta?.fechaFin) return { clave: "completada", texto: "Completada" };
  const pasos = cita.procedimientos.flatMap(({ pasos: lista }) => lista);
  if (pasos.length === 0) return { clave: "sin-pasos", texto: "Sin pasos" };
  const completados = pasos.filter(({ estado }) => estado === "completado").length;
  if (completados === pasos.length) return { clave: "completada", texto: "Completada" };
  if (completados > 0 || pasos.some(({ registro }) => registro)) return { clave: "proceso", texto: "En proceso" };
  return { clave: "programada", texto: "Programada" };
}

export function edadDe(persona) {
  if (!persona?.fechaNacimiento) return null;
  const nacimiento = new Date(persona.fechaNacimiento);
  if (Number.isNaN(nacimiento.getTime())) return null;
  // La fecha de nacimiento llega a medianoche UTC: se compara con el día local de hoy en UTC.
  const ahora = new Date();
  const hoy = new Date(Date.UTC(ahora.getFullYear(), ahora.getMonth(), ahora.getDate()));
  let edad = hoy.getUTCFullYear() - nacimiento.getUTCFullYear();
  const mes = hoy.getUTCMonth() - nacimiento.getUTCMonth();
  if (mes < 0 || (mes === 0 && hoy.getUTCDate() < nacimiento.getUTCDate())) edad -= 1;
  return edad >= 0 ? edad : null;
}

const ETIQUETAS_PASO = { completado: "Completado", actual: "En curso", pendiente: "Pendiente" };

/**
 * Registro que se muestra de un paso. Un paso pendiente puede conservar el registro de antes de
 * retroceder (para retomarlo al avanzar), pero no se muestra: no tiene fecha ni responsable vigentes.
 */
export function registroVisible({ estado, registro }) {
  return estado === "pendiente" ? null : registro ?? null;
}

export function etiquetaPaso(estado, iniciado = true) {
  if (estado === "actual" && !iniciado) return "Por iniciar";
  return ETIQUETAS_PASO[estado] ?? String(estado ?? "");
}

/** Resumen en una línea de lo registrado en un paso: diagnóstico, notas y plan. */
export function notaDelPaso({ notas = "", diagnostico = "", plan = "" } = {}) {
  return [diagnostico && `CIE-10: ${diagnostico}`, notas && `Notas: ${notas}`, plan && `Plan: ${plan}`]
    .filter(Boolean)
    .join(" | ");
}

export function motivoDe(consulta) {
  const texto = String(consulta?.motivo || consulta?.observaciones || "").trim();
  if (!texto) return "Consulta registrada";
  return texto.length > 120 ? `${texto.slice(0, 117)}…` : texto;
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

export function documentoDePersona(datos, idPersona) {
  const documentos = datos.documentos.filter((documento) => mismoId(documento.idPersona, idPersona));
  if (documentos.length === 0) return null;
  const documento = documentos[0];
  const tipo = buscarPorId(datos, "tiposDocumento", documento.idTipoDocumento);
  return {
    idDocumento: documento.idDocumento,
    valor: documento.valor,
    tipo: tipo?.nombre ?? "Documento",
  };
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

export function pacientesDeClinica(datos, idClinica) {
  const vistos = [];
  for (const personaRol of datos.personasRol) {
    if (String(personaRol.idClinica) !== String(idClinica)) continue;
    const rol = buscarPorId(datos, "roles", personaRol.idRol);
    if (normalizarTexto(rol?.nombre) !== "paciente" || rol?.activo === false) continue;
    const persona = buscarPorId(datos, "personas", personaRol.idPersona);
    if (persona && !vistos.some((item) => mismoId(item.idPersona, persona.idPersona))) vistos.push(persona);
  }
  return vistos.sort((a, b) => nombreCompleto(a).localeCompare(nombreCompleto(b)));
}

export function leerObservaciones(valor) {
  try {
    const datos = JSON.parse(valor);
    if (datos && typeof datos === "object" && !Array.isArray(datos)) {
      return {
        notas: datos.notas ?? "",
        diagnostico: datos.diagnostico ?? "",
        plan: datos.plan ?? "",
        pasos: datos.pasos && typeof datos.pasos === "object" ? datos.pasos : {},
      };
    }
  } catch {
    // Texto libre guardado por versiones anteriores del sistema.
  }
  return { notas: valor ?? "", diagnostico: "", plan: "", pasos: {} };
}

export function serializarObservaciones({ notas = "", diagnostico = "", plan = "", pasos = {} }) {
  return JSON.stringify({ notas, diagnostico, plan, pasos });
}
