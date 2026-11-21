import { definirElemento } from "../base.js";
import { cantidad } from "../texto.js";
import { ahoraISO } from "../fechas.js";
import {
  cargarTodo,
  consultaCompleta,
  recargarTablas,
  serializarObservaciones,
  textoOrden,
} from "../datos.js";
import { crear, actualizar, eliminar } from "../api.js";
import { confirmar } from "./ui-confirmar.js";
import { mostrarTicket } from "./ticket-cita.js";
import {
  PresentacionConsulta,
  valorDeLista,
  guardarEnLista,
  mensajeSegunConexion,
} from "../presentacion-consulta.js";
import "./linea-pasos.js";
import "./ui-autocompletar.js";

class AppConsulta extends PresentacionConsulta {

  connectedCallback() {
    this._id = this.getAttribute("id-consulta");
    this._escucha ??= (evento) => this.manejarClick(evento);
    this._cambio ??= (evento) => this.manejarCambio(evento);
    this.shadowRoot.removeEventListener("click", this._escucha);
    this.shadowRoot.addEventListener("click", this._escucha);
    this.shadowRoot.removeEventListener("change", this._cambio);
    this.shadowRoot.addEventListener("change", this._cambio);
    this._alDatosSincronizados ??= () => this.cargar();
    window.addEventListener("datos-sincronizados", this._alDatosSincronizados);
    this.cargar();
  }

  disconnectedCallback() {
    this._cargaActual = (this._cargaActual ?? 0) + 1;
    window.removeEventListener("datos-sincronizados", this._alDatosSincronizados);
  }

  async cargar({ recursos = null, recargar = false } = {}) {
    const carga = (this._cargaActual ?? 0) + 1;
    this._cargaActual = carga;
    if (!this.isConnected) return;
    // Lo escrito y aún no guardado se conserva al volver a pintar (iniciar paso, asignar exámenes…).
    const borradores = this.leerBorradores();
    const abiertos = this.qa("details.examenes[open]").map((detalle) => detalle.dataset.examenes);
    this.render(`<div class="vacio">Cargando el registro de atención…</div>`);
    try {
      this._datos = recursos ? await recargarTablas(recursos) : await cargarTodo({ recargar });
      if (!this.isConnected || carga !== this._cargaActual) return;
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
      if (!this.isConnected || carga !== this._cargaActual) return;
      this.pintarAviso("No se pudo cargar la consulta. Revise la conexión e intente de nuevo.", true);
    }
  }

