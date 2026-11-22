
import { expect } from "chai";

import {
  documentoDe,
  tiposDocumentoActivos,
  tipoDocumentoElegido,
  indicacionesDocumento,
  mensajeFormatoDocumento,
  cumpleFormato,
  errorDeFormatoDocumento,
  personaDeDocumento,
} from "../../../js/datos.js";

describe("datos.js", () => {

  describe("documentoDe()", () => {

    const datos = {
      documentos: [
        { idDocumento: 1, idPersona: 100, idTipoDocumento: 1, valor: "12345678-9" },
        { idDocumento: 2, idPersona: 200, idTipoDocumento: 2, valor: "AB123456" },
      ],
    };

    it("debe encontrar un documento por tipo y número", () => {
      const resultado = documentoDe(datos, 1, "12345678-9");

      expect(resultado).to.deep.equal(datos.documentos[0]);
    });

    it("debe ignorar diferencias entre mayúsculas y minúsculas", () => {
      const resultado = documentoDe(datos, 2, "ab123456");

      expect(resultado).to.deep.equal(datos.documentos[1]);
    });

    it("debe ignorar diferencias de acentuación y espacios", () => {
      const datosPrueba = {
        documentos: [
          { idTipoDocumento: 1, valor: "  ÁBC123  " },
        ],
      };

      expect(documentoDe(datosPrueba, 1, "abc123"))
        .to.equal(datosPrueba.documentos[0]);
    });

    it("debe devolver undefined cuando no existe el documento", () => {
      expect(documentoDe(datos, 1, "00000000-0")).to.be.undefined;
    });

    it("no debe confundir documentos de diferentes tipos", () => {
      expect(documentoDe(datos, 1, "AB123456")).to.be.undefined;
    });

  });

  describe("tiposDocumentoActivos()", () => {

    it("debe excluir los tipos de documento inactivos", () => {
      const datos = {
        tiposDocumento: [
          { idTipoDocumento: 1, nombre: "DUI", activo: true },
          { idTipoDocumento: 2, nombre: "Pasaporte", activo: false },
        ],
      };

      const resultado = tiposDocumentoActivos(datos);

      expect(resultado).to.have.lengthOf(1);
      expect(resultado[0].nombre).to.equal("DUI");
    });

    it("debe considerar activos los tipos sin propiedad activo", () => {
      const datos = {
        tiposDocumento: [
          { idTipoDocumento: 1, nombre: "DUI" },
        ],
      };

      expect(tiposDocumentoActivos(datos)).to.have.lengthOf(1);
    });

    it("debe ordenar los tipos alfabéticamente", () => {
      const datos = {
        tiposDocumento: [
          { nombre: "Pasaporte" },
          { nombre: "DUI" },
          { nombre: "Carnet" },
        ],
      };

      const resultado = tiposDocumentoActivos(datos);

      expect(resultado.map(({ nombre }) => nombre))
        .to.deep.equal(["Carnet", "DUI", "Pasaporte"]);
    });

    it("no debe modificar el arreglo original", () => {
      const originales = [
        { nombre: "Pasaporte" },
        { nombre: "DUI" },
      ];

      const datos = { tiposDocumento: originales };

      tiposDocumentoActivos(datos);

      expect(datos.tiposDocumento).to.equal(originales);
      expect(datos.tiposDocumento[0].nombre).to.equal("Pasaporte");
    });

    it("debe devolver una lista vacía cuando no existen tipos", () => {
      expect(tiposDocumentoActivos({})).to.deep.equal([]);
    });

  });

  describe("tipoDocumentoElegido()", () => {

    const datos = {
      tiposDocumento: [
        { idTipoDocumento: 1, nombre: "DUI" },
        { idTipoDocumento: 2, nombre: "Pasaporte" },
      ],
    };

    it("debe encontrar el tipo de documento por identificador", () => {
      expect(tipoDocumentoElegido(datos, 1))
        .to.equal(datos.tiposDocumento[0]);
    });

    it("debe aceptar identificadores numéricos y cadenas equivalentes", () => {
      expect(tipoDocumentoElegido(datos, "2"))
        .to.equal(datos.tiposDocumento[1]);
    });

    it("debe devolver null cuando no encuentra el identificador", () => {
      expect(tipoDocumentoElegido(datos, 99)).to.be.null;
    });

    it("debe manejar datos nulos", () => {
      expect(tipoDocumentoElegido(null, 1)).to.be.null;
    });

  });

  describe("indicacionesDocumento()", () => {

    it("debe utilizar las indicaciones configuradas", () => {
      const tipo = {
        nombre: "DUI",
        indicaciones: "Ingrese su número de DUI",
        expresionRegular: "\\d{8}-\\d",
      };

      expect(indicacionesDocumento(tipo)).to.deep.equal({
        texto: "Ingrese su número de DUI",
        ejemplo: "Ej. 00000000-0",
      });
    });

    it("debe mostrar la expresión regular cuando no hay indicaciones", () => {
      const tipo = {
        nombre: "Pasaporte",
        expresionRegular: "[A-Z]{2}\\d{6}",
      };

      expect(indicacionesDocumento(tipo).texto)
        .to.equal("Formato: [A-Z]{2}\\d{6}");
    });

    it("debe mostrar texto vacío cuando no hay instrucciones", () => {
      expect(indicacionesDocumento({ nombre: "Pasaporte" }))
        .to.deep.equal({ texto: "", ejemplo: "" });
    });

    it("debe proporcionar el ejemplo para DUI", () => {
      expect(indicacionesDocumento({ nombre: "DUI" }).ejemplo)
        .to.equal("Ej. 00000000-0");
    });

  });

  describe("mensajeFormatoDocumento()", () => {

    it("debe incluir el nombre del tipo de documento", () => {
      const tipo = {
        nombre: "DUI",
        indicaciones: "Ocho dígitos y un guion",
      };

      expect(mensajeFormatoDocumento(tipo))
        .to.equal("El número de DUI no tiene el formato esperado: Ocho dígitos y un guion.");
    });

    it("debe utilizar la expresión regular como detalle alternativo", () => {
      const tipo = {
        nombre: "Pasaporte",
        expresionRegular: "[A-Z]{2}\\d{6}",
      };

      expect(mensajeFormatoDocumento(tipo))
        .to.equal("El número de Pasaporte no tiene el formato esperado: [A-Z]{2}\\d{6}.");
    });

    it("debe utilizar un mensaje genérico cuando no hay tipo", () => {
      expect(mensajeFormatoDocumento(null))
        .to.equal("El número no tiene el formato esperado: el formato del documento.");
    });

  });

  describe("cumpleFormato()", () => {

    const tipo = {
      expresionRegular: "^\\d{8}-\\d$",
    };

    it("debe aceptar un documento que cumple la expresión regular", () => {
      expect(cumpleFormato(tipo, "12345678-9")).to.be.true;
    });

    it("debe rechazar un documento que no cumple la expresión regular", () => {
      expect(cumpleFormato(tipo, "123456789")).to.be.false;
    });

    it("debe aceptar cualquier valor cuando el tipo no tiene expresión regular", () => {
      expect(cumpleFormato({ nombre: "Documento" }, "cualquier valor"))
        .to.be.true;
    });

    it("debe aceptar el valor cuando la expresión regular está mal configurada", () => {
      expect(cumpleFormato({ expresionRegular: "[" }, "12345"))
        .to.be.true;
    });

    it("debe aceptar cualquier valor cuando el tipo es nulo", () => {
      expect(cumpleFormato(null, "12345")).to.be.true;
    });

  });

  describe("errorDeFormatoDocumento()", () => {

    const tipo = {
      nombre: "DUI",
      expresionRegular: "^\\d{8}-\\d$",
    };

    it("debe devolver una cadena vacía cuando el documento es válido", () => {
      expect(errorDeFormatoDocumento(tipo, "12345678-9")).to.equal("");
    });

    it("debe devolver el mensaje de error cuando el documento es inválido", () => {
      expect(errorDeFormatoDocumento(tipo, "12345"))
        .to.equal(mensajeFormatoDocumento(tipo));
    });

  });

  describe("personaDeDocumento()", () => {

    const datos = {
      documentos: [
        { idDocumento: 1, idPersona: 100, idTipoDocumento: 1, valor: "12345678-9" },
      ],
      personas: [
        { idPersona: 100, nombres: "Ana", apellidos: "López" },
      ],
    };

    it("debe encontrar la persona asociada al documento", () => {
      const resultado = personaDeDocumento(datos, 1, "12345678-9");

      expect(resultado).to.deep.equal(datos.personas[0]);
    });

    it("debe aceptar identificadores numéricos y cadenas equivalentes", () => {
      const resultado = personaDeDocumento(datos, "1", "12345678-9");

      expect(resultado).to.equal(datos.personas[0]);
    });

    it("debe devolver null cuando el documento no existe", () => {
      expect(personaDeDocumento(datos, 1, "00000000-0")).to.be.null;
    });

    it("debe devolver null cuando la persona asociada no existe", () => {
      const datosPrueba = {
        documentos: [
          { idDocumento: 1, idPersona: 999, idTipoDocumento: 1, valor: "12345678-9" },
        ],
        personas: [],
      };

      expect(personaDeDocumento(datosPrueba, 1, "12345678-9")).to.be.null;
    });

  });

});
