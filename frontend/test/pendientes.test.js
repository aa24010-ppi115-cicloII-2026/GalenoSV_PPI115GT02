import { JSDOM } from "jsdom";
import { expect } from "chai";
import sinon from "sinon";
import {
  encolar,
  listarPendientes,
  quitarPendiente,
  vaciarPendientes,
  sincronizar,
  estaConectado,
} from "../js/pendientes.js";

describe("pendientes.js", () => {
  let dom;

  beforeEach(() => {
    dom = new JSDOM("<!DOCTYPE html>", { url: "http://localhost:3000/" });
    globalThis.localStorage = dom.window.localStorage;
    vaciarPendientes();
  });

  afterEach(() => {
    vaciarPendientes();
    dom.window.close();
    delete globalThis.localStorage;
  });

  it("detecta el estado de conexión", () => {
    expect(estaConectado()).to.be.a("boolean");
    Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
    expect(estaConectado()).to.be.false;
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    expect(estaConectado()).to.be.true;
  });

  it("encola operaciones con identificador único", () => {
    encolar({ metodo: "POST", ruta: "consultas-procedimiento-paso", cuerpo: { estado: "EN_PROCESO" } });
    encolar({ metodo: "PUT", ruta: "consultas-procedimiento/1", cuerpo: { observaciones: "x" } });
    const pendientes = listarPendientes();
    expect(pendientes).to.have.length(2);
    expect(pendientes[0].id).to.not.equal(pendientes[1].id);
    expect(pendientes[0].cuerpo.estado).to.equal("EN_PROCESO");
  });

  it("quita una operación por identificador", () => {
    encolar({ metodo: "POST", ruta: "a", cuerpo: {} });
    const id = listarPendientes()[0].id;
    quitarPendiente(id);
    expect(listarPendientes()).to.have.length(0);
  });

  it("vacía la cola completa", () => {
    encolar({ metodo: "POST", ruta: "a", cuerpo: {} });
    vaciarPendientes();
    expect(listarPendientes()).to.have.length(0);
  });

  it("reenvía las operaciones pendientes y las elimina de la cola", async () => {
    encolar({ metodo: "POST", ruta: "a", cuerpo: {} });
    encolar({ metodo: "PUT", ruta: "b/1", cuerpo: {} });
    const ejecutadas = [];
    const resultado = await sincronizar(async (pendiente) => {
      ejecutadas.push(pendiente.ruta);
    });
    expect(resultado.enviadas).to.equal(2);
    expect(resultado.pendientes).to.equal(0);
    expect(ejecutadas).to.deep.equal(["a", "b/1"]);
    expect(listarPendientes()).to.have.length(0);
  });

  it("conserva las operaciones que fallan al reenviar", async () => {
    encolar({ metodo: "POST", ruta: "a", cuerpo: {} });
    const resultado = await sincronizar(async () => {
      throw new Error("sin red");
    });
    expect(resultado.enviadas).to.equal(0);
    expect(resultado.pendientes).to.equal(1);
    expect(listarPendientes()).to.have.length(1);
  });
});
