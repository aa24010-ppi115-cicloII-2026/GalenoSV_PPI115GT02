import { JSDOM } from "jsdom";
import { expect } from "chai";
import {
  leerSesion,
  guardarSesion,
  actualizarSesion,
  cerrarSesion,
  alCambiarSesion,
} from "../js/estado.js";

describe("estado.js", () => {
  let dom;

  beforeEach(() => {
    dom = new JSDOM("<!DOCTYPE html>", { url: "http://localhost:3000/" });
    globalThis.sessionStorage = dom.window.sessionStorage;
  });

  afterEach(() => {
    dom.window.close();
    delete globalThis.sessionStorage;
  });

  it("guarda, lee y actualiza la sesión en sessionStorage", () => {
    expect(leerSesion()).to.be.null;
    guardarSesion({ idPersona: 300, nombre: "Elena", idRol: 3 });
    expect(leerSesion()).to.deep.equal({ idPersona: 300, nombre: "Elena", idRol: 3 });
    actualizarSesion({ idRol: 2 });
    expect(leerSesion().idRol).to.equal(2);
    expect(leerSesion().nombre).to.equal("Elena");
  });

  it("no actualiza si no hay sesión y permite cerrarla", () => {
    actualizarSesion({ idRol: 2 });
    expect(leerSesion()).to.be.null;
    guardarSesion({ idPersona: 1 });
    cerrarSesion();
    expect(leerSesion()).to.be.null;
  });

  it("notifica a los suscriptores cuando cambia la sesión", () => {
    const cambios = [];
    const quitar = alCambiarSesion((evento) => cambios.push(evento.detail));
    guardarSesion({ idPersona: 1 });
    actualizarSesion({ nombre: "Prueba" });
    cerrarSesion();
    quitar();
    guardarSesion({ idPersona: 2 });
    expect(cambios).to.have.length(3);
    expect(cambios[0].idPersona).to.equal(1);
    expect(cambios[1].nombre).to.equal("Prueba");
    expect(cambios[2]).to.be.null;
  });

  it("tolera datos corruptos en sessionStorage", () => {
    sessionStorage.setItem("galenosv.sesion", "{no es json");
    expect(leerSesion()).to.be.null;
  });
});
