import { expect } from "chai";
import { CODIGOS_CIE10 } from "../../js/entity/cie10.js";

describe("cie10.js", () => {
  it("cada código del catálogo tiene formato CIE-10, descripción y no se repite", () => {
    expect(CODIGOS_CIE10.length).to.be.greaterThan(20);
    for (const item of CODIGOS_CIE10) {
      expect(item.codigo).to.match(/^[A-Z]\d{2}(\.\d)?$/);
      expect(item.descripcion).to.be.a("string").and.not.empty;
    }
    expect(new Set(CODIGOS_CIE10.map((item) => item.codigo)).size).to.equal(CODIGOS_CIE10.length);
  });
});
