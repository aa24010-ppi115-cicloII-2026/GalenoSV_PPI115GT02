import { expect } from "chai";
import { datosBase } from "../ayudas.js";
import { edadDe } from "../../js/control/personas.js";
import { pasosDeProcedimiento, pasosConRegistros, resumenCita, citasDePaciente, citasDeEmpleado, examenesAsignables, estadoDeCita } from "../../js/control/citas.js";

describe("citas.js", () => {
  let datos;

  beforeEach(() => {
    datos = datosBase();
  });

  it("ordena los pasos del procedimiento usando la secuencia DESPUES_DE", () => {
    datos.procedimientosPaso.reverse();
    const pasos = pasosDeProcedimiento(datos, 6000);
    expect(pasos.map((paso) => paso.nombre)).to.deep.equal(["Evaluación", "Profilaxis", "Alta"]);
  });

  it("empareja registros con pasos y calcula el estado de cada paso", () => {
    const pasos = pasosConRegistros(datos, datos.consultasProcedimiento.find((item) => String(item.idConsultaProcedimiento) === "8000"));
    expect(pasos.map((paso) => paso.estado)).to.deep.equal(["completado", "actual", "pendiente"]);
    expect(pasos[0].persona.nombres).to.equal("Elena");
    expect(pasos[1].rol.nombre).to.equal("Odontólogo");
  });

  it("no deja pasos en curso cuando la consulta ya fue finalizada", () => {
    const procedimiento = datos.consultasProcedimiento.find((item) => String(item.idConsultaProcedimiento) === "8000");
    const pasos = pasosConRegistros(datos, procedimiento, { finalizada: true });
    expect(pasos.map((paso) => paso.estado)).to.deep.equal(["completado", "pendiente", "pendiente"]);
  });

  it("compone el resumen de una cita con paciente, clínica y procedimientos", () => {
    const cita = resumenCita(datos, datos.consultas.find((item) => String(item.idConsulta) === "5000"));
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
    expect(enfermero.map((cita) => cita.consulta.idConsulta)).to.deep.equal([5002]);
  });

  it("mantiene una cita finalizada en la agenda del rol que participa en sus pasos", () => {
    datos.consultas.find((item) => String(item.idConsulta) === "5000").fechaFin = "2026-11-12T12:00:00-06:00";
    const odontologo = citasDeEmpleado(datos, {
      idClinica: 10,
      idRol: 2,
      desde: "2026-11-12",
      hasta: "2026-11-12",
    });
    expect(odontologo.map((cita) => cita.consulta.idConsulta)).to.deep.equal([5000]);
    expect(odontologo[0].procedimientos[0].pasos.some((paso) => paso.estado === "actual")).to.be.false;
  });

  it("filtra la agenda del empleado por el paciente elegido", () => {
    const citas = citasDeEmpleado(datos, { idClinica: 10, desde: "2026-11-12", hasta: "2026-11-12", idPaciente: 200 });
    expect(citas).to.have.length(1);
    expect(citas[0].paciente.nombres).to.equal("Carlos");
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
    const pasos = pasosConRegistros(datos, datos.consultasProcedimiento.find((item) => String(item.idConsultaProcedimiento) === "8000"));
    expect(pasos.map((paso) => paso.estado)).to.deep.equal(["actual", "pendiente", "pendiente"]);
    expect(pasos[1].registro.idConsultaProcedimientoPaso).to.equal(9001);
  });

  it("calcula el estado de la cita segun los pasos realizados", () => {
    const enProceso = resumenCita(datos, datos.consultas.find((item) => String(item.idConsulta) === "5000"));
    expect(estadoDeCita(enProceso)).to.deep.equal({ clave: "proceso", texto: "En proceso" });
    const programada = resumenCita(datos, datos.consultas.find((item) => String(item.idConsulta) === "5002"));
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

  it("considera completada una cita cerrada aunque conserve pasos pendientes", () => {
    const cita = resumenCita(datos, datos.consultas.find((item) => String(item.idConsulta) === "5000"));
    cita.consulta = { ...cita.consulta, fechaFin: "2026-11-12T12:00:00-06:00" };

    expect(estadoDeCita(cita)).to.deep.equal({ clave: "completada", texto: "Completada" });
  });

  it("obtiene los exámenes configurados para el procedimiento con sus tipos", () => {
    const examenes = examenesAsignables(datos, 6000);
    expect(examenes).to.have.length(1);
    expect(examenes[0].examen.nombre).to.equal("Radiografía periapical");
    expect(examenes[0].tipos).to.deep.equal(["Imagenología"]);
    expect(examenesAsignables(datos, 6001)).to.deep.equal([]);
  });
});
