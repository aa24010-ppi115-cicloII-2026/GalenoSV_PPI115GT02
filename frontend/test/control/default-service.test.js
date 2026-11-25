import { expect } from "chai";
import sinon from "sinon";
import { API } from "../../js/control/api.js";
import DefaultService from "../../js/control/default-service.js";
import DefaultResponse from "../../js/entity/default-response.js";
import DefaultError from "../../js/entity/default-error.js";

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

describe("default-service.js", () => {
  const roles = new DefaultService("roles");
  let fetchFalso;

  beforeEach(() => {
    fetchFalso = sinon.stub(globalThis, "fetch");
  });

  afterEach(() => {
    sinon.restore();
  });

  describe("findRange", () => {
    it("se resuelve con un DefaultResponse con la página y el total de X-Total-Count", async () => {
      fetchFalso.resolves(respuestaJson([{ idRol: "1", nombre: "Paciente" }], 200, { "X-Total-Count": "4" }));

      const respuesta = await roles.findRange(10, 5);

      const [url, opciones] = fetchFalso.firstCall.args;
      expect(url).to.equal(`${API}/roles?first=10&max=5`);
      expect(opciones.headers.Accept).to.equal("application/json");
      expect(respuesta).to.be.instanceOf(DefaultResponse);
      expect(respuesta.total).to.equal(4);
      expect(respuesta.datos).to.deep.equal([{ idRol: "1", nombre: "Paciente" }]);
    });

    it("usa first=0 y max=20 por defecto", async () => {
      fetchFalso.resolves(respuestaJson([], 200, { "X-Total-Count": "0" }));

      await roles.findRange();

      expect(fetchFalso.firstCall.args[0]).to.equal(`${API}/roles?first=0&max=20`);
    });

    it("se rechaza con un DefaultError con el estado y el parámetro inválido de un 422", async () => {
      fetchFalso.resolves(
        respuestaJson({ mensaje: "Ya existe un registro con ese nombre" }, 422, { "X-Parametro-Error": "nombre" })
      );

      const error = await capturarError(roles.findRange());

      expect(error).to.be.instanceOf(DefaultError);
      expect(error.status).to.equal(422);
      // El campo queda en el error para quien lo necesite, pero no en el texto que se muestra.
      expect(error.parametro).to.equal("nombre");
      expect(error.message).to.equal("Ya existe un registro con ese nombre");
    });

    it("se rechaza sin estado y conserva el error original si el servidor no responde", async () => {
      const caida = new TypeError("Failed to fetch");
      fetchFalso.rejects(caida);

      const error = await capturarError(roles.findRange());

      expect(error).to.be.instanceOf(DefaultError);
      expect(error.status).to.be.null;
      expect(error.error).to.equal(caida);
      expect(error.message).to.contain("No se pudo contactar al servidor");
    });

    it("se rechaza si la lista no llega en JSON, con el error de lectura adjunto", async () => {
      fetchFalso.resolves(new Response("<html>", { status: 200, headers: { "X-Total-Count": "1" } }));

      const error = await capturarError(roles.findRange());

      expect(error).to.be.instanceOf(DefaultError);
      expect(error.message).to.equal("Error al procesar los datos de respuesta de roles.");
      expect(error.error).to.be.instanceOf(SyntaxError);
    });

    it("se rechaza si falta X-Total-Count o el cuerpo no es una lista", async () => {
      fetchFalso.resolves(respuestaJson([]));
      expect((await capturarError(roles.findRange())).message).to.contain("no incluye un X-Total-Count válido");

      fetchFalso.resolves(respuestaJson({ idRol: "1" }, 200, { "X-Total-Count": "1" }));
      expect((await capturarError(roles.findRange())).message).to.contain("no contiene una lista");
    });
  });

  describe("findAll", () => {
    it("recorre todas las páginas hasta llegar al total", async () => {
      fetchFalso.onFirstCall().resolves(respuestaJson([{ idRol: "1" }, { idRol: "2" }], 200, { "X-Total-Count": "3" }));
      fetchFalso.onSecondCall().resolves(respuestaJson([{ idRol: "3" }], 200, { "X-Total-Count": "3" }));

      const datos = await roles.findAll();

      expect(datos.map((item) => item.idRol)).to.deep.equal(["1", "2", "3"]);
      expect(fetchFalso.firstCall.args[0]).to.equal(`${API}/roles?first=0&max=100`);
      expect(fetchFalso.secondCall.args[0]).to.equal(`${API}/roles?first=2&max=100`);
    });

    it("informa si la paginación queda incompleta antes del total declarado", async () => {
      fetchFalso.onFirstCall().resolves(respuestaJson([], 200, { "X-Total-Count": "50" }));

      const error = await capturarError(roles.findAll());

      expect(error).to.be.instanceOf(DefaultError);
      expect(error.message).to.contain("quedó incompleta");
      expect(fetchFalso.calledOnce).to.be.true;
    });
  });

  describe("crear, modificar y eliminar", () => {
    it("crea un registro con POST y devuelve la cabecera Location", async () => {
      fetchFalso.resolves(new Response(null, { status: 201, headers: { Location: `${API}/roles/xyz` } }));

      const ubicacion = await roles.crear({ nombre: "Nuevo" });

      const [url, opciones] = fetchFalso.firstCall.args;
      expect(url).to.equal(`${API}/roles`);
      expect(opciones.method).to.equal("POST");
      expect(opciones.headers["Content-Type"]).to.equal("application/json");
      expect(JSON.parse(opciones.body)).to.deep.equal({ nombre: "Nuevo" });
      expect(ubicacion).to.equal(`${API}/roles/xyz`);
    });

    it("modifica un registro con PUT", async () => {
      fetchFalso.resolves(new Response(null, { status: 204 }));

      await roles.modificar("xyz", { nombre: "Editado" });

      const [url, opciones] = fetchFalso.firstCall.args;
      expect(url).to.equal(`${API}/roles/xyz`);
      expect(opciones.method).to.equal("PUT");
      expect(JSON.parse(opciones.body)).to.deep.equal({ nombre: "Editado" });
    });

    it("elimina un registro con DELETE y sin cuerpo", async () => {
      fetchFalso.resolves(new Response(null, { status: 204 }));

      await roles.eliminar("xyz");

      const [url, opciones] = fetchFalso.firstCall.args;
      expect(url).to.equal(`${API}/roles/xyz`);
      expect(opciones.method).to.equal("DELETE");
      expect(opciones.body).to.be.undefined;
    });

    it("propaga el estado 409 de un conflicto de llave foránea", async () => {
      fetchFalso.resolves(respuestaJson({ mensaje: "Registro referenciado" }, 409));

      const error = await capturarError(roles.eliminar("xyz"));

      expect(error).to.be.instanceOf(DefaultError);
      expect(error.status).to.equal(409);
      expect(error.message).to.equal("Registro referenciado");
    });

    it("explica la causa sin el código HTTP cuando el error no viene en JSON", async () => {
      fetchFalso.resolves(new Response("sin json", { status: 400, statusText: "Bad Request" }));

      const error = await capturarError(roles.eliminar("xyz"));

      expect(error.status).to.equal(400);
      expect(error.message).to.equal("Revise los datos ingresados.");
    });

    it("explica cada tipo de falla sin el código cuando la API no manda su mensaje", async () => {
      const casos = [
        [404, "No se encontró el registro solicitado."],
        [409, "La operación no se puede completar porque hay registros relacionados."],
        [422, "Revise los datos ingresados."],
        [500, "El servidor no pudo completar la operación. Intente de nuevo en unos minutos."],
        [503, "El servidor no pudo completar la operación. Intente de nuevo en unos minutos."],
      ];
      for (const [estado, texto] of casos) {
        fetchFalso.resolves(respuestaJson({}, estado));
        const error = await capturarError(roles.eliminar("xyz"));
        expect(error.status).to.equal(estado);
        expect(error.message, `estado ${estado}`).to.equal(texto);
        expect(error.message).not.to.contain(String(estado));
      }
    });

    it("no muestra el texto técnico que otros componentes ponen en message", async () => {
      fetchFalso.resolves(respuestaJson({ message: "RESTEASY003210: Could not find resource" }, 404));

      const error = await capturarError(roles.eliminar("xyz"));

      expect(error.message).to.equal("No se encontró el registro solicitado.");
    });
  });
});
