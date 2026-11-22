import { Vista } from "./base.js";
import { escapar, normalizarTexto, nombreCompleto } from "./texto.js";
import { fechaHora, fechaCorta } from "./fechas.js";
import { examenesAsignables, leerObservaciones, motivoDe, textoOrden } from "./datos.js";
import { pasosParaPresentacion } from "./presentacion.js";
import { CODIGOS_CIE10 } from "./cie10.js";
import { leerSesion } from "./estado.js";
import { estaConectado } from "./pendientes.js";
import { servicioNoResponde } from "./api.js";

const OPCIONES_CIE10 = CODIGOS_CIE10.map(({ codigo, descripcion }) => {
  const texto = `${codigo} · ${descripcion}`;
  return { valor: texto, texto };
});

export function valorDeLista(lista, id) {
  return lista.find((item) => item.id === String(id))?.valor ?? [];
}

export function guardarEnLista(lista, id, valor) {
  const clave = String(id);
  const existente = lista.find((item) => item.id === clave);
  if (existente) existente.valor = valor;
  else lista.push({ id: clave, valor });
}

function guardadoSinConexion() {
  return !estaConectado() || servicioNoResponde();
}

export function mensajeSegunConexion(enLinea, sinConexion) {
  return guardadoSinConexion() ? sinConexion : enLinea;
}

export class PresentacionConsulta extends Vista {

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
    this._examenes = [];
    this._elegidos = [];
    const pasos = procedimientos.flatMap(({ pasos: lista }) => lista);
    const completados = pasos.filter(({ estado }) => estado === "completado").length;
    const todoCompleto = pasos.length > 0 && completados === pasos.length;
    const finalizada = Boolean(consulta.fechaFin);
    const estadoGeneral = finalizada ? "Finalizada" : todoCompleto ? "Completada" : "En proceso";
    this.render(`
      <button type="button" class="secundario volver" data-accion="volver">← Volver al panel</button>
      <section class="tarjeta">
        <div class="encabezado">
          <div class="paciente">
            <div class="titulo">
              <h2>${escapar(nombreCompleto(paciente) || "Paciente")}</h2>
              <span class="estado-pill ${finalizada || todoCompleto ? "completada" : "proceso"}">${escapar(estadoGeneral)}</span>
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
            <button type="button" class="secundario" data-accion="ticket">Ticket</button>
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
      linea.pasos = pasosParaPresentacion(this.buscarProcedimiento(linea.dataset.cp)?.pasos ?? []);
    }
    for (const campo of this.qa('ui-autocompletar[data-campo="diagnostico"]')) {
      campo.opciones = OPCIONES_CIE10;
      campo.value = campo.dataset.valor;
    }
    for (const buscador of this.qa("ui-autocompletar[data-examen-buscador]")) {
      this.pintarElegidos(buscador.dataset.examenBuscador);
    }
    if (this._operacionActiva) this.bloquearAcciones(true);
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
    const id = escapar(cp.idConsultaProcedimiento);
    const cap = capacidades ?? this.capacidadesDePaso(null);
    return `
      <section class="bloque" aria-labelledby="reg-${id}">
        <h5 id="reg-${id}">Registro del paso</h5>
        <div class="campo">
          <label for="notas-${id}">${escapar(cap.etiquetaNotas)}</label>
          <textarea data-campo="notas" id="notas-${id}" placeholder="${escapar(cap.ayudaNotas)}">${escapar(notas.notas)}</textarea>
        </div>
        ${
          cap.clinico
            ? `<div class="rejilla dos">
                 <ui-autocompletar data-campo="diagnostico" id="diag-${id}" etiqueta="Diagnóstico CIE-10 (obligatorio para completar el paso)" data-valor="${escapar(notas.diagnostico)}"></ui-autocompletar>
                 <div class="campo">
                   <label for="plan-${id}">Plan de atención</label>
                   <input data-campo="plan" id="plan-${id}" value="${escapar(notas.plan)}" placeholder="Indicaciones y próximos controles" />
                 </div>
               </div>`
            : ""
        }
        <div class="fila">
          <button type="button" class="secundario" data-accion="guardar-notas" data-cp="${id}">Guardar notas</button>
        </div>
      </section>
    `;
  }

  botonAnterior(idCp) {
    return `<button type="button" class="secundario" data-accion="retroceder" data-cp="${idCp}">← Paso anterior</button>`;
  }

  /** Con todos los pasos completos no hay dónde registrar notas: lo registrado se corrige retrocediendo. */
  panelFinalizado(cp, puedeRetroceder) {
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
        ${puedeRetroceder && !finalizada ? `<div class="acciones-paso pie">${this.botonAnterior(idCp)}</div>` : ""}
      </div>
    `;
  }

