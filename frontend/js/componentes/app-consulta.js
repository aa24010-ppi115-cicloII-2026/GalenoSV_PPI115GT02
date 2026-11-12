import { Vista, escapar, fechaHora, fechaCorta, ahoraISO } from "../base.js";
import {
  cargarTodo,
  consultaCompleta,
  examenesAsignables,
  leerObservaciones,
  motivoDe,
  serializarObservaciones,
} from "../datos.js";
import { crear, crearConId, actualizar, eliminar } from "../api.js";
import { leerSesion } from "../estado.js";
import { estaConectado } from "../pendientes.js";
import { CODIGOS_CIE10, cie10PorCodigo } from "../cie10.js";

function codigoCie10(texto) {
  const valor = String(texto ?? "").trim();
  if (!valor) return "";
  const candidato = valor.split("·")[0].trim().split(/\s+/)[0];
  return cie10PorCodigo(candidato) ? candidato : null;
}

/** Texto que se guarda en el registro del paso. */
export function notaDelPaso({ notas = "", diagnostico = "", plan = "" }) {
  return [
    diagnostico ? `CIE-10: ${diagnostico}` : "",
    notas ? `Notas: ${notas}` : "",
    plan ? `Plan: ${plan}` : "",
  ]
    .filter(Boolean)
    .join(" | ");
}

export class AppConsulta extends Vista {
  get estilos() {
    return `
      .volver { margin-bottom: 0.8rem; }
      .encabezado { display: flex; align-items: flex-start; gap: 0.8rem; flex-wrap: wrap; margin-bottom: 0.4rem; }
      .encabezado h2 { font-size: 1.4rem; }
      .encabezado .datos { display: flex; gap: 0.8rem; flex-wrap: wrap; font-size: 0.82rem; color: #587089; }
      .procedimientos { display: grid; gap: 1rem; margin-top: 1rem; }
      .proc { display: grid; gap: 0.8rem; }
      .proc h3 { font-size: 1.05rem; }
      .estado-pill {
        font-size: 0.7rem; font-weight: 700; text-transform: uppercase; padding: 0.22rem 0.6rem;
        border-radius: 999px; background: #eef2f6; color: #2c4a63; white-space: nowrap;
      }
      .estado-pill.completada { background: #e6f4ea; color: #1e6b3a; }
      .estado-pill.proceso { background: #fff4e0; color: #8a5a00; }
      .stepper { display: flex; flex-wrap: wrap; gap: 0.45rem; margin: 0; padding: 0; list-style: none; }
      .stepper li {
        display: flex; align-items: center; gap: 0.35rem; font-size: 0.78rem;
        border: 1px solid #cbd5e1; border-radius: 999px; padding: 0.25rem 0.6rem; color: #587089;
      }
      .stepper li b {
        width: 20px; height: 20px; border-radius: 999px; display: grid; place-items: center;
        background: #eef2f6; font-size: 0.7rem;
      }
      .stepper li.completado { border-color: #a8d5b5; background: #e6f4ea; color: #1e6b3a; }
      .stepper li.completado b { background: #1e6b3a; color: #fff; }
      .stepper li.actual { border-color: #2c4a63; background: #2c4a63; color: #fff; }
      .stepper li.actual b { background: #fff; color: #2c4a63; }
      .notas { border-top: 1px dashed #d7dee7; padding-top: 0.8rem; display: grid; gap: 0.7rem; }
      .paso-panel { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 14px; padding: 0.85rem; display: grid; gap: 0.6rem; }
      .paso-panel h4 { font-size: 0.92rem; color: #2c4a63; }
      .paso-panel.completada { background: #f2faf4; border-color: #c3e6cc; }
      .examenes { display: grid; gap: 0.45rem; }
      .examen {
        display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem;
        background: #fff; border: 1px solid #dfe7ef; border-radius: 10px; padding: 0.4rem 0.55rem;
      }
      .examen small { color: #587089; }
      .ordenes { font-size: 0.8rem; color: #2c4a63; display: grid; gap: 0.25rem; }
      .ordenes li { margin-left: 1rem; }
      .registro-pasos { border: 1px solid #e2e8f0; border-radius: 12px; padding: 0.5rem 0.7rem; background: #f8fafc; }
      .registro-pasos summary { cursor: pointer; font-size: 0.82rem; font-weight: 700; color: #2c4a63; }
      .registro-pasos ul { list-style: none; margin: 0.6rem 0 0; padding: 0; display: grid; gap: 0.5rem; }
      .registro-pasos li { border-top: 1px solid #e2e8f0; padding-top: 0.45rem; font-size: 0.8rem; }
      .registro-pasos li:first-child { border-top: none; padding-top: 0; }
      .registro-pasos p { margin: 0.15rem 0 0; color: #587089; }
      .registro-pasos .nota-paso { color: #2c4a63; }
      .estado-mini {
        font-size: 0.66rem; text-transform: uppercase; font-weight: 700; padding: 0.1rem 0.4rem;
        border-radius: 999px; background: #eef2f6; color: #2c4a63; margin-left: 0.35rem;
      }
      .estado-mini.completado { background: #e6f4ea; color: #1e6b3a; }
      .estado-mini.actual { background: #2c4a63; color: #fff; }
    `;
  }

