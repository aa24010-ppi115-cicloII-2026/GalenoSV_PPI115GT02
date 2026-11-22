import { Vista, definirElemento } from "../base.js";
import { escapar, nombreCompleto } from "../texto.js";
import { fechaISO, hoyISO, fechaCalendario } from "../fechas.js";
import { cargarTodo, citasDeEmpleado, documentoDePersona, estadoDeCita, pacientesDeClinica } from "../datos.js";
import { leerSesion, actualizarSesion } from "../estado.js";
import { mostrarTicket } from "./ticket-cita.js";
import "./ui-autocompletar.js";

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

<<<<<<< HEAD
export class AppDashboard extends Vista {
  get estilos() {
    return `
      .cabecera { display: grid; gap: 0.3rem; margin-bottom: 1rem; }
      .cabecera h2 { font-size: 1.4rem; letter-spacing: -0.015em; }
      .filtros { display: grid; gap: 0.9rem; }
      .datos { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 0.8rem; }
      .fechas { display: grid; grid-template-columns: repeat(2, minmax(0, 11rem)) 1fr; gap: 0.8rem; align-items: end; }
      .rangos { display: flex; gap: 0.4rem; flex-wrap: wrap; justify-content: flex-end; }
      .rangos button {
        min-height: 2.25rem; padding: 0.3rem 0.9rem; font-size: 0.82rem; border-radius: 999px;
        background: var(--superficie); color: var(--mate); border: 1px solid var(--borde-fuerte); box-shadow: none;
      }
      .rangos button:hover { background: var(--mate-suave); }
      .rangos button[aria-pressed="true"] { background: var(--mate); color: #fff; border-color: var(--mate); }
      #rango-aviso { font-size: 0.85rem; color: var(--texto-suave); margin: 1rem 0 0; }
      #resumen { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.7rem; margin: 0.6rem 0 1.1rem; }
      .indicador {
        background: var(--superficie); border: 1px solid var(--borde); border-radius: var(--radio-grande);
        padding: 0.7rem 0.85rem; display: grid; gap: 0.1rem;
      }
      .indicador span { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--texto-suave); }
      .indicador strong { font-size: 1.5rem; color: var(--mate); font-variant-numeric: tabular-nums; }
      .indicador.proceso strong { color: var(--alerta); }
      .indicador.completada strong { color: var(--exito); }
      #lista { display: grid; gap: 0.85rem; }
      @media (max-width: 900px) {
        .datos { grid-template-columns: 1fr 1fr; }
        .datos #paciente { grid-column: 1 / -1; }
        .fechas { grid-template-columns: 1fr 1fr; }
        .rangos { grid-column: 1 / -1; justify-content: flex-start; }
        #resumen { grid-template-columns: 1fr 1fr; }
      }
      @media (max-width: 520px) { .datos, .fechas { grid-template-columns: minmax(0, 1fr); } }
    `;
  }
=======
class AppDashboard extends Vista {
>>>>>>> v2modificada

