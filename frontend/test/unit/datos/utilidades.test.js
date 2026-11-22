
import { expect } from "chai";
import sinon from "sinon";

import {
  edadDe,
  registroVisible,
  etiquetaPaso,
  notaDelPaso,
  motivoDe,
} from "../../../js/datos.js";

// --------------------------------------------------
// edadDe
// --------------------------------------------------

describe("edadDe", () => {
  let reloj;

  beforeEach(() => {
    // Fijamos la fecha para que las pruebas sean reproducibles.
    reloj = sinon.useFakeTimers({
      now: new Date("2026-11-22T12:00:00-06:00"),
    });
  });

  afterEach(() => {
    reloj.restore();
  });

  it("calcula correctamente la edad cuando ya cumplió años", () => {
    const resultado = edadDe({ fechaNacimiento: "2000-05-10" });

    expect(resultado).to.equal(26);
  });

  it("resta un año si todavía no ha llegado el cumpleaños", () => {
    const resultado = edadDe({ fechaNacimiento: "2000-12-10" });

    expect(resultado).to.equal(25);
  });

  it("considera correctamente el día del cumpleaños", () => {
    const resultado = edadDe({ fechaNacimiento: "2000-11-22" });

    expect(resultado).to.equal(26);
  });

  it("devuelve null cuando no existe fecha de nacimiento", () => {
    expect(edadDe({})).to.equal(null);
    expect(edadDe(null)).to.equal(null);
  });

  it("devuelve null cuando la fecha es inválida", () => {
    expect(edadDe({ fechaNacimiento: "fecha-invalida" }))
      .to.equal(null);
  });

  it("devuelve null cuando la fecha de nacimiento está en el futuro", () => {
    const resultado = edadDe({ fechaNacimiento: "2027-01-01" });

    expect(resultado).to.equal(null);
  });
});

// --------------------------------------------------
// registroVisible
// --------------------------------------------------

describe("registroVisible", () => {
  it("devuelve null cuando el paso está pendiente", () => {
    const registro = { id: 1 };

    expect(registroVisible({
      estado: "pendiente",
      registro,
    })).to.equal(null);
  });

  it("devuelve el registro cuando el paso está en curso", () => {
    const registro = { id: 1 };

    expect(registroVisible({
      estado: "actual",
      registro,
    })).to.equal(registro);
  });

  it("devuelve el registro cuando el paso está completado", () => {
    const registro = { id: 2 };

    expect(registroVisible({
      estado: "completado",
      registro,
    })).to.equal(registro);
  });

  it("devuelve null cuando no existe registro", () => {
    expect(registroVisible({
      estado: "actual",
      registro: null,
    })).to.equal(null);
  });

  it("devuelve null si el registro es undefined y el paso no está pendiente", () => {
    expect(registroVisible({
      estado: "actual",
    })).to.equal(null);
  });
});

// --------------------------------------------------
// etiquetaPaso
// --------------------------------------------------

describe("etiquetaPaso", () => {
  it("devuelve Por iniciar cuando el paso es actual pero no iniciado", () => {
    expect(etiquetaPaso("actual", false)).to.equal("Por iniciar");
  });

  it("utiliza la etiqueta correspondiente cuando el paso está iniciado", () => {
    const resultado = etiquetaPaso("actual", true);

    expect(resultado).to.be.a("string");
    expect(resultado).to.not.equal("");
    expect(resultado).to.not.equal("actual");
  });

  it("devuelve el estado original cuando no existe una etiqueta definida", () => {
    expect(etiquetaPaso("estado-desconocido"))
      .to.equal("estado-desconocido");
  });

  it("devuelve una cadena vacía cuando el estado es null", () => {
    expect(etiquetaPaso(null)).to.equal("");
  });

  it("convierte a texto un estado numérico desconocido", () => {
    expect(etiquetaPaso(123)).to.equal("123");
  });
});

// --------------------------------------------------
// notaDelPaso
// --------------------------------------------------

describe("notaDelPaso", () => {
  it("construye un resumen con diagnóstico, notas y plan", () => {
    const resultado = notaDelPaso({
      diagnostico: "J00",
      notas: "Dolor de garganta",
      plan: "Reposo",
    });

    expect(resultado).to.equal(
      "CIE-10: J00 | Notas: Dolor de garganta | Plan: Reposo"
    );
  });

  it("devuelve solamente el diagnóstico cuando es el único dato", () => {
    expect(notaDelPaso({ diagnostico: "J00" }))
      .to.equal("CIE-10: J00");
  });

  it("devuelve solamente las notas cuando es el único dato", () => {
    expect(notaDelPaso({ notas: "Paciente estable" }))
      .to.equal("Notas: Paciente estable");
  });

  it("devuelve solamente el plan cuando es el único dato", () => {
    expect(notaDelPaso({ plan: "Control en una semana" }))
      .to.equal("Plan: Control en una semana");
  });

  it("omite los campos vacíos", () => {
    expect(notaDelPaso({
      diagnostico: "",
      notas: "Paciente estable",
      plan: "",
    })).to.equal("Notas: Paciente estable");
  });

  it("devuelve una cadena vacía cuando no hay información", () => {
    expect(notaDelPaso({})).to.equal("");
  });

  it("acepta una llamada sin argumentos", () => {
    expect(notaDelPaso()).to.equal("");
  });
});

// --------------------------------------------------
// motivoDe
// --------------------------------------------------

describe("motivoDe", () => {
  it("devuelve el motivo de la consulta", () => {
    expect(motivoDe({ motivo: "Dolor de cabeza" }))
      .to.equal("Dolor de cabeza");
  });

  it("elimina espacios al inicio y al final", () => {
    expect(motivoDe({ motivo: "  Dolor de cabeza  " }))
      .to.equal("Dolor de cabeza");
  });

  it("utiliza las observaciones cuando no existe motivo", () => {
    expect(motivoDe({ observaciones: "Control médico" }))
      .to.equal("Control médico");
  });

  it("prioriza el motivo sobre las observaciones", () => {
    expect(motivoDe({
      motivo: "Fiebre",
      observaciones: "Control médico",
    })).to.equal("Fiebre");
  });

  it("devuelve el texto predeterminado cuando no hay motivo ni observaciones", () => {
    expect(motivoDe({})).to.equal("Consulta registrada");
  });

  it("conserva textos de exactamente 120 caracteres", () => {
    const motivo = "A".repeat(120);

    expect(motivoDe({ motivo })).to.equal(motivo);
  });

  it("recorta textos que superan los 120 caracteres", () => {
    const motivo = "A".repeat(121);
    const resultado = motivoDe({ motivo });

    expect(resultado).to.equal(`${"A".repeat(117)}…`);
    expect(resultado.length).to.equal(118);
  });

  it("acepta una consulta nula", () => {
    expect(motivoDe(null)).to.equal("Consulta registrada");
  });
});
