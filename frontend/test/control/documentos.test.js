import { expect } from "chai";
import { datosBase } from "../ayudas.js";
import {
  cumpleFormato,
  errorDeFormatoDocumento,
  indicacionesDocumento,
  mensajeFormatoDocumento,
  tipoDocumentoElegido,
} from "../../js/control/documentos.js";

describe("documentos.js", () => {
  let datos;

  beforeEach(() => {
    datos = datosBase();
  });

  it("cumpleFormato valida con la expresión del tipo y no bloquea si el catálogo no la trae o está mal escrita", () => {
    const dui = { expresionRegular: "^[0-9]{8}-[0-9]$" };
    expect(cumpleFormato(dui, "01234567-8")).to.be.true;
    expect(cumpleFormato(dui, "123")).to.be.false;
    expect(cumpleFormato({ expresionRegular: "" }, "A123")).to.be.true;
    expect(cumpleFormato({ expresionRegular: "([" }, "A123")).to.be.true;
    expect(cumpleFormato(null, "A123")).to.be.true;
  });

  it("compone el mensaje de formato con el nombre del tipo de documento", () => {
    const tipo = tipoDocumentoElegido(datos, "1");
    expect(tipo.nombre).to.equal("DUI");
    expect(mensajeFormatoDocumento({ ...tipo, indicaciones: "8 dígitos, guion y dígito verificador" })).to.equal(
      "El número de DUI no tiene el formato esperado: 8 dígitos, guion y dígito verificador."
    );
  });

  it("la ayuda y el error nunca muestran la expresión regular del catálogo", () => {
    const pasaporte = { nombre: "Pasaporte", expresionRegular: "^[A-Z][0-9]{8}$", indicaciones: "" };

    expect(indicacionesDocumento(pasaporte).texto).to.equal("");
    expect(mensajeFormatoDocumento(pasaporte)).to.equal("El número de Pasaporte no tiene el formato esperado.");
    expect(errorDeFormatoDocumento(pasaporte, "123")).not.to.contain(pasaporte.expresionRegular);
  });

  it("indicacionesDocumento ofrece las indicaciones del catálogo y un ejemplo para el DUI", () => {
    const dui = { nombre: "DUI", expresionRegular: "^[0-9]{8}-[0-9]$", indicaciones: "8 dígitos, guion y dígito verificador" };

    expect(indicacionesDocumento(dui)).to.deep.equal({
      texto: "8 dígitos, guion y dígito verificador",
      ejemplo: "Ej. 00000000-0",
    });
    expect(indicacionesDocumento(null)).to.deep.equal({ texto: "", ejemplo: "" });
  });

  it("errorDeFormatoDocumento devuelve el mensaje solo cuando el número no cumple el formato", () => {
    const dui = { nombre: "DUI", expresionRegular: "^[0-9]{8}-[0-9]$" };
    expect(errorDeFormatoDocumento(dui, "01234567-8")).to.equal("");
    expect(errorDeFormatoDocumento(dui, "123")).to.contain("no tiene el formato esperado");
  });
});