  connectedCallback() {
    const sesion = leerSesion();
    const clinicas = sesion?.clinicas ?? [];
    const clinicaActual = sesion?.idClinica || clinicas[0]?.idClinica || "";
    this.render(`
      <section class="cabecera">
        <h2>Agenda de consultas</h2>
        <p class="aviso">${escapar(sesion?.nombre ?? "")} · elija clínica y rol para ver las consultas que le toca atender.</p>
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
      <section id="resumen" aria-label="Resumen de consultas" aria-busy="true">${this.indicadores()}</section>
      <section id="lista" aria-live="polite" aria-label="Consultas"><div class="vacio">Cargando agenda…</div></section>
    `);
    const campoClinica = this.q("#clinica");
    campoClinica.opciones = clinicas.map((clinica) => ({ valor: clinica.idClinica, texto: clinica.nombre }));
    campoClinica.value = clinicaActual;
    campoClinica.addEventListener("change", () => {
      this.cargarRoles();
      this.cargarPacientes();
      this.aplicar();
    });
    this.q("#rol").addEventListener("change", () => this.aplicar());
    this.q("#desde").addEventListener("change", () => this.aplicar());
    this.q("#hasta").addEventListener("change", () => this.aplicar());
    this.q("#paciente").addEventListener("change", () => this.aplicar());
    this._alClicRango ??= (evento) => {
      const boton = evento.target.closest("button[data-rango]");
      if (!boton) return;
      this.aplicarRango(boton.dataset.rango);
    };
    this._alVerTicket ??= (evento) => mostrarTicket(this.shadowRoot, evento.detail);
    this.shadowRoot.removeEventListener("click", this._alClicRango);
    this.shadowRoot.addEventListener("click", this._alClicRango);
    this.shadowRoot.removeEventListener("ver-ticket", this._alVerTicket);
    this.shadowRoot.addEventListener("ver-ticket", this._alVerTicket);
    this._alDatosSincronizados ??= () => this.cargar();
    window.addEventListener("datos-sincronizados", this._alDatosSincronizados);
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
    this._cargaActual = (this._cargaActual ?? 0) + 1;
    window.removeEventListener("datos-sincronizados", this._alDatosSincronizados);
  }

  /** Indicadores del resumen; sin conteos (mientras carga) muestran «—» en el mismo espacio. */
  indicadores(conteos = { total: "—", programada: "—", proceso: "—", completada: "—" }) {
    return `
      <div class="indicador"><span>Total</span><strong>${conteos.total}</strong></div>
      <div class="indicador"><span>Programadas</span><strong>${conteos.programada}</strong></div>
      <div class="indicador proceso"><span>En proceso</span><strong>${conteos.proceso}</strong></div>
      <div class="indicador completada"><span>Completadas</span><strong>${conteos.completada}</strong></div>
    `;
  }

  cargarRoles() {
    const sesion = leerSesion();
    const idClinica = this.q("#clinica").value;
    const roles = (sesion?.roles ?? []).filter(
      (rol) => String(rol.idClinica) === String(idClinica)
    );
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
    if (!this._datos) return;
    const campo = this.q("#paciente");
    const idClinica = this.q("#clinica").value;
    const pacientes = idClinica ? pacientesDeClinica(this._datos, idClinica) : [];
    campo.opciones = pacientes.map((persona) => {
      const documento = documentoDePersona(this._datos, persona.idPersona);
      return {
        valor: persona.idPersona,
        texto: [nombreCompleto(persona), documento && `${documento.tipo} ${documento.valor}`]
          .filter(Boolean)
          .join(" · "),
      };
    });
    if (pacientes.length === 0) {
      campo.mensaje = idClinica ? "Sin pacientes en esta clínica" : "Seleccione una clínica primero";
    }
  }

  ponerRango(rango) {
    const fechas = atajosDeFecha()[rango];
    if (!fechas) return false;
    const [desde, hasta] = fechas;
    this.q("#desde").value = desde;
    this.q("#hasta").value = hasta;
    return true;
  }

  aplicarRango(rango) {
    if (!this.ponerRango(rango)) return false;
    this.aplicar();
    return true;
  }

  /** Marca el atajo que coincide con las fechas elegidas y describe el rango en texto. */
  describirRango(desde, hasta) {
    const hoy = hoyISO();
    const atajos = atajosDeFecha();
    for (const boton of this.qa("button[data-rango]")) {
      const [d, h] = atajos[boton.dataset.rango];
      boton.setAttribute("aria-pressed", String(d === desde && h === hasta));
    }
    const texto = (iso) => fechaCalendario(iso);
    let descripcion;
    if (desde === hoy && hasta === hoy) descripcion = `Consultas de hoy, ${texto(hoy)}.`;
    else if (desde && hasta && desde === hasta) descripcion = `Consultas del ${texto(desde)}.`;
    else if (desde && hasta) descripcion = `Consultas del ${texto(desde)} al ${texto(hasta)}.`;
    else if (desde) descripcion = `Consultas desde el ${texto(desde)}.`;
    else if (hasta) descripcion = `Consultas hasta el ${texto(hasta)}, la más reciente primero.`;
    else descripcion = "Consultas de todas las fechas.";
    this.q("#rango-aviso").textContent = descripcion;
  }

