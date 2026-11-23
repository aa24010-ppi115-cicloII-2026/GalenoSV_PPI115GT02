import { JSDOM } from "jsdom";
import { expect } from "chai";
import { CLAVE_TEMA, temaActual, elegirTema, alternarTema, alCambiarTema } from "../../js/control/tema.js";

describe("tema.js", () => {
  let dom;

  const temaAplicado = () => document.documentElement.dataset.tema;

  beforeEach(() => {
    dom = new JSDOM("<!DOCTYPE html>", { url: "http://localhost:3000/" });
    // El sistema prefiere oscuro: la aplicación no debe hacerle caso.
    dom.window.matchMedia = () => ({ matches: true, addEventListener() {}, removeEventListener() {} });
    globalThis.window = dom.window;
    globalThis.document = dom.window.document;
    globalThis.localStorage = dom.window.localStorage;
  });

  afterEach(() => {
    dom.window.close();
    delete globalThis.window;
    delete globalThis.document;
    delete globalThis.localStorage;
  });

  it("empieza en claro aunque el sistema prefiera oscuro", () => {
    expect(temaActual()).to.equal("claro");
  });

  it("al elegir oscuro lo aplica y lo recuerda; al volver a claro también lo recuerda", () => {
    expect(alternarTema()).to.equal("oscuro");
    expect(temaAplicado()).to.equal("oscuro");
    expect(localStorage.getItem(CLAVE_TEMA)).to.equal("oscuro");
    expect(temaActual()).to.equal("oscuro");

    expect(alternarTema()).to.equal("claro");
    expect(temaAplicado()).to.equal("claro");
    expect(localStorage.getItem(CLAVE_TEMA)).to.equal("claro");
  });

  it("un valor guardado que no es un tema se trata como claro", () => {
    localStorage.setItem(CLAVE_TEMA, "azul");
    expect(temaActual()).to.equal("claro");
    expect(elegirTema("azul")).to.equal("claro");
    expect(temaAplicado()).to.equal("claro");
  });

  it("si el navegador bloquea el almacenamiento el tema se cambia igual", () => {
    expect(temaActual()).to.equal("claro");
    Object.defineProperty(globalThis, "localStorage", {
      configurable: true,
      get() {
        throw new Error("SecurityError");
      },
    });
    expect(alternarTema()).to.equal("oscuro");
    expect(temaAplicado()).to.equal("oscuro");
    expect(temaActual()).to.equal("oscuro");
    expect(alternarTema()).to.equal("claro");
  });

  it("aplica el tema elegido en otra pestaña y deja de escuchar cuando se le pide", () => {
    const avisos = [];
    const dejar = alCambiarTema((tema) => avisos.push(tema));

    localStorage.setItem(CLAVE_TEMA, "oscuro");
    window.dispatchEvent(new dom.window.StorageEvent("storage", { key: CLAVE_TEMA }));
    window.dispatchEvent(new dom.window.StorageEvent("storage", { key: "otra-clave" }));
    expect(avisos).to.deep.equal(["oscuro"]);
    expect(temaAplicado()).to.equal("oscuro");

    dejar();
    window.dispatchEvent(new dom.window.StorageEvent("storage", { key: CLAVE_TEMA }));
    expect(avisos).to.have.length(1);
  });
});
