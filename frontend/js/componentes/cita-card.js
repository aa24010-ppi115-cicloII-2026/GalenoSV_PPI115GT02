import { Vista, escapar, fechaHora, fechaCalendario } from "../base.js";
import { leerSesion } from "../estado.js";
import { motivoDe, estadoDeCita } from "../datos.js";
import "./linea-pasos.js";

export class CitaCard extends Vista {
  get estilos() {
    return `
      article { display: grid; gap: 0.85rem; }
      .cabecera { display: flex; align-items: flex-start; gap: 0.9rem; }
      .fecha {
        flex: none; display: grid; justify-items: center; align-content: center; gap: 0.05rem;
        min-width: 4.6rem; padding: 0.5rem 0.6rem; border-radius: var(--radio, 10px);
        background: var(--acento-suave, #e6f3f0); color: var(--acento-fuerte, #0b6155);
        font-variant-numeric: tabular-nums; line-height: 1.1; text-align: center;
      }
      .fecha .mes { font-size: 0.68rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; }
      .fecha .dia { font-size: 1.55rem; font-weight: 700; letter-spacing: -0.02em; color: var(--texto, #14202b); }
      .fecha .hora { font-size: 0.7rem; font-weight: 600; white-space: nowrap; }
      .fecha.sin { font-size: 0.75rem; font-weight: 600; }
      .paciente { display: grid; gap: 0.15rem; min-width: 0; padding-top: 0.15rem; }
      .paciente h3 { font-size: 1.08rem; line-height: 1.3; }
      .paciente p { font-size: 0.85rem; color: var(--texto-suave, #4d6378); }
      .estado {
        margin-left: auto; display: inline-flex; align-items: center; gap: 0.4rem;
        font-size: 0.72rem; font-weight: 600; letter-spacing: 0.02em;
        padding: 0.25rem 0.65rem; border-radius: 999px; white-space: nowrap;
        background: var(--mate-suave, #e9eff4); color: var(--mate, #2c4a63);
      }
      .estado::before { content: ""; width: 0.45rem; height: 0.45rem; border-radius: 999px; background: currentColor; }
      .estado.completada { background: var(--exito-suave, #e8f4ec); color: var(--exito, #1e6b3a); }
      .estado.proceso { background: var(--alerta-suave, #fdf3e1); color: var(--alerta, #85570a); }
      .general {
        display: flex; flex-wrap: wrap; gap: 0.35rem 1.1rem; font-size: 0.8rem; color: var(--texto-suave, #4d6378);
        padding: 0.6rem 0.8rem; border-radius: var(--radio, 10px); background: var(--superficie-suave, #f7f9fb);
        font-variant-numeric: tabular-nums;
      }
      .general b { color: var(--mate, #2c4a63); font-weight: 600; }
      .procedimientos { display: grid; gap: 0.75rem; }
      .procedimiento { font-size: 0.88rem; }
      .nombre-procedimiento { display: block; color: var(--texto, #14202b); font-weight: 650; }
      linea-pasos { display: block; margin-top: 0.5rem; }
      .acciones { display: flex; gap: 0.5rem; flex-wrap: wrap; padding-top: 0.85rem; border-top: 1px solid var(--borde, #d6dee7); }
      .acciones button { min-height: 2.35rem; padding: 0.4rem 0.9rem; font-size: 0.84rem; }
      @media (max-width: 620px) {
        .cabecera { display: grid; grid-template-columns: auto minmax(0, 1fr); gap: 0.6rem 0.85rem; }
        .estado { grid-column: 1 / -1; justify-self: start; margin-left: 0; }
      }
    `;
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
    this.shadowRoot.addEventListener("click", (evento) => {
      const boton = evento.target.closest("button[data-accion]");
      if (!boton) return;
      if (boton.dataset.accion === "ticket") {
        this.dispatchEvent(new CustomEvent("ver-ticket", { bubbles: true, composed: true, detail: this._cita }));
      } else if (boton.dataset.accion === "atender") {
        this.navegar(`consulta/${this._cita.consulta.idConsulta}`);
      }
    });
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
    const accion = estado === "completada" || cita.consulta?.fechaFin ? "Ver atención" : "Atender consulta";
    const sesion = leerSesion();
    const fecha = consulta.fechaInicio ? fechaHora(consulta.fechaInicio) : "Sin fecha";
    const acciones = `
      <div class="acciones">
        <button type="button" class="secundario" data-accion="ticket">Ver ticket</button>
        ${sesion && this.getAttribute("vista") !== "paciente" ? `<button type="button" data-accion="atender">${accion}</button>` : ""}
      </div>
    `;
    const etiquetaPaciente = `${paciente?.nombres ?? "Paciente"} ${paciente?.apellidos ?? ""}`.trim();
    const nombreClinica = clinica?.nombre ?? "Clínica no disponible";
    // En la búsqueda del propio paciente su nombre sobra: el título es el motivo de la cita.
    const paraPaciente = this.getAttribute("vista") === "paciente";
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
      linea.pasos = procedimientos[indice].pasos;
    });
  }
}

customElements.define("cita-card", CitaCard);
