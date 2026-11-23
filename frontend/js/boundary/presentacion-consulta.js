import { Vista, fragmento } from "./base.js";
import { html, normalizarTexto, nombreCompleto } from "../comun/texto.js";
import { fechaHora, fechaCorta } from "../comun/fechas.js";
import { examenesAsignables, textoOrden } from "../control/citas.js";
import { leerObservaciones } from "../entity/observaciones.js";
import { motivoDe, pasosParaPresentacion } from "./presentacion.js";
import { CODIGOS_CIE10 } from "../entity/cie10.js";
import { leerSesion } from "../control/sesion.js";
import { estaConectado } from "../control/pendientes.js";
import { servicioNoResponde } from "../control/api.js";

const OPCIONES_CIE10 = CODIGOS_CIE10.map(({ codigo, descripcion }) => {
  const texto = `${codigo} · ${descripcion}`;
  return { valor: texto, texto };
});

function guardadoSinConexion() {
  return !estaConectado() || servicioNoResponde();
}

export function mensajeSegunConexion(enLinea, sinConexion) {
  return guardadoSinConexion() ? sinConexion : enLinea;
}

/** Línea con quién y cuándo se inició un paso. */
function lineaInicio(registro, persona) {
  return html`<p class="aviso">Iniciado ${fechaHora(registro.fechaInicio)}${persona && ` por ${nombreCompleto(persona)}`}.</p>`;
}

/**
 * Cómo se ve el registro de atención (HU4). AppConsulta hereda de aquí y agrega las acciones.
 * Estado compartido con la subclase: `_cita` y `_datos` (los asigna AppConsulta al cargar),
 * y por procedimiento los exámenes asignables y los elegidos en el buscador.
 */
export class PresentacionConsulta extends Vista {
  _cita = null;
  _datos = null;
  /** idConsultaProcedimiento → exámenes que se pueden asignar en el paso actual. */
  _examenes = new Map();
  /** idConsultaProcedimiento → ids de los exámenes elegidos en el buscador, en orden. */
  _elegidos = new Map();

  examenesDe(idCp) {
    return this._examenes.get(String(idCp)) ?? [];
  }

  elegidosDe(idCp) {
    return this._elegidos.get(String(idCp)) ?? [];
  }

  ponerElegidos(idCp, ids) {
    this._elegidos.set(String(idCp), ids);
    this.pintarElegidos(idCp);
  }

  pintarAviso(mensaje, reintentar = false) {
    this.render(html`
      <div class="vacio aviso-carga" role="alert">
        <p>${mensaje}</p>
        <div class="fila">
          <button type="button" class="secundario" data-accion="volver">← Volver al panel</button>
          ${reintentar && html`<button type="button" data-accion="reintentar">Reintentar</button>`}
        </div>
      </div>
    `);
  }

  pintar() {
    const { consulta, paciente, clinica, procedimientos } = this._cita;
    this._examenes = new Map();
    this._elegidos = new Map();
    const pasos = procedimientos.flatMap(({ pasos: lista }) => lista);
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const todoCompleto = pasos.length > 0 && completados === pasos.length;
    const finalizada = Boolean(consulta.fechaFin);
    const estadoGeneral = finalizada ? "Finalizada" : todoCompleto ? "Completada" : "En proceso";
    const datosConsulta = [
      paciente?.edad !== null && paciente?.edad !== undefined ? `${paciente.edad} años` : "",
      clinica?.nombre ?? "Clínica",
      fechaHora(consulta.fechaInicio),
      motivoDe(consulta),
    ]
      .filter(Boolean)
      .join(" · ");
    // Con todos los pasos completos, finalizar es la acción principal.
    const cierre = finalizada
      ? html`<span class="estado-pill completada">Finalizada ${fechaCorta(consulta.fechaFin)}</span>`
      : todoCompleto
        ? html`<button type="button" data-accion="finalizar">Finalizar consulta</button>`
        : html`<button type="button" class="secundario" data-accion="finalizar">Finalizar consulta</button>`;
    const tarjetas = procedimientos.map((item) => this.pintarProcedimiento(item));
    this.render(html`
      <button type="button" class="secundario volver" data-accion="volver">← Volver al panel</button>
      <section class="tarjeta">
        <div class="encabezado">
          <div class="paciente">
            <div class="titulo">
              <h2>${nombreCompleto(paciente) || "Paciente"}</h2>
              <span class="estado-pill ${finalizada || todoCompleto ? "completada" : "proceso"}">${estadoGeneral}</span>
            </div>
            <p class="datos">${datosConsulta}</p>
          </div>
          <div class="separador"></div>
          <div class="fila">
            <button type="button" class="secundario" data-accion="ticket">Ticket</button>
            ${cierre}
          </div>
        </div>
      </section>
      <section class="procedimientos">
        ${tarjetas.length ? tarjetas : html`<div class="vacio">La consulta no tiene procedimientos asignados.</div>`}
      </section>
    `);
    for (const linea of this.qa("linea-pasos[data-cp]")) {
      linea.pasos = pasosParaPresentacion(this.buscarProcedimiento(linea.dataset.cp)?.pasos ?? []);
    }
    for (const campo of this.qa('ui-autocompletar[data-campo="diagnostico"]')) {
      campo.opciones = OPCIONES_CIE10;
    }
    for (const buscador of this.qa("ui-autocompletar[data-examen-buscador]")) {
      this.pintarElegidos(buscador.dataset.examenBuscador);
    }
  }

