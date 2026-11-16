import { expect } from "chai";
import { CODIGOS_CIE10, buscarCie10, cie10PorCodigo } from "../js/cie10.js";

describe("cie10.js", () => {
  it("incluye códigos odontológicos con descripción", () => {
    expect(CODIGOS_CIE10.length).to.be.greaterThan(20);
    for (const item of CODIGOS_CIE10) {
      expect(item.codigo).to.match(/^[A-Z]\d{2}(\.\d)?$/);
      expect(item.descripcion).to.be.a("string").and.not.empty;
    }
  });

  it("busca por código sin distinguir mayúsculas", () => {
    const resultados = buscarCie10("k02.1");
    expect(resultados[0].codigo).to.equal("K02.1");
  });

  it("busca por texto de la descripción", () => {
    const resultados = buscarCie10("gingivitis");
    expect(resultados.length).to.be.greaterThan(0);
    expect(resultados.every((item) => /gingivitis/i.test(item.descripcion))).to.be.true;
  });

  it("respeta el límite de resultados y devuelve vacío sin texto", () => {
    expect(buscarCie10("caries", 2)).to.have.length(2);
    expect(buscarCie10("")).to.deep.equal([]);
  });

  it("obtiene un código exacto", () => {
    expect(cie10PorCodigo("K02.9").descripcion).to.contain("Caries");
    expect(cie10PorCodigo("X99")).to.be.null;
  });
});

