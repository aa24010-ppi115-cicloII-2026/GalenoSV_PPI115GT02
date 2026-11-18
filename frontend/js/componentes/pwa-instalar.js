import { Vista } from "../base.js";

/**
 * Instalación de la PWA, discreta: un botón redondo de descarga abajo a la derecha.
 * Al pulsarlo se despliega la tarjeta con «Instalar»; «Ahora no» la vuelve a plegar en el botón.
 * Solo aparece cuando el navegador ofrece instalarla.
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
      .tarjeta[hidden], #abrir[hidden] { display: none; }
      #abrir {
        position: fixed; right: 1rem; bottom: 0.75rem; z-index: 40;
        display: grid; place-items: center; width: 2.9rem; height: 2.9rem; padding: 0;
        border-radius: 999px; border: none; color: #fff;
        background: linear-gradient(145deg, #3d6a91, var(--mate, #2c4a63));
        box-shadow: 0 6px 16px rgba(31, 58, 80, 0.32);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        /* Aparece con un pequeño rebote y luego late tres veces para hacerse notar sin molestar. */
        animation: aparecer 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both,
          latido 2.2s ease-out 0.6s 3;
      }
      #abrir svg { width: 20px; height: 20px; }
      #abrir:hover { transform: translateY(-2px); box-shadow: 0 10px 22px rgba(31, 58, 80, 0.38); }
      #abrir:hover .flecha, #abrir:focus-visible .flecha { animation: bajar 0.9s ease-in-out infinite; }
      .tarjeta:not([hidden]) { animation: desplegar 0.22s ease-out both; transform-origin: bottom right; }
      @keyframes aparecer { from { opacity: 0; transform: scale(0.5); } to { opacity: 1; transform: scale(1); } }
      @keyframes latido {
        0% { box-shadow: 0 6px 16px rgba(31, 58, 80, 0.32), 0 0 0 0 rgba(61, 106, 145, 0.45); }
        70%, 100% { box-shadow: 0 6px 16px rgba(31, 58, 80, 0.32), 0 0 0 14px rgba(61, 106, 145, 0); }
      }
      @keyframes bajar { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(2.5px); } }
      @keyframes desplegar { from { opacity: 0; transform: translateY(6px) scale(0.94); } to { opacity: 1; transform: none; } }
      @media (prefers-reduced-motion: reduce) {
        #abrir, .tarjeta:not([hidden]), #abrir .flecha { animation: none !important; transition: none; }
      }
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
      <button id="abrir" type="button" aria-label="Instalar la aplicación" title="Instalar la aplicación"
        aria-expanded="false" aria-controls="tarjeta" hidden>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path class="flecha" d="M12 4v11m0 0-4-4m4 4 4-4"/>
          <path d="M5 19h14"/>
        </svg>
      </button>
      <div class="tarjeta" id="tarjeta" role="region" aria-label="Instalar la aplicación" hidden>
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
    const abrir = this.q("#abrir");
    // plegada: solo el botón; desplegada: la tarjeta; oculta: el navegador no ofrece instalar.
    const mostrar = (estado) => {
      abrir.hidden = estado !== "plegada";
      tarjeta.hidden = estado !== "desplegada";
      abrir.setAttribute("aria-expanded", String(estado === "desplegada"));
    };
    this._evento = null;
    window.addEventListener("beforeinstallprompt", (evento) => {
      evento.preventDefault();
      this._evento = evento;
      if (tarjeta.hidden) mostrar("plegada");
    });
    window.addEventListener("appinstalled", () => {
      this._evento = null;
      mostrar("oculta");
    });
    abrir.addEventListener("click", () => {
      mostrar("desplegada");
      this.q("#instalar").focus();
    });
    this.q("#cerrar").addEventListener("click", () => {
      mostrar("plegada");
      abrir.focus();
    });
    this.q("#instalar").addEventListener("click", async () => {
      if (!this._evento) return;
      this._evento.prompt();
      await this._evento.userChoice;
      // El aviso del navegador solo se puede usar una vez.
      this._evento = null;
      mostrar("oculta");
    });
  }
}

customElements.define("pwa-instalar", PwaInstalar);
