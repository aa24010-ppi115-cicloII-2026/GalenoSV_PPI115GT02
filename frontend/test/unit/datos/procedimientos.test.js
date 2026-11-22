
import { expect } from "chai";

import {
  pasosDeProcedimiento,
  pasosConRegistros,
  estadoDeCita,
  examenesAsignables,
} from "../../../js/datos.js";

describe("datos.js - Procedimientos", () => {

  const crearDatos = () => ({
    procedimientosPaso: [
      { idProcedimientoPaso: 3, idProcedimiento: 10, nombre: "Paso C" },
      { idProcedimientoPaso: 1, idProcedimiento: 10, nombre: "Paso A" },
      { idProcedimientoPaso: 2, idProcedimiento: 10, nombre: "Paso B" },
      { idProcedimientoPaso: 4, idProcedimiento: 20, nombre: "Otro procedimiento" },
    ],

    procedimientosPasoSecuencia: [
      {
        idProcedimientoPaso: 2,
        idProcedimientoPasoReferencia: 1,
        tipoSecuencia: "despues_de",
      },
      {
        idProcedimientoPaso: 3,
        idProcedimientoPasoReferencia: 2,
        tipoSecuencia: "despues_de",
      },
    ],
  });

  describe("pasosDeProcedimiento()", () => {

    it("debe ordenar los pasos respetando sus dependencias", () => {
      const datos = crearDatos();

      const resultado = pasosDeProcedimiento(datos, 10);

      expect(resultado.map(({ idProcedimientoPaso }) => idProcedimientoPaso))
        .to.deep.equal([1, 2, 3]);
    });

    it("debe mantener el orden original cuando no existen dependencias", () => {
      const datos = crearDatos();
      datos.procedimientosPasoSecuencia = [];

      const resultado = pasosDeProcedimiento(datos, 10);

      expect(resultado.map(({ idProcedimientoPaso }) => idProcedimientoPaso))
        .to.deep.equal([3, 1, 2]);
    });

    it("debe excluir los pasos pertenecientes a otros procedimientos", () => {
      const datos = crearDatos();

      const resultado = pasosDeProcedimiento(datos, 10);

      expect(resultado.every((paso) => paso.idProcedimiento === 10))
        .to.equal(true);

      expect(resultado).to.have.lengthOf(3);
    });

    it("debe ignorar secuencias de tipos diferentes a despues_de", () => {
      const datos = crearDatos();

      datos.procedimientosPasoSecuencia = [
        {
          idProcedimientoPaso: 2,
          idProcedimientoPasoReferencia: 1,
          tipoSecuencia: "antes_de",
        },
      ];

      const resultado = pasosDeProcedimiento(datos, 10);

      expect(resultado.map(({ idProcedimientoPaso }) => idProcedimientoPaso))
        .to.deep.equal([3, 1, 2]);
    });

    it("debe reconocer IDs numéricos y cadenas equivalentes", () => {
      const datos = crearDatos();

      datos.procedimientosPasoSecuencia = [
        {
          idProcedimientoPaso: "2",
          idProcedimientoPasoReferencia: "1",
          tipoSecuencia: "despues_de",
        },
        {
          idProcedimientoPaso: "3",
          idProcedimientoPasoReferencia: "2",
          tipoSecuencia: "despues_de",
        },
      ];

      const resultado = pasosDeProcedimiento(datos, "10");

      expect(resultado.map(({ idProcedimientoPaso }) => idProcedimientoPaso))
        .to.deep.equal([1, 2, 3]);
    });

    it("debe reconocer el tipo de secuencia sin importar mayúsculas o acentos", () => {
      const datos = crearDatos();

      datos.procedimientosPasoSecuencia[0].tipoSecuencia = "DESPUÉS_DE";

      const resultado = pasosDeProcedimiento(datos, 10);

      expect(resultado.map(({ idProcedimientoPaso }) => idProcedimientoPaso))
        .to.deep.equal([1, 2, 3]);
    });

    it("debe incluir cada paso una sola vez cuando existen dependencias circulares", () => {
      const datos = crearDatos();

      datos.procedimientosPasoSecuencia = [
        {
          idProcedimientoPaso: 1,
          idProcedimientoPasoReferencia: 2,
          tipoSecuencia: "despues_de",
        },
        {
          idProcedimientoPaso: 2,
          idProcedimientoPasoReferencia: 1,
          tipoSecuencia: "despues_de",
        },
      ];

      const resultado = pasosDeProcedimiento(datos, 10);

      expect(resultado).to.have.lengthOf(3);

      const ids = resultado.map(({ idProcedimientoPaso }) => idProcedimientoPaso);

      expect(new Set(ids).size).to.equal(3);
    });

    it("debe devolver una lista vacía si el procedimiento no tiene pasos", () => {
      const datos = crearDatos();

      const resultado = pasosDeProcedimiento(datos, 999);

      expect(resultado).to.deep.equal([]);
    });

  });


  describe("pasosConRegistros()", () => {

    const crearDatosConRegistros = () => ({
      procedimientosPaso: [
        { idProcedimientoPaso: 1, idProcedimiento: 10, nombre: "Evaluación", idRol: 1 },
        { idProcedimientoPaso: 2, idProcedimiento: 10, nombre: "Tratamiento", idRol: 2 },
        { idProcedimientoPaso: 3, idProcedimiento: 10, nombre: "Finalización", idRol: 3 },
      ],

      procedimientosPasoSecuencia: [
        {
          idProcedimientoPaso: 2,
          idProcedimientoPasoReferencia: 1,
          tipoSecuencia: "despues_de",
        },
        {
          idProcedimientoPaso: 3,
          idProcedimientoPasoReferencia: 2,
          tipoSecuencia: "despues_de",
        },
      ],

      consultasProcedimientoPaso: [],

      roles: [
        { idRol: 1, nombre: "Médico" },
        { idRol: 2, nombre: "Odontólogo" },
        { idRol: 3, nombre: "Enfermero" },
      ],

      personasRol: [],
      personas: [],
    });

    const crearConsultaProcedimiento = (observaciones = "") => ({
      idConsultaProcedimiento: 500,
      idProcedimiento: 10,
      observaciones,
    });

    const crearRegistro = (id, fecha, estado, idConsulta = 500) => ({
      idConsultaProcedimientoPaso: id,
      idConsultaProcedimiento: idConsulta,
      fechaInicio: fecha,
      estado,
    });

    it("debe devolver todos los pasos en el orden del procedimiento", () => {
      const datos = crearDatosConRegistros();

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado.map(({ paso }) => paso.nombre))
        .to.deep.equal(["Evaluación", "Tratamiento", "Finalización"]);
    });

    it("debe asignar cada registro según su posición cronológica", () => {
      const datos = crearDatosConRegistros();

      datos.consultasProcedimientoPaso = [
        crearRegistro("registro-2", "2026-11-22T11:00:00", "EN_PROCESO"),
        crearRegistro("registro-1", "2026-11-22T10:00:00", "FINALIZADO"),
      ];

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado[0].registro.idConsultaProcedimientoPaso)
        .to.equal("registro-1");

      expect(resultado[1].registro.idConsultaProcedimientoPaso)
        .to.equal("registro-2");
    });

    it("debe marcar como completados los pasos con registros finalizados", () => {
      const datos = crearDatosConRegistros();

      datos.consultasProcedimientoPaso = [
        crearRegistro("registro-1", "2026-11-22T10:00:00", "FINALIZADO"),
        crearRegistro("registro-2", "2026-11-22T11:00:00", "FINALIZADO"),
      ];

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado.map(({ estado }) => estado))
        .to.deep.equal(["completado", "completado", "actual"]);
    });

    it("debe marcar solamente el primer paso sin finalizar como actual", () => {
      const datos = crearDatosConRegistros();

      datos.consultasProcedimientoPaso = [
        crearRegistro("registro-1", "2026-11-22T10:00:00", "FINALIZADO"),
        crearRegistro("registro-2", "2026-11-22T11:00:00", "EN_PROCESO"),
        crearRegistro("registro-3", "2026-11-22T12:00:00", "EN_PROCESO"),
      ];

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado.map(({ estado }) => estado))
        .to.deep.equal(["completado", "actual", "pendiente"]);
    });

    it("debe marcar como actual el primer paso cuando no existen registros", () => {
      const datos = crearDatosConRegistros();

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado.map(({ estado }) => estado))
        .to.deep.equal(["actual", "pendiente", "pendiente"]);
    });

    it("debe dejar los pasos incompletos como pendientes cuando la consulta está finalizada", () => {
      const datos = crearDatosConRegistros();

      datos.consultasProcedimientoPaso = [
        crearRegistro("registro-1", "2026-11-22T10:00:00", "FINALIZADO"),
        crearRegistro("registro-2", "2026-11-22T11:00:00", "EN_PROCESO"),
      ];

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento(),
        { finalizada: true }
      );

      expect(resultado.map(({ estado }) => estado))
        .to.deep.equal(["completado", "pendiente", "pendiente"]);
    });

    it("debe excluir registros pertenecientes a otros procedimientos de consulta", () => {
      const datos = crearDatosConRegistros();

      datos.consultasProcedimientoPaso = [
        crearRegistro("registro-ajeno", "2026-11-22T09:00:00", "FINALIZADO", 999),
      ];

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado.every(({ registro }) => registro === null))
        .to.equal(true);
    });

    it("debe asociar las notas almacenadas con el paso correspondiente", () => {
      const datos = crearDatosConRegistros();

      const observaciones = JSON.stringify({
        notas: "Observación general",
        diagnostico: "",
        plan: "",
        pasos: {
          1: "Evaluación realizada",
          2: "Tratamiento aplicado",
        },
      });

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento(observaciones)
      );

      expect(resultado[0].notas).to.equal("Evaluación realizada");
      expect(resultado[1].notas).to.equal("Tratamiento aplicado");
      expect(resultado[2].notas).to.equal(null);
    });

    it("debe asociar el rol correspondiente a cada paso", () => {
      const datos = crearDatosConRegistros();

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado.map(({ rol }) => rol.nombre))
        .to.deep.equal(["Médico", "Odontólogo", "Enfermero"]);
    });

    it("debe devolver persona null cuando el registro no tiene una asignación de persona", () => {
      const datos = crearDatosConRegistros();

      datos.consultasProcedimientoPaso = [
        crearRegistro("registro-1", "2026-11-22T10:00:00", "EN_PROCESO"),
      ];

      const resultado = pasosConRegistros(
        datos,
        crearConsultaProcedimiento()
      );

      expect(resultado[0].persona).to.equal(null);
    });

  });


  describe("estadoDeCita()", () => {

    const crearCita = (pasos = [], fechaFin = null) => ({
      consulta: { fechaFin },
      procedimientos: [
        { pasos },
      ],
    });

    it("debe marcar como completada una consulta que tiene fecha de finalización", () => {
      const cita = crearCita(
        [{ estado: "pendiente", registro: null }],
        "2026-11-22T12:00:00"
      );

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "completada",
        texto: "Completada",
      });
    });

    it("debe devolver sin-pasos cuando no existen pasos", () => {
      const cita = crearCita([]);

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "sin-pasos",
        texto: "Sin pasos",
      });
    });

    it("debe marcar como completada una cita cuando todos sus pasos están completados", () => {
      const cita = crearCita([
        { estado: "completado", registro: {} },
        { estado: "completado", registro: {} },
      ]);

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "completada",
        texto: "Completada",
      });
    });

    it("debe marcar como en proceso una cita cuando existe al menos un paso completado", () => {
      const cita = crearCita([
        { estado: "completado", registro: {} },
        { estado: "pendiente", registro: null },
      ]);

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "proceso",
        texto: "En proceso",
      });
    });

    it("debe marcar como en proceso una cita cuando existe un registro aunque ningún paso esté completado", () => {
      const cita = crearCita([
        { estado: "actual", registro: { id: 1 } },
        { estado: "pendiente", registro: null },
      ]);

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "proceso",
        texto: "En proceso",
      });
    });

    it("debe marcar como programada una cita cuando ningún paso está completado ni tiene registros", () => {
      const cita = crearCita([
        { estado: "pendiente", registro: null },
        { estado: "pendiente", registro: null },
      ]);

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "programada",
        texto: "Programada",
      });
    });

    it("debe considerar todos los procedimientos al determinar el estado", () => {
      const cita = {
        consulta: { fechaFin: null },
        procedimientos: [
          {
            pasos: [
              { estado: "completado", registro: {} },
            ],
          },
          {
            pasos: [
              { estado: "pendiente", registro: null },
            ],
          },
        ],
      };

      expect(estadoDeCita(cita)).to.deep.equal({
        clave: "proceso",
        texto: "En proceso",
      });
    });

  });


