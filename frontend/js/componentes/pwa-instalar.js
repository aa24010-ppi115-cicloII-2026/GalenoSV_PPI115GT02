import { Vista, definirElemento } from "../base.js";
import { escapar } from "../texto.js";

/**
<<<<<<< HEAD
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
=======
 * Instalación de la PWA, discreta: un botón redondo de descarga abajo a la derecha.
 * Al pulsarlo se despliega la tarjeta con «Instalar»; «Ahora no» la vuelve a plegar en el botón.
 * Solo aparece cuando el navegador ofrece instalarla.
 */
export class PwaInstalar extends Vista {

  connectedCallback() {
    if (!this._renderizado) {
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
        <div class="texto"><strong>Instale ${escapar(this.getAttribute("nombre") ?? "GalenoSV")}</strong><span>${escapar(this.getAttribute("descripcion") ?? "Acceso directo y uso sin conexión")}</span></div>
        <button id="instalar" type="button">Instalar</button>
        <button id="cerrar" type="button" aria-label="Ahora no" title="Ahora no">✕</button>
      </div>
      `);
      this._renderizado = true;
      const tarjeta = this.q(".tarjeta");
      const abrir = this.q("#abrir");
      // plegada: solo el botón; desplegada: la tarjeta; oculta: el navegador no ofrece instalar.
      const mostrar = (estado) => {
        abrir.hidden = estado !== "plegada";
        tarjeta.hidden = estado !== "desplegada";
        abrir.setAttribute("aria-expanded", String(estado === "desplegada"));
      };
>>>>>>> v2modificada
      this._evento = null;
      this._alOfrecerInstalacion = (evento) => {
        evento.preventDefault();
        this._evento = evento;
        if (tarjeta.hidden) mostrar("plegada");
      };
      this._alInstalar = () => {
        this._evento = null;
        mostrar("oculta");
      };
      abrir.addEventListener("click", () => {
        mostrar("desplegada");
        this.q("#instalar").focus();
      });
      this.q("#cerrar").addEventListener("click", () => {
        mostrar("plegada");
        abrir.focus();
      });
      const instalar = this.q("#instalar");
      instalar.addEventListener("click", async () => {
        if (!this._evento) return;
        const evento = this._evento;
        this._evento = null;
        instalar.disabled = true;
        try {
          await evento.prompt();
          await evento.userChoice;
        } catch {
          // El navegador puede rechazar el prompt si cambió el estado de instalación.
        } finally {
          instalar.disabled = false;
          mostrar("oculta");
        }
      });
    }
    window.addEventListener("beforeinstallprompt", this._alOfrecerInstalacion);
    window.addEventListener("appinstalled", this._alInstalar);
  }

  disconnectedCallback() {
    window.removeEventListener("beforeinstallprompt", this._alOfrecerInstalacion);
    window.removeEventListener("appinstalled", this._alInstalar);
  }
}

definirElemento("pwa-instalar", PwaInstalar);
