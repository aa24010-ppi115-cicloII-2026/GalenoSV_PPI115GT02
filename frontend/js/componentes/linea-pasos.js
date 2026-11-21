import { Vista, definirElemento } from "../base.js";
import { escapar } from "../texto.js";

/**
 * Línea de tiempo presentacional, independiente del modelo de datos de la aplicación.
 *   linea.pasos = [{ nombre, estado, etiqueta, resumen, detalles: [{ etiqueta, valor }] }]
 */
export class LineaPasos extends Vista {

  set pasos(lista) {
    this._pasos = lista ?? [];
    if (this.isConnected) this.pintar();
  }

  get pasos() {
    return this._pasos ?? [];
  }

  connectedCallback() {
    this.pintar();
  }

  pintar() {
    this.render(`
      <ol class="pasos" aria-label="Pasos del procedimiento">
        ${this.pasos.map((item, indice) => this.pintarPaso(item, indice)).join("") || '<li class="aviso">Sin pasos definidos.</li>'}
      </ol>
    `);
  }

  /** Lo que se registró en un paso: quién, rol, cuándo y sus notas. Solo existe si el paso tiene registro. */
  pintarRegistro(item) {
    if (!item.detalles?.length) return "";
    return `
      <details class="registro">
        <summary>${escapar(item.textoDetalles ?? "Ver detalles")}</summary>
        <dl>${item.detalles.map(({ etiqueta, valor }) => `<dt>${escapar(etiqueta)}</dt><dd>${escapar(valor)}</dd>`).join("")}</dl>
      </details>
    `;
  }

  /** Un paso dentro del procedimiento: número, nombre, estado, quién y cuándo, y su registro. */
  pintarPaso(item, indice) {
    const estado = ["completado", "actual", "pendiente"].includes(item.estado) ? item.estado : "pendiente";
    return `
      <li class="paso ${escapar(estado)}">
        <div class="fila">
          <span class="num" aria-hidden="true">${indice + 1}</span>
          <span class="nombre">${escapar(item.nombre)}</span>
          <span class="quien">${escapar([item.etiqueta, item.resumen].filter(Boolean).join(" · "))}</span>
        </div>
        ${this.pintarRegistro(item)}
      </li>`;
  }

}

definirElemento("linea-pasos", LineaPasos);
