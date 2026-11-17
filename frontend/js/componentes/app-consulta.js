import { Vista, escapar, fechaHora, fechaCorta, ahoraISO, cantidad } from "../base.js";
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
import { CODIGOS_CIE10 } from "../cie10.js";
import { confirmar } from "./ui-confirmar.js";
import "./linea-pasos.js";
import "./ui-autocompletar.js";

// El diagnóstico se guarda con su texto completo («K02.1 · Caries de la dentina»).
const OPCIONES_CIE10 = CODIGOS_CIE10.map(({ codigo, descripcion }) => {
  const texto = `${codigo} · ${descripcion}`;
  return { valor: texto, texto };
});

export class AppConsulta extends Vista {
  get estilos() {
    return `
      .volver { margin-bottom: 0.8rem; }
      .aviso-carga { display: grid; gap: 0.9rem; justify-items: center; }
      .aviso-carga .fila { display: flex; gap: 0.5rem; flex-wrap: wrap; justify-content: center; }
      .encabezado { display: flex; align-items: flex-start; gap: 0.8rem; flex-wrap: wrap; margin-bottom: 0.4rem; }
      .encabezado { align-items: center; }
      .encabezado .paciente { display: grid; gap: 0.3rem; min-width: 0; }
      .encabezado .titulo { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; }
      .encabezado h2 { font-size: 1.4rem; letter-spacing: -0.015em; }
      .encabezado .datos { font-size: 0.9rem; color: var(--texto-suave); font-variant-numeric: tabular-nums; }
      .procedimientos { display: grid; gap: 1rem; margin-top: 1rem; }
      .proc { display: grid; gap: 0.8rem; }
      .proc h3 { font-size: 1.05rem; }
      .estado-pill {
        font-size: 0.7rem; font-weight: 700; text-transform: uppercase; padding: 0.22rem 0.6rem;
        border-radius: 999px; background: var(--mate-suave); color: var(--mate); white-space: nowrap;
      }
      .estado-pill.completada { background: var(--exito-suave); color: var(--exito); }
      .estado-pill.proceso { background: var(--alerta-suave); color: var(--alerta); }
      .bloque { display: grid; gap: 0.7rem; padding-top: 0.85rem; border-top: 1px solid var(--borde); }
      .bloque h5 { font-size: 0.8rem; font-weight: 700; color: var(--mate); text-transform: uppercase; letter-spacing: 0.05em; }
      .bloque h5 small { text-transform: none; letter-spacing: 0; font-weight: 500; color: var(--texto-suave); }
      .paso-cabecera { display: flex; align-items: baseline; gap: 0.6rem; flex-wrap: wrap; }
      .paso-cabecera .meta { font-size: 0.78rem; color: var(--texto-suave); font-variant-numeric: tabular-nums; }
      .acciones-paso { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
      .acciones-paso .separador { flex: 1; }
      h4, h5 { margin: 0; }
      .paso-panel { display: grid; gap: 0.75rem; padding-top: 1rem; border-top: 1px solid var(--borde); }
      .paso-panel h4 { font-size: 1.05rem; color: var(--texto); }
      .paso-panel.completada h4 { color: var(--exito); }
      /* Pie con las acciones del paso, separado de las acciones de cada sección. */
      .acciones-paso.pie { padding-top: 0.9rem; border-top: 1px solid var(--borde); }
      details.examenes > summary {
        cursor: pointer; width: fit-content; border-radius: 6px; padding: 0.1rem 0.2rem; margin-left: -0.2rem;
        font-size: 0.8rem; font-weight: 700; color: var(--mate); text-transform: uppercase; letter-spacing: 0.05em;
      }
      details.examenes > summary small { text-transform: none; letter-spacing: 0; font-weight: 500; color: var(--texto-suave); }
      details.examenes > summary:focus-visible { outline: none; box-shadow: var(--anillo); }
      details.examenes[open] { gap: 0.6rem; }
      .asignacion { display: grid; gap: 0.6rem; }
      .elegidos { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 0.4rem; }
      .elegidos li {
        display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.84rem; color: var(--mate);
        background: var(--mate-suave); border: 1px solid var(--borde); border-radius: 999px; padding: 0.15rem 0.2rem 0.15rem 0.7rem;
      }
      .elegidos li small { color: var(--texto-suave); }
      .elegidos .quitar {
        min-height: 0; width: 1.6rem; height: 1.6rem; padding: 0; border-radius: 999px; line-height: 1;
        background: transparent; color: var(--mate); border: none; box-shadow: none; font-size: 1rem;
      }
      .elegidos .quitar:hover { background: var(--superficie); }
      .ordenes { margin-top: 0.5rem; font-size: 0.84rem; color: var(--mate); display: grid; gap: 0.4rem; }
      .barra-ordenes { display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; flex-wrap: wrap; }
      .barra-ordenes button { min-height: 2rem; padding: 0.2rem 0.75rem; font-size: 0.8rem; }
      .barra-ordenes button:disabled { border-color: var(--borde); color: var(--texto-tenue); background: transparent; }
      .ordenes ul { list-style: none; margin: 0; padding: 0; display: grid; gap: 0.35rem; }
      .ordenes label {
        display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; cursor: pointer;
        padding: 0.5rem 0.7rem; background: var(--superficie); border: 1px solid var(--borde); border-radius: var(--radio);
      }
      .ordenes label:has(input:checked) { border-color: var(--peligro); background: var(--peligro-suave); }
      .ordenes label:has(input:disabled) { cursor: default; }
      .ordenes label > span { flex: 1; min-width: 12rem; }
      .con-resultado { font-size: 0.72rem; font-weight: 600; color: var(--exito); background: var(--exito-suave); border-radius: 999px; padding: 0.15rem 0.55rem; }
        font-size: 0.66rem; text-transform: uppercase; font-weight: 700; padding: 0.1rem 0.4rem;
        border-radius: 999px; background: var(--mate-suave); color: var(--mate); margin-left: 0.35rem;
      }
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
    // Lo escrito y aún no guardado se conserva al volver a pintar (iniciar paso, asignar exámenes…).
    const borradores = this.leerBorradores();
    const abiertos = this.qa("details.examenes[open]").map((detalle) => detalle.dataset.examenes);
    this.render(`<div class="vacio">Cargando el registro de atención…</div>`);
    try {
      this._datos = await cargarTodo({ recargar });
      this._cita = consultaCompleta(this._datos, this._id);
      if (!this._cita) {
        this.pintarAviso("La consulta solicitada no existe.");
        return;
      }
      this.pintar();
      this.aplicarBorradores(borradores);
      for (const idCp of abiertos) {
        const detalle = this.q(`details.examenes[data-examenes="${idCp}"]`);
        if (detalle) detalle.open = true;
      }
    } catch {
      this.pintarAviso("No se pudo cargar la consulta. Revise la conexión e intente de nuevo.", true);
    }
  }

  /** Estado sin consulta que mostrar: siempre deja una salida (volver) y, si falló la carga, reintentar. */
  pintarAviso(mensaje, reintentar = false) {
    this.render(`
      <div class="vacio aviso-carga" role="alert">
        <p>${escapar(mensaje)}</p>
        <div class="fila">
          <button type="button" class="secundario" data-accion="volver">← Volver al panel</button>
          ${reintentar ? '<button type="button" data-accion="reintentar">Reintentar</button>' : ""}
        </div>
      </div>
    `);
  }

  pintar() {
    const { consulta, paciente, clinica, procedimientos } = this._cita;
    // Exámenes que se pueden asignar y los ya elegidos en el buscador, por procedimiento.
    this._examenes = new Map();
    this._elegidos = new Map();
    const pasos = procedimientos.flatMap(({ pasos: lista }) => lista);
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const todoCompleto = pasos.length > 0 && completados === pasos.length;
    const estadoGeneral = todoCompleto ? "Completada" : "En proceso";
    this.render(`
      <button type="button" class="secundario volver" data-accion="volver">← Volver al panel</button>
      <section class="tarjeta">
        <div class="encabezado">
          <div class="paciente">
            <div class="titulo">
              <h2>${escapar(`${paciente?.nombres ?? "Paciente"} ${paciente?.apellidos ?? ""}`.trim())}</h2>
              <span class="estado-pill ${todoCompleto ? "completada" : "proceso"}">${escapar(estadoGeneral)}</span>
            </div>
            <p class="datos">${escapar(
              [
                paciente?.edad !== null && paciente?.edad !== undefined ? `${paciente.edad} años` : "",
                clinica?.nombre ?? "Clínica",
                fechaHora(consulta.fechaInicio),
                motivoDe(consulta),
              ]
                .filter(Boolean)
                .join(" · ")
            )}</p>
          </div>
          <div class="separador"></div>
          <div class="fila">
            <button type="button" class="secundario" data-accion="ticket">Ticket / PDF</button>
            ${
              consulta.fechaFin
                ? `<span class="estado-pill completada">Finalizada ${escapar(fechaCorta(consulta.fechaFin))}</span>`
                : `<button type="button" ${todoCompleto ? "" : 'class="secundario"'} data-accion="finalizar">Finalizar consulta</button>`
            }
          </div>
        </div>
      </section>
      <section class="procedimientos">
        ${procedimientos.map((item) => this.pintarProcedimiento(item)).join("") || '<div class="vacio">La consulta no tiene procedimientos asignados.</div>'}
      </section>
    `);
    for (const linea of this.qa("linea-pasos[data-cp]")) {
      linea.pasos = this.buscarProcedimiento(linea.dataset.cp)?.pasos ?? [];
    }
    for (const campo of this.qa('ui-autocompletar[data-campo="diagnostico"]')) {
      campo.opciones = OPCIONES_CIE10;
      campo.value = campo.dataset.valor;
    }
    for (const buscador of this.qa("ui-autocompletar[data-examen-buscador]")) {
      this.pintarElegidos(buscador.dataset.examenBuscador);
    }
  }

  pintarProcedimiento({ consultaProcedimiento, procedimiento, pasos }) {
    const cp = consultaProcedimiento;
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const finalizado = pasos.length > 0 && completados === pasos.length;
    const actual = pasos.find(({ estado }) => estado === "actual") ?? null;
    const hayRegistros = pasos.some(({ registro }) => registro);
    // Se retrocede al paso anterior si el actual no es el primero, o al último si todos están completos.
    const puedeRetroceder = finalizado ? hayRegistros : (actual?.indice ?? 0) > 0;
    const esUltimo = actual ? actual.indice === pasos.length - 1 : false;
    const notas = this.notasDelFormulario(cp, actual);
    return `
      <article class="tarjeta proc" data-cp="${escapar(cp.idConsultaProcedimiento)}">
        <div class="fila">
          <h3>${escapar(procedimiento?.nombre ?? "Procedimiento")}</h3>
          <span class="separador"></span>
          ${finalizado ? '<span class="estado-pill completada">Completado</span>' : ""}
        </div>
        <linea-pasos data-cp="${escapar(cp.idConsultaProcedimiento)}"></linea-pasos>
        ${finalizado ? this.panelFinalizado(cp, puedeRetroceder, notas) : this.panelPaso(cp, actual, puedeRetroceder, esUltimo, pasos.length, notas)}
      </article>
    `;
  }

  /**
   * Valores del formulario: el borrador del paso en curso. Si no hay borrador y el paso se
   * reabrió al retroceder, se muestra lo que ese paso ya tenía registrado para corregirlo.
   */
  notasDelFormulario(cp, actual) {
    const guardadas = leerObservaciones(cp.observaciones);
    const hayBorrador = guardadas.notas || guardadas.diagnostico || guardadas.plan;
    const delPaso = actual ? guardadas.pasos?.[actual.paso.idProcedimientoPaso] : null;
    if (hayBorrador || !delPaso) return guardadas;
    return { ...guardadas, notas: delPaso.notas ?? "", diagnostico: delPaso.diagnostico ?? "", plan: delPaso.plan ?? "" };
  }

  /** Registro de atención del procedimiento: notas, diagnóstico CIE-10 y plan. */
  formularioNotas(cp, notas) {
    const id = escapar(cp.idConsultaProcedimiento);
    return `
      <section class="bloque" aria-labelledby="reg-${id}">
        <h5 id="reg-${id}">Registro de atención</h5>
        <div class="campo">
          <label for="notas-${id}">Notas de evolución</label>
          <textarea data-campo="notas" id="notas-${id}" placeholder="Hallazgos, evolución y observaciones clínicas">${escapar(notas.notas)}</textarea>
        </div>
        <div class="rejilla dos">
          <ui-autocompletar data-campo="diagnostico" id="diag-${id}" etiqueta="Diagnóstico CIE-10 (obligatorio para completar el paso)" data-valor="${escapar(notas.diagnostico)}"></ui-autocompletar>
          <div class="campo">
            <label for="plan-${id}">Plan de atención</label>
            <input data-campo="plan" id="plan-${id}" value="${escapar(notas.plan)}" placeholder="Indicaciones y próximos controles" />
          </div>
        </div>
        <div class="fila">
          <button type="button" class="secundario" data-accion="guardar-notas" data-cp="${id}">Guardar notas</button>
        </div>
      </section>
    `;
  }

  botonAnterior(idCp) {
    return `<button type="button" class="secundario" data-accion="retroceder" data-cp="${idCp}">← Paso anterior</button>`;
  }

  panelFinalizado(cp, puedeRetroceder, notas) {
    const idCp = escapar(cp.idConsultaProcedimiento);
    const finalizada = Boolean(this._cita.consulta.fechaFin);
    return `
      <div class="paso-panel completada">
        <h4>Todos los pasos están completados</h4>
        <p class="aviso">${
          finalizada
            ? `Consulta finalizada el ${escapar(fechaHora(this._cita.consulta.fechaFin))}.`
            : "Si la atención terminó, use «Finalizar consulta». Para corregir un paso, vuelva al anterior."
        }</p>
        ${this.formularioNotas(cp, notas)}
        ${puedeRetroceder ? `<div class="acciones-paso pie">${this.botonAnterior(idCp)}</div>` : ""}
      </div>
    `;
  }

  panelPaso(cp, actual, puedeRetroceder, esUltimo, total, notas) {
    if (!actual) {
      return `<div class="paso-panel"><p class="aviso">Este procedimiento no tiene un paso pendiente.</p></div>`;
    }
    const { paso, registro, rol, persona, indice } = actual;
    const idCp = escapar(cp.idConsultaProcedimiento);
    const examenes = examenesAsignables(this._datos, cp.idProcedimiento);
    this._examenes.set(String(cp.idConsultaProcedimiento), examenes);
    const ordenes = registro
      ? this._datos.ordenesExamen.filter((orden) => orden.idConsultaProcedimientoPaso === registro.idConsultaProcedimientoPaso)
      : [];
    const anterior = puedeRetroceder ? this.botonAnterior(idCp) : "";
    // Sin iniciar, la acción principal va arriba; iniciado, «Completar» va al final del registro.
    const acciones = registro
      ? `<div class="acciones-paso pie">${anterior}<span class="separador"></span>
           <button type="button" data-accion="completar" data-cp="${idCp}" data-paso="${escapar(paso.idProcedimientoPaso)}">${esUltimo ? "Completar último paso" : "Guardar y avanzar al siguiente paso"}</button>
         </div>`
      : `<div class="acciones-paso">
           <button type="button" data-accion="iniciar" data-cp="${idCp}" data-paso="${escapar(paso.idProcedimientoPaso)}">Iniciar paso</button>
           ${anterior}
         </div>`;
    return `
      <div class="paso-panel">
        <div class="paso-cabecera">
          <h4>Paso actual: ${escapar(paso.nombre)}</h4>
          <span class="meta">Paso ${indice + 1} de ${total}${rol ? ` · ${escapar(rol.nombre)}` : ""}</span>
        </div>
        ${
          registro
            ? `<p class="aviso">Iniciado ${escapar(fechaHora(registro.fechaInicio))}${persona ? ` por ${escapar(`${persona.nombres} ${persona.apellidos}`.trim())}` : ""}.</p>`
            : `<p class="aviso">Aún no se ha iniciado este paso. Inícielo para asignar exámenes y poder completarlo.</p>`
        }
        ${registro ? "" : acciones}
        ${this.formularioNotas(cp, notas)}
        <details class="bloque examenes" data-examenes="${idCp}">
          <summary>Exámenes del paso <small>(opcional)${ordenes.length ? ` · ${ordenes.length} ${ordenes.length === 1 ? "orden" : "órdenes"}` : ""}</small></summary>
          ${
            !registro
              ? '<p class="aviso">Inicie el paso para poder asignar exámenes.</p>'
              : examenes.length === 0
                ? '<p class="aviso">No hay exámenes configurados para este procedimiento.</p>'
                : `<ui-autocompletar id="examen-${idCp}" data-examen-buscador="${idCp}" etiqueta="Buscar examen por nombre o categoría"></ui-autocompletar>
                   <ul class="elegidos" data-elegidos="${idCp}" aria-label="Exámenes elegidos"></ul>
                   <div class="asignacion" hidden>
                     <div class="campo">
                       <label for="ind-${idCp}">Indicaciones para los exámenes</label>
                       <input id="ind-${idCp}" data-campo="indicaciones" placeholder="Ej. Radiografía periapical del sector" />
                     </div>
                     <div class="fila"><button type="button" class="secundario" data-accion="asignar" data-cp="${idCp}">Asignar exámenes seleccionados</button></div>
                   </div>`
          }
          ${
            ordenes.length > 0
              ? `<div class="ordenes">
                   <div class="barra-ordenes">
                     <span class="etiqueta" id="ord-${idCp}">Órdenes registradas</span>
                     <button type="button" class="peligro" data-accion="eliminar-ordenes" data-cp="${idCp}" disabled>Eliminar seleccionadas</button>
                   </div>
                   <ul role="group" aria-labelledby="ord-${idCp}">${ordenes.map((orden) => this.pintarOrden(orden)).join("")}</ul>
                 </div>`
              : ""
          }
        </details>
        ${registro ? acciones : ""}
      </div>
    `;
  }

  /** Orden de examen del paso en curso: se selecciona para eliminarla si se registró por error y aún no tiene resultado. */
  pintarOrden(orden) {
    const tieneResultado = this._datos.examenesResultado.some(
      (resultado) => String(resultado.idOrdenExamen) === String(orden.idOrdenExamen)
    );
    return `
      <li>
        <label>
          <input type="checkbox" name="orden" value="${escapar(orden.idOrdenExamen)}" ${tieneResultado ? "disabled" : ""} />
          <span>${escapar(fechaHora(orden.fechaCreacion))} · ${escapar(orden.indicaciones ?? "Sin indicaciones")}</span>
          ${tieneResultado ? '<span class="con-resultado">Con resultado</span>' : ""}
        </label>
      </li>`;
  }

  /** Habilita «Eliminar seleccionadas» y muestra cuántas hay. */
  actualizarEliminarOrdenes(idCp) {
    const boton = this.q(`[data-accion="eliminar-ordenes"][data-cp="${idCp}"]`);
    const total = this.qa(`[data-cp="${idCp}"] .ordenes input:checked`).length;
    boton.disabled = total === 0;
    boton.textContent = total ? `Eliminar seleccionadas (${total})` : "Eliminar seleccionadas";
  }

  /** Lista de exámenes elegidos y opciones del buscador sin los ya elegidos. */
  pintarElegidos(idCp) {
    const examenes = this._examenes.get(String(idCp)) ?? [];
    const elegidos = this._elegidos.get(String(idCp)) ?? [];
    const esElegido = ({ examen }) => elegidos.includes(String(examen.idExamen));
    // En el orden en que se eligieron.
    this.q(`[data-elegidos="${idCp}"]`).innerHTML = elegidos
      .map((id) => examenes.find(({ examen }) => String(examen.idExamen) === id))
      .filter(Boolean)
      .map(
        ({ examen }) => `<li><span>${escapar(examen.nombre)}</span><button type="button" class="quitar" data-quitar-examen="${escapar(examen.idExamen)}" data-cp="${escapar(idCp)}" aria-label="Quitar ${escapar(examen.nombre)}">×</button></li>`
      )
      .join("");
    const buscador = this.q(`[data-examen-buscador="${idCp}"]`);
    const disponibles = examenes.filter((item) => !esElegido(item));
    // El texto incluye la categoría para poder buscar también por ella.
    buscador.opciones = disponibles.map(({ examen, tipos }) => ({
      valor: examen.idExamen,
      texto: [examen.nombre, tipos.join(", ")].filter(Boolean).join(" · "),
    }));
    if (disponibles.length === 0) buscador.mensaje = "Ya eligió todos los exámenes disponibles";
    // Indicaciones y «Asignar» solo tienen sentido con algún examen elegido.
    this.q(`[data-cp="${idCp}"] .asignacion`).hidden = elegidos.length === 0;
  }

  leerBorradores() {
    return new Map(this.qa("article.proc").map((articulo) => [articulo.dataset.cp, this.leerNotas(articulo.dataset.cp)]));
  }

  aplicarBorradores(borradores) {
    for (const [idCp, valores] of borradores) {
      for (const [campo, valor] of Object.entries(valores)) {
        const elemento = this.q(`[data-cp="${idCp}"] [data-campo="${campo}"]`);
        if (elemento && valor) elemento.value = valor;
      }
    }
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
      // Al completar un paso su registro pasa a `pasos` y el borrador queda vacío para el siguiente.
      observaciones: serializarObservaciones(idPaso ? { notas: "", diagnostico: "", plan: "", pasos } : { ...valores, pasos }),
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
      // El id se genera aquí para poder completar el paso aunque no haya conexión.
      await crearConId("consultas-procedimiento-paso", {
        idConsultaProcedimientoPaso: crypto.randomUUID(),
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
    const notas = this.leerNotas(idCp);
    if (!notas.diagnostico) {
      this.notificar("Elija el diagnóstico CIE-10 de la lista antes de completar el paso.", "error");
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
      // Lo escrito ya quedó en el paso completado: no se arrastra al siguiente al volver a pintar.
      for (const campo of ["notas", "diagnostico", "plan"]) {
        const elemento = this.q(`[data-cp="${idCp}"] [data-campo="${campo}"]`);
        if (elemento) elemento.value = "";
      }
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
    // Sin paso en curso todos están completos: se reabre el último.
    const indicePrevio = indiceActual === -1 ? pasos.length - 1 : indiceActual - 1;
    const previo = pasos[indicePrevio];
    if (!previo?.registro) {
      this.notificar("No hay un paso anterior al que retroceder.", "info");
      return;
    }
    const aceptado = await confirmar({
      titulo: `¿Retroceder al paso «${previo.paso.nombre}»?`,
      mensaje: "El paso volverá a quedar en curso para corregir su registro. No se borra nada de lo registrado.",
      aceptar: "Retroceder",
    });
    if (!aceptado) return;
    try {
      await actualizar("consultas-procedimiento-paso", previo.registro.idConsultaProcedimientoPaso, {
        fechaInicio: previo.registro.fechaInicio,
        estado: "PENDIENTE",
        idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
        idPersonaRol: previo.registro.idPersonaRol,
      });
      this.notificar(
        estaConectado()
          ? `Se retrocedió al paso «${previo.paso.nombre}».`
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
    const sinCompletar = this._cita.procedimientos
      .flatMap(({ pasos }) => pasos)
      .filter(({ estado }) => estado !== "completado").length;
    const aceptado = await confirmar({
      titulo: "¿Finalizar la consulta?",
      mensaje:
        sinCompletar > 0
          ? `Quedan ${sinCompletar} ${sinCompletar === 1 ? "paso" : "pasos"} sin completar. Si finaliza, se registrará la fecha y hora de cierre de todos modos.`
          : "Se registrará la fecha y hora de cierre de la consulta.",
      aceptar: "Finalizar",
      peligro: sinCompletar > 0,
    });
    if (!aceptado) return;
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
    const seleccionados = this._elegidos.get(String(idCp)) ?? [];
    const indicaciones = this.q(`[data-cp="${idCp}"] [data-campo="indicaciones"]`)?.value.trim() ?? "";
    try {
      for (const idExamen of seleccionados) {
        const examen = this._examenes.get(String(idCp))?.find((item) => String(item.examen.idExamen) === idExamen)?.examen;
        await crear("ordenes-examen", {
          idOrdenExamen: crypto.randomUUID(),
          fechaCreacion: ahoraISO(),
          indicaciones: `Examen: ${examen?.nombre ?? idExamen}${indicaciones ? ` — ${indicaciones}` : ""}`,
          idConsultaProcedimientoPaso: actual.registro.idConsultaProcedimientoPaso,
        });
      }
      this.notificar(
        estaConectado()
          ? `${cantidad(seleccionados.length, "examen asignado", "exámenes asignados")}.`
          : `${cantidad(seleccionados.length, "examen guardado", "exámenes guardados")} sin conexión.`,
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudieron asignar los exámenes: ${error.message}`, "error");
    }
  }

  async eliminarOrdenes(idCp) {
    const ids = this.qa(`[data-cp="${idCp}"] .ordenes input:checked`).map((casilla) => casilla.value);
    const ordenes = this._datos.ordenesExamen.filter((orden) => ids.includes(String(orden.idOrdenExamen)));
    if (ordenes.length === 0) return;
    const nombres = ordenes.map((orden) => `«${orden.indicaciones ?? "Orden sin indicaciones"}»`).join(", ");
    const aceptado = await confirmar({
      titulo: ordenes.length === 1 ? "¿Eliminar la orden de examen?" : `¿Eliminar ${ordenes.length} órdenes de examen?`,
      mensaje: `Se eliminará de este paso: ${nombres}. Esta acción no se puede deshacer.`,
      aceptar: "Eliminar",
      peligro: true,
    });
    if (!aceptado) return;
    try {
      for (const orden of ordenes) await eliminar("ordenes-examen", orden.idOrdenExamen);
      this.notificar(
        estaConectado()
          ? `${cantidad(ordenes.length, "orden de examen eliminada", "órdenes de examen eliminadas")}.`
          : "Eliminación guardada sin conexión: se enviará al recuperar la red.",
        "ok"
      );
      await this.cargar(true);
    } catch (error) {
      this.notificar(`No se pudo eliminar la orden: ${error.message}`, "error");
    }
  }

  abrirTicket() {
    this.q("ticket-cita")?.remove();
    const ticket = document.createElement("ticket-cita");
    ticket.cita = this._cita;
    this.shadowRoot.append(ticket);
  }

  manejarCambio(evento) {
    const orden = evento.target.closest('.ordenes input[name="orden"]');
    if (orden) return this.actualizarEliminarOrdenes(orden.closest("article.proc").dataset.cp);
    const buscador = evento.target.closest("[data-examen-buscador]");
    if (!buscador?.value) return;
    const idCp = buscador.dataset.examenBuscador;
    this._elegidos.set(idCp, [...(this._elegidos.get(idCp) ?? []), String(buscador.value)]);
    this.pintarElegidos(idCp);
  }

  manejarClick(evento) {
    const quitar = evento.target.closest("button[data-quitar-examen]");
    if (quitar) {
      const idCp = quitar.dataset.cp;
      this._elegidos.set(idCp, (this._elegidos.get(idCp) ?? []).filter((id) => id !== quitar.dataset.quitarExamen));
      this.pintarElegidos(idCp);
      this.q(`[data-examen-buscador="${idCp}"]`).focus();
      return;
    }
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) return;
    const { accion, cp, paso } = boton.dataset;
    if (accion === "volver") this.navegar("dashboard");
    if (accion === "ticket") this.abrirTicket();
    if (accion === "reintentar") this.cargar(true);
    if (accion === "retroceder") this.retroceder(cp);
    if (accion === "finalizar") this.finalizarConsulta();
    if (accion === "eliminar-ordenes") this.eliminarOrdenes(cp);
    // Las acciones que guardan sin pedir confirmación bloquean su botón mientras trabajan:
    // un doble clic no repite la operación (por ejemplo, dos pasos iniciados).
    const guardar = {
      "guardar-notas": () => this.guardarNotas(cp).catch((error) => this.notificar(error.message, "error")),
      iniciar: () => this.iniciarPaso(cp, paso),
      completar: () => this.completarPaso(cp, paso),
      asignar: () => this.asignarExamenes(cp),
    }[accion];
    if (!guardar) return;
    boton.disabled = true;
    boton.setAttribute("aria-busy", "true");
    guardar().finally(() => {
      boton.disabled = false;
      boton.removeAttribute("aria-busy");
    });
  }
}

customElements.define("app-consulta", AppConsulta);
