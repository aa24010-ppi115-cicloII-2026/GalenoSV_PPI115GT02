/** Plantillas estáticas ya analizadas, una por clase de componente. */
const plantillasPorClase = new WeakMap();

/** Hojas de estilo construidas una sola vez por archivo: href → { hoja, lista }. */
const hojasCompartidas = new Map();

/** El navegador permite que varios Shadow DOM compartan una misma hoja (adoptedStyleSheets). */
function comparteHojas() {
  return typeof ShadowRoot !== "undefined" && "adoptedStyleSheets" in ShadowRoot.prototype;
}

/**
 * Hoja construida a partir de un archivo CSS. Se descarga una vez y la adoptan todas las
 * instancias; `lista` se cumple cuando ya tiene sus reglas (o cuando falló, para no bloquear).
 */
function hojaCompartida(href) {
  if (!hojasCompartidas.has(href)) {
    const hoja = new CSSStyleSheet();
    const lista = fetch(href)
      .then((respuesta) => (respuesta.ok ? respuesta.text() : Promise.reject(new Error(`HTTP ${respuesta.status}`))))
      .then((css) => hoja.replace(css))
      .catch((error) => console.error(`No se pudo cargar la hoja de estilo ${href}:`, error));
    hojasCompartidas.set(href, { hoja, lista });
  }
  return hojasCompartidas.get(href);
}

/** Hojas que usa un componente por omisión: la base común y la que lleva su nombre. */
function rutasDeEstilo(nombre) {
  return [
    ...Vista.hojasDeEstiloBase,
    ...(Vista.directorioEstilos ? [new URL(`${nombre}.css`, Vista.directorioEstilos).href] : []),
  ];
}

/**
 * Descarga las hojas de los componentes indicados antes de registrarlos, para que se pinten
 * con estilo desde el primer momento. Sin hojas compartidas no hace nada: se usan <link>.
 */
export function precargarEstilos(nombres) {
  if (!comparteHojas()) return Promise.resolve();
  const rutas = new Set(nombres.flatMap(rutasDeEstilo));
  return Promise.all([...rutas].map((href) => hojaCompartida(href).lista)).then(() => undefined);
}

/**
 * Convierte marcado en nodos listos para insertar. Es el único punto donde se analiza HTML:
 * los datos deben llegar escapados, idealmente armados con la plantilla `html` de texto.js.
 */
export function fragmento(contenido) {
  const plantilla = document.createElement("template");
  plantilla.innerHTML = String(contenido);
  return plantilla.content;
}

/**
 * Selector que vuelve a encontrar un elemento tras repintar: por su id o, si no tiene, por sus
 * atributos data-* (por ejemplo el botón de una acción de un procedimiento).
 */
function selectorEquivalente(elemento) {
  if (!elemento) return null;
  const valor = (texto) => `"${texto.replace(/["\\]/g, "\\$&")}"`;
  if (elemento.id) return `${elemento.localName}[id=${valor(elemento.id)}]`;
  const datos = [...elemento.attributes].filter(({ name }) => name.startsWith("data-"));
  if (datos.length === 0) return null;
  return elemento.localName + datos.map(({ name, value }) => `[${name}=${valor(value)}]`).join("");
}

/**
 * Clase padre de los Web Components: Shadow DOM abierto, hojas de estilo compartidas,
 * plantilla clonada, escuchas que se liberan al desconectarse y avisos con `notificar`.
 */
export class Vista extends HTMLElement {
  static hojasDeEstilo;
  static hojasDeEstiloBase = [];
  static directorioEstilos = null;
  /** Marcado fijo del componente (HTML sin datos). Se analiza una sola vez y cada instancia lo clona. */
  static plantilla = "";
  /** true en componentes que envuelven un campo: el foco, los clics y el <label> pasan al control interno. */
  static delegarFoco = false;

  #enlaces = null;
  #adoptadas = false;
  #controlador = null;

  constructor() {
    super();
    this.attachShadow({ mode: "open", delegatesFocus: this.constructor.delegarFoco });
  }

  disconnectedCallback() {
    this.#controlador?.abort();
    this.#controlador = null;
  }