  connectedCallback() {
    this._id = this.getAttribute("id-consulta");
    if (!this._escucha) {
      this._escucha = (evento) => this.manejarClick(evento);
      this.shadowRoot.addEventListener("click", this._escucha);
      this._cambio = (evento) => this.manejarCambio(evento);
      this.shadowRoot.addEventListener("change", this._cambio);
    }
    this.cargar();
  }

  async cargar(recargar = false) {
    this.render(`<div class="vacio">Cargando el registro de atención…</div>`);
    try {
      this._datos = await cargarTodo({ recargar });
      this._cita = consultaCompleta(this._datos, this._id);
      if (!this._cita) {
        this.render(`<div class="vacio">La consulta solicitada no existe.</div>`);
        return;
      }
      this.pintar();
    } catch {
      this.render(`<div class="vacio">No se pudo cargar la consulta. Intente de nuevo.</div>`);
    }
  }

  pintar() {
    const { consulta, paciente, clinica, procedimientos } = this._cita;
    const pasos = procedimientos.flatMap(({ pasos: lista }) => lista);
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const estadoGeneral = pasos.length > 0 && completados === pasos.length ? "Completada" : "En proceso";
    this.render(`
      <button type="button" class="secundario volver" data-accion="volver">← Volver al panel</button>
      <section class="tarjeta">
        <div class="encabezado">
          <div>
            <span class="etiqueta">Registro de atención</span>
            <h2>${escapar(`${paciente?.nombres ?? "Paciente"} ${paciente?.apellidos ?? ""}`.trim())}</h2>
            <div class="datos">
              <span>${escapar(clinica?.nombre ?? "Clínica")}</span>
              <span>${escapar(fechaHora(consulta.fechaInicio))}</span>
              <span>${escapar(motivoDe(consulta))}</span>
              <span>${escapar(estadoGeneral)}</span>
              ${
                paciente?.fechaNacimiento
                  ? `<span>Nac. ${escapar(fechaCorta(paciente.fechaNacimiento))}${
                      paciente.edad !== null && paciente.edad !== undefined
                        ? ` (${paciente.edad} años)`
                        : ""
                    }</span>`
                  : ""
              }
            </div>
          </div>
          <div class="separador"></div>
          <div class="fila">
            <button type="button" class="secundario" data-accion="ticket">Ticket / PDF</button>
            ${
              consulta.fechaFin
                ? `<span class="estado-pill completada">Finalizada ${escapar(fechaCorta(consulta.fechaFin))}</span>`
                : `<button type="button" data-accion="finalizar">Finalizar consulta</button>`
            }
          </div>
        </div>
      </section>
      <datalist id="cie10-lista">
        ${CODIGOS_CIE10.map(
          (item) => `<option value="${escapar(item.codigo)} · ${escapar(item.descripcion)}"></option>`
        ).join("")}
      </datalist>
      <section class="procedimientos">
        ${procedimientos.map((item) => this.pintarProcedimiento(item)).join("") || '<div class="vacio">La consulta no tiene procedimientos asignados.</div>'}
      </section>
    `);
  }

