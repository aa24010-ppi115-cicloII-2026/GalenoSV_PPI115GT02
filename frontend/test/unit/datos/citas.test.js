
import { expect } from "chai";
import {
    resumenCita,
    citasDePaciente,
    citasDeEmpleado,
    consultaCompleta,
} from "../../../js/datos.js";

const crearDatos = () => ({
    clinicas: [
        { idClinica: 10, nombre: "Clínica Central" },
        { idClinica: 20, nombre: "Clínica Norte" },
    ],

    roles: [
        { idRol: 1, nombre: "Paciente", activo: true },
        { idRol: 2, nombre: "Odontólogo", activo: true },
        { idRol: 3, nombre: "Enfermero", activo: true },
        { idRol: 4, nombre: "Médico", activo: false },
    ],

    personas: [
        { idPersona: 100, nombres: "Ana", apellidos: "López" },
        { idPersona: 200, nombres: "Carlos", apellidos: "Pérez" },
    ],

    personasRol: [
        { idPersonaRol: 1000, idPersona: 100, idRol: 1, idClinica: 10 },
        { idPersonaRol: 1001, idPersona: 100, idRol: 1, idClinica: 20 },
        { idPersonaRol: 2000, idPersona: 200, idRol: 2, idClinica: 10 },
    ],

    consultas: [
        {
            idConsulta: 500,
            idPersonaRol: 1000,
            fechaInicio: "2026-11-10T10:00:00",
            fechaFin: null,
        },
        {
            idConsulta: 501,
            idPersonaRol: 1000,
            fechaInicio: "2026-11-12T10:00:00",
            fechaFin: null,
        },
        {
            idConsulta: 502,
            idPersonaRol: 1001,
            fechaInicio: "2026-11-11T10:00:00",
            fechaFin: null,
        },
    ],

    consultasProcedimiento: [
        {
            idConsultaProcedimiento: 700,
            idConsulta: 500,
            idProcedimiento: 50,
            observaciones: null,
        },
        {
            idConsultaProcedimiento: 701,
            idConsulta: 501,
            idProcedimiento: 50,
            observaciones: null,
        },
        {
            idConsultaProcedimiento: 702,
            idConsulta: 502,
            idProcedimiento: 50,
            observaciones: null,
        },
    ],

    procedimientos: [
        { idProcedimiento: 50, nombre: "Consulta dental" },
    ],

    procedimientosPaso: [
        {
            idProcedimientoPaso: 900,
            idProcedimiento: 50,
            idRol: 2,
            nombre: "Evaluación",
        },
    ],

    procedimientosPasoSecuencia: [],
    consultasProcedimientoPaso: [],
    ordenesExamen: [],
    documentos: [],
    tiposDocumento: [],
    tiposExamen: [],
    examenes: [],
    examenesTipoExamen: [],
    procedimientosPasoExamen: [],
});

// --------------------------------------------------
// resumenCita
// --------------------------------------------------

describe("resumenCita", () => {
    it("identifica correctamente al paciente de la consulta", () => {
        const datos = crearDatos();

        const resultado = resumenCita(datos, datos.consultas[0]);

        expect(resultado.paciente.idPersona).to.equal(100);
        expect(resultado.paciente.nombres).to.equal("Ana");
    });

    it("identifica la clínica de la asignación del paciente", () => {
        const datos = crearDatos();

        const resultado = resumenCita(datos, datos.consultas[0]);

        expect(resultado.clinica.idClinica).to.equal(10);
        expect(resultado.clinica.nombre).to.equal("Clínica Central");
    });

    it("incluye los procedimientos relacionados con la consulta", () => {
        const datos = crearDatos();

        const resultado = resumenCita(datos, datos.consultas[0]);

        expect(resultado.procedimientos).to.have.lengthOf(1);
        expect(resultado.procedimientos[0].procedimiento.nombre)
            .to.equal("Consulta dental");
    });

    it("no incluye procedimientos pertenecientes a otras consultas", () => {
        const datos = crearDatos();

        const resultado = resumenCita(datos, datos.consultas[0]);

        expect(resultado.procedimientos).to.have.lengthOf(1);
        expect(resultado.procedimientos[0].consultaProcedimiento.idConsulta)
            .to.equal(500);
    });

    it("conserva la consulta original en el resumen", () => {
        const datos = crearDatos();
        const consulta = datos.consultas[0];

        const resultado = resumenCita(datos, consulta);

        expect(resultado.consulta).to.equal(consulta);
    });

    it("devuelve paciente y clínica nulos si la asignación no existe", () => {
        const datos = crearDatos();
        const consulta = {
            idConsulta: 999,
            idPersonaRol: 9999,
            fechaInicio: "2026-11-10T10:00:00",
        };

        const resultado = resumenCita(datos, consulta);

        expect(resultado.paciente).to.equal(null);
        expect(resultado.clinica).to.equal(null);
        expect(resultado.procedimientos).to.have.lengthOf(0);
    });
});

// --------------------------------------------------
// citasDePaciente
// --------------------------------------------------

