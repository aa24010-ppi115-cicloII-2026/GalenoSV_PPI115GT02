/** Recursos CRUD que expone la API y campo que identifica cada registro. */
export const FUENTES = {
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

export const CAMPO_ID = {
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

export const CLAVE_DE_RUTA = Object.fromEntries(
  Object.entries(FUENTES).map(([clave, ruta]) => [ruta, clave])
);