  pintarProcedimiento({ consultaProcedimiento: cp, procedimiento, pasos }) {
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const finalizado = pasos.length > 0 && completados === pasos.length;
    const actual = pasos.find(({ estado }) => estado === "actual") ?? null;
    const hayRegistros = pasos.some(({ registro }) => registro);
    // Se retrocede al paso anterior si el actual no es el primero, o al último si todos están completos.
    const puedeRetroceder = finalizado ? hayRegistros : (actual?.indice ?? 0) > 0;
    const esUltimo = actual ? actual.indice === pasos.length - 1 : false;
    const notas = this.notasDelFormulario(cp, actual);
    return html`
      <article class="tarjeta proc" data-cp="${cp.idConsultaProcedimiento}">
        <div class="fila">
          <h3>${procedimiento?.nombre ?? "Procedimiento"}</h3>
          <span class="separador"></span>
          ${finalizado && html`<span class="estado-pill completada">Completado</span>`}
        </div>
        <linea-pasos data-cp="${cp.idConsultaProcedimiento}"></linea-pasos>
        ${finalizado ? this.panelFinalizado(cp, puedeRetroceder) : this.panelPaso(cp, actual, puedeRetroceder, esUltimo, pasos.length, notas)}
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

  /** El odontólogo/médico registra el diagnóstico, el plan y los exámenes; los demás roles solo notas. */
  capacidadesDePaso(rol) {
    const nombre = normalizarTexto(rol?.nombre);
    const clinico = /odont|doctor|medic/.test(nombre);
    const enfermeria = /enfermer/.test(nombre);
    return {
      clinico,
      diagnostico: clinico,
      plan: clinico,
      examenes: clinico,
      etiquetaNotas: clinico ? "Notas de evolución" : enfermeria ? "Signos vitales" : "Notas del paso",
      ayudaNotas: clinico
        ? "Hallazgos, evolución y observaciones clínicas"
        : enfermeria
          ? "Presión arterial, frecuencia cardíaca, temperatura, saturación y observaciones"
          : "Motivo de consulta, alergias y observaciones de recepción",
    };
  }

  /** Registro del paso: notas para todos; diagnóstico, plan y exámenes solo para el rol clínico. */
  formularioNotas(cp, notas, capacidades) {
    const id = cp.idConsultaProcedimiento;
    const cap = capacidades ?? this.capacidadesDePaso(null);
    return html`
      <section class="bloque" aria-labelledby="reg-${id}">
        <h5 id="reg-${id}">Registro del paso</h5>
        <div class="campo">
          <label for="notas-${id}">${cap.etiquetaNotas}</label>
          <textarea data-campo="notas" id="notas-${id}" placeholder="${cap.ayudaNotas}">${notas.notas}</textarea>
        </div>
        ${cap.clinico &&
        html`<div class="rejilla dos">
          <ui-autocompletar data-campo="diagnostico" id="diag-${id}" etiqueta="Diagnóstico CIE-10 (obligatorio para completar el paso)" value="${notas.diagnostico}"></ui-autocompletar>
          <div class="campo">
            <label for="plan-${id}">Plan de atención</label>
            <input data-campo="plan" id="plan-${id}" value="${notas.plan}" placeholder="Indicaciones y próximos controles" />
          </div>
        </div>`}
        <div class="fila">
          <button type="button" class="secundario" data-accion="guardar-notas" data-cp="${id}">Guardar notas</button>
        </div>
      </section>
    `;
  }

  botonAnterior(idCp) {
    return html`<button type="button" class="secundario" data-accion="retroceder" data-cp="${idCp}">← Paso anterior</button>`;
  }

  /** Con todos los pasos completos no hay dónde registrar notas: lo registrado se corrige retrocediendo. */
  panelFinalizado(cp, puedeRetroceder) {
    const { fechaFin } = this._cita.consulta;
    return html`
      <div class="paso-panel completada">
        <h4>Todos los pasos están completados</h4>
        <p class="aviso">${
          fechaFin
            ? `Consulta finalizada el ${fechaHora(fechaFin)}.`
            : "Si la atención terminó, use «Finalizar consulta». Para corregir un paso, vuelva al anterior."
        }</p>
        ${puedeRetroceder && !fechaFin && html`<div class="acciones-paso pie">${this.botonAnterior(cp.idConsultaProcedimiento)}</div>`}
      </div>
    `;
  }

  panelPaso(cp, actual, puedeRetroceder, esUltimo, total, notas) {
    if (this._cita.consulta.fechaFin) {
      return html`<div class="paso-panel"><p class="aviso">La consulta está finalizada y se muestra en modo de solo lectura.</p></div>`;
    }
    if (!actual) {
      return html`<div class="paso-panel"><p class="aviso">Este procedimiento no tiene un paso pendiente.</p></div>`;
    }
    const { paso, registro, rol, persona, indice } = actual;
    const idCp = cp.idConsultaProcedimiento;
    const cap = this.capacidadesDePaso(rol);
    const cabecera = html`
        <div class="paso-cabecera">
          <h4>Paso actual: ${paso.nombre}</h4>
          <span class="meta">Paso ${indice + 1} de ${total}${rol && ` · ${rol.nombre}`}</span>
        </div>`;
    // Cada paso solo puede operarse con el rol asignado: los demás quedan a la espera.
    if (!this.personaRolPara(paso)) {
      return html`
      <div class="paso-panel espera">
        ${cabecera}
        <p class="aviso" role="status">
          <strong>Paso en espera.</strong> Este paso lo atiende ${rol?.nombre ?? "otro rol"}.
          Cambie de rol en el panel para continuar cuando corresponda.
        </p>
        ${registro ? lineaInicio(registro, persona) : html`<p class="aviso">Aún no se ha iniciado este paso.</p>`}
      </div>`;
    }
    const examenes = cap.examenes ? examenesAsignables(this._datos, cp.idProcedimiento, paso.idProcedimientoPaso) : [];
    this._examenes.set(String(idCp), examenes);
    const ordenes = registro
      ? this._datos.ordenesExamen.filter(
          (orden) => String(orden.idConsultaProcedimientoPaso) === String(registro.idConsultaProcedimientoPaso)
        )
      : [];
    const anterior = puedeRetroceder && this.botonAnterior(idCp);
    // Sin iniciar, la acción principal va arriba; iniciado, «Completar» va al final del registro.
    const acciones = registro
      ? html`<div class="acciones-paso pie">${anterior}<span class="separador"></span>
           <button type="button" data-accion="completar" data-cp="${idCp}" data-paso="${paso.idProcedimientoPaso}">${esUltimo ? "Completar último paso" : "Guardar y avanzar al siguiente paso"}</button>
         </div>`
      : html`<div class="acciones-paso">
           <button type="button" data-accion="iniciar" data-cp="${idCp}" data-paso="${paso.idProcedimientoPaso}">Iniciar paso</button>
           ${anterior}
         </div>`;
    return html`
      <div class="paso-panel">
        ${cabecera}
        ${registro
          ? lineaInicio(registro, persona)
          : html`<p class="aviso">Aún no se ha iniciado este paso. ${
              cap.examenes
                ? "Inícielo para registrar las notas y poder asignar exámenes."
                : "Inícielo para registrar las notas y poder completarlo."
            }</p>`}
        ${!registro && acciones}
        ${this.formularioNotas(cp, notas, cap)}
        ${cap.examenes && this.bloqueExamenes(idCp, registro, examenes, ordenes)}
        ${registro && acciones}
      </div>
    `;
  }

  /** Exámenes del paso: buscador para elegirlos, indicaciones y las órdenes ya registradas. */
  bloqueExamenes(idCp, registro, examenes, ordenes) {
    const cuantas = ordenes.length ? ` · ${ordenes.length} ${ordenes.length === 1 ? "orden" : "órdenes"}` : "";
    let asignacion;
    if (!registro) asignacion = html`<p class="aviso">Inicie el paso para poder asignar exámenes.</p>`;
    else if (examenes.length === 0) asignacion = html`<p class="aviso">No hay exámenes configurados para este procedimiento.</p>`;
    else {
      asignacion = html`<ui-autocompletar id="examen-${idCp}" data-examen-buscador="${idCp}" etiqueta="Buscar examen por nombre o categoría"></ui-autocompletar>
                   <ul class="elegidos" data-elegidos="${idCp}" aria-label="Exámenes elegidos"></ul>
                   <div class="asignacion" hidden>
                     <div class="campo">
                       <label for="ind-${idCp}">Indicaciones para los exámenes</label>
                       <input id="ind-${idCp}" data-campo="indicaciones" placeholder="Ej. Radiografía periapical del sector" />
                     </div>
                     <div class="fila"><button type="button" class="secundario" data-accion="asignar" data-cp="${idCp}">Asignar exámenes seleccionados</button></div>
                   </div>`;
    }
    return html`<details class="bloque examenes" data-examenes="${idCp}">
          <summary>Exámenes del paso <small>(opcional)${cuantas}</small></summary>
          ${asignacion}
          ${ordenes.length > 0 &&
          html`<div class="ordenes">
                   <div class="barra-ordenes">
                     <span class="etiqueta" id="ord-${idCp}">Órdenes registradas</span>
                     <button type="button" class="peligro" data-accion="eliminar-ordenes" data-cp="${idCp}" disabled>Eliminar seleccionadas</button>
                   </div>
                   <ul role="group" aria-labelledby="ord-${idCp}">${ordenes.map((orden) => this.pintarOrden(orden))}</ul>
                 </div>`}
        </details>`;
  }

  /** Orden de examen del paso en curso: se selecciona para eliminarla si se registró por error y aún no tiene resultado. */
  pintarOrden(orden) {
    const tieneResultado = this._datos.examenesResultado.some(
      (resultado) => String(resultado.idOrdenExamen) === String(orden.idOrdenExamen)
    );
    return html`
      <li>
        <label>
          <input type="checkbox" name="orden" value="${orden.idOrdenExamen}" ${tieneResultado && html`disabled`} />
          <span>${fechaHora(orden.fechaCreacion)} · ${textoOrden(orden)}</span>
          ${tieneResultado && html`<span class="con-resultado">Con resultado</span>`}
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
    const examenes = this.examenesDe(idCp);
    const elegidos = this.elegidosDe(idCp);
    // En el orden en que se eligieron.
    const items = elegidos
      .map((id) => examenes.find(({ examen }) => String(examen.idExamen) === id))
      .filter(Boolean)
      .map(
        ({ examen }) =>
          html`<li><span>${examen.nombre}</span><button type="button" class="quitar" data-quitar-examen="${examen.idExamen}" data-cp="${idCp}" aria-label="Quitar ${examen.nombre}">×</button></li>`
      );
    this.q(`[data-elegidos="${idCp}"]`).replaceChildren(fragmento(html`${items}`));
    const buscador = this.q(`[data-examen-buscador="${idCp}"]`);
    const disponibles = examenes.filter(({ examen }) => !elegidos.includes(String(examen.idExamen)));
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
    return this.qa("article.proc").map((articulo) => ({
      id: articulo.dataset.cp,
      valores: this.leerNotas(articulo.dataset.cp),
    }));
  }

  aplicarBorradores(borradores) {
    for (const { id: idCp, valores } of borradores) {
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

  personaRolPara(paso) {
    const sesion = leerSesion();
    if (!sesion?.roles?.length) return null;
    const idClinica = this._cita.clinica?.idClinica;
    return sesion.roles.find(
      (rol) =>
        String(rol.idClinica) === String(idClinica) &&
        (!paso?.idRol || String(rol.idRol) === String(paso.idRol))
    );
  }

  bloquearAcciones(bloquear, conservar = null) {
    for (const boton of this.qa('button[data-accion]:not([data-accion="volver"]):not([data-accion="ticket"])')) {
      boton.disabled = bloquear && boton !== conservar;
      if (bloquear) boton.setAttribute("aria-busy", "true");
      else boton.removeAttribute("aria-busy");
    }
  }
}
