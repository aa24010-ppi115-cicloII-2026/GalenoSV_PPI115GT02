import { Vista, definirElemento, modalConFoco } from "../base.js";
import { escapar, nombreCompleto } from "../texto.js";
import { fechaHora } from "../fechas.js";
import { motivoDe, estadoDeCita, etiquetaPaso, notaDelPaso, registroVisible } from "../datos.js";

const ESTILO_TICKET = new URL("../../css/ticket-documento.css", import.meta.url).href;

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
  const columnas = ["Procedimiento", "Paso", "Estado", "Fecha", "Observaciones"];
  const filas = filasTicket(cita)
    .map((fila) => {
      const valores = [fila.procedimiento, fila.paso, fila.estado, fila.fecha, fila.observaciones || "—"];
      return `<tr>${valores
        .map((valor, i) => `<td data-etiqueta="${columnas[i]}"${i === 3 ? ' class="fecha"' : ""}>${escapar(valor)}</td>`)
        .join("")}</tr>`;
    })
    .join("");
  return `<table>
    <thead><tr>${columnas.map((columna) => `<th>${columna}</th>`).join("")}</tr></thead>
    <tbody>${filas || '<tr><td colspan="5">Sin pasos registrados.</td></tr>'}</tbody>
  </table>`;
}

function encabezadoTicket(cita, nivel, claseParrafo = "") {
  return `<header>
    <${nivel}>GalenoSV · Comprobante de cita</${nivel}>
    <p${claseParrafo ? ` class="${claseParrafo}"` : ""}>Ticket <span class="codigo">#${escapar(codigoTicket(cita.consulta.idConsulta))}</span> · ${escapar(fechaHora(cita.consulta.fechaInicio))}</p>
  </header>`;
}

function datosTicket(cita) {
  return `<section class="datos">
    <div><span>Paciente</span>${escapar(nombreCompleto(cita.paciente) || "—")}</div>
    <div><span>Clínica</span>${escapar(cita.clinica?.nombre ?? "—")}</div>
    <div><span>Motivo</span>${escapar(motivoDe(cita.consulta))}</div>
    <div><span>Estado</span>${escapar(estadoDeCita(cita).texto)}</div>
  </section>`;
}

export function documentoImprimible(cita) {
  return `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <title>Ticket de cita ${escapar(codigoTicket(cita.consulta.idConsulta))}</title>
  <link rel="stylesheet" href="${ESTILO_TICKET}" />
</head>
<body>
  ${encabezadoTicket(cita, "h1")}
  ${datosTicket(cita)}
  <h2>Procedimientos y pasos</h2>
  ${tablaTicket(cita)}
  <footer>Documento generado desde GalenoSV. Use Imprimir para conservar una copia.</footer>
</body>
</html>`;
}

class TicketCita extends Vista {

  set cita(valor) {
    this._cita = valor;
    if (this.isConnected) this.pintar();
  }

  get cita() {
    return this._cita;
  }

  disconnectedCallback() {
    this._liberarModal?.();
    this._liberarModal = null;
  }

  connectedCallback() {
    if (!this._escucha) {
      this._escucha = (evento) => {
        const boton = evento.target.closest("button[data-accion]");
        if (!boton) return;
        if (boton.dataset.accion === "cerrar") this.remove();
        if (boton.dataset.accion === "imprimir") this.imprimir();
      };
      this.shadowRoot.addEventListener("click", this._escucha);
    }
    this._liberarModal ??= modalConFoco(this, () => this.remove());
    this.pintar();
    this.q("button[data-accion='cerrar']")?.focus();
  }

  pintar() {
    const cita = this._cita;
    if (!cita) {
      this.render("");
      return;
    }
    this.render(`
      <div class="fondo" role="dialog" aria-modal="true" aria-label="Ticket de la cita">
        <div class="hoja">
          ${encabezadoTicket(cita, "h2", "aviso")}
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

  imprimir() {
    if (!this._cita) return;
    const ventana = window.open("", "_blank");
    if (!ventana) {
      this.notificar("Permita las ventanas emergentes para imprimir el ticket.", "error");
      return;
    }
    const documento = new DOMParser().parseFromString(documentoImprimible(this._cita), "text/html");
    ventana.document.replaceChildren(...documento.childNodes);
    ventana.focus();
    ventana.print();
  }
}

definirElemento("ticket-cita", TicketCita);
