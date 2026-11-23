import { Vista, definirElemento } from "../base.js";
import { html, nombreCompleto } from "../../comun/texto.js";
import { fechaHora, fechaCalendario } from "../../comun/fechas.js";
import { motivoDe } from "../presentacion.js";
import { estadoDeCita } from "../../control/citas.js";
import { pasosParaPresentacion } from "../presentacion.js";
import "./linea-pasos.js";

/**
 * Tarjeta de una cita: fecha, estado, procedimientos con su línea de pasos y botón de ticket.
 *   tarjeta.cita = resumenCita(...);   vista="paciente" (por omisión) | "empleado"
 *   Evento "ver-ticket" con la cita en detail.
 *   <button slot="acciones">…</button>  la pantalla que la usa agrega sus propias acciones.
 */
class CitaCard extends Vista {
  static observedAttributes = ["vista"];

  #cita;

  get vista() {
    return this.getAttribute("vista") ?? "paciente";
  }

  set vista(valor) {
    if (valor === "empleado" || valor === "paciente") this.setAttribute("vista", valor);
    else this.removeAttribute("vista");
  }

  attributeChangedCallback(nombre, anterior, actual) {
    if (nombre === "vista" && anterior !== actual && this.isConnected) this.pintar();
  }

  set cita(valor) {
    this.#cita = valor;
    if (this.isConnected) this.pintar();
  }

  get cita() {
    return this.#cita;
  }

  connectedCallback() {
    this.escuchar(this.shadowRoot, "click", (evento) => {
      if (!evento.target.closest('button[data-accion="ticket"]')) return;
      this.dispatchEvent(new CustomEvent("ver-ticket", { bubbles: true, composed: true, detail: this.#cita }));
    });
    this.pintar();
  }

  /** Fecha de la cita como bloque de calendario: mes, día y hora. */
  bloqueFecha(valor, textoCompleto) {
    const fecha = valor ? new Date(valor) : null;
    if (!fecha || Number.isNaN(fecha.getTime())) return html`<div class="fecha sin">Sin fecha</div>`;
    const mes = fecha.toLocaleDateString("es-SV", { month: "short" }).replace(".", "");
    const dia = fecha.toLocaleDateString("es-SV", { day: "2-digit" });
    const hora = fecha.toLocaleTimeString("es-SV", { hour: "numeric", minute: "2-digit" });
    return html`<div class="fecha" role="img" aria-label="${textoCompleto}">
      <span class="mes">${mes}</span><span class="dia">${dia}</span><span class="hora">${hora}</span>
    </div>`;
  }

  /** Documento y fecha de nacimiento: solo los ve el personal de la clínica. */
  datosGenerales(paciente) {
    const documento = paciente?.documento ? `${paciente.documento.tipo}: ${paciente.documento.valor}` : "No registrado";
    const tieneEdad = paciente?.edad !== null && paciente?.edad !== undefined;
    return html`<div class="general">
      <span><b>Documento:</b> ${documento}</span>
      ${paciente?.fechaNacimiento &&
      html`<span><b>Nacimiento:</b> ${fechaCalendario(paciente.fechaNacimiento)}${tieneEdad && ` (${paciente.edad} años)`}</span>`}
    </div>`;
  }

  pintar() {
    const cita = this.#cita;
    if (!cita) {
      this.render(html`<div class="vacio">Sin información de la cita.</div>`);
      return;
    }
    const { consulta, paciente, clinica, procedimientos } = cita;
    const { clave: estado, texto: textoEstado } = estadoDeCita(cita);
    const fecha = consulta.fechaInicio ? fechaHora(consulta.fechaInicio) : "Sin fecha";
    const etiquetaPaciente = nombreCompleto(paciente) || "Paciente";
    const nombreClinica = clinica?.nombre ?? "Clínica no disponible";
    // En la búsqueda del propio paciente su nombre sobra: el título es el motivo de la cita.
    const paraPaciente = this.vista !== "empleado";
    const titulo = paraPaciente ? motivoDe(consulta) : etiquetaPaciente;
    const subtitulo = paraPaciente ? nombreClinica : `${nombreClinica} · ${motivoDe(consulta)}`;
    const secciones = procedimientos.map(
      ({ procedimiento }) => html`
        <section class="procedimiento" aria-label="Procedimiento ${procedimiento?.nombre ?? ""}">
          <strong class="nombre-procedimiento">${procedimiento?.nombre ?? "Procedimiento"}</strong>
          <linea-pasos></linea-pasos>
        </section>`
    );
    this.render(html`
      <article class="tarjeta" aria-label="Cita de ${etiquetaPaciente}">
        <div class="cabecera">
          ${this.bloqueFecha(consulta.fechaInicio, fecha)}
          <div class="paciente">
            <h3>${titulo}</h3>
            <p>${subtitulo}</p>
          </div>
          <span class="estado ${estado}">${textoEstado}</span>
        </div>
        ${!paraPaciente && this.datosGenerales(paciente)}
        <div class="procedimientos">
          ${secciones.length ? secciones : html`<p class="aviso">La cita no tiene procedimientos asignados.</p>`}
        </div>
        <div class="acciones">
          <button type="button" class="secundario" data-accion="ticket">Ver ticket</button>
          <slot name="acciones"></slot>
        </div>
      </article>
    `);
    this.qa("linea-pasos").forEach((linea, indice) => {
      linea.pasos = pasosParaPresentacion(procedimientos[indice].pasos);
    });
  }
}

definirElemento("cita-card", CitaCard);