  async cargar() {
    const carga = (this._cargaActual ?? 0) + 1;
    this._cargaActual = carga;
    if (!this.isConnected) return;
    const resumen = this.q("#resumen");
    const lista = this.q("#lista");
    resumen.setAttribute("aria-busy", "true");
    resumen.innerHTML = this.indicadores();
    lista.innerHTML = `<div class="vacio">Cargando agenda…</div>`;
    try {
      this._datos = await cargarTodo();
      if (!this.isConnected || carga !== this._cargaActual) return;
      this.cargarPacientes();
      const idPaciente = leerSesion()?.filtros?.idPaciente;
      if (idPaciente) this.q("#paciente").value = idPaciente;
      this.aplicar();
    } catch {
      if (!this.isConnected || carga !== this._cargaActual) return;
      resumen.removeAttribute("aria-busy");
      resumen.innerHTML = "";
      lista.innerHTML = `<div class="vacio"><p>No se pudo cargar la agenda.</p><button type="button" id="reintentar">Reintentar</button></div>`;
      lista.querySelector("#reintentar").addEventListener("click", () => this.cargar());
    }
  }

  aplicar() {
    if (!this._datos) return;
    const sesion = leerSesion();
    if (!sesion) return;
    const idClinica = this.q("#clinica").value || null;
    const idRol = this.q("#rol").value || null;
    const desde = this.q("#desde").value;
    const hasta = this.q("#hasta").value;
    const idPaciente = this.q("#paciente").value || null;
    this.describirRango(desde, hasta);
    const lista = this.q("#lista");
    if (desde && hasta && desde > hasta) {
      this.q("#rango-aviso").textContent = "";
      const resumen = this.q("#resumen");
      resumen.removeAttribute("aria-busy");
      resumen.innerHTML = "";
      lista.innerHTML = `<p class="error" role="alert">La fecha «Desde» es posterior a «Hasta». Corrija el rango.</p>`;
      return;
    }
    actualizarSesion({ idClinica, idRol, filtros: { desde, hasta, idPaciente } });
    if (!idClinica || !idRol) {
      const resumen = this.q("#resumen");
      resumen.removeAttribute("aria-busy");
      resumen.innerHTML = this.indicadores({ total: 0, programada: 0, proceso: 0, completada: 0 });
      lista.innerHTML = `<div class="vacio">Seleccione una clínica y un rol para ver las consultas que le corresponde atender.</div>`;
      return;
    }
    const citas = citasDeEmpleado(this._datos, { idClinica, idRol, desde: desde || null, hasta: hasta || null, idPaciente });
    // Sin fecha inicial solo se ven consultas pasadas: primero la más reciente.
    if (!desde && hasta) citas.reverse();
    const conteos = { total: citas.length, programada: 0, proceso: 0, completada: 0 };
    for (const cita of citas) {
      const { clave } = estadoDeCita(cita);
      const grupo = clave === "sin-pasos" ? "programada" : clave;
      if (conteos[grupo] !== undefined) conteos[grupo] += 1;
    }
    const resumen = this.q("#resumen");
    resumen.removeAttribute("aria-busy");
    resumen.innerHTML = this.indicadores(conteos);
    lista.innerHTML = "";
    if (citas.length === 0) {
      lista.innerHTML = `<div class="vacio">No hay consultas para las fechas y filtros seleccionados.</div>`;
      return;
    }
    for (const cita of citas) {
      const tarjeta = document.createElement("cita-card");
      tarjeta.setAttribute("vista", "empleado");
      tarjeta.cita = cita;
      lista.append(tarjeta);
    }
  }

}

definirElemento("app-dashboard", AppDashboard);
