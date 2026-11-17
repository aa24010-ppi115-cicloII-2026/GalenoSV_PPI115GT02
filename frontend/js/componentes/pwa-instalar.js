import { Vista } from "../base.js";

/**
 * Invitación a instalar la PWA: tarjeta abajo a la derecha, por debajo de los avisos.
 * Solo aparece cuando el navegador ofrece instalarla y se puede cerrar para no tapar el contenido.
 */
export class PwaInstalar extends Vista {
  get estilos() {
    return `
      .tarjeta {
        position: fixed; right: 1rem; bottom: 0.75rem; z-index: 40;
        display: flex; align-items: center; gap: 0.75rem;
        padding: 0.55rem 0.55rem 0.55rem 0.8rem; border-radius: 12px;
        background: var(--superficie, #fff); border: 1px solid var(--borde, #d6dee7);
        box-shadow: 0 10px 24px rgba(20, 32, 43, 0.18);
      }
      .tarjeta[hidden] { display: none; }
      .icono {
        display: grid; place-items: center; width: 2rem; height: 2rem; flex: none;
        border-radius: 8px; background: var(--acento-suave, #e3f3ef); color: var(--acento-fuerte, #0b6155);
      }
      svg { width: 18px; height: 18px; }
      .texto { display: grid; line-height: 1.25; }
      strong { font-size: 0.85rem; color: var(--texto, #14202b); }
      span { font-size: 0.74rem; color: var(--texto-tenue, #5f7387); }
      button { font: inherit; cursor: pointer; border-radius: 8px; }
      #instalar {
        min-height: 2.25rem; padding: 0.35rem 0.9rem; border: none;
        background: var(--mate, #2c4a63); color: #fff; font-size: 0.82rem; font-weight: 600;
      }
      #instalar:hover { background: var(--mate-fuerte, #1f3a50); }
      #cerrar {
        width: 2rem; height: 2rem; border: none; background: transparent;
        color: var(--texto-tenue, #5f7387); font-size: 1.1rem; line-height: 1;
      }
      #cerrar:hover { background: var(--superficie-suave, #f1f4f7); color: var(--texto, #14202b); }
      button:focus-visible { outline: 2px solid var(--acento, #0f7b6c); outline-offset: 2px; }
      @media (max-width: 520px) { .tarjeta { left: 0.75rem; right: 0.75rem; bottom: 0.75rem; } .texto { flex: 1; } span { display: none; } }
    `;
  }

  connectedCallback() {
    this.render(`
      <div class="tarjeta" role="region" aria-label="Instalar la aplicación" hidden>
        <div class="icono" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 4v11m0 0-4-4m4 4 4-4M5 19h14"/>
          </svg>
        </div>
        <div class="texto"><strong>Instale GalenoSV</strong><span>Acceso directo y uso sin conexión</span></div>
        <button id="instalar" type="button">Instalar</button>
        <button id="cerrar" type="button" aria-label="Ahora no" title="Ahora no">✕</button>
      </div>
    `);
    const tarjeta = this.q(".tarjeta");
    this._evento = null;
    window.addEventListener("beforeinstallprompt", (evento) => {
      evento.preventDefault();
      this._evento = evento;
      tarjeta.hidden = false;
    });
    window.addEventListener("appinstalled", () => {
      this._evento = null;
      tarjeta.hidden = true;
    });
    this.q("#cerrar").addEventListener("click", () => {
      tarjeta.hidden = true;
    });
    this.q("#instalar").addEventListener("click", async () => {
      if (!this._evento) return;
      this._evento.prompt();
      await this._evento.userChoice;
      this._evento = null;
      tarjeta.hidden = true;
    });
  }
}

customElements.define("pwa-instalar", PwaInstalar);