  panelPaso(cp, actual, puedeRetroceder, esUltimo, total, notas) {
    if (this._cita.consulta.fechaFin) {
      return `<div class="paso-panel"><p class="aviso">La consulta está finalizada y se muestra en modo de solo lectura.</p></div>`;
    }
    if (!actual) {
      return `<div class="paso-panel"><p class="aviso">Este procedimiento no tiene un paso pendiente.</p></div>`;
    }
    const { paso, registro, rol, persona, indice } = actual;
    const idCp = escapar(cp.idConsultaProcedimiento);
    const cap = this.capacidadesDePaso(rol);
    const cabecera = `
        <div class="paso-cabecera">
          <h4>Paso actual: ${escapar(paso.nombre)}</h4>
          <span class="meta">Paso ${indice + 1} de ${total}${rol ? ` · ${escapar(rol.nombre)}` : ""}</span>
        </div>`;
    // Cada paso solo puede operarse con el rol asignado: los demás quedan a la espera.
    if (!this.personaRolPara(paso)) {
      return `
      <div class="paso-panel espera">
        ${cabecera}
        <p class="aviso" role="status">
          <strong>Paso en espera.</strong> Este paso lo atiende ${escapar(rol?.nombre ?? "otro rol")}.
          Cambie de rol en el panel para continuar cuando corresponda.
        </p>
        ${registro ? `<p class="aviso">Iniciado ${escapar(fechaHora(registro.fechaInicio))}${persona ? ` por ${escapar(nombreCompleto(persona))}` : ""}.</p>` : '<p class="aviso">Aún no se ha iniciado este paso.</p>'}
      </div>`;
    }
    const examenes = cap.examenes ? examenesAsignables(this._datos, cp.idProcedimiento, paso.idProcedimientoPaso) : [];
    guardarEnLista(this._examenes, cp.idConsultaProcedimiento, examenes);
    const ordenes = registro
      ? this._datos.ordenesExamen.filter(
          (orden) => String(orden.idConsultaProcedimientoPaso) === String(registro.idConsultaProcedimientoPaso)
        )
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
    const examenesHtml = cap.examenes
      ? `<details class="bloque examenes" data-examenes="${idCp}">
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
        </details>`
      : "";
    return `
      <div class="paso-panel">
        ${cabecera}
        ${
          registro
            ? `<p class="aviso">Iniciado ${escapar(fechaHora(registro.fechaInicio))}${persona ? ` por ${escapar(nombreCompleto(persona))}` : ""}.</p>`
            : `<p class="aviso">Aún no se ha iniciado este paso. ${
                cap.examenes
                  ? "Inícielo para registrar las notas y poder asignar exámenes."
                  : "Inícielo para registrar las notas y poder completarlo."
              }</p>`
        }
        ${registro ? "" : acciones}
        ${this.formularioNotas(cp, notas, cap)}
        ${examenesHtml}
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
          <span>${escapar(fechaHora(orden.fechaCreacion))} · ${escapar(textoOrden(orden))}</span>
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
    const examenes = valorDeLista(this._examenes, idCp);
    const elegidos = valorDeLista(this._elegidos, idCp);
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

  /** Nombre del empleado autenticado, para la bitacora del paso. */
  _nombreSesion() {
    return leerSesion()?.nombre ?? "";
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
