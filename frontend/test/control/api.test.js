import { expect } from "chai";
import sinon from "sinon";
import { listarPendientes } from "../../js/control/pendientes.js";
import { comprobarServicio, servicioNoResponde } from "../../js/control/api.js";
import DefaultService from "../../js/control/default-service.js";

const respuestaJson = (cuerpo, estado = 200, cabeceras = {}) =>
  new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { "Content-Type": "application/json", ...cabeceras },
  });

describe("api.js", () => {
  const roles = new DefaultService("roles");
  let fetchFalso;

  beforeEach(() => {
    fetchFalso = sinon.stub(globalThis, "fetch");
  });

  afterEach(() => {
    sinon.restore();
  });

  it("encola las escrituras cuando no hay conexión", async () => {
    const almacen = {};
    globalThis.localStorage = {
      getItem: (clave) => Object.hasOwn(almacen, clave) ? almacen[clave] : null,
      setItem: (clave, valor) => { almacen[clave] = String(valor); },
      removeItem: (clave) => { delete almacen[clave]; },
    };
    Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
    try {
      await roles.crear({ nombre: "Sin red" });
      await roles.modificar("xyz", { nombre: "Sin red" });
      await roles.eliminar("xyz");
      const pendientes = listarPendientes();
      expect(pendientes).to.have.length(3);
      expect(pendientes.map((p) => p.metodo)).to.deep.equal(["POST", "PUT", "DELETE"]);
      expect(pendientes[1].ruta).to.equal("roles/xyz");
      expect(fetchFalso.called).to.be.false;
    } finally {
      Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
      delete globalThis.localStorage;
    }
  });

  it("encola si el servidor no responde, conserva el aviso con datos cacheados y lo quita al recuperarse", async () => {
    const almacen = {};
    globalThis.localStorage = {
      getItem: (clave) => Object.hasOwn(almacen, clave) ? almacen[clave] : null,
      setItem: (clave, valor) => { almacen[clave] = String(valor); },
      removeItem: (clave) => { delete almacen[clave]; },
    };
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    try {
      fetchFalso.rejects(new TypeError("Failed to fetch"));
      expect(await roles.crear({ nombre: "Nuevo" })).to.be.null;
      expect(listarPendientes()).to.have.length(1);
      expect(servicioNoResponde()).to.be.true;

      // El service worker respondió con lo guardado en el dispositivo.
      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0", "X-Sin-Conexion": "true" }));
      await roles.findRange();
      expect(servicioNoResponde()).to.be.true;

      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0" }));
      await roles.findRange();
      expect(servicioNoResponde()).to.be.false;
    } finally {
      Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
      delete globalThis.localStorage;
    }
  });

  it("comprobarServicio solo se da por buena una respuesta del servidor", async () => {
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    try {
      fetchFalso.rejects(new TypeError("Failed to fetch"));
      await roles.findRange().catch(() => {});
      expect(servicioNoResponde()).to.be.true;
      expect(await comprobarServicio()).to.be.false;

      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0", "X-Sin-Conexion": "true" }));
      expect(await comprobarServicio()).to.be.false;
      expect(servicioNoResponde()).to.be.true;

      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0" }));
      expect(await comprobarServicio()).to.be.true;
      expect(servicioNoResponde()).to.be.false;
    } finally {
      Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    }
  });
});
