import { Vista, definirElemento } from "../base.js";
import { escapar, nombreCompleto } from "../texto.js";
import { fechaHora, fechaCalendario } from "../fechas.js";
import { motivoDe, estadoDeCita } from "../datos.js";
import { pasosParaPresentacion } from "../presentacion.js";
import "./linea-pasos.js";

class CitaCard extends Vista {
  static get observedAttributes() {
    return ["vista"];
  }

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
    this._cita = valor;
    if (this.isConnected) this.pintar();
  }

  get cita() {
    return this._cita;
  }

  /** Fecha de la cita como bloque de calendario: mes, día y hora. */
  bloqueFecha(valor, textoCompleto) {
    const fecha = valor ? new Date(valor) : null;
    if (!fecha || Number.isNaN(fecha.getTime())) return `<div class="fecha sin">Sin fecha</div>`;
    const mes = fecha.toLocaleDateString("es-SV", { month: "short" }).replace(".", "");
    const dia = fecha.toLocaleDateString("es-SV", { day: "2-digit" });
    const hora = fecha.toLocaleTimeString("es-SV", { hour: "numeric", minute: "2-digit" });
    return `<div class="fecha" role="img" aria-label="${escapar(textoCompleto)}">
      <span class="mes">${escapar(mes)}</span><span class="dia">${escapar(dia)}</span><span class="hora">${escapar(hora)}</span>
    </div>`;
  }

  connectedCallback() {
    this._alClic ??= (evento) => {
      const boton = evento.target.closest("button[data-accion]");
      if (!boton) return;
      if (boton.dataset.accion === "ticket") {
        this.dispatchEvent(new CustomEvent("ver-ticket", { bubbles: true, composed: true, detail: this._cita }));
      } else if (boton.dataset.accion === "atender" && this._cita) {
        this.navegar(`consulta/${this._cita.consulta.idConsulta}`);
      }
    };
    this.shadowRoot.addEventListener("click", this._alClic);
    this.pintar();
  }

  pintar() {
    const cita = this._cita;
    if (!cita) {
      this.render(`<div class="vacio">Sin información de la cita.</div>`);
      return;
    }
    const { consulta, paciente, clinica, procedimientos } = cita;
    const documentoPaciente = paciente?.documento
      ? `${paciente.documento.tipo}: ${paciente.documento.valor}`
      : null;
    const { clave: estado, texto: textoEstado } = estadoDeCita(cita);
    // Con todos los pasos completos ya no hay nada que atender: solo revisar o finalizar.
    const accion = estado === "completada" ? "Ver atención" : "Atender consulta";
    const fecha = consulta.fechaInicio ? fechaHora(consulta.fechaInicio) : "Sin fecha";
    const acciones = `
      <div class="acciones">
        <button type="button" class="secundario" data-accion="ticket">Ver ticket</button>
        ${this.vista === "empleado" ? `<button type="button" data-accion="atender">${accion}</button>` : ""}
      </div>
    `;
    const etiquetaPaciente = nombreCompleto(paciente) || "Paciente";
    const nombreClinica = clinica?.nombre ?? "Clínica no disponible";
    // En la búsqueda del propio paciente su nombre sobra: el título es el motivo de la cita.
    const paraPaciente = this.vista !== "empleado";
    const titulo = paraPaciente ? motivoDe(consulta) : etiquetaPaciente;
    const subtitulo = paraPaciente ? nombreClinica : `${nombreClinica} · ${motivoDe(consulta)}`;
    this.render(`
      <article class="tarjeta" aria-label="Cita de ${escapar(etiquetaPaciente)}">
        <div class="cabecera">
          ${this.bloqueFecha(consulta.fechaInicio, fecha)}
          <div class="paciente">
            <h3>${escapar(titulo)}</h3>
            <p>${escapar(subtitulo)}</p>
          </div>
          <span class="estado ${estado}">${textoEstado}</span>
        </div>
        ${
          paraPaciente
            ? ""
            : `<div class="general">
          <span><b>Documento:</b> ${escapar(documentoPaciente ?? "No registrado")}</span>
          ${
            cita.paciente?.fechaNacimiento
              ? `<span><b>Nacimiento:</b> ${escapar(fechaCalendario(cita.paciente.fechaNacimiento))}${
                  cita.paciente.edad !== null && cita.paciente.edad !== undefined
                    ? ` (${cita.paciente.edad} años)`
                    : ""
                }</span>`
              : ""
          }
        </div>`
        }
        <div class="procedimientos">
          ${procedimientos
            .map(
              ({ procedimiento, pasos: lista }) => `
                <section class="procedimiento" aria-label="Procedimiento ${escapar(procedimiento?.nombre ?? "")}">
                  <strong class="nombre-procedimiento">${escapar(procedimiento?.nombre ?? "Procedimiento")}</strong>
                  <linea-pasos></linea-pasos>
                </section>`
            )
            .join("") || '<p class="aviso">La cita no tiene procedimientos asignados.</p>'}
        </div>
        ${acciones}
      </article>
    `);
    this.qa("linea-pasos").forEach((linea, indice) => {
      linea.pasos = pasosParaPresentacion(procedimientos[indice].pasos);
    });
  }
}

definirElemento("cita-card", CitaCard);
