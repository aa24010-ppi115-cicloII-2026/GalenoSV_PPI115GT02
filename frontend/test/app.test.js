import { readFileSync } from "node:fs";
import { JSDOM } from "jsdom";
import { expect } from "chai";
import sinon from "sinon";

const html = readFileSync(new URL("../index.html", import.meta.url), "utf8");

const json = (cuerpo, cabeceras = {}) =>
  new Response(JSON.stringify(cuerpo), {
    status: 200,
    headers: { "Content-Type": "application/json", ...cabeceras },
  });

async function esperar(condicion, limite = 100) {
  for (let intento = 0; intento < limite; intento++) {
    if (condicion()) return;
    await new Promise((resolver) => setImmediate(resolver));
  }
  throw new Error("Se agotó el tiempo de espera de la condición");
}

describe("app.js", () => {
  let dom;
  let fetchFalso;
  let roles;

  beforeEach(async () => {
    roles = [
      { idRol: "1", nombre: "Paciente", activo: true, observaciones: "Atiende consulta" },
    ];
    dom = new JSDOM(html, { url: "http://localhost:3000/" });
    const prototipoDialogo = dom.window.HTMLDialogElement?.prototype ?? dom.window.HTMLElement.prototype;
    prototipoDialogo.showModal = function () {
      this.open = true;
    };
    prototipoDialogo.close = function () {
      this.open = false;
    };
    globalThis.window = dom.window;
    globalThis.document = dom.window.document;
    globalThis.HTMLElement = dom.window.HTMLElement;
    globalThis.HTMLDialogElement = dom.window.HTMLDialogElement;
    globalThis.confirm = () => true;

    fetchFalso = sinon.stub(globalThis, "fetch").callsFake(async (url, opciones = {}) => {
      const metodo = (opciones.method ?? "GET").toUpperCase();
      if (metodo === "GET") return json(roles, { "X-Total-Count": String(roles.length) });
      if (metodo === "POST") {
        const datos = JSON.parse(opciones.body);
        roles = [...roles, { idRol: "2", ...datos }];
        return new Response(null, { status: 201, headers: { Location: "http://localhost:9080/api/roles/2" } });
      }
      if (metodo === "PUT") {
        const id = url.split("/").pop();
        const datos = JSON.parse(opciones.body);
        roles = roles.map((rol) => (rol.idRol === id ? { ...rol, ...datos } : rol));
        return new Response(null, { status: 204 });
      }
      if (metodo === "DELETE") {
        const id = url.split("/").pop();
        roles = roles.filter((rol) => rol.idRol !== id);
        return new Response(null, { status: 204 });
      }
      return new Response(null, { status: 405 });
    });

    await import(`../js/app.js?prueba=${Date.now()}-${Math.random()}`);
    await esperar(() => document.querySelector("#navegacion button"));
  });

  afterEach(() => {
    sinon.restore();
    dom.window.close();
    delete globalThis.window;
    delete globalThis.document;
    delete globalThis.HTMLElement;
    delete globalThis.HTMLDialogElement;
    delete globalThis.confirm;
  });

  it("construye la navegación con los 20 recursos", async () => {
    const botones = document.querySelectorAll("#navegacion button");
    expect(botones).to.have.lengthOf(20);
    expect([...botones].some((boton) => boton.textContent === "Roles")).to.be.true;
  });

  it("muestra el recurso inicial con sus registros y el total", async () => {
    await esperar(() => document.querySelector("#panel tbody tr"));
    expect(document.querySelector("#panel h2").textContent).to.equal("Roles");
    expect(document.querySelector("#panel").textContent).to.contain("Paciente");
    expect(document.querySelector("#panel .contador").textContent).to.contain("1 registro");
  });

  it("crea un registro desde el formulario y refresca la tabla", async () => {
    await esperar(() => document.querySelector("#panel tbody tr"));
    document.querySelector('[data-accion="nuevo"]').click();
    const dialogo = document.querySelector("#dialogo");
    expect(dialogo.open).to.be.true;
    expect(document.querySelector("#titulo-formulario").textContent).to.equal("Nuevo Roles");

    const formulario = document.querySelector("#formulario");
    formulario.elements.nombre.value = "Odontólogo";
    formulario.dispatchEvent(new dom.window.Event("submit", { bubbles: true, cancelable: true }));

    await esperar(() => roles.some((rol) => rol.nombre === "Odontólogo"));
    await esperar(() => document.querySelector("#panel").textContent.includes("Odontólogo"));
    const peticion = fetchFalso.getCalls().find((llamada) => llamada.args[1]?.method === "POST");
    expect(peticion).to.exist;
    expect(JSON.parse(peticion.args[1].body).nombre).to.equal("Odontólogo");
    expect(dialogo.open).to.be.false;
  });

  it("edita un registro existente con PUT", async () => {
    await esperar(() => document.querySelector("#panel tbody tr"));
    document.querySelector('[data-accion="editar"]').click();
    const formulario = document.querySelector("#formulario");
    expect(formulario.elements.nombre.value).to.equal("Paciente");

    formulario.elements.nombre.value = "Paciente editado";
    formulario.dispatchEvent(new dom.window.Event("submit", { bubbles: true, cancelable: true }));

    await esperar(() => roles[0].nombre === "Paciente editado");
    await esperar(() => document.querySelector("#panel").textContent.includes("Paciente editado"));
    const peticion = fetchFalso.getCalls().find((llamada) => llamada.args[1]?.method === "PUT");
    expect(peticion.args[0]).to.match(/\/roles\/1$/);
  });

  it("elimina un registro con confirmación", async () => {
    await esperar(() => document.querySelector("#panel tbody tr"));
    document.querySelector('[data-accion="eliminar"]').click();

    await esperar(() => roles.length === 0);
    await esperar(() => document.querySelector("#panel").textContent.includes("No hay registros"));
    const peticion = fetchFalso.getCalls().find((llamada) => llamada.args[1]?.method === "DELETE");
    expect(peticion.args[0]).to.match(/\/roles\/1$/);
  });
});
