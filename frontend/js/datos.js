import { listarTodo } from "./api.js";

const FUENTES = {
  clinicas: "clinicas",
  roles: "roles",
  personas: "personas",
  personasRol: "personas-rol",
  documentos: "documentos",
  tiposDocumento: "tipos-documento",
  consultas: "consultas",
  consultasProcedimiento: "consultas-procedimiento",
  consultasProcedimientoPaso: "consultas-procedimiento-paso",
  procedimientos: "procedimientos",
  procedimientosPaso: "procedimientos-paso",
  procedimientosPasoSecuencia: "procedimientos-paso-secuencia",
  procedimientosPasoExamen: "procedimientos-paso-examen",
  examenes: "examenes",
  tiposExamen: "tipos-examen",
  examenesTipoExamen: "examenes-tipo-examen",
  ordenesExamen: "ordenes-examen",
  examenesResultado: "examenes-resultado",
  mediosContacto: "medios-contacto",
};

const CAMPO_ID = {
  clinicas: "idClinica",
  roles: "idRol",
  personas: "idPersona",
  personasRol: "idPersonaRol",
  documentos: "idDocumento",
  tiposDocumento: "idTipoDocumento",
  consultas: "idConsulta",
  consultasProcedimiento: "idConsultaProcedimiento",
  consultasProcedimientoPaso: "idConsultaProcedimientoPaso",
  procedimientos: "idProcedimiento",
  procedimientosPaso: "idProcedimientoPaso",
  procedimientosPasoSecuencia: "idProcedimientoPasoSecuencia",
  procedimientosPasoExamen: "idProcedimientoPasoExamen",
  examenes: "idExamen",
  tiposExamen: "idTipoExamen",
  examenesTipoExamen: "idExamenTipoExamen",
  ordenesExamen: "idOrdenExamen",
  examenesResultado: "idExamenResultado",
  mediosContacto: "idMedioContacto",
};

let cache = null;
let carga = null;

export async function cargarTodo({ recargar = false } = {}) {
  if (cache && !recargar) return cache;
  if (carga && !recargar) return carga;
  carga = Promise.all(
    Object.entries(FUENTES).map(async ([clave, ruta]) => [clave, await listarTodo(ruta)])
  ).then((entradas) => {
    cache = Object.fromEntries(entradas);
    cache.porId = {};
    for (const [clave, lista] of Object.entries(cache)) {
      if (!Array.isArray(lista)) continue;
      const campo = CAMPO_ID[clave];
      cache.porId[clave] = new Map(campo ? lista.map((item) => [item[campo], item]) : []);
    }
    return cache;
  });
  try {
    return await carga;
  } finally {
    carga = null;
  }
}

export function limpiarCache() {
  cache = null;
  carga = null;
}

const normalizar = (valor) => String(valor ?? "").trim().toLowerCase();

export function documentoDe(datos, idTipoDocumento, valor) {
  return datos.documentos.find(
    (documento) =>
      String(documento.idTipoDocumento) === String(idTipoDocumento) &&
      normalizar(documento.valor) === normalizar(valor)
  );
}

export function personaDeDocumento(datos, idTipoDocumento, valor) {
  const documento = documentoDe(datos, idTipoDocumento, valor);
  return documento ? datos.porId.personas.get(documento.idPersona) ?? null : null;
}

export function rolesDePersona(datos, idPersona) {
  return datos.personasRol
    .filter((personaRol) => personaRol.idPersona === idPersona)
    .map((personaRol) => ({
      personaRol,
      rol: datos.porId.roles.get(personaRol.idRol) ?? null,
      clinica: datos.porId.clinicas.get(personaRol.idClinica) ?? null,
    }));
}

export function clinicasDePaciente(datos, idPersona) {
  const asignaciones = rolesDePersona(datos, idPersona);
  const pacientes = asignaciones.filter(({ rol }) => normalizar(rol?.nombre) === "paciente");
  const base = pacientes.length > 0 ? pacientes : asignaciones;
  const unicas = new Map();
  for (const { clinica } of base) {
    if (clinica) unicas.set(clinica.idClinica, clinica);
  }
  return [...unicas.values()];
}

export function empleadoDePersona(datos, idPersona) {
  return rolesDePersona(datos, idPersona).filter(
    ({ rol }) => rol && normalizar(rol.nombre) !== "paciente" && rol.activo !== false
  );
}

