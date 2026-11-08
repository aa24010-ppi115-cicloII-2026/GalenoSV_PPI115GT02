import { expect } from "chai";
import { recursos, recursoPorClave } from "../js/recursos.js";

const claves = recursos.map((recurso) => recurso.clave);

describe("recursos.js", () => {
  it("define los 20 recursos de la API", () => {
    expect(recursos).to.have.lengthOf(20);
    expect(new Set(claves).size).to.equal(20);
  });

  it("cada recurso tiene clave, ruta, id, título, grupo y etiqueta", () => {
    for (const recurso of recursos) {
      expect(recurso.clave, "clave").to.be.a("string").and.not.empty;
      expect(recurso.ruta, `${recurso.clave}.ruta`).to.match(/^[a-z-]+$/);
      expect(recurso.id, `${recurso.clave}.id`).to.match(/^id[A-Z]/);
      expect(recurso.titulo, `${recurso.clave}.titulo`).to.be.a("string").and.not.empty;
      expect(recurso.grupo, `${recurso.clave}.grupo`).to.be.oneOf(["Catálogos", "Clínicos"]);
      expect(recurso.etiqueta, `${recurso.clave}.etiqueta`).to.be.a("function");
      expect(recurso.campos, `${recurso.clave}.campos`).to.be.an("array").and.not.empty;
    }
  });

  it("los campos tienen nombre y tipo válido, sin duplicados", () => {
    const tipos = ["texto", "textarea", "fecha", "fechaHora", "booleano", "select"];
    for (const recurso of recursos) {
      const nombres = recurso.campos.map((campo) => campo.campo);
      expect(new Set(nombres).size, `${recurso.clave} campos duplicados`).to.equal(nombres.length);
      for (const campo of recurso.campos) {
        expect(campo.campo, `${recurso.clave}.campo`).to.match(/^[a-zA-Z]+$/);
        expect(campo.etiqueta, `${recurso.clave}.${campo.campo}.etiqueta`).to.be.a("string").and.not.empty;
        expect(campo.tipo, `${recurso.clave}.${campo.campo}.tipo`).to.be.oneOf(tipos);
        if (campo.tipo === "select") {
          expect(recursoPorClave(campo.recurso), `${recurso.clave}.${campo.campo} -> ${campo.recurso}`).to.exist;
        }
      }
    }
  });

  it("solo los recursos con nombre lo exigen", () => {
    const conRequeridos = recursos
      .filter((recurso) => recurso.campos.some((campo) => campo.requerido))
      .map((recurso) => recurso.clave)
      .sort();
    expect(conRequeridos).to.deep.equal(
      [
        "clinicas",
        "examenes",
        "personas",
        "procedimientos",
        "procedimientos-paso",
        "roles",
        "tipos-documento",
        "tipos-examen",
        "tipos-medio-contacto",
      ].sort()
    );
  });

  it("encuentra recursos por clave y devuelve undefined si no existe", () => {
    expect(recursoPorClave("roles").ruta).to.equal("roles");
    expect(recursoPorClave("no-existe")).to.be.undefined;
  });

  it("la etiqueta de personas concatena nombres y apellidos", () => {
    expect(recursoPorClave("personas").etiqueta({ nombres: "Ana", apellidos: "López" })).to.equal("Ana López");
    expect(recursoPorClave("personas").etiqueta({ nombres: "Ana" })).to.equal("Ana");
  });
});
