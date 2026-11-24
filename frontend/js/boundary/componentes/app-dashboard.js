import { Vista, definirElemento, fragmento } from "../base.js";
import { html, nombreCompleto } from "../../comun/texto.js";
import { fechaISO, hoyISO, fechaCalendario } from "../../comun/fechas.js";
import { cargarTodo } from "../../control/repositorio.js";
import { citasDeEmpleado, estadoDeCita } from "../../control/citas.js";
import { documentoDePersona } from "../../control/documentos.js";
import { pacientesDeClinica } from "../../control/personas.js";
import { leerSesion, actualizarSesion } from "../../control/sesion.js";
import { mostrarTicket } from "./ticket-cita.js";
import "./ui-autocompletar.js";

/** Orden de los indicadores del resumen, igual al de la plantilla. */
const INDICADORES = ["total", "programada", "proceso", "completada"];

/** Atajos de fechas [desde, hasta]: hoy, todo lo anterior a hoy o todo desde mañana ("" = sin límite). */
function atajosDeFecha() {
  const hoy = new Date();
  const dia = (desplazamiento) => {
    const fecha = new Date(hoy);
    fecha.setDate(fecha.getDate() + desplazamiento);
    return fechaISO(fecha);
  };
  return { hoy: [dia(0), dia(0)], pasadas: ["", dia(-1)], proximas: [dia(1), ""] };
}

/** Rango elegido descrito en una frase para el aviso sobre el resumen. */
function describirFechas(desde, hasta) {
  const hoy = hoyISO();
  if (desde === hoy && hasta === hoy) return `Consultas de hoy, ${fechaCalendario(hoy)}.`;
  if (desde && hasta && desde === hasta) return `Consultas del ${fechaCalendario(desde)}.`;
  if (desde && hasta) return `Consultas del ${fechaCalendario(desde)} al ${fechaCalendario(hasta)}.`;
  if (desde) return `Consultas desde el ${fechaCalendario(desde)}.`;
  if (hasta) return `Consultas hasta el ${fechaCalendario(hasta)}, la más reciente primero.`;
  return "Consultas de todas las fechas.";
}

/** Cuántas citas hay en cada estado; las que no tienen pasos cuentan como programadas. */
function contarPorEstado(citas) {
  const conteos = { total: citas.length, programada: 0, proceso: 0, completada: 0 };
  for (const cita of citas) {
    const { clave } = estadoDeCita(cita);
    const grupo = clave === "sin-pasos" ? "programada" : clave;
    if (conteos[grupo] !== undefined) conteos[grupo] += 1;
  }
  return conteos;
}

/** HU3 · Agenda del empleado: citas de su clínica y rol por rango de fechas y paciente. */
export default class AppDashboard extends Vista {
  static plantilla = `
    <section class="cabecera">
      <h2>Agenda de consultas</h2>
      <p class="aviso" id="saludo"></p>
    </section>
    <section class="tarjeta filtros" id="filtros" aria-label="Filtros de la agenda">
      <div class="datos">
        <ui-autocompletar id="clinica" etiqueta="Clínica"></ui-autocompletar>
        <ui-autocompletar id="rol" etiqueta="Rol"></ui-autocompletar>
        <ui-autocompletar id="paciente" etiqueta="Paciente (opcional)"></ui-autocompletar>
      </div>
      <div class="fechas">
        <div class="campo">
          <label for="desde">Desde</label>
          <input type="date" id="desde" />
        </div>
        <div class="campo">
          <label for="hasta">Hasta</label>
          <input type="date" id="hasta" />
        </div>
        <div class="rangos" role="group" aria-label="Atajos de fechas">
          <button type="button" data-rango="pasadas" aria-pressed="false">Pasadas</button>
          <button type="button" data-rango="hoy" aria-pressed="false">Hoy</button>
          <button type="button" data-rango="proximas" aria-pressed="false">Próximas</button>
        </div>
      </div>
    </section>
    <p id="rango-aviso" role="status"></p>
    <section id="resumen" aria-label="Resumen de consultas" aria-busy="true">
      <div class="indicador"><span>Total</span><strong>—</strong></div>
      <div class="indicador"><span>Programadas</span><strong>—</strong></div>
      <div class="indicador proceso"><span>En proceso</span><strong>—</strong></div>
      <div class="indicador completada"><span>Completadas</span><strong>—</strong></div>
    </section>
    <section id="lista" aria-live="polite" aria-label="Consultas"><div class="vacio">Cargando agenda…</div></section>
  `;

