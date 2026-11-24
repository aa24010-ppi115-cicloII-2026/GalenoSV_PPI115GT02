import { expect } from "chai";
import { buscarPorId, filasDonde, mismoId } from "../../js/control/tablas.js";

/** Tabla que cuenta cuántas veces se lee alguna de sus filas. */
function tablaContada(filas) {
  const lecturas = { total: 0 };
  const tabla = new Proxy(filas, {
    get(objetivo, propiedad, receptor) {
      if (typeof propiedad === "string" && /^\d+$/.test(propiedad)) lecturas.total += 1;
      return Reflect.get(objetivo, propiedad, receptor);
    },
  });
  return { tabla, lecturas };
}

describe("tablas.js", () => {
  const datos = () => ({
    personas: [
      { idPersona: 1, nombres: "Ana" },
      { idPersona: "2", nombres: "Luis" },
      { idPersona: 1, nombres: "Repetida" },
      { idPersona: null, nombres: "Sin id" },
    ],
  });

  it("buscarPorId devuelve el primer registro con ese id, comparando como texto", () => {
    expect(buscarPorId(datos(), "personas", "1").nombres).to.equal("Ana");
    expect(buscarPorId(datos(), "personas", 2).nombres).to.equal("Luis");
    expect(buscarPorId(datos(), "personas", 99)).to.be.null;
    expect(buscarPorId(datos(), "personas", null)).to.be.null;
    expect(buscarPorId(datos(), "personas", undefined)).to.be.null;
    expect(buscarPorId({}, "personas", 1)).to.be.null;
  });

  it("filasDonde devuelve las filas de uno o varios valores en el orden de la tabla", () => {
    const { personas } = datos();
    expect(filasDonde(personas, "idPersona", 1).map((p) => p.nombres)).to.deep.equal(["Ana", "Repetida"]);
    expect(filasDonde(personas, "idPersona", ["2", 1]).map((p) => p.nombres)).to.deep.equal(["Ana", "Luis", "Repetida"]);
    expect(filasDonde(personas, "idPersona", null)).to.deep.equal([]);
    expect(filasDonde(personas, "idPersona", [null, undefined])).to.deep.equal([]);
    expect(filasDonde(undefined, "idPersona", 1)).to.deep.equal([]);
  });

  it("filasDonde coincide con filtrar la tabla usando mismoId", () => {
    const { personas } = datos();
    for (const valor of [1, "1", 2, null, undefined, 7]) {
      expect(filasDonde(personas, "idPersona", valor)).to.deep.equal(personas.filter((p) => mismoId(p.idPersona, valor)));
    }
  });

  it("muchas búsquedas sobre la misma tabla la recorren una sola vez", () => {
    const filas = Array.from({ length: 500 }, (_, i) => ({ idPersona: i, idClinica: i % 5 }));
    const { tabla, lecturas } = tablaContada(filas);
    const tablas = { personas: tabla };
    for (let i = 0; i < 500; i++) {
      expect(buscarPorId(tablas, "personas", i)).to.equal(filas[i]);
      expect(filasDonde(tabla, "idClinica", i % 5)).to.have.length(100);
    }
    // Un recorrido por cada campo indexado (idPersona e idClinica) más las filas que se entregan;
    // recorrer la tabla en cada búsqueda serían cientos de miles de lecturas.
    const recorridos = 2 * filas.length;
    const entregadas = 500 * 1 + 500 * 100;
    expect(lecturas.total).to.be.at.most(recorridos + entregadas);
  });
});
