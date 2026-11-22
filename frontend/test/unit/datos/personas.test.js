
import { expect } from "chai";

import {
  clinicasDePaciente,
  empleadoDePersona,
  pacientesDeClinica,
} from "../../../js/datos.js";

describe("datos.js - Personas y roles", () => {

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
      { idPersona: 300, nombres: "Elena", apellidos: "Martínez" },
    ],

    personasRol: [
      { idPersonaRol: 1000, idPersona: 100, idRol: 1, idClinica: 10 },
      { idPersonaRol: 1001, idPersona: 100, idRol: 1, idClinica: 20 },
      { idPersonaRol: 1002, idPersona: 100, idRol: 2, idClinica: 10 },

      { idPersonaRol: 2000, idPersona: 200, idRol: 1, idClinica: 10 },
      { idPersonaRol: 2001, idPersona: 200, idRol: 4, idClinica: 10 },

      { idPersonaRol: 3000, idPersona: 300, idRol: 3, idClinica: 20 },
    ],
  });

  describe("clinicasDePaciente()", () => {

    it("debe devolver las clínicas donde está registrado el paciente", () => {
      const datos = crearDatos();

      const resultado = clinicasDePaciente(datos, 100);

      expect(resultado.map(({ idClinica }) => idClinica))
        .to.deep.equal([10, 20]);
    });

    it("debe excluir las asignaciones que no corresponden al rol paciente", () => {
      const datos = crearDatos();

      const resultado = clinicasDePaciente(datos, 300);

      expect(resultado).to.deep.equal([]);
    });

    it("debe excluir los roles de paciente inactivos", () => {
      const datos = crearDatos();

      datos.roles[0].activo = false;

      expect(clinicasDePaciente(datos, 100)).to.deep.equal([]);
    });

    it("no debe devolver clínicas duplicadas", () => {
      const datos = crearDatos();

      datos.personasRol.push({
        idPersonaRol: 1003,
        idPersona: 100,
        idRol: 1,
        idClinica: 10,
      });

      const resultado = clinicasDePaciente(datos, 100);

      expect(resultado).to.have.lengthOf(2);
    });

    it("debe aceptar identificadores numéricos y cadenas equivalentes", () => {
      const datos = crearDatos();

      const resultado = clinicasDePaciente(datos, "100");

      expect(resultado).to.have.lengthOf(2);
    });

    it("debe ignorar diferencias entre mayúsculas y minúsculas en el nombre del rol", () => {
      const datos = crearDatos();

      datos.roles[0].nombre = "PACIENTE";

      expect(clinicasDePaciente(datos, 100)).to.have.lengthOf(2);
    });

    it("debe excluir asignaciones cuya clínica no existe", () => {
      const datos = crearDatos();

      datos.personasRol.push({
        idPersonaRol: 1004,
        idPersona: 100,
        idRol: 1,
        idClinica: 999,
      });

      expect(clinicasDePaciente(datos, 100)).to.have.lengthOf(2);
    });

    it("debe devolver una lista vacía cuando la persona no tiene asignaciones", () => {
      const datos = crearDatos();

      expect(clinicasDePaciente(datos, 999)).to.deep.equal([]);
    });

  });

  describe("empleadoDePersona()", () => {

    it("debe devolver los roles laborales activos de una persona", () => {
      const datos = crearDatos();

      const resultado = empleadoDePersona(datos, 100);

      expect(resultado).to.have.lengthOf(1);
      expect(resultado[0].rol.nombre).to.equal("Odontólogo");
    });

    it("debe incluir la asignación y la clínica asociada", () => {
      const datos = crearDatos();

      const resultado = empleadoDePersona(datos, 300);

      expect(resultado).to.have.lengthOf(1);
      expect(resultado[0].personaRol.idPersonaRol).to.equal(3000);
      expect(resultado[0].clinica.nombre).to.equal("Clínica Norte");
    });

    it("debe excluir el rol de paciente", () => {
      const datos = crearDatos();

      const resultado = empleadoDePersona(datos, 200);

      expect(resultado).to.deep.equal([]);
    });

    it("debe excluir los roles inactivos", () => {
      const datos = crearDatos();

      const resultado = empleadoDePersona(datos, 200);

      expect(resultado).to.have.lengthOf(0);
    });

    it("debe excluir asignaciones cuyo rol no existe", () => {
      const datos = crearDatos();

      datos.personasRol.push({
        idPersonaRol: 1005,
        idPersona: 100,
        idRol: 999,
        idClinica: 10,
      });

      const resultado = empleadoDePersona(datos, 100);

      expect(resultado).to.have.lengthOf(1);
      expect(resultado[0].rol.nombre).to.equal("Odontólogo");
    });

    it("debe aceptar identificadores numéricos y cadenas equivalentes", () => {
      const datos = crearDatos();

      const resultado = empleadoDePersona(datos, "100");

      expect(resultado).to.have.lengthOf(1);
    });

    it("debe devolver una lista vacía cuando la persona no tiene roles laborales", () => {
      const datos = crearDatos();

      expect(empleadoDePersona(datos, 999)).to.deep.equal([]);
    });

  });

  describe("pacientesDeClinica()", () => {

    it("debe devolver los pacientes registrados en una clínica", () => {
      const datos = crearDatos();

      const resultado = pacientesDeClinica(datos, 10);

      expect(resultado.map(({ nombres }) => nombres))
        .to.deep.equal(["Ana", "Carlos"]);
    });

    it("debe excluir personas que no tienen el rol de paciente", () => {
      const datos = crearDatos();

  
      const resultado = pacientesDeClinica(datos, 20);

     expect(resultado.map(({ nombres }) => nombres))
      .to.not.include("Elena");
    });

    it("no debe incluir pacientes duplicados", () => {
      const datos = crearDatos();

      datos.personasRol.push({
        idPersonaRol: 1006,
        idPersona: 100,
        idRol: 1,
        idClinica: 10,
      });

      const resultado = pacientesDeClinica(datos, 10);

      expect(resultado).to.have.lengthOf(2);
    });

    it("debe excluir pacientes con roles inactivos", () => {
      const datos = crearDatos();

      datos.roles[0].activo = false;

      expect(pacientesDeClinica(datos, 10)).to.deep.equal([]);
    });

    it("debe ordenar los pacientes alfabéticamente por nombre completo", () => {
      const datos = crearDatos();

      const resultado = pacientesDeClinica(datos, 10);

      expect(resultado.map(({ nombres }) => nombres))
        .to.deep.equal(["Ana", "Carlos"]);
    });

    it("debe excluir asignaciones cuya persona no existe", () => {
      const datos = crearDatos();

      datos.personasRol.push({
        idPersonaRol: 1007,
        idPersona: 999,
        idRol: 1,
        idClinica: 10,
      });

      expect(pacientesDeClinica(datos, 10)).to.have.lengthOf(2);
    });

    it("debe aceptar identificadores numéricos y cadenas equivalentes", () => {
      const datos = crearDatos();

      expect(pacientesDeClinica(datos, "10")).to.have.lengthOf(2);
    });

    it("debe devolver una lista vacía cuando la clínica no tiene pacientes", () => {
      const datos = crearDatos();

      expect(pacientesDeClinica(datos, 999)).to.deep.equal([]);
    });

  });

});