  #datos = null;
  /** Número de la carga en curso: una respuesta vieja no pisa a una más reciente. */
  #carga = 0;

  connectedCallback() {
    const sesion = leerSesion();
    const clinicas = sesion?.clinicas ?? [];
    this.montarPlantilla();
    this.q("#saludo").textContent = `${sesion?.nombre ?? ""} · elija clínica y rol para ver las consultas que le toca atender.`;

    const campoClinica = this.q("#clinica");
    campoClinica.opciones = clinicas.map((clinica) => ({ valor: clinica.idClinica, texto: clinica.nombre }));
    campoClinica.value = sesion?.idClinica || clinicas[0]?.idClinica || "";
    campoClinica.addEventListener("change", () => {
      this.cargarRoles();
      this.cargarPacientes();
      this.aplicar();
    });
    for (const selector of ["#rol", "#desde", "#hasta", "#paciente"]) {
      this.q(selector).addEventListener("change", () => this.aplicar());
    }
    this.escuchar(this.shadowRoot, "click", (evento) => {
      const boton = evento.target.closest("button[data-rango]");
      if (boton) this.aplicarRango(boton.dataset.rango);
      const atender = evento.target.closest("button[data-atender]");
      if (atender) this.navegar(`consulta/${atender.dataset.atender}`);
    });
    this.escuchar(this.shadowRoot, "ver-ticket", (evento) => mostrarTicket(this.shadowRoot, evento.detail));
    this.escuchar(window, "datos-sincronizados", () => this.cargar());

    this.cargarRoles();
    // Al volver de una consulta se recuperan las fechas elegidas; la primera vez se muestra hoy.
    if (sesion?.filtros) {
      this.q("#desde").value = sesion.filtros.desde;
      this.q("#hasta").value = sesion.filtros.hasta;
    } else {
      this.ponerRango("hoy");
    }
    // El rango y el resumen ocupan su lugar desde el inicio: la lista no salta al llegar los datos.
    this.describirRango(this.q("#desde").value, this.q("#hasta").value);
    this.cargar();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.#carga += 1;
  }

  /**
   * Actualiza solo los números del resumen. Sin conteos muestra «—» (cargando);
   * con `null` oculta los indicadores sin que la sección pierda su lugar.
   */
  pintarResumen(conteos = { total: "—", programada: "—", proceso: "—", completada: "—" }, { ocupado = false } = {}) {
    const resumen = this.q("#resumen");
    if (ocupado) resumen.setAttribute("aria-busy", "true");
    else resumen.removeAttribute("aria-busy");
    resumen.querySelectorAll(".indicador").forEach((indicador, indice) => {
      indicador.hidden = conteos === null;
      if (conteos) indicador.querySelector("strong").textContent = conteos[INDICADORES[indice]];
    });
  }

  /** Reemplaza el contenido de la lista por un aviso. */
  avisoEnLista(contenido) {
    this.q("#lista").replaceChildren(fragmento(contenido));
  }

  cargarRoles() {
    const sesion = leerSesion();
    const idClinica = this.q("#clinica").value;
    const roles = (sesion?.roles ?? []).filter((rol) => String(rol.idClinica) === String(idClinica));
    const campo = this.q("#rol");
    campo.opciones = roles.map((rol) => ({ valor: rol.idRol, texto: rol.nombreRol }));
    if (roles.length === 0) {
      campo.mensaje = idClinica ? "Sin roles en esta clínica" : "Seleccione una clínica primero";
      return;
    }
    // Conserva el rol de la sesión si pertenece a la clínica; si solo hay uno, no exige elegirlo.
    const enClinica = roles.some((rol) => String(rol.idRol) === String(sesion.idRol));
    campo.value = enClinica ? sesion.idRol : roles.length === 1 ? roles[0].idRol : "";
  }

