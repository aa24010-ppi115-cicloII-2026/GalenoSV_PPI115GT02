import { Vista } from "../base.js";

export class UiToast extends Vista {
  get estilos() {
    return `
      #lista {
        position: fixed;
        right: 1rem;
        bottom: 3.6rem;
        display: grid;
        gap: 0.5rem;
        z-index: 50;
        max-width: min(360px, 90vw);
      }
      .aviso {
        padding: 0.7rem 1rem;
        border-radius: 12px;
        color: #fff;
        background: #0f7b6c;
        box-shadow: 0 10px 24px rgba(20, 32, 43, 0.28);
        font-size: 0.88rem;
        animation: aparecer 0.18s ease-out;
      }
      .aviso.error { background: #b42318; }
      .aviso.info { background: #2c4a63; }
      @keyframes aparecer {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
      }
    `;
  }

  connectedCallback() {
    if (this._iniciado) return;
    this._iniciado = true;
    this.render(`<div id="lista" role="status" aria-live="polite"></div>`);
    this._escucha = (evento) => this.agregar(evento.detail.mensaje, evento.detail.tipo);
    document.addEventListener("notificar", this._escucha);
  }

  disconnectedCallback() {
    document.removeEventListener("notificar", this._escucha);
  }

  agregar(mensaje, tipo = "info") {
    const lista = this.q("#lista");
    if (!lista) return;
    const aviso = document.createElement("div");
    aviso.className = `aviso ${tipo}`;
    aviso.textContent = mensaje;
    lista.append(aviso);
    setTimeout(() => aviso.remove(), 4000);
  }
}

customElements.define("ui-toast", UiToast);
