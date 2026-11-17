import { JSDOM } from "jsdom";
import { expect } from "chai";
import sinon from "sinon";
import { datosBase, indizar, stubApi, esperar } from "./ayudas.js";
import { limpiarCache, consultaCompleta, resumenCita } from "../js/datos.js";
import { guardarSesion } from "../js/estado.js";
import { listarPendientes, sincronizar, encolar } from "../js/pendientes.js";
import { ejecutarPendiente, listar } from "../js/api.js";

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
    await import("../js/componentes/ui-toast.js");
    await import("../js/componentes/ui-autocompletar.js");
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


  /** Abre la consulta y espera a que se pinte. */
  async function abrirConsulta(id = "5000") {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", id);
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector(".proc"));
    return vista;
  }

  /** Responde el diálogo de confirmación propio y devuelve su texto. */
  async function responderDialogo(aceptar) {
    await esperar(() => document.querySelector("ui-confirmar"));
    const dialogo = document.querySelector("ui-confirmar");
    const texto = dialogo.shadowRoot.textContent;
    dialogo.shadowRoot.querySelector(`[data-respuesta="${aceptar ? "si" : "no"}"]`).click();
    await esperar(() => !document.querySelector("ui-confirmar"));
    return texto;
  }

  const pasoActualizado = () =>
    fetch.getCalls().filter((llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso"));


  /** Simula la falta de red con una cola de cambios real en localStorage. */
  function sinRed() {
    globalThis.localStorage = dom.window.localStorage;
    localStorage.clear();
    Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
  }

  function conRed() {
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
  }


  describe("Componentes compartidos", () => {
    describe("ui-autocompletar · buscador de datos del catálogo", () => {
      const OPCIONES = [
        { valor: 10, texto: "Clínica Central" },
        { valor: 20, texto: "Clínica Municipal Chalchuapa" },
        { valor: 30, texto: "Policlínica Oeste" },
      ];
      function abrirCampo() {
        const campo = montar("ui-autocompletar", { opciones: OPCIONES });
        campo.setAttribute("etiqueta", "Clínica");
        const entrada = campo.shadowRoot.querySelector("input");
        const escribir = (texto) => {
          entrada.value = texto;
          entrada.dispatchEvent(new dom.window.Event("input"));
        };
        const tecla = (key) => {
          const evento = new dom.window.KeyboardEvent("keydown", { key, bubbles: true, composed: true, cancelable: true });
          entrada.dispatchEvent(evento);
          return evento;
        };
        const visibles = () => [...campo.shadowRoot.querySelectorAll("li[role=option]")].map((li) => li.textContent);
        return { campo, entrada, escribir, tecla, visibles };
      }

      it("filtra mientras se escribe, sin distinguir mayúsculas ni acentos, y resalta la coincidencia", () => {
        const { campo, escribir, visibles } = abrirCampo();
        escribir("CLINICA");
        expect(visibles()).to.deep.equal(["Clínica Central", "Clínica Municipal Chalchuapa", "Policlínica Oeste"]);
        escribir("chal");
        expect(visibles()).to.deep.equal(["Clínica Municipal Chalchuapa"]);
        expect(campo.shadowRoot.querySelector("mark").textContent).to.equal("Chal");
        escribir("zzz");
        expect(visibles()).to.deep.equal(["Sin resultados"]);
      });

      it("se elige con flechas y Enter, emite change y Enter no llega al formulario", () => {
        const { campo, entrada, escribir, tecla } = abrirCampo();
        const cambios = [];
        const enFormulario = [];
        campo.addEventListener("change", () => cambios.push(campo.value));
        document.body.addEventListener("keydown", (evento) => enFormulario.push(evento.key));
        escribir("clinica");
        tecla("ArrowDown");
        tecla("ArrowDown");
        expect(entrada.getAttribute("aria-activedescendant")).to.equal("op-1");
        tecla("Enter");
        expect(campo.value).to.equal(20);
        expect(campo.textoElegido).to.equal("Clínica Municipal Chalchuapa");
        expect(entrada.value).to.equal("Clínica Municipal Chalchuapa");
        expect(entrada.getAttribute("aria-expanded")).to.equal("false");
        expect(cambios).to.deep.equal([20]);
        expect(enFormulario).to.not.include("Enter");
      });

      it("Escape cierra la lista y conserva la opción elegida", () => {
        const { campo, entrada, escribir, tecla } = abrirCampo();
        campo.value = 10;
        escribir("poli");
        tecla("Escape");
        expect(entrada.getAttribute("aria-expanded")).to.equal("false");
        expect(entrada.value).to.equal("Clínica Central");
        expect(campo.value).to.equal(10);
      });

      it("al salir acepta un texto exacto, restaura uno incompleto y vacía el valor si se borra", () => {
        const { campo, entrada, escribir } = abrirCampo();
        const salir = () => entrada.dispatchEvent(new dom.window.FocusEvent("blur"));
        escribir("policlinica oeste");
        salir();
        expect(campo.value).to.equal(30);

        escribir("muni");
        salir();
        expect(campo.value).to.equal(30);
        expect(entrada.value).to.equal("Policlínica Oeste");

        escribir("");
        salir();
        expect(campo.value).to.equal("");
      });

      it("muestra un aviso y se deshabilita mientras no hay opciones", () => {
        const campo = montar("ui-autocompletar");
        const entrada = campo.shadowRoot.querySelector("input");
        expect(entrada.disabled).to.be.true;
        expect(entrada.placeholder).to.equal("Cargando…");
        campo.opciones = OPCIONES;
        expect(entrada.disabled).to.be.false;
        campo.mensaje = "No disponible";
        expect(entrada.disabled).to.be.true;
        expect(entrada.placeholder).to.equal("No disponible");
      });
    });

    it("ui-confirmar enfoca Cancelar, mantiene el foco dentro con Tab y Escape cancela", async () => {
      const { confirmar } = await import("../js/componentes/ui-confirmar.js");
      const respuesta = confirmar({ titulo: "¿Seguro?", aceptar: "Sí" });
      const raiz = document.querySelector("ui-confirmar").shadowRoot;
      expect(raiz.querySelector("[role=alertdialog]").getAttribute("aria-modal")).to.equal("true");
      expect(raiz.activeElement.textContent).to.equal("Cancelar");
      const tecla = (key) => raiz.activeElement.dispatchEvent(new dom.window.KeyboardEvent("keydown", { key, bubbles: true, cancelable: true }));
      tecla("Tab");
      expect(raiz.activeElement.textContent).to.equal("Sí");
      tecla("Tab");
      expect(raiz.activeElement.textContent).to.equal("Cancelar");
      tecla("Escape");
      expect(await respuesta).to.be.false;
      expect(document.querySelector("ui-confirmar")).to.be.null;
    });

    it("ui-toast muestra las notificaciones recibidas del documento", async () => {
      const toast = montar("ui-toast");
      document.dispatchEvent(
        new dom.window.CustomEvent("notificar", { detail: { mensaje: "Guardado correcto", tipo: "ok" } })
      );
      expect(toast.shadowRoot.querySelector(".aviso").textContent).to.equal("Guardado correcto");
    });
  });

  describe("HU1 · Búsqueda de citas", () => {
    describe("asistente de búsqueda", () => {
      // El asistente vive dentro de app-busqueda; estas ayudas lo recorren como lo haría el paciente.
      async function abrirAsistente() {
        const busqueda = montar("app-busqueda");
        await esperar(() => busqueda.shadowRoot.querySelector("asistente-citas")?.shadowRoot?.querySelector("#tipo").opciones.length > 0);
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

        // Al corregir el número el error anterior desaparece.
        const numero = raiz.querySelector("#numero");
        numero.value = "99999999-9";
        numero.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
        expect(raiz.querySelector("#error").hidden).to.be.true;

        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.contain("No hay citas registradas con ese documento");
      });

      it("no distingue un documento inexistente de uno sin clínicas como paciente", async () => {
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "33333333-3");
        const desconocido = error(raiz);
        raiz.querySelector("#numero").value = "99999999-9";
        clic(raiz, "#siguiente");
        expect(desconocido).to.equal(error(raiz));
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
        const clinicas = raiz.querySelector("#clinica").opciones.map((opcion) => opcion.texto);
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
        expect(raiz.querySelector("#clinica").textoElegido).to.equal("Clínica Central");
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
        expect(raiz.querySelector("#tipo").mensaje).to.equal("No disponible");
      });

      it("ticket-cita mantiene el foco dentro del comprobante y lo devuelve a «Ver ticket» al cerrar", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelector("cita-card"));
        const verTicket = busqueda.shadowRoot.querySelector("cita-card").shadowRoot.querySelector('[data-accion="ticket"]');
        verTicket.focus();
        verTicket.click();
        await esperar(() => busqueda.shadowRoot.querySelector("ticket-cita"));
        const ticket = busqueda.shadowRoot.querySelector("ticket-cita").shadowRoot;
        const botones = [...ticket.querySelectorAll("button")];
        expect(ticket.activeElement).to.equal(botones[0]);
        const tecla = (key, shiftKey = false) =>
          ticket.activeElement.dispatchEvent(new dom.window.KeyboardEvent("keydown", { key, shiftKey, bubbles: true, composed: true, cancelable: true }));
        tecla("Tab", true);
        expect(ticket.activeElement).to.equal(botones.at(-1));
        tecla("Tab");
        expect(ticket.activeElement).to.equal(botones[0]);
        tecla("Escape");
        expect(busqueda.shadowRoot.querySelector("ticket-cita")).to.be.null;
        expect(verTicket.getRootNode().activeElement).to.equal(verTicket);
      });

      it("abre el ticket de una cita encontrada y permite iniciar una nueva búsqueda", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 2);
        // Los datos del paciente se muestran una sola vez y la vista se lleva a los resultados.
        const titulo = busqueda.shadowRoot.querySelector("#titulo-resultados");
        expect(titulo.textContent).to.equal("Ana López");
        expect(busqueda.shadowRoot.querySelector(".resumen").textContent).to.contain("DUI 11111111-1");
        expect(busqueda.shadowRoot.activeElement).to.equal(titulo);
        busqueda.shadowRoot.querySelector("cita-card").shadowRoot.querySelector('button[data-accion="ticket"]').click();
        await esperar(() => busqueda.shadowRoot.querySelector("ticket-cita"));
        expect(busqueda.shadowRoot.querySelector("ticket-cita").shadowRoot.textContent).to.contain("Comprobante de cita");

        busqueda.shadowRoot.querySelector("#nueva").click();
        expect(pasoVisible(raiz)).to.equal("1");
        expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);
      });
    });

    describe("cards de citas", () => {
      it("cita-card ofrece «Atender consulta» con pasos pendientes y «Ver atención» cuando ya terminó", () => {
        sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
        const indice = indizar(datosBase());
        const cita = resumenCita(indice, indice.porId.consultas.get(5000));
        const boton = (c) => montar("cita-card", { cita: c }).shadowRoot.querySelector('[data-accion="atender"]').textContent;
        expect(boton(cita)).to.equal("Atender consulta");
        const terminada = {
          ...cita,
          procedimientos: cita.procedimientos.map((p) => ({ ...p, pasos: p.pasos.map((paso) => ({ ...paso, estado: "completado" })) })),
        };
        expect(boton(terminada)).to.equal("Ver atención");
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

      it("cita-card muestra los pasos en jerarquía: procedimiento, pasos en orden y lo registrado en cada uno", () => {
        sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
        const base = datosBase();
        base.consultasProcedimiento[0].observaciones = JSON.stringify({ notas: "", pasos: { 7000: { notas: "Encía sana" } } });
        const indice = indizar(base);
        const raiz = montar("cita-card", { cita: resumenCita(indice, indice.porId.consultas.get(5000)) }).shadowRoot;

        const procedimiento = raiz.querySelector(".procedimiento");
        expect(procedimiento.querySelector(".nombre-procedimiento").textContent).to.equal("Limpieza dental");
        const linea = procedimiento.querySelector("linea-pasos").shadowRoot;
        const pasos = [...linea.querySelectorAll("ol.pasos > li.paso")];
        expect(pasos.map((paso) => paso.querySelector(".nombre").textContent)).to.deep.equal(["Evaluación", "Profilaxis", "Alta"]);

        const [hecho, actual, pendiente] = pasos;
        expect(hecho.querySelector(".quien").textContent).to.contain("Elena Martínez");
        expect(hecho.querySelector(".quien").textContent).to.contain("12 nov");
        const registro = hecho.querySelector("details.registro");
        expect(registro.querySelector("summary").textContent).to.equal("Ver registro");
        expect(registro.textContent).to.contain("Encía sana");
        // Sin registro todavía no hay nada que ver: solo se indica el estado.
        expect(actual.querySelector("details.registro")).to.be.null;
        expect(actual.querySelector(".quien").textContent).to.equal("Por iniciar");
        expect(pendiente.querySelector("details.registro")).to.be.null;
      });

      it("cita-card marca el paso actual sin iniciar como «Por iniciar» y muestra la fecha de nacimiento correcta", () => {
        const base = datosBase();
        base.personas[0].fechaNacimiento = "1992-09-30T00:00:00Z";
        const indice = indizar(base);
        const tarjeta = montar("cita-card", { cita: resumenCita(indice, indice.porId.consultas.get(5000)) });
        const actual = tarjeta.shadowRoot.querySelector("linea-pasos").shadowRoot.querySelector(".paso.actual .quien").textContent;
        expect(actual).to.contain("Por iniciar");
        expect(tarjeta.shadowRoot.textContent).to.contain("30 sept 1992");
      });

      it("en la búsqueda del paciente titula con el motivo y no repite datos de la cita", () => {
        // Aunque un empleado tenga la sesión abierta, la vista del paciente no ofrece atender.
        sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
        const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
        const tarjeta = document.createElement("cita-card");
        tarjeta.setAttribute("vista", "paciente");
        tarjeta.cita = cita;
        document.body.append(tarjeta);
        const raiz = tarjeta.shadowRoot;
        expect(raiz.querySelector("h3").textContent).to.equal("Limpieza");
        expect(raiz.querySelector(".paciente p").textContent).to.equal("Clínica Central");
        // Documento y nacimiento ya están en el encabezado de los resultados: no se repiten en cada cita.
        expect(raiz.querySelector(".general")).to.be.null;
        expect(raiz.querySelector('[data-accion="atender"]')).to.be.null;

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

      it("el comprobante impreso y el PDF sin conexión incluyen paciente, código, pasos y lo registrado", () => {
        const cita = consultaCompleta(indizar(datosBase()), 5000);
        const html = documentoImprimible(cita);
        for (const texto of ["Ana López", "#5000", "Evaluación", "Caries de la dentina", "Guardar como PDF"]) {
          expect(html).to.contain(texto);
        }
        const lineas = lineasTicket(cita).join("\n");
        for (const texto of ["Paciente: Ana López", "Ticket: #5000", "Evaluación", "Caries de la dentina"]) {
          expect(lineas).to.contain(texto);
        }
      });

      it("el ticket muestra lo registrado en cada paso y no el borrador del paso en curso", () => {
        const base = datosBase();
        base.consultasProcedimiento[0].observaciones = JSON.stringify({
          notas: "Borrador sin guardar",
          diagnostico: "K00.0 · Anodoncia",
          plan: "",
          pasos: { 7000: { notas: "Encía sana", diagnostico: "K02.1 · Caries de la dentina", plan: "" } },
        });
        const indice = indizar(base);
        const cita = resumenCita(indice, indice.porId.consultas.get(5000));
        const ticket = montar("ticket-cita", { cita }).shadowRoot;
        const primeraFila = ticket.querySelector("tbody tr td:last-child").textContent;
        expect(primeraFila).to.equal("CIE-10: K02.1 · Caries de la dentina | Notas: Encía sana");
        for (const texto of [ticket.textContent, documentoImprimible(cita), lineasTicket(cita).join("\n")]) {
          expect(texto).to.contain("Caries de la dentina");
          expect(texto).to.not.contain("Anodoncia");
          expect(texto).to.not.contain("Borrador sin guardar");
        }
      });

      it("el ticket incluye las órdenes de examen del procedimiento sin repetir la palabra «Examen»", () => {
        const base = datosBase();
        base.ordenesExamen.push({
          idOrdenExamen: 400,
          idConsultaProcedimientoPaso: 9000,
          fechaCreacion: "2026-11-12T09:20:00-06:00",
          indicaciones: "Examen: Radiografía periapical — Sector inferior",
        });
        const indice = indizar(base);
        const cita = resumenCita(indice, indice.porId.consultas.get(5000));
        const filas = [...montar("ticket-cita", { cita }).shadowRoot.querySelectorAll("tbody tr")].map((fila) =>
          [...fila.cells].map((celda) => celda.textContent)
        );
        expect(filas.at(-1).slice(1, 3)).to.deep.equal(["Examen ordenado", "Ordenado"]);
        expect(filas.at(-1)[4]).to.equal("Radiografía periapical — Sector inferior");
        expect(documentoImprimible(cita)).to.contain("Radiografía periapical — Sector inferior");
        expect(lineasTicket(cita)).to.include("   Examen ordenado: Radiografía periapical — Sector inferior");
      });

      it("en pantallas angostas cada celda del ticket lleva su etiqueta para leerse como tarjeta", () => {
        const ticket = montar("ticket-cita", { cita: citaDePrueba() }).shadowRoot;
        const etiquetas = [...ticket.querySelector("tbody tr").cells].map((celda) => celda.dataset.etiqueta);
        expect(etiquetas).to.deep.equal(["Procedimiento", "Paso", "Estado", "Fecha", "Observaciones"]);
      });

      it("un paso pendiente que conserva su registro de antes de retroceder no muestra fecha ni responsable", () => {
        const cita = citaDePrueba();
        const [primero] = cita.procedimientos[0].pasos;
        // Mismo registro que el paso 1, pero el paso quedó pendiente tras retroceder.
        const pendiente = { ...primero, estado: "pendiente" };
        const conPendiente = { ...cita, procedimientos: [{ ...cita.procedimientos[0], pasos: [pendiente] }] };

        const fila = montar("ticket-cita", { cita: conPendiente }).shadowRoot.querySelector("tbody tr");
        expect([...fila.cells].map((celda) => celda.textContent).slice(2, 4)).to.deep.equal(["Pendiente", "Pendiente"]);
        expect(lineasTicket(conPendiente).find((linea) => linea.includes(primero.paso.nombre))).to.contain("Sin asignar");

        const linea = montar("linea-pasos", { pasos: [pendiente] }).shadowRoot;
        expect(linea.querySelector(".quien").textContent).to.equal("Pendiente");
        expect(linea.querySelector("details.registro")).to.be.null;
      });

      it("ticket-cita muestra un código corto del ticket en lugar del identificador completo", () => {
        const cita = resumenCita(indizar(datosBase()), indizar(datosBase()).porId.consultas.get(5000));
        cita.consulta = { ...cita.consulta, idConsulta: "9fcdae6b-1100-401c-881a-892650484990" };
        const ticket = montar("ticket-cita", { cita });
        expect(ticket.shadowRoot.querySelector(".codigo").textContent).to.equal("#9FCDAE6B");
        expect(ticket.shadowRoot.textContent).to.not.contain("892650484990");
        expect(lineasTicket(cita)).to.include("Ticket: #9FCDAE6B");
      });

      it("ticket-cita descarga el PDF desde el backend con el código corto como nombre", async () => {
        const cita = citaDePrueba();
        cita.consulta = { ...cita.consulta, idConsulta: "9fcdae6b-1100-401c-881a-892650484990" };
        const ticket = montar("ticket-cita", { cita });
        const { descargas, avisos } = capturarDescargas(ticket);
        ticket.shadowRoot.querySelector("[data-accion='descargar']").click();
        await esperar(() => descargas.length > 0 && avisos.length > 0);
        expect(peticionesTicket()[0].args[0]).to.contain("/tickets/consultas/9fcdae6b-1100-401c-881a-892650484990");
        expect(descargas[0]).to.equal("ticket-9FCDAE6B.pdf");
        expect(peticionesTicket()[0].args[1].headers.Accept).to.equal("application/pdf");
        expect(avisos[0]).to.equal("Ticket PDF descargado.");
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
        expect(estados).to.deep.equal(["Completado", "Por iniciar", "Pendiente"]);
        expect(ticket.shadowRoot.textContent).to.not.contain("Guardar como PDF");
      });
    });
  });

  describe("HU2 · Login", () => {
    async function abrirLogin() {
      const login = montar("app-login");
      await esperar(() => login.shadowRoot.querySelector("#tipo").opciones.length > 0);
      return login;
    }
    function enviar(login, tipo, numero) {
      login.shadowRoot.querySelector("#tipo").value = tipo;
      login.shadowRoot.querySelector("#numero").value = numero;
      login.shadowRoot.querySelector("#formulario").dispatchEvent(
        new dom.window.Event("submit", { bubbles: true, cancelable: true })
      );
    }
    const errorLogin = (login) => login.shadowRoot.querySelector("#error");

    it("app-login valida el documento y crea la sesión sin autorregistro", async () => {
      const login = montar("app-login");
      const navegaciones = [];
      login.addEventListener("navegar", (evento) => navegaciones.push(evento.detail));
      await esperar(() => login.shadowRoot.querySelector("#tipo").opciones.length > 0);
      login.shadowRoot.querySelector("#tipo").value = "1";
      login.shadowRoot.querySelector("#numero").value = " 33333333-3 ";
      login.shadowRoot.querySelector("#formulario").dispatchEvent(
        new dom.window.Event("submit", { bubbles: true, cancelable: true })
      );
      await esperar(() => sessionStorage.getItem("galenosv.sesion") && navegaciones.length > 0);
      const sesion = JSON.parse(sessionStorage.getItem("galenosv.sesion"));
      expect(sesion.idPersona).to.equal(300);
      expect(sesion.nombre).to.equal("Elena Martínez");
      expect(sesion.nombreCorto).to.equal("Elena Martínez");
      expect(sesion.roles[0].nombreRol).to.equal("Enfermero");
      expect(navegaciones).to.include("dashboard");
    });

    it("app-login rechaza documentos desconocidos y muestra el error", async () => {
      const login = montar("app-login");
      await esperar(() => login.shadowRoot.querySelector("#tipo").opciones.length > 0);
      login.shadowRoot.querySelector("#tipo").value = "1";
      login.shadowRoot.querySelector("#numero").value = "99999999-9";
      login.shadowRoot.querySelector("#formulario").dispatchEvent(
        new dom.window.Event("submit", { bubbles: true, cancelable: true })
      );
      await esperar(() => !login.shadowRoot.querySelector("#error").hidden);
      expect(login.shadowRoot.querySelector("#error").textContent).to.equal(
        "No hay un empleado registrado con ese documento. Verifique el tipo y el número."
      );
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
    });

    it("app-login rechaza a quien solo es paciente con el mismo mensaje, sin revelar que el documento existe", async () => {
      const login = await abrirLogin();
      enviar(login, "1", "11111111-1");
      await esperar(() => !errorLogin(login).hidden);
      expect(errorLogin(login).textContent).to.equal(
        "No hay un empleado registrado con ese documento. Verifique el tipo y el número."
      );
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
    });

    it("app-login oculta el error en cuanto se corrige el número y no sugiere un documento real", async () => {
      const login = await abrirLogin();
      enviar(login, "1", "11111111-1");
      await esperar(() => !errorLogin(login).hidden);
      const numero = login.shadowRoot.querySelector("#numero");
      numero.value = "33333333-3";
      numero.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
      expect(errorLogin(login).hidden).to.be.true;
      expect(numero.placeholder).to.not.match(/[1-9]/);
    });

    it("app-login pide tipo y número, y valida el formato antes de buscar al empleado", async () => {
      datos.tiposDocumento[0].expresionRegular = "^[0-9]{8}-[0-9]$";
      datos.tiposDocumento[0].indicaciones = "8 dígitos, guion y dígito verificador";
      const login = await abrirLogin();
      enviar(login, "", "");
      expect(errorLogin(login).textContent).to.equal("Seleccione el tipo de documento e ingrese el número.");
      expect(errorLogin(login).getAttribute("role")).to.equal("alert");

      enviar(login, "1", "123");
      await esperar(() => errorLogin(login).textContent.includes("formato"));
      expect(errorLogin(login).textContent).to.contain("8 dígitos, guion y dígito verificador");
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
    });

    it("app-login avisa cuando no puede cargar los datos del servicio", async () => {
      fetch.rejects(new TypeError("Failed to fetch"));
      const login = montar("app-login");
      await esperar(() => !errorLogin(login).hidden);
      expect(errorLogin(login).textContent).to.contain("No se pudo conectar con el servicio");
      expect(login.shadowRoot.querySelector("#tipo").mensaje).to.equal("No se pudo cargar");
    });

    it("app-login no ofrece recuperar contraseña ni autorregistro", async () => {
      const login = await abrirLogin();
      expect(login.shadowRoot.textContent).to.not.match(/contraseña|Solicitar acceso/i);
      expect(login.shadowRoot.querySelector("#ir-buscar")).to.not.be.null;
    });

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

  describe("HU3 · Dashboard", () => {
    const tarjetas = (raiz) => [...raiz.querySelectorAll("#lista cita-card")];
    const idsDe = (raiz) => tarjetas(raiz).map((tarjeta) => tarjeta.cita.consulta.idConsulta);
    const cambiar = (campo, valor) => {
      campo.value = valor;
      campo.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
    };
    const rango = (raiz, desde, hasta) => {
      raiz.querySelector("#desde").value = desde;
      cambiar(raiz.querySelector("#hasta"), hasta);
    };
    async function abrirPanel(hoy = "2026-11-12T08:00:00-06:00") {
      // Solo se congela la fecha: los temporizadores siguen reales para esperar el render.
      reloj = sinon.useFakeTimers({ now: new Date(hoy), toFake: ["Date"] });
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const panel = montar("app-dashboard");
      await esperar(() => panel.shadowRoot.querySelectorAll("#resumen .indicador").length === 4);
      return panel.shadowRoot;
    }
    let reloj;
    afterEach(() => reloj?.restore());

    it("conserva las fechas y el paciente elegidos al volver de una consulta", async () => {
      const raiz = await abrirPanel();
      raiz.querySelector('[data-rango="pasadas"]').click();
      const paciente = raiz.querySelector("#paciente");
      paciente.value = 200;
      paciente.dispatchEvent(new dom.window.Event("change"));
      raiz.host.remove();

      const panel = montar("app-dashboard");
      await esperar(() => panel.shadowRoot.querySelectorAll("#resumen .indicador").length === 4);
      const nueva = panel.shadowRoot;
      expect(nueva.querySelector("#desde").value).to.equal("");
      expect(nueva.querySelector("#hasta").value).to.equal("2026-11-11");
      expect(nueva.querySelector('[data-rango="pasadas"]').getAttribute("aria-pressed")).to.equal("true");
      expect(String(nueva.querySelector("#paciente").value)).to.equal("200");
    });

    it("muestra por defecto las consultas de hoy del rol elegido", async () => {
      const raiz = await abrirPanel();
      expect(raiz.querySelector("#desde").value).to.equal("2026-11-12");
      expect(raiz.querySelector("#hasta").value).to.equal("2026-11-12");
      expect(raiz.querySelector('[data-rango="hoy"]').getAttribute("aria-pressed")).to.equal("true");
      expect(raiz.querySelector("#rango-aviso").textContent).to.contain("hoy");
      await esperar(() => tarjetas(raiz).length === 2);
      expect(idsDe(raiz)).to.deep.equal([5000, 5002]);
    });

    it("cambia de rol y muestra solo las consultas con pasos de ese rol", async () => {
      const raiz = await abrirPanel();
      await esperar(() => tarjetas(raiz).length === 2);
      cambiar(raiz.querySelector("#rol"), "2");
      await esperar(() => tarjetas(raiz).length === 1);
      expect(tarjetas(raiz)[0].shadowRoot.textContent).to.contain("Limpieza dental");
    });

    it("filtra consultas pasadas, la más reciente primero", async () => {
      const raiz = await abrirPanel("2026-11-20T08:00:00-06:00");
      await esperar(() => raiz.querySelector("#lista .vacio"));
      raiz.querySelector('[data-rango="pasadas"]').click();
      await esperar(() => tarjetas(raiz).length === 2);
      expect(raiz.querySelector("#desde").value).to.equal("");
      expect(raiz.querySelector("#hasta").value).to.equal("2026-11-19");
      expect(idsDe(raiz)).to.deep.equal([5002, 5000]);
      expect(raiz.querySelector('[data-rango="pasadas"]').getAttribute("aria-pressed")).to.equal("true");
    });

    it("filtra consultas próximas desde mañana sin límite final", async () => {
      const raiz = await abrirPanel("2026-11-01T08:00:00-06:00");
      raiz.querySelector('[data-rango="proximas"]').click();
      await esperar(() => tarjetas(raiz).length === 2);
      expect(raiz.querySelector("#desde").value).to.equal("2026-11-02");
      expect(raiz.querySelector("#hasta").value).to.equal("");
      expect(idsDe(raiz)).to.deep.equal([5000, 5002]);
    });

    it("permite un rango libre de fechas y avisa si el inicio es posterior al fin", async () => {
      const raiz = await abrirPanel("2026-12-01T08:00:00-06:00");
      rango(raiz, "2026-11-01", "2026-11-30");
      await esperar(() => tarjetas(raiz).length === 2);
      expect(raiz.querySelector('[aria-pressed="true"]')).to.be.null;
      expect(raiz.querySelector("#rango-aviso").textContent).to.contain("01 nov 2026");

      rango(raiz, "2026-11-30", "2026-11-01");
      await esperar(() => raiz.querySelector("#lista [role=alert]"));
      expect(tarjetas(raiz)).to.have.length(0);
      expect(raiz.querySelector("#lista").textContent).to.contain("posterior");
      expect(raiz.querySelector("#rango-aviso").textContent).to.equal("");
    });

    it("usa buscadores para clínica, rol y paciente, y filtra por el paciente elegido", async () => {
      const raiz = await abrirPanel();
      await esperar(() => raiz.querySelector("#paciente").opciones.length > 0);
      expect(raiz.querySelector("#clinica").textoElegido).to.equal("Clínica Central");
      expect(raiz.querySelector("#rol").textoElegido).to.equal("Enfermero");
      expect(raiz.querySelector("#paciente").opciones.map((opcion) => opcion.texto)).to.deep.equal([
        "Ana López · DUI 11111111-1",
        "Carlos Pérez · DUI 22222222-2",
      ]);
      await esperar(() => tarjetas(raiz).length === 2);
      cambiar(raiz.querySelector("#paciente"), 200);
      await esperar(() => tarjetas(raiz).length === 1);
      expect(tarjetas(raiz)[0].shadowRoot.textContent).to.contain("Carlos Pérez");
    });
  });

  describe("HU4 · Registro de atención", () => {
    it("app-consulta no ofrece retroceder en el primer paso, aunque ya esté iniciado", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      expect(raiz.querySelector("[data-accion='retroceder']")).to.be.null;
      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      expect(raiz.querySelector("[data-accion='retroceder']")).to.be.null;
    });

    it("app-consulta con todos los pasos completados retrocede al último paso, no al penúltimo", async () => {
      datos.consultasProcedimientoPaso.push(
        { idConsultaProcedimientoPaso: 9001, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T09:40:00-06:00", estado: "FINALIZADO" },
        { idConsultaProcedimientoPaso: 9002, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T10:00:00-06:00", estado: "FINALIZADO" }
      );
      const vista = await abrirConsulta();
      vista.shadowRoot.querySelector("[data-accion='retroceder']").click();
      expect(await responderDialogo(true)).to.contain("Alta");
      await esperar(() => pasoActualizado().length === 1);
      expect(pasoActualizado()[0].args[0]).to.contain("/9002");
    });

    it("app-consulta en el último paso ofrece completarlo en lugar de avanzar", async () => {
      datos.consultasProcedimientoPaso.push(
        { idConsultaProcedimientoPaso: 9001, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T09:40:00-06:00", estado: "FINALIZADO" },
        { idConsultaProcedimientoPaso: 9002, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T10:00:00-06:00", estado: "EN_PROCESO" }
      );
      const vista = await abrirConsulta();
      expect(vista.shadowRoot.querySelector("[data-accion='completar']").textContent).to.equal("Completar último paso");
    });

    it("app-consulta no pierde lo escrito en el registro al iniciar el paso", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      expect(raiz.querySelector(".paso-panel").textContent).to.contain("Paso 1 de 3");
      raiz.querySelector("[data-campo='notas']").value = "Paciente refiere sensibilidad";
      raiz.querySelector("[data-campo='plan']").value = "Control en 15 días";
      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      expect(raiz.querySelector("[data-campo='notas']").value).to.equal("Paciente refiere sensibilidad");
      expect(raiz.querySelector("[data-campo='plan']").value).to.equal("Control en 15 días");
    });

    it("app-consulta destaca «Finalizar consulta» solo cuando todos los pasos están completos", async () => {
      const vista = await abrirConsulta();
      expect(vista.shadowRoot.querySelector("[data-accion='finalizar']").classList.contains("secundario")).to.be.true;

      vista.remove();
      limpiarCache();
      datos.consultasProcedimientoPaso.push(
        { idConsultaProcedimientoPaso: 9001, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T09:40:00-06:00", estado: "FINALIZADO" },
        { idConsultaProcedimientoPaso: 9002, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T10:00:00-06:00", estado: "FINALIZADO" }
      );
      const completa = await abrirConsulta();
      expect(completa.shadowRoot.querySelector("[data-accion='finalizar']").classList.contains("secundario")).to.be.false;
    });

    it("app-consulta resume al paciente en el encabezado con su edad y el estado como insignia", async () => {
      datos.personas[0].fechaNacimiento = "1990-05-15T00:00:00-06:00";
      const vista = await abrirConsulta();
      const encabezado = vista.shadowRoot.querySelector(".encabezado");
      expect(encabezado.querySelector("h2").textContent).to.equal("Ana López");
      expect(encabezado.querySelector(".datos").textContent).to.match(/\d+ años · Clínica Central · .+ · Limpieza/);
      expect(encabezado.querySelector(".estado-pill").textContent).to.equal("En proceso");
    });

    it("app-consulta pliega los exámenes opcionales, resume las órdenes y muestra «Asignar» solo con exámenes elegidos", async () => {
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        estado: "EN_PROCESO",
      });
      datos.ordenesExamen.push({ idOrdenExamen: 400, idConsultaProcedimientoPaso: 9001, fechaCreacion: "2026-11-12T09:45:00-06:00", indicaciones: "Examen: Radiografía periapical" });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      const examenes = raiz.querySelector("details.examenes");
      expect(examenes.open).to.be.false;
      expect(examenes.querySelector("summary").textContent).to.contain("1 orden");

      examenes.open = true;
      const asignar = () => raiz.querySelector("[data-accion='asignar']").closest(".asignacion");
      expect(asignar().hidden).to.be.true;
      const buscador = raiz.querySelector("[data-examen-buscador]");
      buscador.value = 20;
      buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
      expect(asignar().hidden).to.be.false;

      // Tras asignar, el bloque sigue abierto para ver la orden nueva.
      raiz.querySelector("[data-accion='asignar']").click();
      await esperar(() => raiz.querySelector("details.examenes summary").textContent.includes("2 órdenes"));
      expect(raiz.querySelector("details.examenes").open).to.be.true;
    });

    it("app-consulta busca el diagnóstico CIE-10 escribiendo parte del nombre", async () => {
      const vista = await abrirConsulta();
      const campo = vista.shadowRoot.querySelector("[data-campo='diagnostico']");
      expect(campo.localName).to.equal("ui-autocompletar");
      const entrada = campo.shadowRoot.querySelector("input");
      entrada.value = "dentina";
      entrada.dispatchEvent(new dom.window.Event("input"));
      const opciones = [...campo.shadowRoot.querySelectorAll("li[role=option]")].map((li) => li.textContent);
      expect(opciones).to.deep.equal(["K02.1 · Caries de la dentina"]);
    });

    it("app-consulta busca exámenes por nombre o categoría y permite elegir y quitar varios", async () => {
      datos.examenes.push({ idExamen: 21, nombre: "Hemograma", activo: true });
      datos.examenesTipoExamen.push({ idExamenTipoExamen: 2, idExamen: 21, idTipoExamen: 31 });
      datos.tiposExamen.push({ idTipoExamen: 31, nombre: "Laboratorio" });
      datos.procedimientosPasoExamen.push({ idProcedimientoPasoExamen: 2, idProcedimientoPaso: 7001, idExamen: 21, activo: true });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-examen-buscador]"));

      const buscador = raiz.querySelector("[data-examen-buscador]");
      expect(buscador.localName).to.equal("ui-autocompletar");
      const entrada = buscador.shadowRoot.querySelector("input");
      const buscar = (texto) => {
        entrada.value = texto;
        entrada.dispatchEvent(new dom.window.Event("input"));
        return [...buscador.shadowRoot.querySelectorAll("li[role=option]")].map((li) => li.textContent);
      };
      const elegir = (idExamen) => {
        buscador.value = idExamen;
        buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
      };
      const elegidos = () => [...raiz.querySelectorAll(".elegidos li")].map((li) => li.querySelector("span").textContent);

      expect(buscar("laboratorio")).to.deep.equal(["Hemograma · Laboratorio"]);
      expect(buscar("radio")).to.deep.equal(["Radiografía periapical · Imagenología"]);
      elegir(21);
      elegir(20);
      expect(elegidos()).to.deep.equal(["Hemograma", "Radiografía periapical"]);
      // Lo ya elegido no se vuelve a ofrecer.
      expect(buscador.opciones).to.deep.equal([]);
      expect(buscador.mensaje).to.equal("Ya eligió todos los exámenes disponibles");

      raiz.querySelector('.elegidos button[aria-label="Quitar Hemograma"]').click();
      expect(elegidos()).to.deep.equal(["Radiografía periapical"]);
      expect(buscador.opciones.map((opcion) => opcion.valor)).to.deep.equal([21]);
    });

    it("app-consulta elimina las órdenes de examen seleccionadas del paso en curso tras confirmar, salvo las que ya tienen resultado", async () => {
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        estado: "EN_PROCESO",
      });
      datos.ordenesExamen.push(
        { idOrdenExamen: 400, idConsultaProcedimientoPaso: 9001, fechaCreacion: "2026-11-12T09:45:00-06:00", indicaciones: "Examen: Radiografía periapical" },
        { idOrdenExamen: 401, idConsultaProcedimientoPaso: 9001, fechaCreacion: "2026-11-12T09:46:00-06:00", indicaciones: "Examen: Hemograma" }
      );
      datos.examenesResultado.push({ idExamenResultado: 500, idOrdenExamen: 401, resultado: "Normal" });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      const casillas = () => [...raiz.querySelectorAll('.ordenes input[type="checkbox"]')];
      const boton = () => raiz.querySelector("[data-accion='eliminar-ordenes']");
      await esperar(() => casillas().length === 2);

      // La acción va fuera de la lista y está inactiva hasta seleccionar.
      expect(raiz.querySelector(".ordenes li button")).to.be.null;
      expect(boton().disabled).to.be.true;
      const [sinResultado, conResultado] = casillas();
      expect(conResultado.disabled).to.be.true;
      expect(conResultado.closest("li").textContent).to.contain("Con resultado");

      sinResultado.click();
      expect(boton().disabled).to.be.false;
      expect(boton().textContent).to.contain("(1)");

      const borrados = () => fetch.getCalls().filter((llamada) => llamada.args[1]?.method === "DELETE");
      boton().click();
      expect(await responderDialogo(false)).to.contain("Radiografía periapical");
      expect(borrados()).to.have.length(0);

      boton().click();
      expect(await responderDialogo(true)).to.contain("no se puede deshacer");
      await esperar(() => borrados().length === 1);
      expect(borrados()[0].args[0]).to.contain("/ordenes-examen/400");
      await esperar(() => casillas().length === 1);
      expect(raiz.querySelector(".ordenes").textContent).to.contain("Hemograma");
    });

    it("app-consulta finaliza la consulta registrando la fecha de cierre", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='finalizar']"));
      vista.shadowRoot.querySelector("[data-accion='finalizar']").click();
      const texto = await responderDialogo(true);
      expect(texto).to.contain("Finalizar la consulta");
      // La consulta de prueba tiene 2 de 3 pasos sin completar: el diálogo lo advierte.
      expect(texto).to.contain("Quedan 2 pasos sin completar");
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

    it("app-consulta muestra qué se registró en cada paso con la misma línea de pasos del dashboard", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector(".proc linea-pasos"));
      // El mismo historial en jerarquía que las cards del dashboard, sin duplicarlo en otro formato.
      expect(vista.shadowRoot.querySelectorAll(".stepper, .registro-pasos")).to.have.length(0);
      const linea = vista.shadowRoot.querySelector(".proc linea-pasos").shadowRoot;
      expect(linea.querySelector(".paso.completado").textContent).to.contain("Evaluación");
      expect(linea.querySelector(".paso.completado details.registro").textContent).to.contain("Elena Martínez");
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

      const buscador = vista.shadowRoot.querySelector("[data-examen-buscador]");
      buscador.value = 20;
      buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
      vista.shadowRoot.querySelector("[data-accion='asignar']").click();
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => (llamada.args[1]?.method ?? "GET") === "POST" && llamada.args[0].includes("ordenes-examen")
        )
      );
      const orden = fetch.getCalls().find((llamada) => llamada.args[0].includes("ordenes-examen") && llamada.args[1]?.method === "POST");
      expect(JSON.parse(orden.args[1].body).indicaciones).to.contain("Radiografía periapical");
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));

      vista.shadowRoot.querySelector("[data-campo='diagnostico']").value = "K02.1 · Caries de la dentina";
      vista.shadowRoot.querySelector("[data-accion='completar']").click();
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
        )
      );
      await esperar(() => vista.shadowRoot.textContent.includes("Paso actual: Alta"));
      expect(vista.shadowRoot.querySelector(".paso-cabecera").textContent).to.contain("Paso 3 de 3");
      // El registro queda en el paso completado y el siguiente empieza sin el diagnóstico anterior.
      const observaciones = JSON.parse(datos.consultasProcedimiento[0].observaciones);
      expect(observaciones.pasos["7001"].diagnostico).to.equal("K02.1 · Caries de la dentina");
      expect(observaciones.diagnostico).to.equal("");
      expect(vista.shadowRoot.querySelector("[data-campo='diagnostico']").value).to.equal("");
      expect(vista.shadowRoot.querySelector("[data-campo='notas']").value).to.equal("");
    });

    it("app-consulta ofrece volver al panel si la consulta no existe y reintentar si falla la carga", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const inexistente = document.createElement("app-consulta");
      inexistente.setAttribute("id-consulta", "9999");
      document.body.append(inexistente);
      await esperar(() => inexistente.shadowRoot.textContent.includes("no existe"));
      const navegaciones = [];
      inexistente.addEventListener("navegar", (evento) => navegaciones.push(evento.detail));
      inexistente.shadowRoot.querySelector("[data-accion='volver']").click();
      expect(navegaciones).to.deep.equal(["dashboard"]);

      limpiarCache();
      fetch.rejects(new TypeError("Failed to fetch"));
      const sinRed = document.createElement("app-consulta");
      sinRed.setAttribute("id-consulta", "5000");
      document.body.append(sinRed);
      await esperar(() => sinRed.shadowRoot.querySelector("[data-accion='reintentar']"));
      sinon.restore();
      stubApi(datos);
      sinRed.shadowRoot.querySelector("[data-accion='reintentar']").click();
      await esperar(() => sinRed.shadowRoot.querySelector(".proc"));
    });

    it("app-consulta ignora el doble clic mientras guarda: un solo paso iniciado", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      const boton = raiz.querySelector("[data-accion='iniciar']");
      boton.click();
      expect(boton.disabled).to.be.true;
      boton.click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      const altas = fetch.getCalls().filter(
        (llamada) => llamada.args[1]?.method === "POST" && llamada.args[0].includes("consultas-procedimiento-paso")
      );
      expect(altas).to.have.length(1);
      expect(datos.consultasProcedimientoPaso).to.have.length(1);
    });

    it("app-consulta al retroceder muestra en el formulario lo que ya tenía registrado ese paso", async () => {
      datos.consultasProcedimiento[0].observaciones = JSON.stringify({
        notas: "",
        diagnostico: "",
        plan: "",
        pasos: { 7001: { notas: "Placa en molares", diagnostico: "K03.6 · Depósitos en los dientes", plan: "Control" } },
      });
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        estado: "FINALIZADO",
      });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      expect(raiz.querySelector("[data-campo='notas']").value).to.equal("");
      raiz.querySelector("[data-accion='retroceder']").click();
      await responderDialogo(true);
      await esperar(() => raiz.textContent.includes("Paso actual: Profilaxis"));
      expect(raiz.querySelector("[data-campo='notas']").value).to.equal("Placa en molares");
      expect(raiz.querySelector("[data-campo='diagnostico']").value).to.equal("K03.6 · Depósitos en los dientes");
    });

    it("app-consulta sin conexión inicia, asigna y completa un paso, lo muestra en pantalla y lo envía con los mismos ids al volver la red", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      sinRed();
      try {
        raiz.querySelector("[data-accion='iniciar']").click();
        await esperar(() => raiz.querySelector("[data-accion='completar']"));

        const buscador = raiz.querySelector("[data-examen-buscador]");
        buscador.value = 20;
        buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
        raiz.querySelector("[data-accion='asignar']").click();
        await esperar(() => raiz.querySelector(".ordenes input[name='orden']"));
        expect(raiz.querySelector(".ordenes").textContent).to.contain("Radiografía periapical");

        raiz.querySelector("[data-campo='diagnostico']").value = "K02.1 · Caries de la dentina";
        raiz.querySelector("[data-accion='completar']").click();
        await esperar(() => raiz.textContent.includes("Paso actual: Profilaxis"));

        const escrituras = fetch.getCalls().filter((llamada) => (llamada.args[1]?.method ?? "GET") !== "GET");
        expect(escrituras).to.have.length(0);
        const cola = listarPendientes();
        const [paso, orden] = cola;
        const idPaso = paso.cuerpo.idConsultaProcedimientoPaso;
        expect(idPaso).to.match(/^[0-9a-f-]{36}$/);
        expect(orden.cuerpo.idOrdenExamen).to.match(/^[0-9a-f-]{36}$/);
        expect(orden.cuerpo.idConsultaProcedimientoPaso).to.equal(idPaso);
        // El cierre del paso apunta al id creado sin conexión, no a uno indefinido.
        expect(cola.map((pendiente) => `${pendiente.metodo} ${pendiente.ruta}`)).to.include(
          `PUT consultas-procedimiento-paso/${idPaso}`
        );

        conRed();
        expect(await sincronizar(ejecutarPendiente)).to.deep.equal({ enviadas: cola.length, pendientes: 0 });
        expect(datos.consultasProcedimientoPaso).to.have.length(1);
        expect(datos.consultasProcedimientoPaso[0]).to.include({ idConsultaProcedimientoPaso: idPaso, estado: "FINALIZADO" });
        expect(datos.ordenesExamen[0].idConsultaProcedimientoPaso).to.equal(idPaso);
      } finally {
        conRed();
        localStorage.clear();
        delete globalThis.localStorage;
      }
    });

    it("app-consulta retrocede al paso anterior tras confirmar en un diálogo propio, sin borrar su registro", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='retroceder']"));
      const anterior = vista.shadowRoot.querySelector("[data-accion='retroceder']");
      anterior.focus();
      anterior.click();
      expect(await responderDialogo(false)).to.contain("Evaluación");
      expect(pasoActualizado()).to.have.length(0);
      // Al cerrar, el foco vuelve al botón que abrió el diálogo.
      expect(vista.shadowRoot.activeElement).to.equal(anterior);

      vista.shadowRoot.querySelector("[data-accion='retroceder']").click();
      await responderDialogo(true);
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

    it("app-consulta guarda notas y no completa el paso sin un diagnóstico CIE-10 del catálogo", async () => {
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

  describe("App shell y PWA", () => {
    it("pwa-instalar muestra el botón cuando el navegador ofrece instalar", async () => {
      const instalar = montar("pwa-instalar");
      await esperar(() => instalar.shadowRoot.querySelector(".tarjeta"));
      const tarjeta = instalar.shadowRoot.querySelector(".tarjeta");
      expect(tarjeta.hidden).to.be.true;
      const prompt = sinon.spy();
      const evento = new dom.window.Event("beforeinstallprompt");
      evento.prompt = prompt;
      evento.userChoice = Promise.resolve({ outcome: "accepted" });
      dom.window.dispatchEvent(evento);
      await esperar(() => !tarjeta.hidden);
      instalar.shadowRoot.querySelector("#instalar").click();
      await esperar(() => prompt.called);
      expect(tarjeta.hidden).to.be.true;
    });

    it("pwa-instalar se puede cerrar con «Ahora no» sin abrir la instalación", async () => {
      const instalar = montar("pwa-instalar");
      await esperar(() => instalar.shadowRoot.querySelector(".tarjeta"));
      const tarjeta = instalar.shadowRoot.querySelector(".tarjeta");
      const evento = new dom.window.Event("beforeinstallprompt");
      evento.prompt = sinon.spy();
      dom.window.dispatchEvent(evento);
      await esperar(() => !tarjeta.hidden);
      instalar.shadowRoot.querySelector('[aria-label="Ahora no"]').click();
      expect(tarjeta.hidden).to.be.true;
      expect(evento.prompt.called).to.be.false;
    });

    it("app-shell salta al contenido principal sin cambiar la ruta", async () => {
      dom.window.location.hash = "#/buscar";
      const shell = montar("app-shell");
      await esperar(() => shell.shadowRoot.querySelector("app-busqueda"));
      shell.shadowRoot.querySelector(".saltar").click();
      expect(dom.window.location.hash).to.equal("#/buscar");
      expect(shell.shadowRoot.activeElement?.id).to.equal("vista");
    });

    it("app-shell muestra la invitación a instalar abajo a la derecha, fuera del encabezado", async () => {
      const shell = montar("app-shell");
      await esperar(() => shell.shadowRoot.querySelector("pwa-instalar"));
      const instalar = shell.shadowRoot.querySelector("pwa-instalar");
      expect(shell.shadowRoot.querySelector("header pwa-instalar")).to.be.null;
      expect(instalar.estilos).to.match(/position: fixed; right: 1rem; bottom: 0\.75rem/);
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

    it("app-shell avisa que el servidor no responde aunque haya red, y quita el aviso cuando vuelve", async () => {
      const shell = montar("app-shell");
      await esperar(() => shell.shadowRoot.querySelector("#estado-conexion"));
      fetch.rejects(new TypeError("Failed to fetch"));
      await listar("roles").catch(() => {});
      const aviso = shell.shadowRoot.querySelector("#estado-conexion");
      expect(aviso.hidden).to.be.false;
      expect(aviso.textContent).to.contain("El servidor de GalenoSV no responde");
      sinon.restore();
      stubApi(datosBase());
      await listar("roles");
      expect(shell.shadowRoot.querySelector("#estado-conexion").hidden).to.be.true;
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
  });
});
