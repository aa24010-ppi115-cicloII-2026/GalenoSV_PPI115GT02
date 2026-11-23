import { expect } from "chai";
import { etiquetaPaso } from "../../js/boundary/presentacion.js";

describe("presentacion.js", () => {
  it("traduce el estado interno del paso a un texto legible", () => {
    expect(etiquetaPaso("completado")).to.equal("Completado");
    expect(etiquetaPaso("actual")).to.equal("En curso");
    // El paso siguiente que nadie ha iniciado no está «en curso».
    expect(etiquetaPaso("actual", false)).to.equal("Por iniciar");
    expect(etiquetaPaso("pendiente")).to.equal("Pendiente");
  });
});
