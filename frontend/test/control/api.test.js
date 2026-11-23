import { expect } from "chai";
import sinon from "sinon";
import { listarPendientes } from "../../js/control/pendientes.js";
import { API, comprobarServicio, listar, listarTodo, crear, actualizar, eliminar, servicioNoResponde } from "../../js/control/api.js";

const respuestaJson = (cuerpo, estado = 200, cabeceras = {}) =>
  new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { "Content-Type": "application/json", ...cabeceras },
  });

async function capturarError(promesa) {
  try {
    await promesa;
  } catch (error) {
    return error;
  }
  throw new Error("Se esperaba un error y la promesa se resolvió");
}

describe("api.js", () => {
  let fetchFalso;

  beforeEach(() => {
    fetchFalso = sinon.stub(globalThis, "fetch");
  });

  afterEach(() => {
    sinon.restore();
  });

  it("lista un recurso con paginación y lee X-Total-Count", async () => {
    fetchFalso.resolves(
      respuestaJson([{ idRol: "1", nombre: "Paciente" }], 200, { "X-Total-Count": "4" })
    );

    const resultado = await listar("roles", { first: 10, max: 5 });

    expect(fetchFalso.calledOnce).to.be.true;
    const [url, opciones] = fetchFalso.firstCall.args;
    expect(url).to.equal(`${API}/roles?first=10&max=5`);
    expect(opciones.headers.Accept).to.equal("application/json");
    expect(resultado.total).to.equal(4);
    expect(resultado.datos).to.deep.equal([{ idRol: "1", nombre: "Paciente" }]);
  });

  it("usa first=0 y max=20 por defecto", async () => {
    fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0" }));

    await listar("roles");

    expect(fetchFalso.firstCall.args[0]).to.equal(`${API}/roles?first=0&max=20`);
  });

  it("recorre todas las páginas con listarTodo", async () => {
    fetchFalso.onFirstCall().resolves(
      respuestaJson([{ idRol: "1" }, { idRol: "2" }], 200, { "X-Total-Count": "3" })
    );
    fetchFalso.onSecondCall().resolves(
      respuestaJson([{ idRol: "3" }], 200, { "X-Total-Count": "3" })
    );

    const datos = await listarTodo("roles");

    expect(datos.map((item) => item.idRol)).to.deep.equal(["1", "2", "3"]);
    expect(fetchFalso.firstCall.args[0]).to.equal(`${API}/roles?first=0&max=100`);
    expect(fetchFalso.secondCall.args[0]).to.equal(`${API}/roles?first=2&max=100`);
  });

  it("informa si la paginación queda incompleta antes del total declarado", async () => {
    fetchFalso.onFirstCall().resolves(respuestaJson([], 200, { "X-Total-Count": "50" }));
    const error = await capturarError(listarTodo("roles"));
    expect(error.message).to.contain("quedó incompleta");
    expect(fetchFalso.calledOnce).to.be.true;
  });

  it("crea un registro y devuelve la cabecera Location", async () => {
    fetchFalso.resolves(
      new Response(null, { status: 201, headers: { Location: `${API}/roles/xyz` } })
    );

    const ubicacion = await crear("roles", { nombre: "Nuevo" });

    const [url, opciones] = fetchFalso.firstCall.args;
    expect(url).to.equal(`${API}/roles`);
    expect(opciones.method).to.equal("POST");
    expect(JSON.parse(opciones.body)).to.deep.equal({ nombre: "Nuevo" });
    expect(ubicacion).to.equal(`${API}/roles/xyz`);
  });

  it("actualiza un registro con PUT", async () => {
    fetchFalso.resolves(new Response(null, { status: 204 }));

    await actualizar("roles", "xyz", { nombre: "Editado" });

    const [url, opciones] = fetchFalso.firstCall.args;
    expect(url).to.equal(`${API}/roles/xyz`);
    expect(opciones.method).to.equal("PUT");
    expect(JSON.parse(opciones.body)).to.deep.equal({ nombre: "Editado" });
  });

  it("elimina un registro con DELETE", async () => {
    fetchFalso.resolves(new Response(null, { status: 204 }));

    await eliminar("roles", "xyz");

    const [url, opciones] = fetchFalso.firstCall.args;
    expect(url).to.equal(`${API}/roles/xyz`);
    expect(opciones.method).to.equal("DELETE");
  });

  it("incluye el parámetro inválido en el mensaje de un 422", async () => {
    fetchFalso.resolves(
      respuestaJson({ mensaje: "Ya existe un registro con ese nombre" }, 422, {
        "X-Parametro-Error": "nombre",
      })
    );

    const error = await capturarError(listar("roles"));

    expect(error).to.be.instanceOf(Error);
    expect(error.status).to.equal(422);
    expect(error.message).to.contain("Ya existe");
    expect(error.message).to.contain("(nombre)");
  });

  it("construye un mensaje legible cuando la respuesta no es JSON", async () => {
    fetchFalso.resolves(new Response("sin json", { status: 400, statusText: "Bad Request" }));

    const error = await capturarError(eliminar("roles", "xyz"));

    expect(error.status).to.equal(400);
    expect(error.message).to.contain("400");
    expect(error.message).to.contain("Bad Request");
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
      await crear("roles", { nombre: "Sin red" });
      await actualizar("roles", "xyz", { nombre: "Sin red" });
      await eliminar("roles", "xyz");
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
      expect(await crear("roles", { nombre: "Nuevo" })).to.be.null;
      expect(listarPendientes()).to.have.length(1);
      expect(servicioNoResponde()).to.be.true;

      // El service worker respondió con lo guardado en el dispositivo.
      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0", "X-Sin-Conexion": "true" }));
      await listar("roles");
      expect(servicioNoResponde()).to.be.true;

      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0" }));
      await listar("roles");
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
      await listar("roles").catch(() => {});
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

  it("propaga el estado 409 de un conflicto de llave foránea", async () => {
    fetchFalso.resolves(respuestaJson({ mensaje: "Registro referenciado" }, 409));

    const error = await capturarError(eliminar("roles", "xyz"));

    expect(error.status).to.equal(409);
    expect(error.message).to.equal("Registro referenciado");
  });
});
