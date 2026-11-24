import { Vista, definirElemento } from "../base.js";

/**
 * Instalación de la PWA, discreta: un botón redondo de descarga abajo a la derecha.
 * Al pulsarlo se despliega la tarjeta con «Instalar»; «Ahora no» la vuelve a plegar en el botón.
 * Solo aparece cuando el navegador ofrece instalarla.
 *   <pwa-instalar nombre="GalenoSV" descripcion="Acceso directo y uso sin conexión"></pwa-instalar>
 */
export default class PwaInstalar extends Vista {
  static plantilla = `
    <button id="abrir" part="boton-abrir" type="button" aria-label="Instalar la aplicación" title="Instalar la aplicación"
      aria-expanded="false" aria-controls="tarjeta" hidden>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path class="flecha" d="M12 4v11m0 0-4-4m4 4 4-4"/>
        <path d="M5 19h14"/>
      </svg>
    </button>
    <div class="tarjeta" part="tarjeta" id="tarjeta" role="region" aria-label="Instalar la aplicación" hidden>
      <div class="icono" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 4v11m0 0-4-4m4 4 4-4M5 19h14"/>
        </svg>
      </div>
      <div class="texto"><strong></strong><span></span></div>
      <button id="instalar" part="boton-instalar" type="button">Instalar</button>
      <button id="cerrar" type="button" aria-label="Ahora no" title="Ahora no">✕</button>
    </div>
  `;

  static observedAttributes = ["nombre", "descripcion"];

  /** Evento beforeinstallprompt guardado hasta que la persona pulse «Instalar». */
  #evento = null;

  connectedCallback() {
    if (!this.q("#tarjeta")) this.#montar();
    this.escuchar(window, "beforeinstallprompt", (evento) => {
      evento.preventDefault();
      this.#evento = evento;
      if (this.q("#tarjeta").hidden) this.#mostrar("plegada");
    });
    this.escuchar(window, "appinstalled", () => {
      this.#evento = null;
      this.#mostrar("oculta");
    });
  }

  attributeChangedCallback() {
    if (this.q("#tarjeta")) this.#pintarTextos();
  }

  #pintarTextos() {
    this.q(".texto strong").textContent = `Instale ${this.getAttribute("nombre") ?? "GalenoSV"}`;
    this.q(".texto span").textContent = this.getAttribute("descripcion") ?? "Acceso directo y uso sin conexión";
  }

  #montar() {
    this.montarPlantilla();
    this.#pintarTextos();
    const abrir = this.q("#abrir");
    const instalar = this.q("#instalar");
    abrir.addEventListener("click", () => {
      this.#mostrar("desplegada");
      instalar.focus();
    });
    this.q("#cerrar").addEventListener("click", () => {
      this.#mostrar("plegada");
      abrir.focus();
    });
    instalar.addEventListener("click", () => {
      if (!this.#evento) return;
      const evento = this.#evento;
      this.#evento = null;
      instalar.disabled = true;
      new Promise((resolve) => resolve(evento.prompt()))
        .then(() => evento.userChoice)
        // El navegador puede rechazar el prompt si cambió el estado de instalación.
        .catch(() => null)
        .finally(() => {
          instalar.disabled = false;
          this.#mostrar("oculta");
        });
    });
  }

  /** plegada: solo el botón; desplegada: la tarjeta; oculta: el navegador no ofrece instalar. */
  #mostrar(estado) {
    const abrir = this.q("#abrir");
    abrir.hidden = estado !== "plegada";
    this.q("#tarjeta").hidden = estado !== "desplegada";
    abrir.setAttribute("aria-expanded", String(estado === "desplegada"));
  }
}

definirElemento("pwa-instalar", PwaInstalar);