  pintarProcedimiento({ consultaProcedimiento, procedimiento, pasos }) {
    const cp = consultaProcedimiento;
    const notas = leerObservaciones(cp.observaciones);
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const finalizado = pasos.length > 0 && completados === pasos.length;
    const actual = pasos.find(({ estado }) => estado === "actual") ?? null;
    const hayRegistros = pasos.some(({ registro }) => registro);
    return `
      <article class="tarjeta proc" data-cp="${escapar(cp.idConsultaProcedimiento)}">
        <div class="fila">
          <h3>${escapar(procedimiento?.nombre ?? "Procedimiento")}</h3>
          <span class="separador"></span>
          <span class="estado-pill ${finalizado ? "completada" : "proceso"}">${finalizado ? "Completado" : `${completados}/${pasos.length} pasos`}</span>
        </div>
        <ol class="stepper" aria-label="Pasos del procedimiento">
          ${pasos
            .map(
              ({ paso, estado, persona, registro, indice }) => `
                <li class="${escapar(estado)}" title="${escapar(paso.nombre)}">
                  <b>${indice + 1}</b>
                  <span>${escapar(paso.nombre)}</span>
                  ${
                    registro
                      ? `<small>· ${escapar(persona ? `${persona.nombres} ${persona.apellidos}` : "registrado")} · ${escapar(fechaCorta(registro.fechaInicio))}</small>`
                      : "<small>· pendiente</small>"
                  }
                </li>`
            )
            .join("") || '<li class="actual"><span>Sin pasos configurados</span></li>'}
        </ol>
        ${this.pintarRegistroPasos(pasos)}
        <div class="notas">
          <div class="campo">
            <label for="notas-${escapar(cp.idConsultaProcedimiento)}">Notas de evolución</label>
            <textarea data-campo="notas" id="notas-${escapar(cp.idConsultaProcedimiento)}" placeholder="Hallazgos, evolución y observaciones clínicas">${escapar(notas.notas)}</textarea>
          </div>
          <div class="rejilla dos">
            <div class="campo">
              <label for="diag-${escapar(cp.idConsultaProcedimiento)}">Diagnóstico CIE-10</label>
              <input data-campo="diagnostico" id="diag-${escapar(cp.idConsultaProcedimiento)}" list="cie10-lista" value="${escapar(notas.diagnostico)}" placeholder="Ej. K02.1 · Caries de la dentina" />
            </div>
            <div class="campo">
              <label for="plan-${escapar(cp.idConsultaProcedimiento)}">Plan de atención</label>
              <input data-campo="plan" id="plan-${escapar(cp.idConsultaProcedimiento)}" value="${escapar(notas.plan)}" placeholder="Indicaciones y próximos controles" />
            </div>
          </div>
          <div class="fila">
            <button type="button" class="secundario" data-accion="guardar-notas" data-cp="${escapar(cp.idConsultaProcedimiento)}">Guardar notas</button>
          </div>
        </div>
        ${finalizado ? this.panelFinalizado(cp, hayRegistros) : this.panelPaso(cp, actual, hayRegistros)}
      </article>
    `;
  }

  pintarRegistroPasos(pasos) {
    if (pasos.length === 0) return "";
    return `
      <details class="registro-pasos">
        <summary>Ver qué se registró en cada paso</summary>
        <ul>
          ${pasos
            .map(
              ({ paso, estado, persona, registro, notas, indice }) => `
                <li>
                  <strong>${indice + 1}. ${escapar(paso.nombre)}</strong>
                  <span class="estado-mini ${escapar(estado)}">${escapar(estado)}</span>
                  <p>
                    ${
                      registro
                        ? `${escapar(fechaHora(registro.fechaInicio))} · ${escapar(
                            persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar"
                          )}`
                        : "Todavía no se ha registrado este paso."
                    }
                  </p>
                  ${
                    notas?.notas || notas?.diagnostico || notas?.plan
                      ? `<p class="nota-paso">
                           ${notas.diagnostico ? `<b>CIE-10:</b> ${escapar(notas.diagnostico)} ` : ""}
                           ${notas.notas ? `<b>Notas:</b> ${escapar(notas.notas)} ` : ""}
                           ${notas.plan ? `<b>Plan:</b> ${escapar(notas.plan)}` : ""}
                         </p>`
                      : ""
                  }
                </li>`
            )
            .join("")}
        </ul>
      </details>
    `;
  }

  panelFinalizado(cp, hayRegistros) {
    const idCp = escapar(cp.idConsultaProcedimiento);
    const finalizada = Boolean(this._cita.consulta.fechaFin);
    return `
      <div class="paso-panel completada">
        <h4>Todos los pasos están completados</h4>
        <p class="aviso">${
          finalizada
            ? `Consulta finalizada el ${escapar(fechaHora(this._cita.consulta.fechaFin))}.`
            : "Puede retroceder a un paso anterior para corregir el registro."
        }</p>
        <div class="fila">
          ${
            hayRegistros
              ? `<button type="button" class="peligro" data-accion="retroceder" data-cp="${idCp}">Anterior</button>`
              : ""
          }
        </div>
      </div>
    `;
  }

