import sinon from "sinon";
import { expect } from "chai";
import { datosBase } from "../ayudas.js";
import { personaDeDocumento, clinicasDePaciente, empleadoDePersona, pacientesDeClinica, edadDe } from "../../js/control/personas.js";
import { documentoDe } from "../../js/control/documentos.js";

describe("personas.js", () => {
  let datos;

  beforeEach(() => {
    datos = datosBase();
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

  it("lista los pacientes registrados en la clínica sin repetir y ordenados por nombre", () => {
    expect(pacientesDeClinica(datos, "10").map((persona) => persona.nombres)).to.deep.equal(["Ana", "Carlos"]);
    expect(pacientesDeClinica(datos, 99)).to.deep.equal([]);
  });
});