  async persistirNotas(item, valores, idPaso = null) {
    const cp = item.consultaProcedimiento;
    const pasos = { ...(item.notas?.pasos ?? {}) };
    if (idPaso !== null) {
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
      observaciones: serializarObservaciones(
        idPaso !== null ? { notas: "", diagnostico: "", plan: "", pasos } : { ...valores, pasos }
      ),
      idConsulta: cp.idConsulta,
    });
  }

  async guardarNotas(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return null;
    const actual = item.pasos.find(({ estado }) => estado === "actual");
    if (!actual || !this.personaRolPara(actual.paso)) {
      this.notificar("Este paso está en espera y corresponde a otro rol.", "error");
      return null;
    }
    const cap = this.capacidadesDePaso(actual.rol);
    const valores = this.leerNotas(idCp);
    if (!cap.clinico) {
      valores.diagnostico = "";
      valores.plan = "";
    }
    await this.persistirNotas(item, valores);
    this.notificar(
      mensajeSegunConexion(
        "Notas del paso guardadas.",
        "Notas guardadas en este dispositivo; se enviarán al recuperar la conexión."
      ),
      "ok"
    );
    await this.cargar({ recursos: ["consultasProcedimiento"] });
    return valores;
  }

  async iniciarPaso(idCp, idPaso) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const paso = item.pasos.find(({ paso }) => String(paso.idProcedimientoPaso) === String(idPaso))?.paso;
    const personaRol = this.personaRolPara(paso);
    if (!personaRol) {
      this.notificar("Su sesión no tiene un rol compatible con esta clínica y este paso.", "error");
      return;
    }
    try {
      // El id se genera aquí para poder completar el paso aunque no haya conexión.
      await crear("consultas-procedimiento-paso", {
        idConsultaProcedimientoPaso: crypto.randomUUID(),
        fechaInicio: ahoraISO(),
        estado: "EN_PROCESO",
        idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
        idPersonaRol: personaRol.idPersonaRol,
      });
      this.notificar(
        mensajeSegunConexion(
          "Paso iniciado. Registre notas y exámenes.",
          "Paso iniciado sin conexión: se enviará al recuperar la red."
        ),
        "ok"
      );
      await this.cargar({ recursos: ["consultasProcedimientoPaso"] });
    } catch (error) {
      this.notificar(`No se pudo iniciar el paso: ${error.message}`, "error");
    }
  }

  async completarPaso(idCp, idPaso) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const actual = item.pasos.find(({ paso }) => String(paso.idProcedimientoPaso) === String(idPaso));
    if (!this.personaRolPara(actual?.paso)) {
      this.notificar("Su sesión no tiene el rol responsable de este paso.", "error");
      return;
    }
    const notas = this.leerNotas(idCp);
    const cap = this.capacidadesDePaso(actual?.rol);
    if (cap.diagnostico && !notas.diagnostico) {
      this.notificar("Elija el diagnóstico CIE-10 de la lista antes de completar el paso.", "error");
      return;
    }
    if (!cap.clinico) {
      notas.diagnostico = "";
      notas.plan = "";
    }
    if (!actual?.registro) {
      this.notificar("Inicie el paso antes de completarlo.", "error");
      return;
    }
    try {
      await this.persistirNotas(item, notas, actual.paso.idProcedimientoPaso);
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
        mensajeSegunConexion(
          "Paso completado. Avance registrado.",
          "Paso completado sin conexión: se enviará al recuperar la red."
        ),
        "ok"
      );
      await this.cargar({ recursos: ["consultasProcedimiento", "consultasProcedimientoPaso"] });
    } catch (error) {
      this.notificar(`No se pudo completar el paso: ${error.message}`, "error");
    }
  }

  async retroceder(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const pasos = item.pasos;
    const indiceActual = pasos.findIndex(({ estado }) => estado === "actual");
    const pasoOperable = indiceActual === -1 ? pasos.at(-1) : pasos[indiceActual];
    if (!this.personaRolPara(pasoOperable?.paso)) {
      this.notificar("Solo el rol responsable del paso actual puede devolverlo.", "error");
      return;
    }
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
        mensajeSegunConexion(
          `Se retrocedió al paso «${previo.paso.nombre}».`,
          "Retroceso guardado sin conexión: se enviará al recuperar la red."
        ),
        "ok"
      );
      await this.cargar({ recursos: ["consultasProcedimientoPaso"] });
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
        mensajeSegunConexion("Consulta finalizada.", "Finalización guardada sin conexión."),
        "ok"
      );
      await this.cargar({ recursos: ["consultas"] });
    } catch (error) {
      this.notificar(`No se pudo finalizar la consulta: ${error.message}`, "error");
    }
  }

  async asignarExamenes(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return;
    const actual = item.pasos.find(({ estado }) => estado === "actual");
    if (!actual?.registro) {
      this.notificar("Inicie el paso antes de asignar exámenes.", "error");
      return;
    }
    if (!this.personaRolPara(actual.paso) || !this.capacidadesDePaso(actual.rol).examenes) {
      this.notificar("Solo el odontólogo puede asignar exámenes en este paso.", "error");
      return;
    }
    const seleccionados = valorDeLista(this._elegidos, idCp);
    const indicaciones = this.q(`[data-cp="${idCp}"] [data-campo="indicaciones"]`)?.value.trim() ?? "";
    try {
      for (const idExamen of seleccionados) {
        const examen = valorDeLista(this._examenes, idCp).find((item) => String(item.examen.idExamen) === idExamen)?.examen;
        await crear("ordenes-examen", {
          idOrdenExamen: crypto.randomUUID(),
          fechaCreacion: ahoraISO(),
          indicaciones: `Examen: ${examen?.nombre ?? idExamen}${indicaciones ? ` — ${indicaciones}` : ""}`,
          idConsultaProcedimientoPaso: actual.registro.idConsultaProcedimientoPaso,
        });
      }
      this.notificar(
        mensajeSegunConexion(
          `${cantidad(seleccionados.length, "examen asignado", "exámenes asignados")}.`,
          `${cantidad(seleccionados.length, "examen guardado", "exámenes guardados")} sin conexión.`
        ),
        "ok"
      );
      await this.cargar({ recursos: ["ordenesExamen"] });
    } catch (error) {
      this.notificar(`No se pudieron asignar los exámenes: ${error.message}`, "error");
    }
  }

  async eliminarOrdenes(idCp) {
    const item = this.buscarProcedimiento(idCp);
    const actual = item?.pasos.find(({ estado }) => estado === "actual");
    if (!item || !actual || !this.personaRolPara(actual.paso) || !this.capacidadesDePaso(actual.rol).examenes) {
      this.notificar("Solo el odontólogo puede modificar los exámenes de este paso.", "error");
      return;
    }
    const ids = this.qa(`[data-cp="${idCp}"] .ordenes input:checked`).map((casilla) => casilla.value);
    const ordenes = this._datos.ordenesExamen.filter((orden) => ids.includes(String(orden.idOrdenExamen)));
    if (ordenes.length === 0) return;
    const nombres = ordenes.map((orden) => `«${textoOrden(orden)}»`).join(", ");
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
        mensajeSegunConexion(
          `${cantidad(ordenes.length, "orden de examen eliminada", "órdenes de examen eliminadas")}.`,
          "Eliminación guardada sin conexión: se enviará al recuperar la red."
        ),
        "ok"
      );
      await this.cargar({ recursos: ["ordenesExamen"] });
    } catch (error) {
      this.notificar(`No se pudo eliminar la orden: ${error.message}`, "error");
    }
  }

  abrirTicket() {
    mostrarTicket(this.shadowRoot, this._cita);
  }

  manejarCambio(evento) {
    const orden = evento.target.closest('.ordenes input[name="orden"]');
    if (orden) return this.actualizarEliminarOrdenes(orden.closest("article.proc").dataset.cp);
    const buscador = evento.target.closest("[data-examen-buscador]");
    if (!buscador?.value) return;
    const idCp = buscador.dataset.examenBuscador;
    guardarEnLista(this._elegidos, idCp, [...valorDeLista(this._elegidos, idCp), String(buscador.value)]);
    this.pintarElegidos(idCp);
  }

  manejarClick(evento) {
    const quitar = evento.target.closest("button[data-quitar-examen]");
    if (quitar) {
      const idCp = quitar.dataset.cp;
      guardarEnLista(this._elegidos, idCp, valorDeLista(this._elegidos, idCp).filter((id) => id !== quitar.dataset.quitarExamen));
      this.pintarElegidos(idCp);
      this.q(`[data-examen-buscador="${idCp}"]`).focus();
      return;
    }
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) return;
    const { accion, cp, paso } = boton.dataset;
    let trabajo;
    switch (accion) {
      case "volver":
        this.navegar("dashboard");
        return;
      case "ticket":
        this.abrirTicket();
        return;
      case "reintentar":
        trabajo = () => this.cargar({ recargar: true });
        break;
      case "guardar-notas":
        trabajo = () => this.guardarNotas(cp);
        break;
      case "iniciar":
        trabajo = () => this.iniciarPaso(cp, paso);
        break;
      case "completar":
        trabajo = () => this.completarPaso(cp, paso);
        break;
      case "retroceder":
        trabajo = () => this.retroceder(cp);
        break;
      case "finalizar":
        trabajo = () => this.finalizarConsulta();
        break;
      case "asignar":
        trabajo = () => this.asignarExamenes(cp);
        break;
      case "eliminar-ordenes":
        trabajo = () => this.eliminarOrdenes(cp);
        break;
      default:
        return;
    }
    if (this._operacionActiva) return;
    this._operacionActiva = true;
    const conservaFoco = ["retroceder", "finalizar", "eliminar-ordenes"].includes(accion);
    this.bloquearAcciones(true, conservaFoco ? boton : null);
    Promise.resolve()
      .then(trabajo)
      .catch((error) => this.notificar(error.message, "error"))
      .finally(() => {
        this._operacionActiva = false;
        this.bloquearAcciones(false);
      });
  }
}

definirElemento("app-consulta", AppConsulta);
