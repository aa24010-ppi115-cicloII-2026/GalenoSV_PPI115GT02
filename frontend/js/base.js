export class Vista extends HTMLElement {
  static hojasDeEstilo;
  static hojasDeEstiloBase = [];
  static directorioEstilos = null;

  constructor() {
    super();
    this.attachShadow({ mode: "open" });
  }

  render(contenido) {
    const nombreEstilo = this.constructor.elementName ?? this.localName;
    const hojas = this.constructor.hojasDeEstilo ?? [
      ...Vista.hojasDeEstiloBase,
      ...(Vista.directorioEstilos ? [new URL(`${nombreEstilo}.css`, Vista.directorioEstilos).href] : []),
    ];
    const enlaces = hojas.map((href) => {
      const enlace = document.createElement("link");
      enlace.rel = "stylesheet";
      enlace.href = href;
      return enlace;
    });
    this.shadowRoot.replaceChildren(...enlaces);
    const plantilla = document.createElement("template");
    plantilla.innerHTML = contenido;
    this.shadowRoot.append(plantilla.content);
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
