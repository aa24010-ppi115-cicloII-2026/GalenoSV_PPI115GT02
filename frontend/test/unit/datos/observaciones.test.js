
import { expect } from "chai";

import {
  leerObservaciones,
  serializarObservaciones,
} from "../../../js/datos.js";

// --------------------------------------------------
// leerObservaciones
// --------------------------------------------------

describe("leerObservaciones", () => {
  it("interpreta correctamente un objeto JSON completo", () => {
    const valor = JSON.stringify({
      notas: "Paciente estable",
      diagnostico: "J00",
      plan: "Reposo",
      pasos: { 10: "Evaluación inicial" },
    });

    expect(leerObservaciones(valor)).to.deep.equal({
      notas: "Paciente estable",
      diagnostico: "J00",
      plan: "Reposo",
      pasos: { 10: "Evaluación inicial" },
    });
  });

  it("utiliza valores vacíos cuando faltan propiedades", () => {
    expect(leerObservaciones(JSON.stringify({ notas: "Observación" })))
      .to.deep.equal({
        notas: "Observación",
        diagnostico: "",
        plan: "",
        pasos: {},
      });
  });

  it("reemplaza propiedades null por sus valores predeterminados", () => {
    const valor = JSON.stringify({
      notas: null,
      diagnostico: null,
      plan: null,
      pasos: null,
    });

    expect(leerObservaciones(valor)).to.deep.equal({
      notas: "",
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("conserva los pasos cuando son un objeto", () => {
    const pasos = {
      10: "Primera observación",
      20: "Segunda observación",
    };

    const resultado = leerObservaciones(JSON.stringify({ pasos }));

    expect(resultado.pasos).to.deep.equal(pasos);
  });

  it("reemplaza pasos que son un valor primitivo por un objeto vacío", () => {
    const resultado = leerObservaciones(
      JSON.stringify({ pasos: "texto" })
    );

    expect(resultado.pasos).to.deep.equal({});
  });

  it("conserva texto libre de versiones anteriores", () => {
    const resultado = leerObservaciones("Paciente presenta dolor");

    expect(resultado).to.deep.equal({
      notas: "Paciente presenta dolor",
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("conserva el texto original cuando el JSON está malformado", () => {
    const texto = '{"notas": "incompleto"';

    expect(leerObservaciones(texto)).to.deep.equal({
      notas: texto,
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("devuelve valores vacíos cuando recibe null", () => {
    expect(leerObservaciones(null)).to.deep.equal({
      notas: "",
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("devuelve valores vacíos cuando recibe undefined", () => {
    expect(leerObservaciones(undefined)).to.deep.equal({
      notas: "",
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("trata un arreglo JSON como texto libre", () => {
    const valor = '["nota 1","nota 2"]';

    expect(leerObservaciones(valor)).to.deep.equal({
      notas: valor,
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("trata un número JSON como texto libre", () => {
    const valor = "123";

    expect(leerObservaciones(valor)).to.deep.equal({
      notas: valor,
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });
});

// --------------------------------------------------
// serializarObservaciones
// --------------------------------------------------

describe("serializarObservaciones", () => {
  it("serializa todas las propiedades", () => {
    const observaciones = {
      notas: "Paciente estable",
      diagnostico: "J00",
      plan: "Reposo",
      pasos: { 10: "Evaluación" },
    };

    const resultado = JSON.parse(
      serializarObservaciones(observaciones)
    );

    expect(resultado).to.deep.equal(observaciones);
  });

  it("utiliza valores predeterminados para propiedades omitidas", () => {
    const resultado = JSON.parse(
      serializarObservaciones({ notas: "Observación" })
    );

    expect(resultado).to.deep.equal({
      notas: "Observación",
      diagnostico: "",
      plan: "",
      pasos: {},
    });
  });

  it("serializa correctamente observaciones vacías", () => {
    expect(serializarObservaciones({})).to.equal(
      '{"notas":"","diagnostico":"","plan":"","pasos":{}}'
    );
  });

  it("conserva caracteres especiales y tildes", () => {
    const observaciones = {
      notas: "Dolor, náuseas y revisión: \"pendiente\"",
      diagnostico: "J00",
      plan: "Control médico",
      pasos: {},
    };

    const resultado = JSON.parse(
      serializarObservaciones(observaciones)
    );

    expect(resultado).to.deep.equal(observaciones);
  });

  it("conserva los pasos vacíos", () => {
    const resultado = JSON.parse(
      serializarObservaciones({
        notas: "",
        diagnostico: "",
        plan: "",
        pasos: {},
      })
    );

    expect(resultado.pasos).to.deep.equal({});
  });

  it("permite recuperar los datos después de serializarlos", () => {
    const original = {
      notas: "Dolor de cabeza",
      diagnostico: "R51",
      plan: "Seguimiento",
      pasos: { 10: "Revisión completada" },
    };

    const serializado = serializarObservaciones(original);
    const recuperado = leerObservaciones(serializado);

    expect(recuperado).to.deep.equal(original);
  });
});
