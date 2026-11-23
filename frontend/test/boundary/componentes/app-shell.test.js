import sinon from "sinon";
import { expect } from "chai";
import { datosBase, stubApi, esperar } from "../../ayudas.js";
import { guardarSesion } from "../../../js/control/sesion.js";
import { listarPendientes, encolar } from "../../../js/control/pendientes.js";
import { listar } from "../../../js/control/api.js";
import { usarDom, montar, sinRed, conRed, dom, SESION_ENFERMERO } from "../entorno-dom.js";

describe("app-shell", () => {
  usarDom();

  describe("HU2 · Login", () => {
    it("app-shell actualiza el encabezado al entrar, salir y entrar con otro empleado", async () => {
      dom.window.location.hash = "#/login";
      const shell = montar("app-shell");
      const encabezado = () => shell.shadowRoot.querySelector("header").textContent.replace(/\s+/g, " ");
      await esperar(() => shell.shadowRoot.querySelector("app-login"));
      expect(shell.shadowRoot.querySelector("#acceso").getAttribute("aria-current")).to.equal("page");
      guardarSesion(SESION_ENFERMERO);
      expect(encabezado()).to.contain("Elena Martínez").and.contain("Mi panel");

      shell.shadowRoot.querySelector("#salir").click();
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
      expect(encabezado()).to.contain("Acceso empleados").and.not.contain("Elena Martínez");

      guardarSesion({ ...SESION_ENFERMERO, idPersona: 100, nombre: "Ana López" });
      expect(encabezado()).to.contain("Ana López").and.not.contain("Elena Martínez");

      // Un nombre largo se muestra corto, con el nombre completo como título.
      guardarSesion({ ...SESION_ENFERMERO, nombre: "Roberto Antonio Hernández Flores", nombreCorto: "Roberto Hernández" });
      const nombre = shell.shadowRoot.querySelector(".usuario .nombre");
      expect(nombre.textContent).to.equal("Roberto Hernández");
      expect(nombre.title).to.equal("Roberto Antonio Hernández Flores");
    });
  });

  it("app-shell extiende el encabezado a todo el ancho y deja centrado el contenido", async () => {
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("header .barra"));
    const barra = shell.shadowRoot.querySelector("header .barra");
    expect(barra.classList.contains("ancho")).to.be.true;
    expect(barra.classList.contains("contenedor")).to.be.false;
    expect(shell.shadowRoot.querySelector("main").classList.contains("contenedor")).to.be.true;
  });

  it("app-shell salta al contenido principal sin cambiar la ruta", async () => {
    dom.window.location.hash = "#/buscar";
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("app-busqueda"));
    const saltar = shell.shadowRoot.querySelector(".saltar");
    expect(saltar.tagName).to.equal("BUTTON");
    expect(saltar.textContent).to.equal("Ir al contenido principal");
    saltar.click();
    expect(dom.window.location.hash).to.equal("#/buscar");
    expect(shell.shadowRoot.activeElement?.id).to.equal("vista");
  });

  it("app-shell muestra el botón de instalar abajo a la derecha, fuera del encabezado", async () => {
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("pwa-instalar"));
    const instalar = shell.shadowRoot.querySelector("pwa-instalar");
    expect(shell.shadowRoot.querySelector("header pwa-instalar")).to.be.null;
    const hojas = [...instalar.shadowRoot.querySelectorAll('link[rel="stylesheet"]')].map((hoja) => hoja.href);
    expect(hojas.some((hoja) => hoja.endsWith("pwa-instalar.css"))).to.be.true;
  });

  it("app-shell muestra el aviso de trabajo sin conexión", async () => {
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion"));
    const aviso = shell.shadowRoot.querySelector("#estado-conexion");
    expect(aviso.hidden).to.be.true;
    Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
    dom.window.dispatchEvent(new dom.window.Event("offline"));
    await esperar(() => !shell.shadowRoot.querySelector("#estado-conexion").hidden);
    expect(aviso.textContent).to.contain("Sin conexión");
    expect(aviso.classList.contains("sin-red")).to.be.true;
    expect(aviso.getAttribute("role")).to.equal("status");
    expect(aviso.querySelector("#reintentar-servicio")).to.be.null;
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    dom.window.dispatchEvent(new dom.window.Event("online"));
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion").hidden);
  });

  it("app-shell avisa que el servidor no responde aunque haya red, y quita el aviso cuando vuelve", async () => {
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion"));
    fetch.rejects(new TypeError("Failed to fetch"));
    await listar("roles").catch(() => {});
    const aviso = shell.shadowRoot.querySelector("#estado-conexion");
    expect(aviso.hidden).to.be.false;
    expect(aviso.textContent).to.contain("El servidor de GalenoSV no responde");
    expect(aviso.classList.contains("servidor-caido")).to.be.true;
    expect(aviso.getAttribute("role")).to.equal("alert");
    expect(aviso.querySelector("strong").textContent).to.equal("Servicio no disponible");
    expect(aviso.textContent).to.contain("Está viendo datos guardados en este dispositivo");
    const reintentar = aviso.querySelector("#reintentar-servicio");
    expect(reintentar).to.not.be.null;
    sinon.restore();
    stubApi(datosBase());
    reintentar.click();
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion").hidden);
  });

  it("app-shell cuenta los cambios sin conexión en cuanto se guardan y vacía la cola al volver la red", async () => {
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion"));
    sinRed();
    try {
      dom.window.dispatchEvent(new dom.window.Event("offline"));
      const aviso = () => shell.shadowRoot.querySelector("#estado-conexion").textContent;
      expect(aviso()).to.not.contain("pendiente");
      encolar({ metodo: "DELETE", ruta: "ordenes-examen/400", cuerpo: null });
      expect(aviso()).to.contain("hay 1 cambio pendiente por enviar");
      encolar({ metodo: "DELETE", ruta: "ordenes-examen/401", cuerpo: null });
      expect(aviso()).to.contain("hay 2 cambios pendientes por enviar");

      conRed();
      dom.window.dispatchEvent(new dom.window.Event("online"));
      await esperar(() => listarPendientes().length === 0);
      expect(fetch.getCalls().some((llamada) => llamada.args[1]?.method === "DELETE")).to.be.true;
      await esperar(() => shell.shadowRoot.querySelector("#estado-conexion").hidden);
    } finally {
      conRed();
      localStorage.clear();
      delete globalThis.localStorage;
    }
  });

  it("app-shell enruta las vistas y protege las rutas de empleados", async () => {
    dom.window.location.hash = "#/buscar";
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("app-busqueda"));
    expect(document.title).to.equal("Buscar mis citas · GalenoSV");

    dom.window.location.hash = "#/dashboard";
    dom.window.dispatchEvent(new dom.window.Event("hashchange"));
    await esperar(() => dom.window.location.hash === "#/login");

    guardarSesion(SESION_ENFERMERO);
    dom.window.location.hash = "#/dashboard";
    dom.window.dispatchEvent(new dom.window.Event("hashchange"));
    await esperar(() => shell.shadowRoot.querySelector("app-dashboard"));
    expect(document.title).to.equal("Agenda de consultas · GalenoSV");

    dom.window.location.hash = "#/buscar";
    dom.window.dispatchEvent(new dom.window.Event("hashchange"));
    await esperar(() => shell.shadowRoot.querySelector("app-busqueda"));
    shell.shadowRoot.querySelector('[data-ruta="dashboard"]').click();
    await esperar(() => dom.window.location.hash === "#/dashboard");
  });

  it("app-shell tiene un interruptor de tema oscuro que se recuerda y no sigue al sistema", async () => {
    globalThis.localStorage = dom.window.localStorage;
    localStorage.clear();
    dom.window.matchMedia = () => ({ matches: true, addEventListener() {}, removeEventListener() {} });
    try {
      const shell = montar("app-shell");
      const boton = shell.shadowRoot.querySelector("header #tema");
      const tema = () => document.documentElement.dataset.tema;
      expect(boton.getAttribute("aria-label")).to.equal("Tema oscuro");
      expect(boton.getAttribute("aria-pressed")).to.equal("false");
      expect(tema(), "empieza en claro aunque el sistema prefiera oscuro").to.equal("claro");

      boton.click();
      expect(tema()).to.equal("oscuro");
      expect(boton.getAttribute("aria-pressed")).to.equal("true");
      expect(boton.getAttribute("aria-label"), "el nombre no cambia con el estado").to.equal("Tema oscuro");
      expect(localStorage.getItem("galenosv.tema")).to.equal("oscuro");

      // Al volver a abrir la aplicación se aplica el tema recordado.
      shell.remove();
      delete document.documentElement.dataset.tema;
      const otra = montar("app-shell");
      expect(tema()).to.equal("oscuro");
      expect(otra.shadowRoot.querySelector("#tema").getAttribute("aria-pressed")).to.equal("true");

      otra.shadowRoot.querySelector("#tema").click();
      expect(tema()).to.equal("claro");
      expect(localStorage.getItem("galenosv.tema")).to.equal("claro");
    } finally {
      delete dom.window.matchMedia;
      delete document.documentElement.dataset.tema;
    }
  });
});
