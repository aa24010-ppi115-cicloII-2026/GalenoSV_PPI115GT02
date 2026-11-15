import { Vista, escapar, fechaHora, fechaCorta, fechaCalendario } from "../base.js";
import { leerSesion } from "../estado.js";
import { motivoDe, estadoDeCita, etiquetaPaso } from "../datos.js";

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
      .pasos { list-style: none; margin: 0.5rem 0 0; padding: 0 0 0 0.65rem; display: grid; gap: 0.35rem; }
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
      .acciones { display: flex; gap: 0.5rem; flex-wrap: wrap; padding-top: 0.85rem; border-top: 1px solid var(--borde, #d6dee7); }
      .acciones button { min-height: 2.35rem; padding: 0.4rem 0.9rem; font-size: 0.84rem; }
      @media (max-width: 620px) {
        .cabecera { display: grid; grid-template-columns: auto minmax(0, 1fr); gap: 0.6rem 0.85rem; }
        .estado { grid-column: 1 / -1; justify-self: start; margin-left: 0; }
        .fila { flex-wrap: wrap; row-gap: 0.1rem; }
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
    const hora = fecha.toLocaleTimeString("es-SV", { hour: "2-digit", minute: "2-digit" });
    return `<div class="fecha" role="img" aria-label="${escapar(textoCompleto)}">
      <span class="mes">${escapar(mes)}</span><span class="dia">${escapar(dia)}</span><span class="hora">${escapar(hora)}</span>
    </div>`;
  }

  /** Lo que se registró en un paso: quién, rol, cuándo y sus notas. Solo existe si el paso tiene registro. */
  pintarRegistro({ registro, persona, rol, notas }) {
    if (!registro) return "";
    const filas = [
      ["Realizó", persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar"],
      ["Rol", rol?.nombre],
      ["Inicio", registro.fechaInicio && fechaHora(registro.fechaInicio)],
      ["Fin", registro.fechaFin && fechaHora(registro.fechaFin)],
      ["Notas", notas?.notas || "Sin notas en este paso."],
      ["CIE-10", notas?.diagnostico],
    ].filter(([, valor]) => valor);
    return `
      <details class="registro">
        <summary>Ver registro</summary>
        <dl>${filas.map(([clave, valor]) => `<dt>${clave}</dt><dd>${escapar(valor)}</dd>`).join("")}</dl>
      </details>
    `;
  }

  /** Un paso dentro del procedimiento: número, nombre, estado, quién y cuándo, y su registro. */
  pintarPaso(item) {
    const { paso, estado, persona, registro, indice } = item;
    const quien = [
      etiquetaPaso(estado),
      registro && `${persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar"} · ${fechaCorta(registro.fechaInicio)}`,
    ]
      .filter(Boolean)
      .join(" · ");
    return `
      <li class="paso ${escapar(estado)}">
        <div class="fila">
          <span class="num" aria-hidden="true">${indice + 1}</span>
          <span class="nombre">${escapar(paso.nombre)}</span>
          <span class="quien">${escapar(quien)}</span>
        </div>
        ${this.pintarRegistro(item)}
      </li>`;
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
    const sesion = leerSesion();
    const fecha = consulta.fechaInicio ? fechaHora(consulta.fechaInicio) : "Sin fecha";
    const acciones = `
      <div class="acciones">
        <button type="button" class="secundario" data-accion="ticket">Ver ticket</button>
        ${sesion ? `<button type="button" data-accion="atender">Atender consulta</button>` : ""}
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
        <div class="general">
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
        </div>
        <div class="procedimientos">
          ${procedimientos
            .map(
              ({ procedimiento, pasos: lista }) => `
                <section class="procedimiento" aria-label="Procedimiento ${escapar(procedimiento?.nombre ?? "")}">
                  <strong class="nombre-procedimiento">${escapar(procedimiento?.nombre ?? "Procedimiento")}</strong>
                  <ol class="pasos">
                    ${lista.map((item) => this.pintarPaso(item)).join("") || '<li class="aviso">Sin pasos definidos.</li>'}
                  </ol>
                </section>`
            )
            .join("") || '<p class="aviso">La cita no tiene procedimientos asignados.</p>'}
        </div>
        ${acciones}
      </article>
    `);
  }
}

customElements.define("cita-card", CitaCard);
