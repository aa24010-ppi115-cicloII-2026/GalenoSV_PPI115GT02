import { Vista, definirElemento } from "../base.js";
import { html } from "../../comun/texto.js";

const ESTADOS = ["completado", "actual", "pendiente"];

/**
 * Línea de tiempo presentacional, independiente del modelo de datos de la aplicación.
 *   linea.pasos = [{ nombre, estado, etiqueta, resumen, detalles: [{ etiqueta, valor }] }]
 */
export default class LineaPasos extends Vista {
  #pasos = [];

  set pasos(lista) {
    this.#pasos = lista ?? [];
    if (this.isConnected) this.pintar();
  }

  get pasos() {
    return this.#pasos;
  }

  connectedCallback() {
    this.pintar();
  }

  pintar() {
    const pasos = this.#pasos.map((item, indice) => this.pintarPaso(item, indice));
    this.render(html`
      <ol class="pasos" aria-label="Pasos del procedimiento">
        ${pasos.length ? pasos : html`<li class="aviso">Sin pasos definidos.</li>`}
      </ol>
    `);
  }

  /** Lo que se registró en un paso: quién, rol, cuándo y sus notas. Solo existe si el paso tiene registro. */
  pintarRegistro(item) {
    if (!item.detalles?.length) return "";
    return html`
      <details class="registro">
        <summary>${item.textoDetalles ?? "Ver detalles"}</summary>
        <dl>${item.detalles.map(({ etiqueta, valor }) => html`<dt>${etiqueta}</dt><dd>${valor}</dd>`)}</dl>
      </details>
    `;
  }

  /** Un paso dentro del procedimiento: número, nombre, estado, quién y cuándo, y su registro. */
  pintarPaso(item, indice) {
    const estado = ESTADOS.includes(item.estado) ? item.estado : "pendiente";
    return html`
      <li class="paso ${estado}" part="paso ${estado}">
        <div class="fila">
          <span class="num" part="numero" aria-hidden="true">${indice + 1}</span>
          <span class="nombre">${item.nombre}</span>
          <span class="quien">${[item.etiqueta, item.resumen].filter(Boolean).join(" · ")}</span>
        </div>
        ${this.pintarRegistro(item)}
      </li>`;
  }
}

definirElemento("linea-pasos", LineaPasos);
