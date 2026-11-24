import { definirElemento } from "../base.js";
import { cantidad, html } from "../../comun/texto.js";
import { ahoraISO } from "../../comun/fechas.js";
import { leerSesion } from "../../control/sesion.js";
import { cargarTodo, recargarTablas } from "../../control/repositorio.js";
import { consultaCompleta, textoOrden } from "../../control/citas.js";
import { serializarObservaciones } from "../../entity/observaciones.js";
import { servicios } from "../../control/servicios.js";
import { confirmar } from "./ui-confirmar.js";
import { mostrarTicket } from "./ticket-cita.js";
import PresentacionConsulta, { mensajeSegunConexion } from "../presentacion-consulta.js";
import "./linea-pasos.js";
import "./ui-autocompletar.js";

/**
 * HU4 · Registro de atención: inicia, completa y retrocede pasos, guarda notas y diagnóstico,
 * asigna exámenes y finaliza la consulta. Lo que se ve lo pinta PresentacionConsulta.
 */
export default class AppConsulta extends PresentacionConsulta {
  #id = null;
  /** Número de la carga en curso: una respuesta vieja no pisa a una más reciente. */
  #carga = 0;
  /** Mientras una acción se ejecuta no se aceptan otras. */
  #operacionActiva = false;

  connectedCallback() {
    this.#id = this.getAttribute("id-consulta");
    this.escuchar(this.shadowRoot, "click", (evento) => this.manejarClick(evento));
    this.escuchar(this.shadowRoot, "change", (evento) => this.manejarCambio(evento));
    this.escuchar(window, "datos-sincronizados", () => this.cargar());
    this.cargar();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.#carga += 1;
  }

