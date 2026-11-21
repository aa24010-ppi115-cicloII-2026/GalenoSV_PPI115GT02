import { Vista, definirElemento } from "../base.js";

export class UiToast extends Vista {

  connectedCallback() {
    if (!this._renderizado) {
      this.render(`<div id="lista" role="status" aria-live="polite"></div>`);
      this._renderizado = true;
      this._escucha = (evento) => {
        if (evento.detail?.mensaje) this.mostrar(evento.detail.mensaje, { tipo: evento.detail.tipo });
      };
    }
    document.addEventListener("notificar", this._escucha);
  }

  disconnectedCallback() {
    document.removeEventListener("notificar", this._escucha);
  }

  mostrar(mensaje, { tipo = "info", duracion = this.duracion } = {}) {
    const lista = this.q("#lista");
    if (!lista || !mensaje) return null;
    const aviso = document.createElement("div");
    const tiposPermitidos = ["info", "ok", "error"];
    aviso.className = `aviso ${tiposPermitidos.includes(tipo) ? tipo : "info"}`;
    aviso.textContent = String(mensaje);
    lista.append(aviso);
    while (lista.children.length > this.maximo) lista.firstElementChild.remove();
    if (Number.isFinite(duracion) && duracion > 0) setTimeout(() => aviso.remove(), duracion);
    return aviso;
  }

  get duracion() {
    const valor = Number(this.getAttribute("duracion"));
    return Number.isFinite(valor) && valor > 0 ? valor : 4000;
  }

  get maximo() {
    const valor = Number(this.getAttribute("maximo"));
    return Number.isInteger(valor) && valor > 0 ? valor : 3;
  }
}

definirElemento("ui-toast", UiToast);
