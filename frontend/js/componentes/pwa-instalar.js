import { Vista } from "../base.js";

/**
 * Boton de instalacion de la PWA. Escucha el evento beforeinstallprompt
 * y muestra la accion unicamente cuando el navegador ofrece instalarla.
 */
export class PwaInstalar extends Vista {
  get estilos() {
    return `
      :host { position: fixed; right: 1rem; bottom: 0.75rem; z-index: 40; display: block; }
      button {
        border: none;
        border-radius: 999px;
        padding: 0.55rem 1rem;
        background: var(--mate, #2c4a63);
        color: #fff;
        font: inherit;
        font-size: 0.82rem;
        cursor: pointer;
        box-shadow: 0 10px 24px rgba(20, 32, 43, 0.25);
      }
      button[hidden] { display: none; }
    `;
  }

  connectedCallback() {
    this.render(`<button type="button" hidden>Instalar aplicación</button>`);
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