  cargar({ recursos = null, recargar = false } = {}) {
    const carga = ++this.#carga;
    if (!this.isConnected) return Promise.resolve();
    // «Cargando» solo la primera vez: al recargar tras una acción la pantalla se conserva hasta tener los datos.
    if (!this.q("article.proc")) this.render(html`<div class="vacio">Cargando el registro de atención…</div>`);
    return (recursos ? recargarTablas(recursos) : cargarTodo({ recargar }))
      .then((datos) => {
        this._datos = datos;
        if (!this.isConnected || carga !== this.#carga) return;
        this._cita = consultaCompleta(this._datos, this.#id);
        if (!this._cita) {
          this.pintarAviso("La consulta solicitada no existe.");
          return;
        }
        // Lo escrito y aún no guardado, y los exámenes abiertos, se conservan al volver a pintar.
        const borradores = this.leerBorradores();
        const abiertos = this.qa("details.examenes[open]").map((detalle) => detalle.dataset.examenes);
        this.pintar();
        if (this.#operacionActiva) this.bloquearAcciones(true);
        this.aplicarBorradores(borradores);
        for (const idCp of abiertos) {
          const detalle = this.q(`details.examenes[data-examenes="${idCp}"]`);
          if (detalle) detalle.open = true;
        }
      })
      .catch(() => {
        if (!this.isConnected || carga !== this.#carga) return;
        this.pintarAviso("No se pudo cargar la consulta. Revise la conexión e intente de nuevo.", true);
      });
  }

  persistirNotas(item, valores, idPaso = null) {
    return new Promise((resolve) => {
      const cp = item.consultaProcedimiento;
      const pasos = { ...(item.notas?.pasos ?? {}) };
      if (idPaso !== null) {
        pasos[idPaso] = {
          notas: valores.notas,
          diagnostico: valores.diagnostico,
          plan: valores.plan,
          fecha: ahoraISO(),
          responsable: leerSesion()?.nombre ?? "",
        };
      }
      resolve(
        servicios.consultasProcedimiento.modificar(cp.idConsultaProcedimiento, {
          idProcedimiento: cp.idProcedimiento,
          fechaInicio: cp.fechaInicio,
          fechaFin: cp.fechaFin,
          // Al completar un paso su registro pasa a `pasos` y el borrador queda vacío para el siguiente.
          observaciones: serializarObservaciones(
            idPaso !== null ? { notas: "", diagnostico: "", plan: "", pasos } : { ...valores, pasos }
          ),
          idConsulta: cp.idConsulta,
        })
      );
    });
  }

  guardarNotas(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return Promise.resolve(null);
    const actual = item.pasos.find(({ estado }) => estado === "actual");
    if (!actual || !this.personaRolPara(actual.paso)) {
      this.notificar("Este paso está en espera y corresponde a otro rol.", "error");
      return Promise.resolve(null);
    }
    const cap = this.capacidadesDePaso(actual.rol);
    const valores = this.leerNotas(idCp);
    if (!cap.clinico) {
      valores.diagnostico = "";
      valores.plan = "";
    }
    return this.persistirNotas(item, valores)
      .then(() => {
        this.notificar(
          mensajeSegunConexion(
            "Notas del paso guardadas.",
            "Notas guardadas en este dispositivo; se enviarán al recuperar la conexión."
          ),
          "ok"
        );
        return this.cargar({ recursos: ["consultasProcedimiento"] });
      })
      .then(() => valores);
  }

  iniciarPaso(idCp, idPaso) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return Promise.resolve();
    const paso = item.pasos.find(({ paso }) => String(paso.idProcedimientoPaso) === String(idPaso))?.paso;
    const personaRol = this.personaRolPara(paso);
    if (!personaRol) {
      this.notificar("Su sesión no tiene un rol compatible con esta clínica y este paso.", "error");
      return Promise.resolve();
    }
    // El id se genera aquí para poder completar el paso aunque no haya conexión.
    return servicios.consultasProcedimientoPaso.crear({
      idConsultaProcedimientoPaso: crypto.randomUUID(),
      fechaInicio: ahoraISO(),
      estado: "EN_PROCESO",
      idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
      idPersonaRol: personaRol.idPersonaRol,
    })
      .then(() => {
        this.notificar(
          mensajeSegunConexion(
            "Paso iniciado. Registre notas y exámenes.",
            "Paso iniciado sin conexión: se enviará al recuperar la red."
          ),
          "ok"
        );
        return this.cargar({ recursos: ["consultasProcedimientoPaso"] });
      })
      .catch((error) => this.notificar(`No se pudo iniciar el paso: ${error.message}`, "error"));
  }

  completarPaso(idCp, idPaso) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return Promise.resolve();
    const actual = item.pasos.find(({ paso }) => String(paso.idProcedimientoPaso) === String(idPaso));
    if (!this.personaRolPara(actual?.paso)) {
      this.notificar("Su sesión no tiene el rol responsable de este paso.", "error");
      return Promise.resolve();
    }
    const notas = this.leerNotas(idCp);
    const cap = this.capacidadesDePaso(actual?.rol);
    if (cap.diagnostico && !notas.diagnostico) {
      this.notificar("Elija el diagnóstico CIE-10 de la lista antes de completar el paso.", "error");
      return Promise.resolve();
    }
    if (!cap.clinico) {
      notas.diagnostico = "";
      notas.plan = "";
    }
    if (!actual?.registro) {
      this.notificar("Inicie el paso antes de completarlo.", "error");
      return Promise.resolve();
    }
    const registro = actual.registro;
    return this.persistirNotas(item, notas, actual.paso.idProcedimientoPaso)
      .then(() =>
        servicios.consultasProcedimientoPaso.modificar(registro.idConsultaProcedimientoPaso, {
          fechaInicio: registro.fechaInicio,
          fechaFin: ahoraISO(),
          estado: "FINALIZADO",
          idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
          idPersonaRol: registro.idPersonaRol,
        })
      )
      .then(() => {
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
        return this.cargar({ recursos: ["consultasProcedimiento", "consultasProcedimientoPaso"] });
      })
      .catch((error) => this.notificar(`No se pudo completar el paso: ${error.message}`, "error"));
  }

  retroceder(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return Promise.resolve();
    const pasos = item.pasos;
    const indiceActual = pasos.findIndex(({ estado }) => estado === "actual");
    const pasoOperable = indiceActual === -1 ? pasos.at(-1) : pasos[indiceActual];
    if (!this.personaRolPara(pasoOperable?.paso)) {
      this.notificar("Solo el rol responsable del paso actual puede devolverlo.", "error");
      return Promise.resolve();
    }
    // Sin paso en curso todos están completos: se reabre el último.
    const indicePrevio = indiceActual === -1 ? pasos.length - 1 : indiceActual - 1;
    const previo = pasos[indicePrevio];
    if (!previo?.registro) {
      this.notificar("No hay un paso anterior al que retroceder.", "info");
      return Promise.resolve();
    }
    return confirmar({
      titulo: `¿Retroceder al paso «${previo.paso.nombre}»?`,
      mensaje: "El paso volverá a quedar en curso para corregir su registro. No se borra nada de lo registrado.",
      aceptar: "Retroceder",
    }).then((aceptado) => {
      if (!aceptado) return;
      return servicios.consultasProcedimientoPaso.modificar(previo.registro.idConsultaProcedimientoPaso, {
        fechaInicio: previo.registro.fechaInicio,
        estado: "PENDIENTE",
        idConsultaProcedimiento: item.consultaProcedimiento.idConsultaProcedimiento,
        idPersonaRol: previo.registro.idPersonaRol,
      })
        .then(() => {
          this.notificar(
            mensajeSegunConexion(
              `Se retrocedió al paso «${previo.paso.nombre}».`,
              "Retroceso guardado sin conexión: se enviará al recuperar la red."
            ),
            "ok"
          );
          return this.cargar({ recursos: ["consultasProcedimientoPaso"] });
        })
        .catch((error) => this.notificar(`No se pudo retroceder: ${error.message}`, "error"));
    });
  }

  finalizarConsulta() {
    const consulta = this._cita.consulta;
    const sinCompletar = this._cita.procedimientos
      .flatMap(({ pasos }) => pasos)
      .filter(({ estado }) => estado !== "completado").length;
    return confirmar({
      titulo: "¿Finalizar la consulta?",
      mensaje:
        sinCompletar > 0
          ? `Quedan ${sinCompletar} ${sinCompletar === 1 ? "paso" : "pasos"} sin completar. Si finaliza, se registrará la fecha y hora de cierre de todos modos.`
          : "Se registrará la fecha y hora de cierre de la consulta.",
      aceptar: "Finalizar",
      peligro: sinCompletar > 0,
    }).then((aceptado) => {
      if (!aceptado) return;
      return servicios.consultas.modificar(consulta.idConsulta, {
        fechaInicio: consulta.fechaInicio,
        fechaFin: ahoraISO(),
        observaciones: consulta.observaciones ?? "",
        referenciaExterna: consulta.referenciaExterna ?? "",
        idPersonaRol: consulta.idPersonaRol,
      })
        .then(() => {
          this.notificar(
            mensajeSegunConexion("Consulta finalizada.", "Finalización guardada sin conexión."),
            "ok"
          );
          return this.cargar({ recursos: ["consultas"] });
        })
        .catch((error) => this.notificar(`No se pudo finalizar la consulta: ${error.message}`, "error"));
    });
  }

  asignarExamenes(idCp) {
    const item = this.buscarProcedimiento(idCp);
    if (!item) return Promise.resolve();
    const actual = item.pasos.find(({ estado }) => estado === "actual");
    if (!actual?.registro) {
      this.notificar("Inicie el paso antes de asignar exámenes.", "error");
      return Promise.resolve();
    }
    if (!this.personaRolPara(actual.paso) || !this.capacidadesDePaso(actual.rol).examenes) {
      this.notificar("Solo el odontólogo puede asignar exámenes en este paso.", "error");
      return Promise.resolve();
    }
    const seleccionados = this.elegidosDe(idCp);
    const indicaciones = this.q(`[data-cp="${idCp}"] [data-campo="indicaciones"]`)?.value.trim() ?? "";
    // Las órdenes se crean una tras otra, en el orden en que se eligieron.
    const crearOrdenes = seleccionados.reduce(
      (anterior, idExamen) =>
        anterior.then(() => {
          const examen = this.examenesDe(idCp).find((item) => String(item.examen.idExamen) === idExamen)?.examen;
          return servicios.ordenesExamen.crear({
            idOrdenExamen: crypto.randomUUID(),
            fechaCreacion: ahoraISO(),
            indicaciones: `Examen: ${examen?.nombre ?? idExamen}${indicaciones ? ` — ${indicaciones}` : ""}`,
            idConsultaProcedimientoPaso: actual.registro.idConsultaProcedimientoPaso,
          });
        }),
      Promise.resolve()
    );
    return crearOrdenes
      .then(() => {
        this.notificar(
          mensajeSegunConexion(
            `${cantidad(seleccionados.length, "examen asignado", "exámenes asignados")}.`,
            `${cantidad(seleccionados.length, "examen guardado", "exámenes guardados")} sin conexión.`
          ),
          "ok"
        );
        return this.cargar({ recursos: ["ordenesExamen"] });
      })
      .catch((error) => this.notificar(`No se pudieron asignar los exámenes: ${error.message}`, "error"));
  }

  eliminarOrdenes(idCp) {
    const item = this.buscarProcedimiento(idCp);
    const actual = item?.pasos.find(({ estado }) => estado === "actual");
    if (!item || !actual || !this.personaRolPara(actual.paso) || !this.capacidadesDePaso(actual.rol).examenes) {
      this.notificar("Solo el odontólogo puede modificar los exámenes de este paso.", "error");
      return Promise.resolve();
    }
    const ids = this.qa(`[data-cp="${idCp}"] .ordenes input:checked`).map((casilla) => casilla.value);
    const ordenes = this._datos.ordenesExamen.filter((orden) => ids.includes(String(orden.idOrdenExamen)));
    if (ordenes.length === 0) return Promise.resolve();
    const nombres = ordenes.map((orden) => `«${textoOrden(orden)}»`).join(", ");
    return confirmar({
      titulo: ordenes.length === 1 ? "¿Eliminar la orden de examen?" : `¿Eliminar ${ordenes.length} órdenes de examen?`,
      mensaje: `Se eliminará de este paso: ${nombres}. Esta acción no se puede deshacer.`,
      aceptar: "Eliminar",
      peligro: true,
    }).then((aceptado) => {
      if (!aceptado) return;
      // Se eliminan una tras otra, igual que se crearon.
      return ordenes
        .reduce((anterior, orden) => anterior.then(() => servicios.ordenesExamen.eliminar(orden.idOrdenExamen)), Promise.resolve())
        .then(() => {
          this.notificar(
            mensajeSegunConexion(
              `${cantidad(ordenes.length, "orden de examen eliminada", "órdenes de examen eliminadas")}.`,
              "Eliminación guardada sin conexión: se enviará al recuperar la red."
            ),
            "ok"
          );
          return this.cargar({ recursos: ["ordenesExamen"] });
        })
        .catch((error) => this.notificar(`No se pudo eliminar la orden: ${error.message}`, "error"));
    });
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
    this.ponerElegidos(idCp, [...this.elegidosDe(idCp), String(buscador.value)]);
  }

  manejarClick(evento) {
    const quitar = evento.target.closest("button[data-quitar-examen]");
    if (quitar) {
      const idCp = quitar.dataset.cp;
      this.ponerElegidos(idCp, this.elegidosDe(idCp).filter((id) => id !== quitar.dataset.quitarExamen));
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
    if (this.#operacionActiva) return;
    this.#operacionActiva = true;
    const conservaFoco = ["retroceder", "finalizar", "eliminar-ordenes"].includes(accion);
    this.bloquearAcciones(true, conservaFoco ? boton : null);
    Promise.resolve()
      .then(trabajo)
      .catch((error) => this.notificar(error.message, "error"))
      .finally(() => {
        this.#operacionActiva = false;
        this.bloquearAcciones(false);
      });
  }
}

definirElemento("app-consulta", AppConsulta);
