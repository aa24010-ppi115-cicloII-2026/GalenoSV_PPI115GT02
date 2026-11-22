
import { expect } from "chai";

import {
  fechaHora,
  fechaCorta,
  fechaCalendario,
  fechaISO,
  hoyISO,
  ahoraISO,
} from "../../../js/fechas.js";

describe("fechas.js", () => {

  describe("fechaHora()", () => {

    it("debe formatear una fecha y hora válida", () => {
      const resultado = fechaHora("2026-11-22T10:30:00-06:00");

      expect(resultado).to.be.a("string");
      expect(resultado).to.not.equal("—");
      expect(resultado).to.include("2026");
    });

    it("debe retornar un guion cuando recibe null", () => {
      expect(fechaHora(null)).to.equal("—");
    });

    it("debe retornar un guion cuando recibe una fecha inválida", () => {
      expect(fechaHora("fecha-invalida")).to.equal("—");
    });

  });

  describe("fechaCorta()", () => {

    it("debe formatear una fecha válida", () => {
      const resultado = fechaCorta("2026-11-22T10:30:00-06:00");

      expect(resultado).to.be.a("string");
      expect(resultado).to.not.equal("—");
      expect(resultado).to.include("2026");
    });

    it("debe retornar un guion para una fecha vacía", () => {
      expect(fechaCorta("")).to.equal("—");
    });

  });

  describe("fechaCalendario()", () => {

    it("debe conservar el día UTC de la fecha de nacimiento", () => {
      const resultado = fechaCalendario("2026-11-22T00:00:00Z");

      expect(resultado).to.include("22");
      expect(resultado).to.include("2026");
    });

    it("debe retornar un guion para una fecha inválida", () => {
      expect(fechaCalendario("invalida")).to.equal("—");
    });

  });

  describe("fechaISO()", () => {

    it("debe retornar la fecha en formato YYYY-MM-DD", () => {
      const fecha = new Date(2026, 10, 22, 12);
      
      expect(fechaISO(fecha)).to.equal("2026-11-22");
    });

    it("debe agregar cero a los meses menores a diez", () => {
      const fecha = new Date(2026, 0, 15, 12);

      expect(fechaISO(fecha)).to.equal("2026-01-15");
    });

    it("debe agregar cero a los días menores a diez", () => {
      const fecha = new Date(2026, 10, 5, 12);

      expect(fechaISO(fecha)).to.equal("2026-11-05");
    });

  });

  describe("hoyISO()", () => {

    it("debe retornar una fecha con formato YYYY-MM-DD", () => {
      expect(hoyISO()).to.match(/^\d{4}-\d{2}-\d{2}$/);
    });

  });

  describe("ahoraISO()", () => {

    it("debe retornar una fecha con hora y zona horaria", () => {
      const resultado = ahoraISO();

      expect(resultado).to.match(
        /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}[+-]\d{2}:\d{2}$/
      );
    });

    it("debe retornar una fecha que pueda interpretarse correctamente", () => {
      const resultado = ahoraISO();

      expect(new Date(resultado).getTime()).to.be.a("number");
      expect(new Date(resultado).getTime()).to.be.finite;
    });

  });

});
