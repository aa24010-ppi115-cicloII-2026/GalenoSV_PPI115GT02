import { expect } from "chai";
import { leerObservaciones, serializarObservaciones } from "../../js/entity/observaciones.js";

describe("observaciones.js", () => {
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
});