  /** Opciones del buscador de pacientes: los registrados en la clínica elegida. */
  cargarPacientes() {
    if (!this.#datos) return;
    const campo = this.q("#paciente");
    const idClinica = this.q("#clinica").value;
    const pacientes = idClinica ? pacientesDeClinica(this.#datos, idClinica) : [];
    campo.opciones = pacientes.map((persona) => {
      const documento = documentoDePersona(this.#datos, persona.idPersona);
      return {
        valor: persona.idPersona,
        texto: [nombreCompleto(persona), documento && `${documento.tipo} ${documento.valor}`].filter(Boolean).join(" · "),
      };
    });
    if (pacientes.length === 0) {
      campo.mensaje = idClinica ? "Sin pacientes en esta clínica" : "Seleccione una clínica primero";
    }
  }

  ponerRango(rango) {
    const fechas = atajosDeFecha()[rango];
    if (!fechas) return false;
    [this.q("#desde").value, this.q("#hasta").value] = fechas;
    return true;
  }

  aplicarRango(rango) {
    if (!this.ponerRango(rango)) return false;
    this.aplicar();
    return true;
  }

  /** Marca el atajo que coincide con las fechas elegidas y describe el rango en texto. */
  describirRango(desde, hasta) {
    const atajos = atajosDeFecha();
    for (const boton of this.qa("button[data-rango]")) {
      const [d, h] = atajos[boton.dataset.rango];
      boton.setAttribute("aria-pressed", String(d === desde && h === hasta));
    }
    this.q("#rango-aviso").textContent = describirFechas(desde, hasta);
  }

  cargar() {
    const carga = ++this.#carga;
    if (!this.isConnected) return Promise.resolve();
    this.pintarResumen(undefined, { ocupado: true });
    this.avisoEnLista(html`<div class="vacio">Cargando agenda…</div>`);
    return cargarTodo()
      .then((datos) => {
        this.#datos = datos;
        if (!this.isConnected || carga !== this.#carga) return;
        this.cargarPacientes();
        const idPaciente = leerSesion()?.filtros?.idPaciente;
        if (idPaciente) this.q("#paciente").value = idPaciente;
        this.aplicar();
      })
      .catch(() => {
        if (!this.isConnected || carga !== this.#carga) return;
        this.pintarResumen(null);
        this.avisoEnLista(html`<div class="vacio"><p>No se pudo cargar la agenda.</p><button type="button" id="reintentar">Reintentar</button></div>`);
        this.q("#reintentar").addEventListener("click", () => this.cargar());
      });
  }

  aplicar() {
    if (!this.#datos) return;
    const sesion = leerSesion();
    if (!sesion) return;
    const idClinica = this.q("#clinica").value || null;
    const idRol = this.q("#rol").value || null;
    const desde = this.q("#desde").value;
    const hasta = this.q("#hasta").value;
    const idPaciente = this.q("#paciente").value || null;
    this.describirRango(desde, hasta);
    if (desde && hasta && desde > hasta) {
      this.q("#rango-aviso").textContent = "";
      this.pintarResumen(null);
      this.avisoEnLista(html`<p class="error" role="alert">La fecha «Desde» es posterior a «Hasta». Corrija el rango.</p>`);
      return;
    }
    actualizarSesion({ idClinica, idRol, filtros: { desde, hasta, idPaciente } });
    if (!idClinica || !idRol) {
      this.pintarResumen({ total: 0, programada: 0, proceso: 0, completada: 0 });
      this.avisoEnLista(html`<div class="vacio">Seleccione una clínica y un rol para ver las consultas que le corresponde atender.</div>`);
      return;
    }
    const citas = citasDeEmpleado(this.#datos, { idClinica, idRol, desde: desde || null, hasta: hasta || null, idPaciente });
    // Sin fecha inicial solo se ven consultas pasadas: primero la más reciente.
    if (!desde && hasta) citas.reverse();
    this.pintarResumen(contarPorEstado(citas));
    if (citas.length === 0) {
      this.avisoEnLista(html`<div class="vacio">No hay consultas para las fechas y filtros seleccionados.</div>`);
      return;
    }
    const tarjetas = citas.map((cita) => {
      const tarjeta = document.createElement("cita-card");
      tarjeta.setAttribute("vista", "empleado");
      tarjeta.cita = cita;
      // Con todos los pasos completos ya no hay nada que atender: solo revisar o finalizar.
      const accion = estadoDeCita(cita).clave === "completada" ? "Ver atención" : "Atender consulta";
      tarjeta.append(fragmento(html`<button type="button" slot="acciones" data-atender="${cita.consulta.idConsulta}">${accion}</button>`));
      return tarjeta;
    });
    this.q("#lista").replaceChildren(...tarjetas);
  }
}

definirElemento("app-dashboard", AppDashboard);
