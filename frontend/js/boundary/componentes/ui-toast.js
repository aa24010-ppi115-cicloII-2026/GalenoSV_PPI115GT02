import { Vista, definirElemento } from "../base.js";

const TIPOS = ["info", "ok", "error"];

/** Avisos breves: muestra los eventos `notificar` que emiten los componentes. */
export default class UiToast extends Vista {
  static plantilla = `<div id="lista" role="status" aria-live="polite"></div>`;

  connectedCallback() {
    if (!this.q("#lista")) this.montarPlantilla();
    this.escuchar(document, "notificar", (evento) => {
      if (evento.detail?.mensaje) this.mostrar(evento.detail.mensaje, { tipo: evento.detail.tipo });
    });
  }

  mostrar(mensaje, { tipo = "info", duracion = this.duracion } = {}) {
    const lista = this.q("#lista");
    if (!lista || !mensaje) return null;
    const aviso = document.createElement("div");
    const tipoValido = TIPOS.includes(tipo) ? tipo : "info";
    aviso.className = `aviso ${tipoValido}`;
    aviso.setAttribute("part", `aviso ${tipoValido}`);
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