describe("examenesAsignables()", () => {

  const crearDatos = () => ({
    procedimientosPaso: [
      { idProcedimientoPaso: 1, idProcedimiento: 10 },
      { idProcedimientoPaso: 2, idProcedimiento: 10 },
      { idProcedimientoPaso: 3, idProcedimiento: 20 },
    ],

    procedimientosPasoSecuencia: [],

    procedimientosPasoExamen: [
      { idProcedimientoPaso: 1, idExamen: 100, activo: true },
      { idProcedimientoPaso: 2, idExamen: 200, activo: true },
      { idProcedimientoPaso: 2, idExamen: 300, activo: true },
    ],

    examenes: [
      { idExamen: 100, nombre: "Hemograma", activo: true },
      { idExamen: 200, nombre: "Glucosa", activo: true },
      { idExamen: 300, nombre: "Colesterol", activo: true },
      { idExamen: 400, nombre: "Examen inactivo", activo: false },
    ],

    examenesTipoExamen: [
      { idExamen: 100, idTipoExamen: 1 },
      { idExamen: 100, idTipoExamen: 2 },
      { idExamen: 200, idTipoExamen: 1 },
      { idExamen: 300, idTipoExamen: 999 },
    ],

    tiposExamen: [
      { idTipoExamen: 1, nombre: "Sangre" },
      { idTipoExamen: 2, nombre: "Laboratorio" },
    ],
  });

  it("debe devolver los exámenes asignados a los pasos del procedimiento", () => {
    const datos = crearDatos();

    const resultado = examenesAsignables(datos, 10);

    expect(resultado.map(({ examen }) => examen.nombre))
      .to.deep.equal(["Hemograma", "Glucosa", "Colesterol"]);
  });

  it("debe filtrar los exámenes por un paso específico", () => {
    const datos = crearDatos();

    const resultado = examenesAsignables(datos, 10, 2);

    expect(resultado.map(({ examen }) => examen.nombre))
      .to.deep.equal(["Glucosa", "Colesterol"]);
  });

  it("debe aceptar identificadores numéricos y cadenas equivalentes", () => {
    const datos = crearDatos();

    const resultado = examenesAsignables(datos, "10", "1");

    expect(resultado.map(({ examen }) => examen.nombre))
      .to.deep.equal(["Hemograma"]);
  });

  it("debe excluir las relaciones inactivas", () => {
    const datos = crearDatos();

    datos.procedimientosPasoExamen[0].activo = false;

    const resultado = examenesAsignables(datos, 10);

    expect(resultado.map(({ examen }) => examen.nombre))
      .to.not.include("Hemograma");
  });

  it("debe excluir los exámenes inactivos", () => {
    const datos = crearDatos();

    datos.procedimientosPasoExamen.push({
      idProcedimientoPaso: 1,
      idExamen: 400,
      activo: true,
    });

    const resultado = examenesAsignables(datos, 10);

    expect(resultado.map(({ examen }) => examen.nombre))
      .to.not.include("Examen inactivo");
  });

  it("debe ignorar relaciones cuyo examen no existe", () => {
    const datos = crearDatos();

    datos.procedimientosPasoExamen.push({
      idProcedimientoPaso: 1,
      idExamen: 999,
      activo: true,
    });

    const resultado = examenesAsignables(datos, 10);

    expect(resultado).to.have.lengthOf(3);
  });

  it("debe evitar devolver un mismo examen más de una vez", () => {
    const datos = crearDatos();

    datos.procedimientosPasoExamen.push({
      idProcedimientoPaso: 1,
      idExamen: 100,
      activo: true,
    });

    const resultado = examenesAsignables(datos, 10);

    const ids = resultado.map(({ examen }) => examen.idExamen);

    expect(ids).to.have.lengthOf(3);
    expect(new Set(ids).size).to.equal(3);
  });

  it("debe devolver los nombres de los tipos asociados a cada examen", () => {
    const datos = crearDatos();

    const resultado = examenesAsignables(datos, 10);

    const hemograma = resultado.find(
      ({ examen }) => examen.idExamen === 100
    );

    expect(hemograma.tipos)
      .to.deep.equal(["Sangre", "Laboratorio"]);
  });

  it("debe ignorar tipos de examen que no existen", () => {
    const datos = crearDatos();

    const resultado = examenesAsignables(datos, 10);

    const colesterol = resultado.find(
      ({ examen }) => examen.idExamen === 300
    );

    expect(colesterol.tipos).to.deep.equal([]);
  });

  it("debe devolver una lista vacía cuando el procedimiento no tiene exámenes", () => {
    const datos = crearDatos();

    const resultado = examenesAsignables(datos, 999);

    expect(resultado).to.deep.equal([]);
  });

});


});
