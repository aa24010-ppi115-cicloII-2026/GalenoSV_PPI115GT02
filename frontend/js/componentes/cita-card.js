import { Vista, escapar, fechaHora, fechaCorta } from "../base.js";
import { leerSesion } from "../estado.js";
import { motivoDe, estadoDeCita } from "../datos.js";

export class CitaCard extends Vista {
  get estilos() {
    return `
      article { display: grid; gap: 0.55rem; }
      .cabecera { display: flex; align-items: flex-start; gap: 0.6rem; }
      .fecha {
        background: #eef5f2; color: #0f7b6c; border-radius: 12px; padding: 0.5rem 0.7rem;
        font-weight: 700; text-align: center; min-width: 74px; font-size: 0.78rem; line-height: 1.2;
      }
      .paciente { display: grid; gap: 0.1rem; }
      .paciente h3 { font-size: 1.02rem; }
      .paciente p { font-size: 0.82rem; color: #587089; }
      .estado {
        margin-left: auto; font-size: 0.7rem; font-weight: 700; text-transform: uppercase;
        padding: 0.22rem 0.55rem; border-radius: 999px; background: #eef2f6; color: #2c4a63;
        white-space: nowrap;
      }
      .estado.completada { background: #e6f4ea; color: #1e6b3a; }
      .estado.proceso { background: #fff4e0; color: #8a5a00; }
      .procedimientos { display: grid; gap: 0.35rem; }
      .procedimiento { font-size: 0.85rem; }
      .procedimiento strong { color: #2c4a63; }
      .pasos { display: grid; gap: 0.3rem; margin-top: 0.35rem; }
      .paso {
        display: flex; align-items: baseline; gap: 0.4rem; font-size: 0.76rem;
        padding: 0.25rem 0.5rem; border-radius: 10px; border: 1px solid #cbd5e1; color: #587089;
        background: #fff;
      }
      .paso .num {
        width: 18px; height: 18px; border-radius: 999px; display: inline-grid; place-items: center;
        background: #eef2f6; color: #2c4a63; font-size: 0.66rem; font-weight: 700; flex: none;
      }
      .paso .nombre { font-weight: 600; color: #2c4a63; }
      .paso .quien { margin-left: auto; font-size: 0.7rem; text-align: right; }
      .paso.completado { background: #f2faf4; border-color: #c3e6cc; }
      .paso.completado .num { background: #1e6b3a; color: #fff; }
      .paso.actual { border-color: #2c4a63; background: #eef2f6; }
      .paso.actual .num { background: #2c4a63; color: #fff; }
      .general { display: flex; flex-wrap: wrap; gap: 0.35rem 0.8rem; font-size: 0.76rem; color: #587089; }
      .general b { color: #2c4a63; font-weight: 600; }
      .acciones { display: flex; gap: 0.4rem; flex-wrap: wrap; margin-top: 0.25rem; }
      .acciones button { padding: 0.4rem 0.75rem; font-size: 0.8rem; }
      details.registro { font-size: 0.78rem; }
      details.registro summary { cursor: pointer; color: #0f7b6c; font-weight: 600; }
      details.registro ul { margin: 0.4rem 0 0; padding-left: 1rem; color: #587089; display: grid; gap: 0.2rem; }
    `;
  }

  set cita(valor) {
    this._cita = valor;
    if (this.isConnected) this.pintar();
  }

  get cita() {
    return this._cita;
  }

  pintarRegistro(cita) {
    const pasos = cita.procedimientos.flatMap(({ pasos: lista }) => lista);
    if (pasos.length === 0) return "";
    return `
      <details class="registro">
        <summary>Ver qué se registró en cada paso</summary>
        <ul>
          ${pasos
            .map(
              ({ paso, estado, persona, registro, notas, indice }) => `
                <li>
                  ${indice + 1}. ${escapar(paso.nombre)} — ${escapar(estado)} ${
                    registro
                      ? `· ${escapar(fechaHora(registro.fechaInicio))} · ${escapar(
                          persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar"
                        )}`
                      : "· pendiente"
                  }${
                    notas?.notas ? `<br />Notas: ${escapar(notas.notas)}` : ""
                  }${notas?.diagnostico ? `<br />CIE-10: ${escapar(notas.diagnostico)}` : ""}
                </li>`
            )
            .join("")}
        </ul>
      </details>
    `;
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
    this.render(`
      <article class="tarjeta" aria-label="Cita de ${escapar(etiquetaPaciente)}">
        <div class="cabecera">
          <div class="fecha">${escapar(fecha)}</div>
          <div class="paciente">
            <h3>${escapar(`${paciente?.nombres ?? "Paciente"} ${paciente?.apellidos ?? ""}`)}</h3>
            <p>${escapar(clinica?.nombre ?? "Clínica no disponible")} · ${escapar(motivoDe(consulta))}</p>
          </div>
          <span class="estado ${estado}">${textoEstado}</span>
        </div>
        <div class="general">
          <span><b>Documento:</b> ${escapar(documentoPaciente ?? "No registrado")}</span>
          ${
            cita.paciente?.fechaNacimiento
              ? `<span><b>Nacimiento:</b> ${escapar(fechaCorta(cita.paciente.fechaNacimiento))}${
                  cita.paciente.edad !== null && cita.paciente.edad !== undefined
                    ? ` (${cita.paciente.edad} años)`
                    : ""
                }</span>`
              : ""
          }
          <span><b>Inicio:</b> ${escapar(fechaHora(consulta.fechaInicio))}</span>
          ${cita.clinica ? `<span><b>Clínica:</b> ${escapar(cita.clinica.nombre)}</span>` : ""}
        </div>
        <div class="procedimientos">
          ${procedimientos
            .map(
              ({ procedimiento, pasos: lista }) => `
                <div class="procedimiento">
                  <strong>${escapar(procedimiento?.nombre ?? "Procedimiento")}</strong>
                  <div class="pasos">
                    ${lista
                      .map(
                        ({ paso, estado: estadoPaso, persona, registro, indice }) => `
                          <div class="paso ${escapar(estadoPaso)}">
                            <span class="num">${indice + 1}</span>
                            <span class="nombre">${escapar(paso.nombre)}</span>
                            <span class="quien">${
                              registro
                                ? `${escapar(persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar")} · ${escapar(fechaCorta(registro.fechaInicio))}`
                                : "Pendiente"
                            }</span>
                          </div>`
                      )
                      .join("") || '<span class="aviso">Sin pasos definidos.</span>'}
                  </div>
                </div>`
            )
            .join("") || '<p class="aviso">La cita no tiene procedimientos asignados.</p>'}
        </div>
        ${this.pintarRegistro(cita)}
        ${acciones}
      </article>
    `);
  }
}

customElements.define("cita-card", CitaCard);