  panelPaso(cp, actual, hayRegistros) {
    if (!actual) {
      return `<div class="paso-panel"><p class="aviso">Este procedimiento no tiene un paso pendiente.</p></div>`;
    }
    const { paso, registro, rol, persona } = actual;
    const idCp = escapar(cp.idConsultaProcedimiento);
    const examenes = examenesAsignables(this._datos, cp.idProcedimiento);
    const categorias = [...new Set(examenes.flatMap(({ tipos }) => tipos))];
    const ordenes = registro
      ? this._datos.ordenesExamen.filter((orden) => orden.idConsultaProcedimientoPaso === registro.idConsultaProcedimientoPaso)
      : [];
    return `
      <div class="paso-panel">
        <h4>Paso actual: ${escapar(paso.nombre)} ${rol ? `· ${escapar(rol.nombre)}` : ""}</h4>
        ${
          registro
            ? `<p class="aviso">Iniciado ${escapar(fechaHora(registro.fechaInicio))}${persona ? ` por ${escapar(persona.nombres)}` : ""}.</p>`
            : `<p class="aviso">Aún no se ha iniciado este paso.</p>`
        }
        <div class="fila">
          ${
            registro
              ? `<button type="button" data-accion="completar" data-cp="${idCp}" data-paso="${escapar(paso.idProcedimientoPaso)}">Guardar y avanzar al siguiente paso</button>`
              : `<button type="button" data-accion="iniciar" data-cp="${idCp}" data-paso="${escapar(paso.idProcedimientoPaso)}">Iniciar paso</button>`
          }
          ${hayRegistros ? `<button type="button" class="peligro" data-accion="retroceder" data-cp="${idCp}">Anterior</button>` : ""}
        </div>
        <div class="examenes">
          <span class="etiqueta">Exámenes del paso</span>
          ${
            registro
              ? examenes
                  .map(
                    ({ examen, tipos }) => `
                      <label class="examen" data-categoria="${escapar(tipos.join("|"))}">
                        <input type="checkbox" name="examen-${idCp}" value="${escapar(examen.idExamen)}" />
                        <span>${escapar(examen.nombre)}${tipos.length ? ` <small>(${escapar(tipos.join(", "))})</small>` : ""}</span>
                      </label>`
                  )
                  .join("") || '<p class="aviso">No hay exámenes configurados para este procedimiento.</p>'
              : '<p class="aviso">Inicie el paso para poder asignar exámenes.</p>'
          }
          ${
            registro && categorias.length > 0
              ? `<div class="campo">
                   <label for="cat-${idCp}">Filtrar por categoría</label>
                   <select id="cat-${idCp}" data-categoria-filtro="${idCp}">
                     <option value="">Todas las categorías</option>
                     ${categorias.map((categoria) => `<option value="${escapar(categoria)}">${escapar(categoria)}</option>`).join("")}
                   </select>
                 </div>`
              : ""
          }
          ${
            registro && examenes.length > 0
              ? `<div class="campo">
                   <label for="ind-${idCp}">Indicaciones para los exámenes</label>
                   <input id="ind-${idCp}" data-campo="indicaciones" placeholder="Ej. Radiografía periapical del sector" />
                 </div>
                 <div class="fila"><button type="button" class="secundario" data-accion="asignar" data-cp="${idCp}">Asignar exámenes seleccionados</button></div>`
              : ""
          }
        </div>
        ${
          ordenes.length > 0
            ? `<div class="ordenes"><span class="etiqueta">Órdenes registradas</span><ul>${ordenes
                .map(
                  (orden) =>
                    `<li>${escapar(fechaHora(orden.fechaCreacion))} · ${escapar(orden.indicaciones ?? "Sin indicaciones")}</li>`
                )
                .join("")}</ul></div>`
            : ""
        }
      </div>
    `;
  }

  buscarProcedimiento(idCp) {
    return this._cita.procedimientos.find(
      ({ consultaProcedimiento }) => String(consultaProcedimiento.idConsultaProcedimiento) === String(idCp)
    );
  }

