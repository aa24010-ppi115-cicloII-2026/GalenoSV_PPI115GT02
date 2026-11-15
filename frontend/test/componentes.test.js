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

  describe("HU1 · Búsqueda de citas", () => {
    describe("asistente de búsqueda", () => {
      // El asistente vive dentro de app-busqueda; estas ayudas lo recorren como lo haría el paciente.
      async function abrirAsistente() {
        const busqueda = montar("app-busqueda");
        await esperar(() => busqueda.shadowRoot.querySelector("asistente-citas")?.shadowRoot?.querySelectorAll("#tipo option").length > 1);
        return { busqueda, raiz: busqueda.shadowRoot.querySelector("asistente-citas").shadowRoot };
      }
      const pasoVisible = (raiz) => raiz.querySelector("section[data-paso]:not([hidden])").dataset.paso;
      const error = (raiz) => (raiz.querySelector("#error").hidden ? "" : raiz.querySelector("#error").textContent);
      const clic = (raiz, selector) => raiz.querySelector(selector).click();
      async function hastaPaso(raiz, paso) {
        await esperar(() => pasoVisible(raiz) === paso);
      }
      async function completarDocumento(raiz, numero) {
        raiz.querySelector("#tipo").value = "1";
        clic(raiz, "#siguiente");
        await hastaPaso(raiz, "2");
        raiz.querySelector("#numero").value = numero;
        clic(raiz, "#siguiente");
      }

      it("empieza en el paso 1 y no avanza sin elegir el tipo de documento", async () => {
        const { raiz } = await abrirAsistente();
        expect(pasoVisible(raiz)).to.equal("1");
        expect(raiz.querySelector('ol.pasos li[aria-current="step"]').textContent).to.contain("Tipo de documento");
        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("1");
        expect(error(raiz)).to.contain("tipo de documento");
      });

      it("valida el número con el formato del tipo de documento y que esté registrado", async () => {
        datos.tiposDocumento[0].expresionRegular = "^[0-9]{8}-[0-9]$";
        datos.tiposDocumento[0].indicaciones = "8 dígitos, guion y dígito verificador";
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "123");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.contain("8 dígitos, guion y dígito verificador");

        raiz.querySelector("#numero").value = "99999999-9";
        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.contain("no está registrado");
      });

      it("lista solo las clínicas donde es paciente y permite volver al paso anterior", async () => {
        datos.clinicas.push({ idClinica: 20, nombre: "Clínica Norte" }, { idClinica: 30, nombre: "Clínica Oeste" });
        datos.personasRol.push(
          { idPersonaRol: 1001, idPersona: 100, idRol: 2, idClinica: 20 },
          { idPersonaRol: 1002, idPersona: 100, idRol: 1, idClinica: 30 }
        );
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        const clinicas = [...raiz.querySelectorAll('input[name="clinica"]')].map((radio) => radio.closest("label").textContent.trim());
        expect(clinicas).to.deep.equal(["Clínica Central", "Clínica Oeste"]);

        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("3");
        expect(error(raiz)).to.contain("clínica");

        clic(raiz, "#atras");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(raiz.querySelector("#numero").value).to.equal("11111111-1");
      });

      it("busca por clínica y rango de fechas, sin ocultar citas futuras por defecto", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        expect(raiz.querySelector('input[name="clinica"]').checked).to.be.true;
        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("4");
        expect(raiz.querySelector("#desde").value).to.equal("");
        expect(raiz.querySelector("#hasta").value).to.equal("");

        raiz.querySelector("#desde").value = "2026-12-01";
        raiz.querySelector("#hasta").value = "2026-11-01";
        clic(raiz, "#buscar");
        expect(error(raiz)).to.contain("fecha inicial");
        expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);

        raiz.querySelector("#desde").value = "";
        raiz.querySelector("#hasta").value = "";
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 2);

        raiz.querySelector("#desde").value = "2026-11-01";
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 1);
        const tarjeta = busqueda.shadowRoot.querySelector("cita-card").shadowRoot;
        expect(tarjeta.textContent).to.contain("Clínica Central");
        expect(tarjeta.textContent).to.contain("Limpieza dental");
        expect(tarjeta.querySelector(".fecha").getAttribute("aria-label")).to.contain("12 nov 2026");
      });

      it("informa cuando no hay citas en la clínica y las fechas elegidas", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        raiz.querySelector("#desde").value = "2027-01-01";
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelector(".resumen"));
        expect(busqueda.shadowRoot.querySelector(".resumen").textContent).to.contain("0 citas");
        expect(busqueda.shadowRoot.querySelector("#lista-citas").textContent).to.contain("No hay citas para la clínica y las fechas elegidas");
        expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);
      });

      it("avanza con Enter y pide el número cuando se deja vacío", async () => {
        const { raiz } = await abrirAsistente();
        const enter = (selector) =>
          raiz.querySelector(selector).dispatchEvent(new dom.window.KeyboardEvent("keydown", { key: "Enter", bubbles: true }));
        raiz.querySelector("#tipo").value = "1";
        enter("#tipo");
        expect(pasoVisible(raiz)).to.equal("2");

        enter("#numero");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.equal("Escriba el número de documento.");

        raiz.querySelector("#numero").value = "11111111-1";
        enter("#numero");
        expect(pasoVisible(raiz)).to.equal("3");
      });

      it("avisa al paciente cuando no se pueden cargar los datos del servicio", async () => {
        fetch.rejects(new TypeError("Failed to fetch"));
        const busqueda = montar("app-busqueda");
        const raiz = busqueda.shadowRoot.querySelector("asistente-citas").shadowRoot;
        await esperar(() => !raiz.querySelector("#error").hidden);
        expect(error(raiz)).to.contain("No se pudo conectar con el servicio");
        expect(raiz.querySelector("#tipo").textContent).to.equal("No disponible");
      });

      it("abre el ticket de una cita encontrada y permite iniciar una nueva búsqueda", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 2);
        busqueda.shadowRoot.querySelector("cita-card").shadowRoot.querySelector('button[data-accion="ticket"]').click();
        await esperar(() => busqueda.shadowRoot.querySelector("ticket-cita"));
        expect(busqueda.shadowRoot.querySelector("ticket-cita").shadowRoot.textContent).to.contain("Comprobante de cita");

        busqueda.shadowRoot.querySelector("#nueva").click();
        expect(pasoVisible(raiz)).to.equal("1");
        expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);
      });
    });

    describe("cards de citas", () => {
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

      it("cita-card marca el paso actual como «En curso» y muestra la fecha de nacimiento correcta", () => {
        const base = datosBase();
        base.personas[0].fechaNacimiento = "1992-09-30T00:00:00Z";
        const indice = indizar(base);
        const tarjeta = montar("cita-card", { cita: resumenCita(indice, indice.porId.consultas.get(5000)) });
        const actual = tarjeta.shadowRoot.querySelector(".paso.actual .quien").textContent;
        expect(actual).to.contain("En curso");
        expect(tarjeta.shadowRoot.textContent).to.contain("30 sept 1992");
      });

      it("en la búsqueda del paciente titula con el motivo y no repite datos de la cita", () => {
        const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
        const tarjeta = document.createElement("cita-card");
        tarjeta.setAttribute("vista", "paciente");
        tarjeta.cita = cita;
        document.body.append(tarjeta);
        const raiz = tarjeta.shadowRoot;
        expect(raiz.querySelector("h3").textContent).to.equal("Limpieza");
        expect(raiz.querySelector(".paciente p").textContent).to.equal("Clínica Central");
        expect(raiz.querySelector(".general").textContent).to.not.match(/Inicio:|Clínica:/);

        const enDashboard = montar("cita-card", { cita }).shadowRoot;
        expect(enDashboard.querySelector("h3").textContent).to.equal("Ana López");
      });

      it("fechaCalendario muestra una fecha sin hora guardada en UTC con el día correcto", async () => {
        const { fechaCalendario } = await import("../js/base.js");
        expect(fechaCalendario("1992-09-30T00:00:00Z")).to.match(/^30 /);
        expect(fechaCalendario("1992-09-30")).to.match(/^30 /);
        expect(fechaCalendario(null)).to.equal("—");
        expect(fechaCalendario("no es fecha")).to.equal("—");
      });
    });

    describe("ticket PDF", () => {
      const citaDePrueba = () => resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));

      // jsdom no descarga archivos: se interceptan los enlaces <a download> y los avisos del componente.
      function capturarDescargas(ticket) {
        const descargas = [];
        const avisos = [];
        const crear = globalThis.document.createElement.bind(globalThis.document);
        sinon.stub(globalThis.document, "createElement").callsFake((etiqueta) => {
          const elemento = crear(etiqueta);
          if (etiqueta === "a") elemento.click = () => descargas.push(elemento.download);
          return elemento;
        });
        globalThis.URL.createObjectURL = () => "blob:prueba";
        globalThis.URL.revokeObjectURL = () => {};
        ticket.addEventListener("notificar", (evento) => avisos.push(evento.detail.mensaje));
        return { descargas, avisos };
      }
      const peticionesTicket = () => fetch.getCalls().filter((llamada) => llamada.args[0].includes("/tickets/consultas/"));

      it("documentoImprimible incluye datos, pasos y notas de atención", () => {
        const cita = consultaCompleta(indizar(datosBase()), 5000);
        const html = documentoImprimible(cita);
        expect(html).to.contain("Ana López");
        expect(html).to.contain("#5000");
        expect(html).to.contain("Evaluación");
        expect(html).to.contain("Caries de la dentina");
        expect(html).to.contain("Guardar como PDF");
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
        const ticket = montar("ticket-cita", { cita: citaDePrueba() });
        const { descargas, avisos } = capturarDescargas(ticket);
        ticket.shadowRoot.querySelector("[data-accion='descargar']").click();
        await esperar(() => descargas.length > 0 && avisos.length > 0);
        expect(descargas[0]).to.equal("ticket-5000.pdf");
        expect(peticionesTicket()[0].args[1].headers.Accept).to.equal("application/pdf");
        expect(avisos[0]).to.match(/^Ticket PDF descargado/);
      });

      it("ticket-cita genera el PDF en el dispositivo cuando el backend falla", async () => {
        fetch.withArgs(sinon.match(/\/tickets\/consultas\//)).resolves(new Response(null, { status: 500 }));
        const ticket = montar("ticket-cita", { cita: citaDePrueba() });
        const { descargas, avisos } = capturarDescargas(ticket);
        ticket.shadowRoot.querySelector("[data-accion='descargar']").click();
        await esperar(() => descargas.length > 0 && avisos.length > 0);
        expect(peticionesTicket()).to.have.length(1);
        expect(descargas[0]).to.equal("ticket-5000.pdf");
        expect(avisos[0]).to.match(/^Ticket PDF generado en el dispositivo/);
      });

      it("ticket-cita genera el PDF sin conexión sin llamar al backend", async () => {
        Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
        try {
          const ticket = montar("ticket-cita", { cita: citaDePrueba() });
          const { descargas, avisos } = capturarDescargas(ticket);
          ticket.shadowRoot.querySelector("[data-accion='descargar']").click();
          await esperar(() => descargas.length > 0 && avisos.length > 0);
          expect(peticionesTicket()).to.have.length(0);
          expect(avisos[0]).to.match(/^Ticket PDF generado sin conexión/);
        } finally {
          Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
        }
      });

      it("ticket-cita imprime el comprobante en una ventana nueva y avisa si el navegador la bloquea", () => {
        const ventana = { document: { write: sinon.spy(), close: sinon.spy() }, focus: sinon.spy(), print: sinon.spy() };
        const abrir = sinon.stub(dom.window, "open").returns(ventana);
        const ticket = montar("ticket-cita", { cita: citaDePrueba() });
        const avisos = [];
        ticket.addEventListener("notificar", (evento) => avisos.push(evento.detail.mensaje));
        ticket.shadowRoot.querySelector("[data-accion='imprimir']").click();
        expect(ventana.document.write.firstCall.args[0]).to.contain("GalenoSV · Comprobante de cita");
        expect(ventana.print.calledOnce).to.be.true;

        abrir.returns(null);
        ticket.shadowRoot.querySelector("[data-accion='imprimir']").click();
        expect(avisos).to.deep.equal(["Permita las ventanas emergentes para imprimir el ticket."]);
      });

      it("ticket-cita muestra los estados de los pasos con texto legible", () => {
        const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
        const ticket = montar("ticket-cita", { cita });
        const estados = [...ticket.shadowRoot.querySelectorAll("tbody td:nth-child(3)")].map((td) => td.textContent);
        expect(estados).to.deep.equal(["Completado", "En curso", "Pendiente"]);
        expect(ticket.shadowRoot.textContent).to.not.contain("Guardar como PDF");
      });
    });
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

