import { Vista, definirElemento, modalConFoco } from "../base.js";
import { html, nombreCompleto } from "../../comun/texto.js";
import { fechaHora } from "../../comun/fechas.js";
import { motivoDe, etiquetaPaso, notaDelPaso, registroVisible } from "../presentacion.js";
import { estadoDeCita } from "../../control/citas.js";

const ESTILO_TICKET = new URL("../../../css/ticket-documento.css", import.meta.url).href;

export function mostrarTicket(contenedor, cita) {
  contenedor.querySelector("ticket-cita")?.remove();
  const ticket = document.createElement("ticket-cita");
  ticket.cita = cita;
  contenedor.append(ticket);
  return ticket;
}

/** Código corto y legible del ticket: el primer bloque del identificador de la consulta. */
function codigoTicket(idConsulta) {
  return String(idConsulta ?? "").split("-")[0].toUpperCase();
}

/** Filas de un procedimiento: pasos y órdenes comparten su representación en pantalla y al imprimir. */
function filasProcedimiento({ procedimiento, pasos, ordenes = [] }) {
  const nombre = procedimiento?.nombre ?? "Procedimiento";
  return [
    ...pasos.map((item) => {
      const registro = registroVisible(item);
      return {
        procedimiento: nombre,
        paso: item.paso.nombre,
        estado: etiquetaPaso(item.estado, Boolean(registro)),
        fecha: registro?.fechaInicio ? fechaHora(registro.fechaInicio) : "Pendiente",
        // El borrador del paso en curso no forma parte del comprobante.
        observaciones: notaDelPaso(item.notas ?? {}),
      };
    }),
    ...ordenes.map((orden) => ({
      procedimiento: nombre,
      paso: "Examen ordenado",
      estado: "Ordenado",
      fecha: orden.fecha ? fechaHora(orden.fecha) : "—",
      observaciones: orden.texto,
    })),
  ];
}

function filasTicket(cita) {
  return cita.procedimientos.flatMap(filasProcedimiento);
}

/** Filas de la tabla en HTML; cada celda lleva su etiqueta para mostrarse como tarjeta en pantallas angostas. */
function tablaTicket(cita) {
  const filas = filasTicket(cita).map(
    (fila) => html`<tr><td data-etiqueta="Procedimiento">${fila.procedimiento}</td><td data-etiqueta="Paso">${fila.paso}</td><td data-etiqueta="Estado">${fila.estado}</td><td data-etiqueta="Fecha" class="fecha">${fila.fecha}</td><td data-etiqueta="Observaciones">${fila.observaciones || "—"}</td></tr>`
  );
  return html`<table>
    <thead><tr><th>Procedimiento</th><th>Paso</th><th>Estado</th><th>Fecha</th><th>Observaciones</th></tr></thead>
    <tbody>${filas.length ? filas : html`<tr><td colspan="5">Sin pasos registrados.</td></tr>`}</tbody>
  </table>`;
}

/** En pantalla el título es h2 dentro del diálogo; en el documento impreso es el h1 de la página. */
function encabezadoTicket(cita, { enPantalla = false } = {}) {
  const titulo = "GalenoSV · Comprobante de cita";
  const detalle = html`Ticket <span class="codigo">#${codigoTicket(cita.consulta.idConsulta)}</span> · ${fechaHora(cita.consulta.fechaInicio)}`;
  return enPantalla
    ? html`<header>
    <h2>${titulo}</h2>
    <p class="aviso">${detalle}</p>
  </header>`
    : html`<header>
    <h1>${titulo}</h1>
    <p>${detalle}</p>
  </header>`;
}

function datosTicket(cita) {
  return html`<section class="datos">
    <div><span>Paciente</span>${nombreCompleto(cita.paciente) || "—"}</div>
    <div><span>Clínica</span>${cita.clinica?.nombre ?? "—"}</div>
    <div><span>Motivo</span>${motivoDe(cita.consulta)}</div>
    <div><span>Estado</span>${estadoDeCita(cita).texto}</div>
  </section>`;
}

export function documentoImprimible(cita) {
  return String(html`<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <title>Ticket de cita ${codigoTicket(cita.consulta.idConsulta)}</title>
  <link rel="stylesheet" href="${ESTILO_TICKET}" />
</head>
<body>
  ${encabezadoTicket(cita)}
  ${datosTicket(cita)}
  <h2>Procedimientos y pasos</h2>
  ${tablaTicket(cita)}
  <footer>Documento generado desde GalenoSV. Use Imprimir para conservar una copia.</footer>
</body>
</html>`);
}

class TicketCita extends Vista {
  #cita;
  #liberarModal = null;

  set cita(valor) {
    this.#cita = valor;
    if (this.isConnected) this.pintar();
  }

  get cita() {
    return this.#cita;
  }

  connectedCallback() {
    this.escuchar(this.shadowRoot, "click", (evento) => {
      const boton = evento.target.closest("button[data-accion]");
      if (boton?.dataset.accion === "cerrar") this.remove();
      if (boton?.dataset.accion === "imprimir") this.imprimir();
    });
    this.#liberarModal ??= modalConFoco(this, () => this.remove());
    this.pintar();
    this.q("button[data-accion='cerrar']")?.focus();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.#liberarModal?.();
    this.#liberarModal = null;
  }

  pintar() {
    const cita = this.#cita;
    if (!cita) {
      this.render("");
      return;
    }
    this.render(html`
      <div class="fondo" role="dialog" aria-modal="true" aria-label="Ticket de la cita">
        <div class="hoja">
          ${encabezadoTicket(cita, { enPantalla: true })}
          ${datosTicket(cita)}
          <div class="tabla">${tablaTicket(cita)}</div>
          <div class="acciones">
            <button type="button" class="secundario" data-accion="cerrar">Cerrar</button>
            <button type="button" class="secundario" data-accion="imprimir">Imprimir</button>
          </div>
        </div>
      </div>
    `);
  }

  /**
   * Imprime el comprobante desde un marco oculto (el patrón de MDN para imprimir un documento sin
   * abrirlo): no depende de ventanas emergentes y funciona sin conexión. El marco se retira al
   * terminar la impresión. El contenido llega escapado por `html`, así que srcdoc no inyecta HTML.
   */
  imprimir() {
    if (!this.#cita) return;
    const marco = document.createElement("iframe");
    marco.setAttribute("aria-hidden", "true");
    marco.tabIndex = -1;
    marco.style.cssText = "position: fixed; inset-inline-end: 0; inset-block-end: 0; width: 0; height: 0; border: 0;";
    marco.addEventListener(
      "load",
      () => {
        const ventana = marco.contentWindow;
        ventana.addEventListener("afterprint", () => marco.remove(), { once: true });
        ventana.print();
      },
      { once: true }
    );
    marco.srcdoc = documentoImprimible(this.#cita);
    document.body.append(marco);
  }
}

definirElemento("ticket-cita", TicketCita);
