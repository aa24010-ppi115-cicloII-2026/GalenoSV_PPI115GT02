<<<<<<< HEAD
import { Vista, escapar, fechaHora, fechaCorta } from "../base.js";
import { etiquetaPaso } from "../datos.js";

/**
 * Pasos de un procedimiento en jerarquía: línea de tiempo con estado, quién y cuándo,
 * y «Ver registro» en cada paso que tiene registro. Se usa en las cards (HU1/HU3) y en la consulta (HU4).
 *   linea.pasos = pasosConRegistros(...)
 */
export class LineaPasos extends Vista {
  get estilos() {
    return `
      .pasos { list-style: none; margin: 0; padding: 0 0 0 0.65rem; display: grid; gap: 0.35rem; }
      .paso { position: relative; padding-left: 1.1rem; }
      /* Conector vertical: une los pasos del procedimiento en una línea de tiempo. */
      .paso::before {
        content: ""; position: absolute; left: 0; top: -0.35rem; bottom: -0.35rem; width: 2px; background: var(--borde, #d6dee7);
      }
      .paso:first-child::before { top: 1.1rem; }
      .paso:last-child::before { bottom: auto; height: 1.15rem; }
      .paso:only-child::before { display: none; }
      .paso::after {
        content: ""; position: absolute; left: 0; top: 1.1rem; width: 0.8rem; height: 2px; background: var(--borde, #d6dee7);
      }
      .fila {
        display: flex; align-items: center; gap: 0.55rem; font-size: 0.8rem; min-height: 2.25rem;
        padding: 0.35rem 0.65rem; border-radius: var(--radio, 10px);
        border: 1px solid var(--borde, #d6dee7); color: var(--texto-suave, #4d6378); background: var(--superficie, #fff);
      }
      .num {
        width: 1.3rem; height: 1.3rem; border-radius: 999px; display: inline-grid; place-items: center; flex: none;
        font-size: 0.68rem; font-weight: 700; font-variant-numeric: tabular-nums;
        background: var(--superficie, #fff); color: var(--texto-tenue, #5f7387);
        box-shadow: inset 0 0 0 1.5px var(--borde-fuerte, #b5c3d1);
      }
      .nombre { font-weight: 600; color: var(--texto, #14202b); }
      .quien { margin-left: auto; font-size: 0.74rem; text-align: right; font-variant-numeric: tabular-nums; }
      .paso.pendiente .nombre { color: var(--texto-suave, #4d6378); font-weight: 500; }
      .paso.completado .fila { background: var(--exito-suave, #e8f4ec); border-color: var(--exito-borde, #c3e2cd); }
      .paso.completado .num { background: var(--exito, #1e6b3a); color: #fff; box-shadow: none; }
      .paso.completado::before, .paso.completado::after { background: var(--exito-borde, #c3e2cd); }
      .paso.actual .fila { border-color: var(--mate, #2c4a63); background: var(--mate-suave, #e9eff4); box-shadow: inset 0 0 0 1px var(--mate, #2c4a63); }
      .paso.actual .num { background: var(--mate, #2c4a63); color: #fff; box-shadow: none; }
      .paso.actual .quien { color: var(--mate, #2c4a63); font-weight: 600; }
      details.registro { font-size: 0.8rem; margin: 0.3rem 0 0 0.65rem; }
      details.registro summary {
        cursor: pointer; color: var(--acento, #0f7b6c); font-weight: 600; width: fit-content;
        border-radius: 6px; padding: 0.1rem 0.2rem;
      }
      details.registro summary:hover { color: var(--acento-fuerte, #0b6155); }
      details.registro summary:focus-visible { outline: none; box-shadow: var(--anillo); }
      details.registro dl {
        margin: 0.35rem 0 0.2rem; padding: 0.55rem 0.75rem; display: grid; grid-template-columns: auto 1fr; gap: 0.25rem 0.8rem;
        border-left: 2px solid var(--acento-suave, #e6f3f0); background: var(--superficie-suave, #f7f9fb);
        border-radius: 0 var(--radio, 10px) var(--radio, 10px) 0; color: var(--texto-suave, #4d6378);
      }
      details.registro dt { font-weight: 600; color: var(--mate, #2c4a63); }
      details.registro dd { margin: 0; }
      @media (max-width: 620px) { .fila { flex-wrap: wrap; row-gap: 0.1rem; } }
    `;
  }
=======
import { Vista, definirElemento } from "../base.js";
import { escapar } from "../texto.js";

/**
 * Línea de tiempo presentacional, independiente del modelo de datos de la aplicación.
 *   linea.pasos = [{ nombre, estado, etiqueta, resumen, detalles: [{ etiqueta, valor }] }]
 */
export class LineaPasos extends Vista {
>>>>>>> v2modificada

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
<<<<<<< HEAD
        ${this.pasos.map((item) => this.pintarPaso(item)).join("") || '<li class="aviso">Sin pasos definidos.</li>'}
=======
        ${this.pasos.map((item, indice) => this.pintarPaso(item, indice)).join("") || '<li class="aviso">Sin pasos definidos.</li>'}
>>>>>>> v2modificada
      </ol>
    `);
  }

  /** Lo que se registró en un paso: quién, rol, cuándo y sus notas. Solo existe si el paso tiene registro. */
<<<<<<< HEAD
  pintarRegistro({ registro, persona, rol, notas }) {
    if (!registro) return "";
    const filas = [
      ["Realizó", persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar"],
      ["Rol", rol?.nombre],
      ["Inicio", registro.fechaInicio && fechaHora(registro.fechaInicio)],
      ["Fin", registro.fechaFin && fechaHora(registro.fechaFin)],
      ["Notas", notas?.notas || "Sin notas en este paso."],
      ["CIE-10", notas?.diagnostico],
      ["Plan", notas?.plan],
    ].filter(([, valor]) => valor);
    return `
      <details class="registro">
        <summary>Ver registro</summary>
        <dl>${filas.map(([clave, valor]) => `<dt>${clave}</dt><dd>${escapar(valor)}</dd>`).join("")}</dl>
=======
  pintarRegistro(item) {
    if (!item.detalles?.length) return "";
    return `
      <details class="registro">
        <summary>${escapar(item.textoDetalles ?? "Ver detalles")}</summary>
        <dl>${item.detalles.map(({ etiqueta, valor }) => `<dt>${escapar(etiqueta)}</dt><dd>${escapar(valor)}</dd>`).join("")}</dl>
>>>>>>> v2modificada
      </details>
    `;
  }