describe("citasDePaciente", () => {
    it("devuelve las citas del paciente", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, { idPersona: 100 });

        expect(resultado).to.have.lengthOf(3);
    });

    it("ordena las citas de la más reciente a la más antigua", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, { idPersona: 100 });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.deep.equal([501, 502, 500]);
    });

    it("filtra las citas por clínica", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, {
            idPersona: 100,
            idClinica: 10,
        });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.deep.equal([501, 500]);
    });

    it("acepta identificadores numéricos y de texto", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, {
            idPersona: "100",
            idClinica: "10",
        });

        expect(resultado).to.have.lengthOf(2);
    });

    it("filtra las citas desde una fecha determinada", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, {
            idPersona: 100,
            desde: "2026-11-11",
        });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.deep.equal([501, 502]);
    });

    it("filtra las citas hasta una fecha determinada, incluyéndola", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, {
            idPersona: 100,
            hasta: "2026-11-11",
        });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.deep.equal([502, 500]);
    });

    it("devuelve una lista vacía si la persona no tiene citas", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, { idPersona: 999 });

        expect(resultado).to.deep.equal([]);
    });

    it("no incluye citas de personas que no son pacientes", () => {
        const datos = crearDatos();

        const resultado = citasDePaciente(datos, { idPersona: 200 });

        expect(resultado).to.deep.equal([]);
    });

    it("no incluye consultas sin fecha de inicio válida", () => {
        const datos = crearDatos();

        datos.consultas.push({
            idConsulta: 503,
            idPersonaRol: 1000,
            fechaInicio: "fecha-invalida",
        });

        const resultado = citasDePaciente(datos, { idPersona: 100 });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.not.include(503);
    });

    it("no incluye citas asociadas a un rol de paciente inactivo", () => {
        const datos = crearDatos();

        datos.roles.find(({ idRol }) => idRol === 1).activo = false;

        const resultado = citasDePaciente(datos, { idPersona: 100 });

        expect(resultado).to.deep.equal([]);
    });

    it("incluye citas realizadas durante todo el día límite", () => {
        const datos = crearDatos();

        datos.consultas.push({
            idConsulta: 503,
            idPersonaRol: 1000,
            fechaInicio: "2026-11-11T23:59:59",
        });

        const resultado = citasDePaciente(datos, {
            idPersona: 100,
            hasta: "2026-11-11",
        });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.include(503);
    });
});

// --------------------------------------------------
// citasDeEmpleado
// --------------------------------------------------

describe("citasDeEmpleado", () => {
    it("devuelve las citas de una clínica", () => {
        const datos = crearDatos();

        const resultado = citasDeEmpleado(datos, { idClinica: 10 });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.deep.equal([500, 501]);
    });

    it("filtra las citas por paciente", () => {
        const datos = crearDatos();

        const resultado = citasDeEmpleado(datos, {
            idClinica: 10,
            idPaciente: 100,
        });

        expect(resultado).to.have.lengthOf(2);
        expect(resultado.every(({ paciente }) => paciente.idPersona === 100))
            .to.equal(true);
    });

    it("filtra por el rol del paso actual", () => {
        const datos = crearDatos();

        const resultado = citasDeEmpleado(datos, {
            idClinica: 10,
            idRol: 2,
        });

        expect(resultado).to.have.lengthOf(2);
    });

    it("no incluye citas cuyo paso actual pertenece a otro rol", () => {
        const datos = crearDatos();

        const resultado = citasDeEmpleado(datos, {
            idClinica: 10,
            idRol: 3,
        });

        expect(resultado).to.deep.equal([]);
    });

    it("ordena las citas de la más antigua a la más reciente", () => {
        const datos = crearDatos();

        const resultado = citasDeEmpleado(datos, { idClinica: 10 });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.deep.equal([500, 501]);
    });

    it("acepta identificadores de clínica y paciente como texto", () => {
        const datos = crearDatos();

        const resultado = citasDeEmpleado(datos, {
            idClinica: "10",
            idPaciente: "100",
        });

        expect(resultado).to.have.lengthOf(2);
    });

    it("no incluye consultas cuya asignación no existe", () => {
        const datos = crearDatos();

        datos.consultas.push({
            idConsulta: 999,
            idPersonaRol: 9999,
            fechaInicio: "2026-11-10T10:00:00",
        });

        const resultado = citasDeEmpleado(datos, {});

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.not.include(999);
    });

    it("incluye consultas finalizadas para los roles que participaron", () => {
        const datos = crearDatos();

        datos.consultas[0].fechaFin = "2026-11-10T11:00:00";

        const resultado = citasDeEmpleado(datos, {
            idClinica: 10,
            idRol: 2,
        });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.include(500);
    });

    it("no incluye consultas finalizadas para roles ajenos al procedimiento", () => {
        const datos = crearDatos();

        datos.consultas[0].fechaFin = "2026-11-10T11:00:00";

        const resultado = citasDeEmpleado(datos, {
            idClinica: 10,
            idRol: 3,
        });

        expect(resultado.map(({ consulta }) => consulta.idConsulta))
            .to.not.include(500);
    });
});

// --------------------------------------------------
// consultaCompleta
// --------------------------------------------------

describe("consultaCompleta", () => {
    it("devuelve el resumen de una consulta existente", () => {
        const datos = crearDatos();

        const resultado = consultaCompleta(datos, 500);

        expect(resultado).to.not.equal(null);
        expect(resultado.consulta.idConsulta).to.equal(500);
        expect(resultado.paciente.idPersona).to.equal(100);
    });

    it("acepta el identificador como texto", () => {
        const datos = crearDatos();

        const resultado = consultaCompleta(datos, "500");

        expect(resultado.consulta.idConsulta).to.equal(500);
    });

    it("devuelve null cuando la consulta no existe", () => {
        const datos = crearDatos();

        const resultado = consultaCompleta(datos, 999);

        expect(resultado).to.equal(null);
    });
});
