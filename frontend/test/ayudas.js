import sinon from "sinon";

export function datosBase() {
  return {
    clinicas: [{ idClinica: 10, nombre: "Clínica Central" }],
    roles: [
      { idRol: 1, nombre: "Paciente", activo: true },
      { idRol: 2, nombre: "Odontólogo", activo: true },
      { idRol: 3, nombre: "Enfermero", activo: true },
    ],
    personas: [
      { idPersona: 100, nombres: "Ana", apellidos: "López" },
      { idPersona: 200, nombres: "Carlos", apellidos: "Pérez" },
      { idPersona: 300, nombres: "Elena", apellidos: "Martínez" },
    ],
    tiposDocumento: [{ idTipoDocumento: 1, nombre: "DUI" }],
    documentos: [
      { idDocumento: 1, idPersona: 100, idTipoDocumento: 1, valor: "11111111-1" },
      { idDocumento: 2, idPersona: 200, idTipoDocumento: 1, valor: "22222222-2" },
      { idDocumento: 3, idPersona: 300, idTipoDocumento: 1, valor: "33333333-3" },
    ],
    personasRol: [
      { idPersonaRol: 1000, idPersona: 100, idRol: 1, idClinica: 10 },
      { idPersonaRol: 2000, idPersona: 200, idRol: 1, idClinica: 10 },
      { idPersonaRol: 3000, idPersona: 300, idRol: 3, idClinica: 10 },
    ],
    consultas: [
      { idConsulta: 5000, fechaInicio: "2026-11-12T09:00:00-06:00", motivo: "Limpieza", idPersonaRol: 1000, estado: "PROGRAMADA" },
      { idConsulta: 5001, fechaInicio: "2026-01-05T10:00:00-06:00", motivo: "Control", idPersonaRol: 1000, estado: "FINALIZADA" },
      { idConsulta: 5002, fechaInicio: "2026-11-12T11:00:00-06:00", motivo: "Endodoncia", idPersonaRol: 2000, estado: "PROGRAMADA" },
    ],
    consultasProcedimiento: [
      {
        idConsultaProcedimiento: 8000,
        idConsulta: 5000,
        idProcedimiento: 6000,
        observaciones: JSON.stringify({ notas: "Sin dolor", diagnostico: "K02.1 · Caries de la dentina", plan: "Control" }),
      },
      { idConsultaProcedimiento: 8001, idConsulta: 5002, idProcedimiento: 6001, observaciones: null },
    ],
    consultasProcedimientoPaso: [
      {
        idConsultaProcedimientoPaso: 9000,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:05:00-06:00",
        fechaFin: "2026-11-12T09:30:00-06:00",
        estado: "FINALIZADO",
      },
    ],
    procedimientos: [
      { idProcedimiento: 6000, nombre: "Limpieza dental", activo: true },
      { idProcedimiento: 6001, nombre: "Endodoncia", activo: true },
    ],
    procedimientosPaso: [
      { idProcedimientoPaso: 7000, idProcedimiento: 6000, nombre: "Evaluación", idRol: 2 },
      { idProcedimientoPaso: 7001, idProcedimiento: 6000, nombre: "Profilaxis", idRol: 2 },
      { idProcedimientoPaso: 7002, idProcedimiento: 6000, nombre: "Alta", idRol: 3 },
      { idProcedimientoPaso: 7003, idProcedimiento: 6001, nombre: "Apertura", idRol: 3 },
    ],
    procedimientosPasoSecuencia: [
      { idProcedimientoPasoSecuencia: 1, idProcedimientoPaso: 7001, idProcedimientoPasoReferencia: 7000, tipoSecuencia: "DESPUES_DE" },
      { idProcedimientoPasoSecuencia: 2, idProcedimientoPaso: 7002, idProcedimientoPasoReferencia: 7001, tipoSecuencia: "DESPUES_DE" },
    ],
    procedimientosPasoExamen: [
      { idProcedimientoPasoExamen: 1, idProcedimientoPaso: 7001, idExamen: 20, activo: true },
    ],
    examenes: [{ idExamen: 20, nombre: "Radiografía periapical", activo: true }],
    tiposExamen: [{ idTipoExamen: 30, nombre: "Imagenología" }],
    examenesTipoExamen: [{ idExamenTipoExamen: 1, idExamen: 20, idTipoExamen: 30 }],
    ordenesExamen: [],
    examenesResultado: [],
    mediosContacto: [],
  };
}

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

export function indizar(datos) {
  const copia = { ...datos, porId: {} };
  for (const [clave, lista] of Object.entries(datos)) {
    copia.porId[clave] = new Map(lista.map((item) => [item[CAMPO_ID[clave]], item]));
  }
  return copia;
}

export const RUTAS = {
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

export function stubApi(datos) {
  let secuencia = 10000;
  return sinon.stub(globalThis, "fetch").callsFake(async (url, opciones = {}) => {
    const metodo = (opciones.method ?? "GET").toUpperCase();
    const limpia = String(url).split("?")[0].replace(/\/$/, "");
    const ruta = limpia.split("/api/")[1] ?? "";
    const [recurso, id] = ruta.split("/");

    if (recurso === "consultas" && id && String(url).includes("/ticket")) {
      return new Response("%PDF-1.4 fake", {
        status: 200,
        headers: { "Content-Type": "application/pdf" },
      });
    }

    const clave = Object.keys(RUTAS).find((nombre) => RUTAS[nombre] === recurso);
    if (!clave) return new Response(null, { status: 404 });
    const lista = datos[clave];
    if (metodo === "GET") {
      return new Response(JSON.stringify(lista), {
        status: 200,
        headers: { "Content-Type": "application/json", "X-Total-Count": String(lista.length) },
      });
    }
    if (metodo === "POST") {
      const cuerpo = JSON.parse(opciones.body ?? "{}");
      const nuevoId = ++secuencia;
      lista.push({ [CAMPO_ID[clave]]: nuevoId, ...cuerpo });
      return new Response(null, {
        status: 201,
        headers: { Location: `http://localhost:9080/api/${recurso}/${nuevoId}` },
      });
    }
    if (metodo === "PUT") {
      const cuerpo = JSON.parse(opciones.body ?? "{}");
      const indice = lista.findIndex((item) => String(item[CAMPO_ID[clave]]) === String(id));
      if (indice >= 0) lista[indice] = { ...lista[indice], ...cuerpo };
      return new Response(null, { status: 204 });
    }
    if (metodo === "DELETE") {
      const indice = lista.findIndex((item) => String(item[CAMPO_ID[clave]]) === String(id));
      if (indice >= 0) lista.splice(indice, 1);
      return new Response(null, { status: 204 });
    }
    return new Response(null, { status: 405 });
  });
}

export async function esperar(condicion, limite = 200) {
  for (let intento = 0; intento < limite; intento++) {
    if (condicion()) return;
    await new Promise((resolver) => setImmediate(resolver));
  }
  throw new Error("Se agotó el tiempo de espera de la condición");
}
