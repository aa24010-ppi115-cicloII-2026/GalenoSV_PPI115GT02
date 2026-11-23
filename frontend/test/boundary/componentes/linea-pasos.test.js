import { expect } from "chai";
import { usarDom, montar } from "../entorno-dom.js";

describe("linea-pasos", () => {
  usarDom();

  it("linea-pasos consume un modelo neutral y trata sus textos como texto", () => {
    const linea = montar("linea-pasos", {
      pasos: [{ nombre: "<Paso externo>", estado: "actual", etiqueta: "En curso", resumen: "Equipo A", detalles: [] }],
    });
    expect(linea.shadowRoot.querySelector(".nombre").textContent).to.equal("<Paso externo>");
    expect(linea.shadowRoot.querySelector(".nombre").querySelector("script")).to.be.null;
    expect(linea.shadowRoot.querySelector(".paso.actual .quien").textContent).to.equal("En curso · Equipo A");
  });
});