  /** Un paso dentro del procedimiento: número, nombre, estado, quién y cuándo, y su registro. */
<<<<<<< HEAD
  pintarPaso(item) {
    const { paso, estado, persona, registro, indice } = item;
    const quien = [
      etiquetaPaso(estado),
      registro && `${persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar"} · ${fechaCorta(registro.fechaInicio)}`,
    ]
      .filter(Boolean)
      .join(" · ");
=======
  pintarPaso(item, indice) {
    const estado = ["completado", "actual", "pendiente"].includes(item.estado) ? item.estado : "pendiente";
>>>>>>> v2modificada
    return `
      <li class="paso ${escapar(estado)}">
        <div class="fila">
          <span class="num" aria-hidden="true">${indice + 1}</span>
<<<<<<< HEAD
          <span class="nombre">${escapar(paso.nombre)}</span>
          <span class="quien">${escapar(quien)}</span>
=======
          <span class="nombre">${escapar(item.nombre)}</span>
          <span class="quien">${escapar([item.etiqueta, item.resumen].filter(Boolean).join(" · "))}</span>
>>>>>>> v2modificada
        </div>
        ${this.pintarRegistro(item)}
      </li>`;
  }

}

<<<<<<< HEAD
customElements.define("linea-pasos", LineaPasos);
=======
definirElemento("linea-pasos", LineaPasos);
>>>>>>> v2modificada
