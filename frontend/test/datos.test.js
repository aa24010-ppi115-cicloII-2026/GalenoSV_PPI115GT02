import { expect } from "chai";
import sinon from "sinon";
import { datosBase, indizar, stubApi } from "./ayudas.js";
import { cargarTodo, limpiarCache, aplicarPendientes } from "../js/datos.js";
import {
  personaDeDocumento,
  documentoDe,
  cumpleFormato,
  clinicasDePaciente,
  empleadoDePersona,
  pasosDeProcedimiento,
  pasosConRegistros,
  resumenCita,
  citasDePaciente,
  citasDeEmpleado,
  pacientesDeClinica,
  examenesAsignables,
  estadoDeCita,
  edadDe,
  etiquetaPaso,
  leerObservaciones,
  serializarObservaciones,
} from "../js/datos.js";

describe("datos.js", () => {
  let datos;

  beforeEach(() => {
    datos = indizar(datosBase());
  });

  it("cumpleFormato valida con la expresión del tipo y no bloquea si el catálogo no la trae o está mal escrita", () => {
    const dui = { expresionRegular: "^[0-9]{8}-[0-9]$" };
    expect(cumpleFormato(dui, "01234567-8")).to.be.true;
    expect(cumpleFormato(dui, "123")).to.be.false;
    expect(cumpleFormato({ expresionRegular: "" }, "A123")).to.be.true;
    expect(cumpleFormato({ expresionRegular: "([" }, "A123")).to.be.true;
    expect(cumpleFormato(null, "A123")).to.be.true;
  });

  it("encuentra una persona por tipo y número de documento, sin distinguir espacios", () => {
    const persona = personaDeDocumento(datos, 1, " 11111111-1 ");
    expect(persona.idPersona).to.equal(100);
    expect(personaDeDocumento(datos, 1, "99999999-9")).to.be.null;
    expect(documentoDe(datos, 1, "11111111-1").idDocumento).to.equal(1);
  });

  it("lista las clínicas asociadas al paciente", () => {
    const clinicas = clinicasDePaciente(datos, 100);
    expect(clinicas.map((clinica) => clinica.idClinica)).to.deep.equal([10]);
  });

  it("no ofrece clínicas donde la persona solo trabaja como empleado", () => {
    expect(clinicasDePaciente(datos, 300)).to.deep.equal([]);
  });

  it("traduce el estado interno del paso a un texto legible", () => {
    expect(etiquetaPaso("completado")).to.equal("Completado");
    expect(etiquetaPaso("actual")).to.equal("En curso");
    // El paso siguiente que nadie ha iniciado no está «en curso».
    expect(etiquetaPaso("actual", false)).to.equal("Por iniciar");
    expect(etiquetaPaso("pendiente")).to.equal("Pendiente");
  });

  it("calcula la edad con fechas sin hora guardadas en UTC sin restar un día", () => {
    const reloj = sinon.useFakeTimers(new Date("2026-09-30T12:00:00-06:00"));
    try {
      expect(edadDe({ fechaNacimiento: "1992-09-30T00:00:00Z" })).to.equal(34);
    } finally {
      reloj.restore();
    }
  });

  it("separa los roles de empleado de los roles de paciente", () => {
    const roles = empleadoDePersona(datos, 300);
    expect(roles).to.have.length(1);
    expect(roles[0].rol.nombre).to.equal("Enfermero");
    expect(empleadoDePersona(datos, 100)).to.deep.equal([]);
  });

  it("ordena los pasos del procedimiento usando la secuencia DESPUES_DE", () => {
    datos.procedimientosPaso.reverse();
    const pasos = pasosDeProcedimiento(datos, 6000);
    expect(pasos.map((paso) => paso.nombre)).to.deep.equal(["Evaluación", "Profilaxis", "Alta"]);
  });

  it("empareja registros con pasos y calcula el estado de cada paso", () => {
    const pasos = pasosConRegistros(datos, datos.porId.consultasProcedimiento.get(8000));
    expect(pasos.map((paso) => paso.estado)).to.deep.equal(["completado", "actual", "pendiente"]);
    expect(pasos[0].persona.nombres).to.equal("Elena");
    expect(pasos[1].rol.nombre).to.equal("Odontólogo");
  });

  it("compone el resumen de una cita con paciente, clínica y procedimientos", () => {
    const cita = resumenCita(datos, datos.porId.consultas.get(5000));
    expect(cita.paciente.nombres).to.equal("Ana");
    expect(cita.paciente.documento.tipo).to.equal("DUI");
    expect(cita.paciente.documento.valor).to.equal("11111111-1");
    expect(edadDe({ fechaNacimiento: "1990-05-15T00:00:00-06:00" })).to.be.a("number");
    expect(edadDe({})).to.be.null;
    expect(edadDe(null)).to.be.null;
    expect(cita.clinica.nombre).to.equal("Clínica Central");
    expect(cita.procedimientos).to.have.length(1);
    expect(cita.procedimientos[0].procedimiento.nombre).to.equal("Limpieza dental");
  });

  it("filtra las citas del paciente por clínica y rango de fechas", () => {
    expect(citasDePaciente(datos, { idPersona: 100 })).to.have.length(2);
    expect(
      citasDePaciente(datos, { idPersona: 100, desde: "2026-11-12", hasta: "2026-11-12" })
    ).to.have.length(1);
    expect(citasDePaciente(datos, { idPersona: 100, idClinica: 99 })).to.have.length(0);
    expect(
      citasDePaciente(datos, { idPersona: 100, desde: "2027-01-01" })
    ).to.have.length(0);
  });

  it("filtra la agenda del empleado por clínica, rol y fecha", () => {
    const odontologo = citasDeEmpleado(datos, {
      idClinica: 10,
      idRol: 2,
      desde: "2026-11-12",
      hasta: "2026-11-12",
    });
    expect(odontologo.map((cita) => cita.consulta.idConsulta)).to.deep.equal([5000]);
    const enfermero = citasDeEmpleado(datos, {
      idClinica: 10,
      idRol: 3,
      desde: "2026-11-12",
      hasta: "2026-11-12",
    });
    expect(enfermero.map((cita) => cita.consulta.idConsulta)).to.deep.equal([5000, 5002]);
  });

  it("filtra la agenda del empleado por el paciente elegido", () => {
    const citas = citasDeEmpleado(datos, { idClinica: 10, desde: "2026-11-12", hasta: "2026-11-12", idPaciente: 200 });
    expect(citas).to.have.length(1);
    expect(citas[0].paciente.nombres).to.equal("Carlos");
  });

  it("lista los pacientes registrados en la clínica sin repetir y ordenados por nombre", () => {
    expect(pacientesDeClinica(datos, "10").map((persona) => persona.nombres)).to.deep.equal(["Ana", "Carlos"]);
    expect(pacientesDeClinica(datos, 99)).to.deep.equal([]);
  });

  it("tras retroceder solo el primer paso sin terminar queda en curso y el siguiente conserva su registro", () => {
    // Paso 1 reabierto (PENDIENTE) cuando el paso 2 ya se había iniciado.
    datos.consultasProcedimientoPaso[0].estado = "PENDIENTE";
    datos.consultasProcedimientoPaso.push({
      idConsultaProcedimientoPaso: 9001,
      idConsultaProcedimiento: 8000,
      idPersonaRol: 3000,
      fechaInicio: "2026-11-12T09:40:00-06:00",
      estado: "EN_PROCESO",
    });
    const pasos = pasosConRegistros(datos, datos.porId.consultasProcedimiento.get(8000));
    expect(pasos.map((paso) => paso.estado)).to.deep.equal(["actual", "pendiente", "pendiente"]);
    expect(pasos[1].registro.idConsultaProcedimientoPaso).to.equal(9001);
  });

  it("calcula el estado de la cita segun los pasos realizados", () => {
    const enProceso = resumenCita(datos, datos.porId.consultas.get(5000));
    expect(estadoDeCita(enProceso)).to.deep.equal({ clave: "proceso", texto: "En proceso" });
    const programada = resumenCita(datos, datos.porId.consultas.get(5002));
    expect(estadoDeCita(programada).clave).to.equal("programada");
    const completada = {
      ...enProceso,
      procedimientos: enProceso.procedimientos.map((item) => ({
        ...item,
        pasos: item.pasos.map((paso) => ({ ...paso, estado: "completado" })),
      })),
    };
    expect(estadoDeCita(completada)).to.deep.equal({ clave: "completada", texto: "Completada" });
  });

  it("obtiene los exámenes configurados para el procedimiento con sus tipos", () => {
    const examenes = examenesAsignables(datos, 6000);
    expect(examenes).to.have.length(1);
    expect(examenes[0].examen.nombre).to.equal("Radiografía periapical");
    expect(examenes[0].tipos).to.deep.equal(["Imagenología"]);
    expect(examenesAsignables(datos, 6001)).to.deep.equal([]);
  });

  it("indexa por el identificador propio aunque el JSON tenga otros campos id primero", async () => {
    const fuente = datosBase();
    fuente.personasRol = fuente.personasRol.map((item) => ({
      fechaCreacion: "2026-01-01T08:00-06:00",
      idClinica: item.idClinica,
      idPersona: item.idPersona,
      idPersonaRol: item.idPersonaRol,
      idRol: item.idRol,
    }));
    stubApi(fuente);
    try {
      const cargados = await cargarTodo();
      expect(cargados.porId.personasRol.size).to.equal(3);
      expect(cargados.porId.personasRol.get(1000).idPersona).to.equal(100);
    } finally {
      sinon.restore();
      limpiarCache();
    }
  });

  it("interpreta y serializa las notas, el diagnóstico y el plan", () => {
    const notas = leerObservaciones(
      JSON.stringify({ notas: "Evolución favorable", diagnostico: "K02.1", plan: "Control" })
    );
    expect(notas).to.deep.equal({
      notas: "Evolución favorable",
      diagnostico: "K02.1",
      plan: "Control",
      pasos: {},
    });
    expect(leerObservaciones("Texto libre")).to.deep.equal({
      notas: "Texto libre",
      diagnostico: "",
      plan: "",
      pasos: {},
    });
    const texto = serializarObservaciones(notas);
    expect(leerObservaciones(texto)).to.deep.equal(notas);
    expect(
      leerObservaciones(
        serializarObservaciones({ ...notas, pasos: { 7000: { notas: "Paso 1", fecha: "2026-11-12T09:00:00-06:00" } } })
      ).pasos["7000"].notas
    ).to.equal("Paso 1");
  });
  it("muestra sobre los datos del servidor los cambios guardados sin conexión, sin alterar la copia original", () => {
    datos.ordenesExamen.push({ idOrdenExamen: 4000, idConsultaProcedimientoPaso: 9000 });
    const vista = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "consultas-procedimiento-paso",
        cuerpo: { idConsultaProcedimientoPaso: "c1", idConsultaProcedimiento: 8000, estado: "EN_PROCESO" },
      },
      { metodo: "PUT", ruta: "consultas-procedimiento-paso/c1", cuerpo: { estado: "FINALIZADO" } },
      { metodo: "PUT", ruta: "consultas-procedimiento-paso/9000", cuerpo: { observaciones: "Editado" } },
      { metodo: "DELETE", ruta: "ordenes-examen/4000", cuerpo: null },
    ]);
    expect(vista.porId.consultasProcedimientoPaso.get("c1").estado).to.equal("FINALIZADO");
    expect(vista.porId.consultasProcedimientoPaso.get(9000).observaciones).to.equal("Editado");
    expect(vista.ordenesExamen).to.deep.equal([]);
    expect(datos.consultasProcedimientoPaso).to.have.length(1);
    expect(datos.consultasProcedimientoPaso[0].observaciones).to.be.undefined;
    expect(datos.ordenesExamen).to.have.length(1);
    expect(aplicarPendientes(datos, [])).to.equal(datos);
  });
});