  leerNotas(idCp) {
    return {
      notas: this.q(`[data-cp="${idCp}"] [data-campo="notas"]`)?.value ?? "",
      diagnostico: this.q(`[data-cp="${idCp}"] [data-campo="diagnostico"]`)?.value ?? "",
      plan: this.q(`[data-cp="${idCp}"] [data-campo="plan"]`)?.value ?? "",
    };
  }

  /** Nombre del empleado autenticado, para la bitacora del paso. */
  _nombreSesion() {
    return leerSesion()?.nombre ?? "";
  }

  personaRolPara(paso) {
    const sesion = leerSesion();
    if (!sesion?.roles?.length) return null;
    const idClinica = this._cita.clinica?.idClinica;
    return (
      sesion.roles.find(
        (rol) =>
          String(rol.idClinica) === String(idClinica) &&
          (!paso?.idRol || String(rol.idRol) === String(paso.idRol))
      ) ??
      sesion.roles.find((rol) => String(rol.idClinica) === String(idClinica)) ??
      sesion.roles[0]
    );
  }

  async guardarNotas(idCp, { silencioso = false, idPaso = null } = {}) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return null;
    const cp = item.consultaProcedimiento;
    const valores = this.leerNotas(idCp);
    const pasos = { ...(item.notas?.pasos ?? {}) };
    if (idPaso) {
      pasos[idPaso] = {
        notas: valores.notas,
        diagnostico: valores.diagnostico,
        plan: valores.plan,
        fecha: ahoraISO(),
        responsable: this._nombreSesion(),
      };
    }
    await actualizar("consultas-procedimiento", cp.idConsultaProcedimiento, {
      idProcedimiento: cp.idProcedimiento,
      fechaInicio: cp.fechaInicio,
      fechaFin: cp.fechaFin,
      observaciones: serializarObservaciones({ ...valores, pasos }),
      idConsulta: cp.idConsulta,
    });
    if (!silencioso) this.notificar("Notas de atención guardadas.", "ok");
    return valores;
  }

  async iniciarPaso(idCp, idPaso) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const paso = item.pasos.find(({ paso }) => String(paso.idProcedimientoPaso) === String(idPaso))?.paso;
    const personaRol = this.personaRolPara(paso);
    if (!personaRol) {
      this.notificar("Su sesión no tiene un rol asociado a esta clínica.", "error");
      return;
    }
    try {
      await crearConId("consultas-procedimiento-paso", {
        fechaInicio: ahoraISO(),
        estado: "EN_PROCESO",
        idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
        idPersonaRol: personaRol.idPersonaRol,
      });
      this.notificar(
        estaConectado()
          ? "Paso iniciado. Registre notas y exámenes."
          : "Paso iniciado sin conexión: se enviará al recuperar la red.",
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudo iniciar el paso: ${error.message}`, "error");
    }
  }

  async completarPaso(idCp, idPaso) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const actual = item.pasos.find(({ paso }) => String(paso.idProcedimientoPaso) === String(idPaso));
    if (!actual?.registro) {
      this.notificar("Primero inicie el paso actual.", "error");
      return;
    }
    const notas = this.leerNotas(idCp);
    if (!notas.diagnostico.trim()) {
      this.notificar("Registre el diagnóstico CIE-10 antes de completar el paso.", "error");
      return;
    }
    if (codigoCie10(notas.diagnostico) === null) {
      this.notificar("Seleccione un código CIE-10 válido de la lista sugerida.", "error");
      return;
    }
    try {
      await this.guardarNotas(idCp, { silencioso: true, idPaso: actual.paso.idProcedimientoPaso });
      const registro = actual.registro;
      await actualizar("consultas-procedimiento-paso", registro.idConsultaProcedimientoPaso, {
        fechaInicio: registro.fechaInicio,
        fechaFin: ahoraISO(),
        estado: "FINALIZADO",
        idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
        idPersonaRol: registro.idPersonaRol,
      });
      this.notificar(
        estaConectado()
          ? "Paso completado. Avance registrado."
          : "Paso completado sin conexión: se enviará al recuperar la red.",
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudo completar el paso: ${error.message}`, "error");
    }
  }

  async retroceder(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const pasos = item.pasos;
    const indiceActual = pasos.findIndex(({ estado }) => estado === "actual");
    const indicePrevio = indiceActual > 0 ? indiceActual - 1 : pasos.length - 2;
    if (indicePrevio < 0) {
      this.notificar("No hay un paso anterior al que retroceder.", "info");
      return;
    }
    const previo = pasos[indicePrevio];
    if (!previo) return;
    if (!window.confirm(`¿Retroceder al paso "${previo.paso.nombre}"? El paso volverá a estar pendiente.`)) return;
    try {
      if (previo.registro) {
        await actualizar("consultas-procedimiento-paso", previo.registro.idConsultaProcedimientoPaso, {
          fechaInicio: previo.registro.fechaInicio,
          estado: "PENDIENTE",
          idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
          idPersonaRol: previo.registro.idPersonaRol,
        });
      }
      this.notificar(
        estaConectado()
          ? `Se retrocedió al paso "${previo.paso.nombre}".`
          : `Retroceso guardado sin conexión: se enviará al recuperar la red.`,
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudo retroceder: ${error.message}`, "error");
    }
  }

  async finalizarConsulta() {
    const consulta = this._cita.consulta;
    if (consulta.fechaFin) {
      this.notificar("La consulta ya fue finalizada.", "info");
      return;
    }
    if (!window.confirm("¿Finalizar la consulta? Se registrará la fecha de cierre.")) return;
    try {
      await actualizar("consultas", consulta.idConsulta, {
        fechaInicio: consulta.fechaInicio,
        fechaFin: ahoraISO(),
        observaciones: consulta.observaciones ?? "",
        referenciaExterna: consulta.referenciaExterna ?? "",
        idPersonaRol: consulta.idPersonaRol,
      });
      this.notificar(
        estaConectado() ? "Consulta finalizada." : "Finalización guardada sin conexión.",
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudo finalizar la consulta: ${error.message}`, "error");
    }
  }

  async asignarExamenes(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const actual = item.pasos.find(({ estado }) => estado === "actual");
    if (!actual?.registro) {
      this.notificar("Inicie el paso actual antes de asignar exámenes.", "error");
      return;
    }
    const seleccionados = this.qa(`input[name="examen-${idCp}"]:checked`).map((input) => input.value);
    if (seleccionados.length === 0) {
      this.notificar("Seleccione al menos un examen.", "error");
      return;
    }
    const indicaciones = this.q(`[data-cp="${idCp}"] [data-campo="indicaciones"]`)?.value.trim() ?? "";
    try {
      for (const idExamen of seleccionados) {
        const examen = this._datos.porId.examenes.get(idExamen);
        await crear("ordenes-examen", {
          fechaCreacion: ahoraISO(),
          indicaciones: `Examen: ${examen?.nombre ?? idExamen}${indicaciones ? ` — ${indicaciones}` : ""}`,
          idConsultaProcedimientoPaso: actual.registro.idConsultaProcedimientoPaso,
        });
      }
      this.notificar(
        estaConectado()
          ? `${seleccionados.length} examen(es) asignado(s).`
          : `${seleccionados.length} examen(es) guardado(s) sin conexión.`,
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudieron asignar los exámenes: ${error.message}`, "error");
    }
  }

  abrirTicket() {
    this.q("ticket-cita")?.remove();
    const ticket = document.createElement("ticket-cita");
    ticket.cita = this._cita;
    this.shadowRoot.append(ticket);
  }

  manejarCambio(evento) {
    const filtro = evento.target.closest("[data-categoria-filtro]");
    if (!filtro) return;
    const idCp = filtro.dataset.categoriaFiltro;
    const categoria = filtro.value;
    for (const etiqueta of this.qa(`[data-cp="${idCp}"] label.examen`)) {
      const categorias = (etiqueta.dataset.categoria ?? "").split("|").filter(Boolean);
      const visible = !categoria || categorias.includes(categoria);
      etiqueta.hidden = !visible;
      const casilla = etiqueta.querySelector("input");
      if (casilla && !visible) casilla.checked = false;
    }
  }

  manejarClick(evento) {
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) return;
    const { accion, cp, paso } = boton.dataset;
    if (accion === "volver") this.navegar("dashboard");
    if (accion === "ticket") this.abrirTicket();
    if (accion === "guardar-notas") this.guardarNotas(cp).catch((error) => this.notificar(error.message, "error"));
    if (accion === "iniciar") this.iniciarPaso(cp, paso);
    if (accion === "completar") this.completarPaso(cp, paso);
    if (accion === "retroceder") this.retroceder(cp);
    if (accion === "asignar") this.asignarExamenes(cp);
    if (accion === "finalizar") this.finalizarConsulta();
  }
}

customElements.define("app-consulta", AppConsulta);