  /**
   * Estilos del componente. Con hojas compartidas se adoptan una vez y no ocupan nodos; si no,
   * se crean <link> una vez por instancia y se conservan en cada pintado.
   */
  #hojas() {
    const hojas = this.constructor.hojasDeEstilo ?? rutasDeEstilo(this.constructor.elementName ?? this.localName);
    if (comparteHojas()) {
      if (!this.#adoptadas) {
        this.shadowRoot.adoptedStyleSheets = hojas.map((href) => hojaCompartida(href).hoja);
        this.#adoptadas = true;
      }
      return [];
    }
    if (this.#enlaces) return this.#enlaces;
    this.#enlaces = hojas.map((href) => {
      const enlace = document.createElement("link");
      enlace.rel = "stylesheet";
      enlace.href = href;
      return enlace;
    });
    return this.#enlaces;
  }

  /**
   * Reemplaza el contenido del Shadow DOM conservando las hojas de estilo. Si el foco estaba
   * dentro, vuelve al elemento equivalente del contenido nuevo (cuando solo hay uno).
   */
  render(contenido) {
    const selector = selectorEquivalente(this.shadowRoot.activeElement);
    this.shadowRoot.replaceChildren(...this.#hojas(), fragmento(contenido));
    const candidatos = selector ? this.shadowRoot.querySelectorAll(selector) : [];
    if (candidatos.length === 1) candidatos[0].focus({ preventScroll: true });
  }

  /** Pinta la plantilla estática de la clase clonando el <template> analizado una sola vez. */
  montarPlantilla() {
    let plantilla = plantillasPorClase.get(this.constructor);
    if (!plantilla) {
      plantilla = document.createElement("template");
      plantilla.innerHTML = this.constructor.plantilla;
      plantillasPorClase.set(this.constructor, plantilla);
    }
    this.shadowRoot.replaceChildren(...this.#hojas(), plantilla.content.cloneNode(true));
  }

  /** Escucha un evento mientras el componente esté conectado; se quita sola al desconectarlo. */
  escuchar(objetivo, tipo, manejador, opciones = {}) {
    this.#controlador ??= new AbortController();
    objetivo.addEventListener(tipo, manejador, { ...opciones, signal: this.#controlador.signal });
  }

  q(selector) {
    return this.shadowRoot.querySelector(selector);
  }

  qa(selector) {
    return [...this.shadowRoot.querySelectorAll(selector)];
  }

  notificar(mensaje, tipo = "info") {
    this.dispatchEvent(
      new CustomEvent("notificar", { bubbles: true, composed: true, detail: { mensaje, tipo } })
    );
  }

  navegar(ruta) {
    this.dispatchEvent(new CustomEvent("navegar", { bubbles: true, composed: true, detail: ruta }));
  }
}

/** Elemento con el foco real, aunque esté dentro de otro Shadow DOM. */
function focoPrevio() {
  let previo = document.activeElement;
  while (previo?.shadowRoot?.activeElement) previo = previo.shadowRoot.activeElement;
  return previo;
}

/**
 * Dónde están las hojas de estilo: la hoja base común y la carpeta con una hoja por componente.
 * @param {{base?: string, directorio?: string}} [rutas] rutas relativas a este módulo
 */
export function configurarEstilos({ base, directorio } = {}) {
  Vista.hojasDeEstiloBase = base ? [new URL(base, import.meta.url).href] : [];
  Vista.directorioEstilos = directorio ? new URL(directorio, import.meta.url).href : null;
}

/** Mantiene el foco dentro de un diálogo: Tab y Shift+Tab recorren sus controles. */
function moverFocoConTab(evento, controles, actual) {
  if (controles.length === 0) return;
  evento.preventDefault();
  const indice = controles.indexOf(actual);
  const paso = evento.shiftKey ? -1 : 1;
  controles[(indice + paso + controles.length) % controles.length].focus();
}

/** Registra el elemento una sola vez y permite importar la biblioteca desde varios módulos. */
export function definirElemento(nombre, constructor) {
  constructor.elementName ??= nombre;
  if (!customElements.get(nombre)) customElements.define(nombre, constructor);
  return customElements.get(nombre);
}

/**
 * Convierte un componente en diálogo modal: Escape lo cierra y Tab no sale de sus botones.
 * @param {Vista} elemento diálogo ya conectado
 * @param {() => void} alCerrar se llama al pulsar Escape
 * @returns {() => void} quita las escuchas y devuelve el foco a donde estaba antes de abrir
 */
export function modalConFoco(elemento, alCerrar) {
  const previo = focoPrevio();
  const tecla = (evento) => {
    if (evento.key === "Escape") alCerrar();
    if (evento.key === "Tab") moverFocoConTab(evento, elemento.qa("button"), elemento.shadowRoot.activeElement);
  };
  elemento.shadowRoot.addEventListener("keydown", tecla);
  return () => {
    elemento.shadowRoot.removeEventListener("keydown", tecla);
    previo?.focus?.();
  };
}