export function estadoDeCita(cita) {
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
  const hoy = new Date();
  let edad = hoy.getFullYear() - nacimiento.getFullYear();
  const mes = hoy.getMonth() - nacimiento.getMonth();
  if (mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())) edad -= 1;
  return edad >= 0 ? edad : null;
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
    if (valor < inicio) return false;
  }
  if (hasta) {
    const fin = new Date(`${hasta}T23:59:59`).getTime();
    if (valor > fin) return false;
  }
  return true;
}

export function pasosDeProcedimiento(datos, idProcedimiento) {
  const pasos = datos.procedimientosPaso.filter((paso) => paso.idProcedimiento === idProcedimiento);
  const ids = new Set(pasos.map((paso) => paso.idProcedimientoPaso));
  const secuencias = datos.procedimientosPasoSecuencia.filter(
    (secuencia) =>
      ids.has(secuencia.idProcedimientoPaso) &&
      ids.has(secuencia.idProcedimientoPasoReferencia) &&
      normalizar(secuencia.tipoSecuencia) === "despues_de"
  );
  const siguiente = new Map();
  const conPredecesor = new Set();
  for (const secuencia of secuencias) {
    siguiente.set(secuencia.idProcedimientoPasoReferencia, secuencia.idProcedimientoPaso);
    conPredecesor.add(secuencia.idProcedimientoPaso);
  }
  const porId = new Map(pasos.map((paso) => [paso.idProcedimientoPaso, paso]));
  const ordenados = [];
  const visitados = new Set();
  const visitar = (id) => {
    if (!id || visitados.has(id)) return;
    visitados.add(id);
    const paso = porId.get(id);
    if (paso) ordenados.push(paso);
    visitar(siguiente.get(id));
  };
  for (const paso of pasos) {
    if (!conPredecesor.has(paso.idProcedimientoPaso)) visitar(paso.idProcedimientoPaso);
  }
  for (const paso of pasos) {
    if (!visitados.has(paso.idProcedimientoPaso)) visitar(paso.idProcedimientoPaso);
  }
  return ordenados;
}

export function pasosConRegistros(datos, consultaProcedimiento) {
  const pasos = pasosDeProcedimiento(datos, consultaProcedimiento.idProcedimiento);
  const registros = datos.consultasProcedimientoPaso
    .filter((registro) => registro.idConsultaProcedimiento === consultaProcedimiento.idConsultaProcedimiento)
    .sort((a, b) => new Date(a.fechaInicio ?? 0) - new Date(b.fechaInicio ?? 0));
  const observaciones = leerObservaciones(consultaProcedimiento.observaciones);
  let actualAsignado = false;
  return pasos.map((paso, indice) => {
    const registro = registros[indice] ?? null;
    const finalizado = registro ? normalizar(registro.estado) === "finalizado" : false;
    let estado = "pendiente";
    if (finalizado) {
      estado = "completado";
    } else if (registro) {
      estado = "actual";
      actualAsignado = true;
    } else if (!actualAsignado) {
      estado = "actual";
      actualAsignado = true;
    }
    const personaRol = registro ? datos.porId.personasRol.get(registro.idPersonaRol) ?? null : null;
    const persona = personaRol ? datos.porId.personas.get(personaRol.idPersona) ?? null : null;
    return {
      paso,
      registro,
      personaRol,
      persona,
      indice,
      estado,
      notas: observaciones.pasos?.[paso.idProcedimientoPaso] ?? null,
      rol: paso.idRol ? datos.porId.roles.get(paso.idRol) ?? null : null,
    };
  });
}

export function documentoDePersona(datos, idPersona) {
  const documentos = datos.documentos.filter((documento) => documento.idPersona === idPersona);
  if (documentos.length === 0) return null;
  const documento = documentos[0];
  const tipo = datos.porId.tiposDocumento.get(documento.idTipoDocumento);
  return {
    idDocumento: documento.idDocumento,
    valor: documento.valor,
    tipo: tipo?.nombre ?? "Documento",
  };
}

