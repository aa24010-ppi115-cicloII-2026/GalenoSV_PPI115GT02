import { Vista, definirElemento } from "../base.js";
import { escapar } from "../texto.js";

/**
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
