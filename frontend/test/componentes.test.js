import { JSDOM } from "jsdom";
import { expect } from "chai";
import sinon from "sinon";
import { datosBase, indizar, stubApi, esperar } from "./ayudas.js";
import { limpiarCache, consultaCompleta, resumenCita } from "../js/datos.js";
import { guardarSesion } from "../js/estado.js";
import { construirPdf } from "../js/pdf.js";

let documentoImprimible;
let lineasTicket;

const SESION_ENFERMERO = {
  idPersona: 300,
  nombre: "Elena Martínez",
  documento: { tipo: "DUI", valor: "33333333-3" },
  roles: [
    { idPersonaRol: 3000, idRol: 3, nombreRol: "Enfermero", idClinica: 10, nombreClinica: "Clínica Central" },
    { idPersonaRol: 3001, idRol: 2, nombreRol: "Odontólogo", idClinica: 10, nombreClinica: "Clínica Central" },
  ],
  clinicas: [{ idClinica: 10, nombre: "Clínica Central" }],
  idClinica: 10,
  idRol: 3,
};

describe("componentes Web Components", () => {
  let dom;
  let datos;

  before(async () => {
    dom = new JSDOM("<!DOCTYPE html><body></body>", {
      url: "http://localhost:3000/",
      pretendToBeVisual: true,
    });
    globalThis.window = dom.window;
    globalThis.document = dom.window.document;
    globalThis.HTMLElement = dom.window.HTMLElement;
    globalThis.CustomEvent = dom.window.CustomEvent;
    globalThis.Event = dom.window.Event;
    globalThis.customElements = dom.window.customElements;
    globalThis.sessionStorage = dom.window.sessionStorage;
    dom.window.confirm = () => true;
    globalThis.confirm = dom.window.confirm;
    await import("../js/componentes/ui-toast.js");
    ({ documentoImprimible, lineasTicket } = await import("../js/componentes/ticket-cita.js"));
    await import("../js/componentes/cita-card.js");
    await import("../js/componentes/app-login.js");
    await import("../js/componentes/app-busqueda.js");
    await import("../js/componentes/app-dashboard.js");
    await import("../js/componentes/app-consulta.js");
    await import("../js/componentes/app-shell.js");
    await import("../js/componentes/pwa-instalar.js");
  });

  after(() => {
    dom.window.close();
    for (const clave of [
      "window",
      "document",
      "HTMLElement",
      "CustomEvent",
      "Event",
      "customElements",
      "sessionStorage",
      "confirm",
    ]) {
      delete globalThis[clave];
    }
  });

  beforeEach(() => {
    document.body.innerHTML = "";
    sessionStorage.clear();
    limpiarCache();
    datos = datosBase();
    stubApi(datos);
  });

  afterEach(() => {
    sinon.restore();
  });

  function montar(etiqueta, propiedades = {}) {
    const elemento = document.createElement(etiqueta);
    for (const [clave, valor] of Object.entries(propiedades)) elemento[clave] = valor;
    document.body.append(elemento);
    return elemento;
  }

  it("app-login valida el documento y crea la sesión sin autorregistro", async () => {
    const login = montar("app-login");
    const navegaciones = [];
    login.addEventListener("navegar", (evento) => navegaciones.push(evento.detail));
    await esperar(() => login.shadowRoot.querySelectorAll("#tipo option").length > 1);
    login.shadowRoot.querySelector("#tipo").value = "1";
    login.shadowRoot.querySelector("#numero").value = " 33333333-3 ";
    login.shadowRoot.querySelector("#formulario").dispatchEvent(
      new dom.window.Event("submit", { bubbles: true, cancelable: true })
    );
    await esperar(() => sessionStorage.getItem("galenosv.sesion") && navegaciones.length > 0);
    const sesion = JSON.parse(sessionStorage.getItem("galenosv.sesion"));
    expect(sesion.idPersona).to.equal(300);
    expect(sesion.nombre).to.equal("Elena Martínez");
    expect(sesion.roles[0].nombreRol).to.equal("Enfermero");
    expect(navegaciones).to.include("dashboard");
  });

  it("app-login rechaza documentos desconocidos y muestra el error", async () => {
    const login = montar("app-login");
    await esperar(() => login.shadowRoot.querySelectorAll("#tipo option").length > 1);
    login.shadowRoot.querySelector("#tipo").value = "1";
    login.shadowRoot.querySelector("#numero").value = "99999999-9";
    login.shadowRoot.querySelector("#formulario").dispatchEvent(
      new dom.window.Event("submit", { bubbles: true, cancelable: true })
    );
    await esperar(() => !login.shadowRoot.querySelector("#error").hidden);
    expect(login.shadowRoot.querySelector("#error").textContent).to.contain("No existe");
    expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
  });

  it("app-busqueda filtra citas del paciente y abre el ticket imprimible", async () => {
    const busqueda = montar("app-busqueda");
    await esperar(() => busqueda.shadowRoot.querySelectorAll("#tipo option").length > 1);
    busqueda.shadowRoot.querySelector("#tipo").value = "1";
    busqueda.shadowRoot.querySelector("#numero").value = "11111111-1";
    busqueda.shadowRoot.querySelector("#desde").value = "2026-01-01";
    busqueda.shadowRoot.querySelector("#hasta").value = "2026-12-31";
    busqueda.shadowRoot.querySelector("#formulario").dispatchEvent(
      new dom.window.Event("submit", { bubbles: true, cancelable: true })
    );
    await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 2);
    expect(busqueda.shadowRoot.textContent).to.contain("Ana López");
    const tarjeta = busqueda.shadowRoot.querySelector("cita-card");
    tarjeta.shadowRoot.querySelector('button[data-accion="ticket"]').click();
    await esperar(() => busqueda.shadowRoot.querySelector("ticket-cita"));
    const ticket = busqueda.shadowRoot.querySelector("ticket-cita");
    expect(ticket.shadowRoot.textContent).to.contain("Comprobante de cita");
  });

  it("app-busqueda lista solo las clínicas donde el paciente está registrado", async () => {
    datos.clinicas.push({ idClinica: 20, nombre: "Clínica Norte" });
    const busqueda = montar("app-busqueda");
    await esperar(() => busqueda.shadowRoot.querySelectorAll("#tipo option").length > 1);
    expect(busqueda.shadowRoot.querySelector("#clinica").disabled).to.be.true;

    busqueda.shadowRoot.querySelector("#tipo").value = "1";
    busqueda.shadowRoot.querySelector("#numero").value = "11111111-1";
    busqueda.shadowRoot.querySelector("#numero").dispatchEvent(
      new dom.window.Event("input", { bubbles: true })
    );
    await esperar(() => !busqueda.shadowRoot.querySelector("#clinica").disabled);
    const opciones = [...busqueda.shadowRoot.querySelectorAll("#clinica option")].map((o) => o.textContent);
    expect(opciones).to.include("Clínica Central");
    expect(opciones).to.not.include("Clínica Norte");
  });

  it("documentoImprimible incluye datos, pasos y notas de atención", () => {
    const cita = consultaCompleta(indizar(datosBase()), 5000);
    const html = documentoImprimible(cita);
    expect(html).to.contain("Ana López");
    expect(html).to.contain("#5000");
    expect(html).to.contain("Evaluación");
    expect(html).to.contain("Caries de la dentina");
    expect(html).to.contain("Guardar como PDF");
  });

  it("app-dashboard muestra la agenda del día y filtra por rol", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const panel = montar("app-dashboard");
    await esperar(() => panel.shadowRoot.querySelectorAll("#resumen .indicador").length === 4);
    panel.shadowRoot.querySelector("#fecha").value = "2026-11-12";
    panel.shadowRoot.querySelector("#fecha").dispatchEvent(new dom.window.Event("change", { bubbles: true }));
    await esperar(() => panel.shadowRoot.querySelectorAll("#lista cita-card").length === 2);
    panel.shadowRoot.querySelector("#rol").value = "2";
    panel.shadowRoot.querySelector("#rol").dispatchEvent(new dom.window.Event("change", { bubbles: true }));
    await esperar(() => panel.shadowRoot.querySelectorAll("#lista cita-card").length === 1);
    expect(panel.shadowRoot.querySelector("#lista cita-card").shadowRoot.textContent).to.contain("Limpieza dental");
  });

  it("ui-toast muestra las notificaciones recibidas del documento", async () => {
    const toast = montar("ui-toast");
    document.dispatchEvent(
      new dom.window.CustomEvent("notificar", { detail: { mensaje: "Guardado correcto", tipo: "ok" } })
    );
    expect(toast.shadowRoot.querySelector(".aviso").textContent).to.equal("Guardado correcto");
  });

  it("pwa-instalar muestra el botón cuando el navegador ofrece instalar", async () => {
    const instalar = montar("pwa-instalar");
    await esperar(() => instalar.shadowRoot.querySelector("button"));
    expect(instalar.shadowRoot.querySelector("button").hidden).to.be.true;
    const prompt = sinon.spy();
    const evento = new dom.window.Event("beforeinstallprompt");
    evento.prompt = prompt;
    evento.userChoice = Promise.resolve({ outcome: "accepted" });
    dom.window.dispatchEvent(evento);
    await esperar(() => !instalar.shadowRoot.querySelector("button").hidden);
    instalar.shadowRoot.querySelector("button").click();
    await esperar(() => prompt.called);
    expect(instalar.shadowRoot.querySelector("button").hidden).to.be.true;
  });

  it("app-shell muestra el aviso de trabajo sin conexión", async () => {
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion"));
    const aviso = shell.shadowRoot.querySelector("#estado-conexion");
    expect(aviso.hidden).to.be.true;
    Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
    dom.window.dispatchEvent(new dom.window.Event("offline"));
    await esperar(() => !shell.shadowRoot.querySelector("#estado-conexion").hidden);
    expect(shell.shadowRoot.querySelector("#estado-conexion").textContent).to.contain("Sin conexión");
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    dom.window.dispatchEvent(new dom.window.Event("online"));
    await esperar(() => shell.shadowRoot.querySelector("#estado-conexion").hidden);
  });

  it("cita-card emite ver-ticket y navega al registro de atención", () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
    const tarjeta = montar("cita-card", { cita });
    const tickets = [];
    const navegaciones = [];
    tarjeta.addEventListener("ver-ticket", (evento) => tickets.push(evento.detail));
    tarjeta.addEventListener("navegar", (evento) => navegaciones.push(evento.detail));
    tarjeta.shadowRoot.querySelector('[data-accion="ticket"]').click();
    tarjeta.shadowRoot.querySelector('[data-accion="atender"]').click();
    expect(tickets[0].consulta.idConsulta).to.equal(5000);
    expect(navegaciones[0]).to.equal("consulta/5000");
  });

  it("app-shell enruta las vistas y protege las rutas de empleados", async () => {
    dom.window.location.hash = "#/buscar";
    const shell = montar("app-shell");
    await esperar(() => shell.shadowRoot.querySelector("app-busqueda"));

    dom.window.location.hash = "#/dashboard";
    dom.window.dispatchEvent(new dom.window.Event("hashchange"));
    await esperar(() => dom.window.location.hash === "#/login");

    guardarSesion(SESION_ENFERMERO);
    dom.window.location.hash = "#/dashboard";
    dom.window.dispatchEvent(new dom.window.Event("hashchange"));
    await esperar(() => shell.shadowRoot.querySelector("app-dashboard"));

    dom.window.location.hash = "#/buscar";
    dom.window.dispatchEvent(new dom.window.Event("hashchange"));
    await esperar(() => shell.shadowRoot.querySelector("app-busqueda"));
    shell.shadowRoot.querySelector('[data-ruta="dashboard"]').click();
    await esperar(() => dom.window.location.hash === "#/dashboard");
  });

  it("app-consulta filtra los exámenes por categoría", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    datos.examenes.push({ idExamen: 21, nombre: "Hemograma", activo: true });
    datos.examenesTipoExamen.push({ idExamenTipoExamen: 2, idExamen: 21, idTipoExamen: 31 });
    datos.tiposExamen.push({ idTipoExamen: 31, nombre: "Laboratorio" });
    datos.procedimientosPasoExamen.push({
      idProcedimientoPasoExamen: 2,
      idProcedimientoPaso: 7001,
      idExamen: 21,
      activo: true,
    });
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", "5000");
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='iniciar']"));
    vista.shadowRoot.querySelector("[data-accion='iniciar']").click();
    await esperar(() => vista.shadowRoot.querySelector("[data-categoria-filtro]"));

    const etiquetas = () => [...vista.shadowRoot.querySelectorAll("label.examen")];
    expect(etiquetas()).to.have.length(2);
    const filtro = vista.shadowRoot.querySelector("[data-categoria-filtro]");
    filtro.value = "Laboratorio";
    filtro.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
    const visibles = etiquetas().filter((etiqueta) => !etiqueta.hidden);
    expect(visibles).to.have.length(1);
    expect(visibles[0].textContent).to.contain("Hemograma");

    filtro.value = "Imagenología";
    filtro.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
    const visibles2 = etiquetas().filter((etiqueta) => !etiqueta.hidden);
    expect(visibles2).to.have.length(1);
    expect(visibles2[0].textContent).to.contain("Radiografía");
  });

  it("app-consulta finaliza la consulta registrando la fecha de cierre", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", "5000");
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='finalizar']"));
    vista.shadowRoot.querySelector("[data-accion='finalizar']").click();
    await esperar(() =>
      fetch.getCalls().some(
        (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("/consultas/")
      )
    );
    const llamada = fetch
      .getCalls()
      .find((item) => item.args[1]?.method === "PUT" && item.args[0].includes("/consultas/"));
    expect(JSON.parse(llamada.args[1].body).fechaFin).to.be.a("string");
  });

  it("ticket-cita ofrece descargar el PDF y genera un archivo válido", () => {
    const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
    const ticket = montar("ticket-cita", { cita });
    expect(ticket.shadowRoot.querySelector("[data-accion='descargar']")).to.not.be.null;
    const lineas = lineasTicket(cita);
    expect(lineas.join("\n")).to.contain("Paciente: Ana López");
    expect(lineas.join("\n")).to.contain("PROCEDIMIENTOS Y PASOS");
    expect(lineas.join("\n")).to.contain("Caries de la dentina");
    const bytes = construirPdf("GalenoSV - Comprobante de cita", lineas);
    expect(Buffer.from(bytes).toString("latin1")).to.contain("(Paciente: Ana López) Tj");
  });

  it("ticket-cita descarga el PDF desde el backend", async () => {
    const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
    const ticket = montar("ticket-cita", { cita });
    const descargas = [];
    const crear = globalThis.document.createElement.bind(globalThis.document);
    sinon.stub(globalThis.document, "createElement").callsFake((etiqueta) => {
      const elemento = crear(etiqueta);
      if (etiqueta === "a") {
        elemento.click = () => descargas.push(elemento.download);
      }
      return elemento;
    });
    globalThis.URL.createObjectURL = () => "blob:prueba";
    globalThis.URL.revokeObjectURL = () => {};
    ticket.shadowRoot.querySelector("[data-accion='descargar']").click();
    await esperar(() => descargas.length > 0);
    expect(descargas[0]).to.equal("ticket-5000.pdf");
    const peticion = fetch.getCalls().find((llamada) => llamada.args[0].includes("/ticket"));
    expect(peticion).to.not.be.undefined;
  });

  it("cita-card muestra el registro de cada paso con quién y cuándo", () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
    const tarjeta = montar("cita-card", { cita });
    const texto = tarjeta.shadowRoot.textContent;
    expect(texto).to.contain("Ana López");
    expect(texto).to.contain("Evaluación");
    expect(texto).to.contain("Elena Martínez");
    expect(texto).to.contain("Ver qué se registró en cada paso");
  });

  it("app-consulta muestra qué se registró en cada paso", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", "5000");
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector(".registro-pasos"));
    const texto = vista.shadowRoot.querySelector(".registro-pasos").textContent;
    expect(texto).to.contain("Evaluación");
    expect(texto).to.contain("Elena Martínez");
  });

  it("app-consulta inicia, asigna exámenes y completa un paso", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", "5000");
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='iniciar']"));

    vista.shadowRoot.querySelector("[data-accion='iniciar']").click();
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));
    const creacion = fetch.getCalls().find(
      (llamada) => (llamada.args[1]?.method ?? "GET") === "POST" && llamada.args[0].includes("consultas-procedimiento-paso")
    );
    expect(creacion).to.not.be.undefined;
    expect(JSON.parse(creacion.args[1].body).estado).to.equal("EN_PROCESO");

    const casilla = vista.shadowRoot.querySelector('input[type="checkbox"]');
    casilla.click();
    vista.shadowRoot.querySelector("[data-accion='asignar']").click();
    await esperar(() =>
      fetch.getCalls().some(
        (llamada) => (llamada.args[1]?.method ?? "GET") === "POST" && llamada.args[0].includes("ordenes-examen")
      )
    );
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));

    vista.shadowRoot.querySelector("[data-campo='diagnostico']").value = "K02.1 · Caries de la dentina";
    vista.shadowRoot.querySelector("[data-accion='completar']").click();
    await esperar(() =>
      fetch.getCalls().some(
        (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
      )
    );
    await esperar(() => vista.shadowRoot.textContent.includes("Paso actual: Alta"));
    expect(vista.shadowRoot.textContent).to.contain("2/3 pasos");
  });

  it("app-consulta retrocede al paso anterior sin borrar su registro", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", "5000");
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='retroceder']"));
    vista.shadowRoot.querySelector("[data-accion='retroceder']").click();
    await esperar(() =>
      fetch.getCalls().some(
        (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
      )
    );
    const llamada = fetch
      .getCalls()
      .find(
        (item) => item.args[1]?.method === "PUT" && item.args[0].includes("consultas-procedimiento-paso")
      );
    expect(JSON.parse(llamada.args[1].body).estado).to.equal("PENDIENTE");
    expect(
      fetch.getCalls().some((item) => item.args[1]?.method === "DELETE")
    ).to.be.false;
    await esperar(() => vista.shadowRoot.textContent.includes("Paso actual: Evaluación"));
  });

  it("app-consulta guarda notas y rechaza un diagnóstico CIE-10 inválido", async () => {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", "5000");
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='guardar-notas']"));

    vista.shadowRoot.querySelector("[data-campo='notas']").value = "Evolución favorable";
    vista.shadowRoot.querySelector("[data-campo='plan']").value = "Control en 7 días";
    vista.shadowRoot.querySelector("[data-accion='guardar-notas']").click();
    await esperar(() =>
      fetch.getCalls().some(
        (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento/")
      )
    );
    const guardado = fetch.getCalls().find(
      (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento/")
    );
    const observaciones = JSON.parse(JSON.parse(guardado.args[1].body).observaciones);
    expect(observaciones.notas).to.equal("Evolución favorable");
    expect(observaciones.plan).to.equal("Control en 7 días");

    vista.shadowRoot.querySelector("[data-accion='iniciar']").click();
    await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));
    vista.shadowRoot.querySelector("[data-campo='diagnostico']").value = "ZZZ.9";
    const notificaciones = [];
    vista.addEventListener("notificar", (evento) => notificaciones.push(evento.detail.mensaje));
    vista.shadowRoot.querySelector("[data-accion='completar']").click();
    await esperar(() => notificaciones.some((mensaje) => mensaje.includes("CIE-10")));
    expect(
      fetch.getCalls().some(
        (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
      )
    ).to.be.false;
  });
});

