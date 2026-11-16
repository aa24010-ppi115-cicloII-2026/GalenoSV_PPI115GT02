import { Vista } from "../base.js";

/**
 * Botón de instalación de la PWA, ubicado en el encabezado (no flota sobre el contenido).
 * Escucha beforeinstallprompt y solo se muestra cuando el navegador ofrece instalarla.
 */
export class PwaInstalar extends Vista {
  get estilos() {
    return `
      :host { display: contents; }
      button {
        display: inline-flex; align-items: center; gap: 0.4rem; min-height: 2.25rem;
        padding: 0.35rem 0.8rem; border-radius: 8px; cursor: pointer;
        background: transparent; color: #d3e0ea; border: 1px solid rgba(255, 255, 255, 0.28);
        font: inherit; font-size: 0.8rem; font-weight: 500; box-shadow: none;
      }
      button:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }
      button:focus-visible { outline: none; box-shadow: 0 0 0 2px #9fe3d6; }
      svg { width: 16px; height: 16px; flex: none; }
      button[hidden] { display: none; }
    `;
  }

  connectedCallback() {
    this.render(`
      <button type="button" hidden>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 4v11m0 0-4-4m4 4 4-4M5 19h14"/>
        </svg>
        Instalar aplicación
      </button>
    `);
    this._evento = null;
    window.addEventListener("beforeinstallprompt", (evento) => {
      evento.preventDefault();
      this._evento = evento;
      this.q("button").hidden = false;
    });
    window.addEventListener("appinstalled", () => {
      this._evento = null;
      this.q("button").hidden = true;
    });
    this.q("button").addEventListener("click", async () => {
      if (!this._evento) return;
      this._evento.prompt();
      await this._evento.userChoice;
      this._evento = null;
      this.q("button").hidden = true;
    });
  }
}

customElements.define("pwa-instalar", PwaInstalar);
