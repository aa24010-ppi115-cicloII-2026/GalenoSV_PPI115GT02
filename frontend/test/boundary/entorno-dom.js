/**
 * Navegador simulado (jsdom) que comparten las pruebas de los Web Components.
 *
 * La ventana se crea una sola vez: los componentes se registran en su `customElements` y heredan
 * de su `HTMLElement`, así que no pueden volver a definirse en otra ventana. Cada archivo de
 * pruebas llama a `usarDom()` dentro de su `describe` para exponer esa ventana mientras corre
 * y dejar todo limpio entre pruebas.
 */
import { JSDOM } from "jsdom";
import sinon from "sinon";
import { datosBase, stubApi } from "../ayudas.js";
import { limpiarCache } from "../../js/control/repositorio.js";

export const SESION_ENFERMERO = {
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

/** Ventana simulada y datos de la prueba en curso (se renuevan antes de cada prueba). */
export let dom;
export let datos;
export let Vista;
export let definirElemento;
export let configurarEstilos;
export let documentoImprimible;

const GLOBALES = ["window", "document", "Node", "HTMLElement", "CustomEvent", "Event", "customElements", "sessionStorage", "DOMParser"];

let carga = null;

/** Importa los componentes una vez, con la ventana ya expuesta como global. */
function cargarComponentes() {
  carga ??= (async () => {
    ({ Vista, definirElemento, configurarEstilos } = await import("../../js/boundary/base.js"));
    configurarEstilos({
      base: new URL("../../css/componentes/base.css", import.meta.url).href,
      directorio: new URL("../../css/componentes/", import.meta.url).href,
    });
    await import("../../js/boundary/componentes/ui-toast.js");
    await import("../../js/boundary/componentes/ui-autocompletar.js");
    ({ documentoImprimible } = await import("../../js/boundary/componentes/ticket-cita.js"));
    await import("../../js/boundary/componentes/cita-card.js");
    await import("../../js/boundary/componentes/app-login.js");
    await import("../../js/boundary/componentes/app-busqueda.js");
    await import("../../js/boundary/componentes/app-dashboard.js");
    await import("../../js/boundary/componentes/app-consulta.js");
    await import("../../js/boundary/componentes/app-shell.js");
    await import("../../js/boundary/componentes/pwa-instalar.js");
  })();
  return carga;
}

/** Registra los hooks que preparan y limpian la ventana para las pruebas del `describe` actual. */
export function usarDom() {
  let abortControllerDeNode;

  before(async () => {
    dom ??= new JSDOM("<!DOCTYPE html><body></body>", {
      url: "http://localhost:3000/",
      pretendToBeVisual: true,
    });
    for (const nombre of GLOBALES) globalThis[nombre] = dom.window[nombre];
    // Los eventos de jsdom solo aceptan señales de su propia ventana, como pasa en un navegador real.
    abortControllerDeNode = globalThis.AbortController;
    globalThis.AbortController = dom.window.AbortController;
    await cargarComponentes();
  });

  after(() => {
    globalThis.AbortController = abortControllerDeNode;
    for (const nombre of GLOBALES) delete globalThis[nombre];
  });

  beforeEach(() => {
    dom.window.history.replaceState({}, "", "/");
    document.body.innerHTML = "";
    sessionStorage.clear();
    limpiarCache();
    datos = datosBase();
    stubApi(datos);
  });

  afterEach(() => {
    sinon.restore();
    Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
    delete globalThis.localStorage;
  });
}

export function montar(etiqueta, propiedades = {}) {
  const elemento = document.createElement(etiqueta);
  for (const [clave, valor] of Object.entries(propiedades)) elemento[clave] = valor;
  document.body.append(elemento);
  return elemento;
}

/** Simula la falta de red con una cola de cambios real en localStorage. */
export function sinRed() {
  globalThis.localStorage = dom.window.localStorage;
  localStorage.clear();
  Object.defineProperty(globalThis.navigator, "onLine", { value: false, configurable: true });
}

export function conRed() {
  Object.defineProperty(globalThis.navigator, "onLine", { value: true, configurable: true });
}
