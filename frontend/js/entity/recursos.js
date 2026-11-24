/**
 * Recursos CRUD que expone la API. Cada uno tiene la clave con que la app guarda su tabla, su
 * ruta REST y el campo que identifica cada registro.
 */
export const RECURSOS = {
  clinicas: { ruta: "clinicas", id: "idClinica" },
  roles: { ruta: "roles", id: "idRol" },
  personas: { ruta: "personas", id: "idPersona" },
  personasRol: { ruta: "personas-rol", id: "idPersonaRol" },
  documentos: { ruta: "documentos", id: "idDocumento" },
  tiposDocumento: { ruta: "tipos-documento", id: "idTipoDocumento" },
  consultas: { ruta: "consultas", id: "idConsulta" },
  consultasProcedimiento: { ruta: "consultas-procedimiento", id: "idConsultaProcedimiento" },
  consultasProcedimientoPaso: { ruta: "consultas-procedimiento-paso", id: "idConsultaProcedimientoPaso" },
  procedimientos: { ruta: "procedimientos", id: "idProcedimiento" },
  procedimientosPaso: { ruta: "procedimientos-paso", id: "idProcedimientoPaso" },
  procedimientosPasoSecuencia: { ruta: "procedimientos-paso-secuencia", id: "idProcedimientoPasoSecuencia" },
  procedimientosPasoExamen: { ruta: "procedimientos-paso-examen", id: "idProcedimientoPasoExamen" },
  examenes: { ruta: "examenes", id: "idExamen" },
  tiposExamen: { ruta: "tipos-examen", id: "idTipoExamen" },
  examenesTipoExamen: { ruta: "examenes-tipo-examen", id: "idExamenTipoExamen" },
  ordenesExamen: { ruta: "ordenes-examen", id: "idOrdenExamen" },
  examenesResultado: { ruta: "examenes-resultado", id: "idExamenResultado" },
  mediosContacto: { ruta: "medios-contacto", id: "idMedioContacto" },
};

const mapa = (campo) => Object.fromEntries(Object.entries(RECURSOS).map(([clave, recurso]) => [clave, recurso[campo]]));

/** Clave de la tabla → ruta REST, por ejemplo `personasRol` → "personas-rol". */
export const FUENTES = mapa("ruta");

/** Clave de la tabla → campo que identifica cada registro. */
export const CAMPO_ID = mapa("id");

/** Ruta REST → clave de la tabla, para leer la cola de cambios pendientes. */
export const CLAVE_DE_RUTA = Object.fromEntries(
  Object.entries(RECURSOS).map(([clave, { ruta }]) => [ruta, clave])
);
