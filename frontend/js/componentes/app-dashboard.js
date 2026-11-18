import { Vista, escapar, hoyISO, fechaCalendario } from "../base.js";
import { cargarTodo, citasDeEmpleado, documentoDePersona, estadoDeCita, pacientesDeClinica } from "../datos.js";
import { leerSesion, actualizarSesion } from "../estado.js";
import "./ui-autocompletar.js";

/** Atajos de fechas [desde, hasta]: hoy, todo lo anterior a hoy o todo desde mañana ("" = sin límite). */
function atajosDeFecha() {
  const dia = (desplazamiento) => {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + desplazamiento);
    return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, "0")}-${String(fecha.getDate()).padStart(2, "0")}`;
  };
  return { hoy: [dia(0), dia(0)], pasadas: ["", dia(-1)], proximas: [dia(1), ""] };
}

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

  connectedCallback() {
    const sesion = leerSesion();
    const clinicas = sesion?.clinicas ?? [];
    const clinicaActual = sesion?.idClinica ?? clinicas[0]?.idClinica ?? "";
    this.render(`
      <section class="cabecera">
        <h2>Agenda de consultas</h2>
        <p class="aviso">${escapar(sesion?.nombre ?? "")} · elija clínica y rol para ver las consultas que le toca atender.</p>
      </section>
      <form class="tarjeta filtros" id="filtros" aria-label="Filtros de la agenda">
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
      </form>
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
    this.shadowRoot.addEventListener("click", (evento) => {
      const boton = evento.target.closest("button[data-rango]");
      if (!boton) return;
      this.aplicarRango(boton.dataset.rango);
    });
    this.shadowRoot.addEventListener("ver-ticket", (evento) => this.abrirTicket(evento.detail));
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
      campo.mensaje = "Sin roles en esta clínica";
      return;
    }
    // Se trabaja con un rol a la vez: el de la sesión si pertenece a esta clínica, o el primero.
    const enClinica = roles.some((rol) => String(rol.idRol) === String(sesion.idRol));
    campo.value = enClinica ? sesion.idRol : roles[0].idRol;
  }

  /** Opciones del buscador de pacientes: los registrados en la clínica elegida. */
  cargarPacientes() {
    if (!this._datos) return;
    const campo = this.q("#paciente");
    const pacientes = pacientesDeClinica(this._datos, this.q("#clinica").value);
    campo.opciones = pacientes.map((persona) => {
      const documento = documentoDePersona(this._datos, persona.idPersona);
      return {
        valor: persona.idPersona,
        texto: [`${persona.nombres} ${persona.apellidos}`.trim(), documento && `${documento.tipo} ${documento.valor}`]
          .filter(Boolean)
          .join(" · "),
      };
    });
    if (pacientes.length === 0) campo.mensaje = "Sin pacientes en esta clínica";
  }

  ponerRango(rango) {
    const [desde, hasta] = atajosDeFecha()[rango];
    this.q("#desde").value = desde;
    this.q("#hasta").value = hasta;
  }

  aplicarRango(rango) {
    this.ponerRango(rango);
    this.aplicar();
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
    try {
      this._datos = await cargarTodo();
      if (!this.isConnected) return;
      this.cargarPacientes();
      const idPaciente = leerSesion()?.filtros?.idPaciente;
      if (idPaciente) this.q("#paciente").value = idPaciente;
      this.aplicar();
    } catch {
      this.q("#resumen").innerHTML = "";
      this.q("#lista").innerHTML = `<div class="vacio">No se pudo cargar la agenda. Intente de nuevo.</div>`;
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
    actualizarSesion({ idClinica: idClinica ?? sesion.idClinica, idRol, filtros: { desde, hasta, idPaciente } });
    this.describirRango(desde, hasta);
    const lista = this.q("#lista");
    if (desde && hasta && desde > hasta) {
      // Sin descripción del rango: contradiría el aviso.
      this.q("#rango-aviso").textContent = "";
      this.q("#resumen").innerHTML = "";
      lista.innerHTML = `<p class="error" role="alert">La fecha «Desde» es posterior a «Hasta». Corrija el rango.</p>`;
      return;
    }
    const citas = citasDeEmpleado(this._datos, { idClinica, idRol, desde: desde || null, hasta: hasta || null, idPaciente });
    // Sin fecha inicial solo se ven consultas pasadas: primero la más reciente.
    if (!desde && hasta) citas.reverse();
    const conteos = { total: citas.length, programada: 0, proceso: 0, completada: 0 };
    for (const cita of citas) {
      const { clave } = estadoDeCita(cita);
      if (conteos[clave] !== undefined) conteos[clave] += 1;
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
      tarjeta.cita = cita;
      lista.append(tarjeta);
    }
  }

  abrirTicket(cita) {
    this.q("ticket-cita")?.remove();
    const ticket = document.createElement("ticket-cita");
    ticket.cita = cita;
    this.shadowRoot.append(ticket);
  }
}

customElements.define("app-dashboard", AppDashboard);