export function resumenCita(datos, consulta) {
  const personaRol = datos.porId.personasRol.get(consulta.idPersonaRol) ?? null;
  const paciente = personaRol ? datos.porId.personas.get(personaRol.idPersona) ?? null : null;
  const documento = paciente ? documentoDePersona(datos, paciente.idPersona) : null;
  const clinica = personaRol ? datos.porId.clinicas.get(personaRol.idClinica) ?? null : null;
  const procedimientos = datos.consultasProcedimiento
    .filter((consultaProcedimiento) => consultaProcedimiento.idConsulta === consulta.idConsulta)
    .map((consultaProcedimiento) => ({
      consultaProcedimiento,
      procedimiento: datos.porId.procedimientos.get(consultaProcedimiento.idProcedimiento) ?? null,
      notas: leerObservaciones(consultaProcedimiento.observaciones),
      pasos: pasosConRegistros(datos, consultaProcedimiento),
    }));
  return {
    consulta,
    personaRol,
    paciente: paciente ? { ...paciente, documento, edad: edadDe(paciente) } : null,
    clinica,
    procedimientos,
  };
}

export function citasDePaciente(datos, { idPersona, idClinica = null, desde = null, hasta = null }) {
  const asignaciones = rolesDePersona(datos, idPersona).filter(
    ({ personaRol }) => !idClinica || String(personaRol.idClinica) === String(idClinica)
  );
  const ids = new Set(asignaciones.map(({ personaRol }) => personaRol.idPersonaRol));
  return datos.consultas
    .filter((consulta) => ids.has(consulta.idPersonaRol) && dentroDeRango(consulta.fechaInicio, desde, hasta))
    .sort((a, b) => new Date(b.fechaInicio ?? 0) - new Date(a.fechaInicio ?? 0))
    .map((consulta) => resumenCita(datos, consulta));
}

export function citasDeEmpleado(datos, { idClinica, idRol, desde = null, hasta = null, paciente = "" }) {
  const filtroPaciente = normalizar(paciente);
  return datos.consultas
    .map((consulta) => resumenCita(datos, consulta))
    .filter((cita) => {
      if (idClinica && String(cita.clinica?.idClinica) !== String(idClinica)) return false;
      if (!dentroDeRango(cita.consulta.fechaInicio, desde, hasta)) return false;
      if (filtroPaciente) {
        const nombre = normalizar(`${cita.paciente?.nombres ?? ""} ${cita.paciente?.apellidos ?? ""}`);
        if (!nombre.includes(filtroPaciente)) return false;
      }
      if (idRol) {
        const tieneRol = cita.procedimientos.some(({ pasos }) =>
          pasos.some(({ paso }) => String(paso.idRol) === String(idRol))
        );
        if (!tieneRol) return false;
      }
      return true;
    })
    .sort((a, b) => new Date(a.consulta.fechaInicio ?? 0) - new Date(b.consulta.fechaInicio ?? 0));
}

export function consultaCompleta(datos, idConsulta) {
  const consulta =
    datos.porId.consultas.get(idConsulta) ??
    datos.consultas.find((item) => String(item.idConsulta) === String(idConsulta)) ??
    null;
  if (!consulta) return null;
  return resumenCita(datos, consulta);
}

export function examenesAsignables(datos, idProcedimiento) {
  const pasos = pasosDeProcedimiento(datos, idProcedimiento);
  const ids = new Set(pasos.map((paso) => paso.idProcedimientoPaso));
  const relaciones = datos.procedimientosPasoExamen.filter(
    (relacion) => ids.has(relacion.idProcedimientoPaso) && relacion.activo !== false
  );
  const unicos = new Map();
  for (const relacion of relaciones) {
    const examen = datos.porId.examenes.get(relacion.idExamen);
    if (!examen || examen.activo === false) continue;
    const tipos = datos.examenesTipoExamen
      .filter((vinculo) => vinculo.idExamen === examen.idExamen)
      .map((vinculo) => datos.porId.tiposExamen.get(vinculo.idTipoExamen)?.nombre)
      .filter(Boolean);
    unicos.set(examen.idExamen, { examen, tipos, paso: relacion.idProcedimientoPaso });
  }
  return [...unicos.values()];
}

export function pacientesDeClinica(datos, idClinica) {
  const vistos = new Map();
  for (const personaRol of datos.personasRol) {
    if (personaRol.idClinica !== idClinica) continue;
    const rol = datos.porId.roles.get(personaRol.idRol);
    if (normalizar(rol?.nombre) !== "paciente") continue;
    const persona = datos.porId.personas.get(personaRol.idPersona);
    if (persona) vistos.set(persona.idPersona, persona);
  }
  return [...vistos.values()];
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
