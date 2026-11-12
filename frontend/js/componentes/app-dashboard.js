import { Vista, escapar, hoyISO } from "../base.js";
import { cargarTodo, citasDeEmpleado, estadoDeCita } from "../datos.js";
import { leerSesion, actualizarSesion } from "../estado.js";

export class AppDashboard extends Vista {
  get estilos() {
    return `
      .cabecera { display: grid; gap: 0.3rem; margin-bottom: 1rem; }
      .cabecera h2 { font-size: 1.4rem; }
      .filtros { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.7rem; align-items: end; }
      .rangos { display: flex; align-items: center; gap: 0.4rem; margin-top: 0.7rem; flex-wrap: wrap; }
      .rangos .rango {
        background: #fff; color: var(--mate); border: 1px solid var(--borde);
        border-radius: 999px; padding: 0.25rem 0.8rem; font-size: 0.78rem;
      }
      .rangos .rango.activo { background: var(--mate); color: #fff; border-color: var(--mate); }
      .acciones-filtros { display: flex; gap: 0.5rem; }
      #resumen { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.7rem; margin: 1.1rem 0; }
      .indicador {
        background: #fff; border: 1px solid #cbd5e1; border-radius: 14px; padding: 0.7rem 0.85rem;
        display: grid; gap: 0.1rem;
      }
      .indicador span { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.05em; color: #587089; }
      .indicador strong { font-size: 1.5rem; color: #2c4a63; }
      .indicador.proceso strong { color: #8a5a00; }
      .indicador.completada strong { color: #1e6b3a; }
      #lista { display: grid; gap: 0.85rem; }
      .nota { font-size: 0.8rem; color: #587089; }
      @media (max-width: 900px) { .filtros { grid-template-columns: 1fr 1fr; } #resumen { grid-template-columns: 1fr 1fr; } }
      @media (max-width: 520px) { .filtros { grid-template-columns: 1fr; } }
    `;
  }

  connectedCallback() {
    const sesion = leerSesion();
    const clinicas = sesion?.clinicas ?? [];
    const clinicaActual = sesion?.idClinica ?? clinicas[0]?.idClinica ?? "";
    this.render(`
      <section class="cabecera">
        <span class="etiqueta">Empleados</span>
        <h2>Consultas del día</h2>
        <p class="aviso">${escapar(sesion?.nombre ?? "")} · filtre por clínica y rol para revisar la agenda y abrir el registro de atención.</p>
      </section>
      <form class="tarjeta filtros" id="filtros">
        <div class="campo">
          <label for="clinica">Clínica</label>
          <select id="clinica">
            ${clinicas
              .map(
                (clinica) =>
                  `<option value="${escapar(clinica.idClinica)}" ${String(clinica.idClinica) === String(clinicaActual) ? "selected" : ""}>${escapar(clinica.nombre)}</option>`
              )
              .join("")}
          </select>
        </div>
        <div class="campo">
          <label for="rol">Rol</label>
          <select id="rol"></select>
        </div>
        <div class="campo">
          <label for="fecha">Fecha</label>
          <input type="date" id="fecha" value="${hoyISO()}" />
        </div>
        <div class="campo">
          <label for="paciente">Paciente</label>
          <input id="paciente" placeholder="Nombre o apellido" />
        </div>
      </form>
      <div class="rangos" role="group" aria-label="Rango de fechas rapido">
        <button type="button" class="rango" data-rango="hoy">Hoy</button>
        <button type="button" class="rango" data-rango="pasado">Pasado</button>
        <button type="button" class="rango" data-rango="futuro">Futuro</button>
        <span class="aviso" id="rango-aviso"></span>
      </div>
      <section id="resumen" aria-label="Resumen de consultas"></section>
      <section id="lista" aria-live="polite" aria-label="Consultas del día"><div class="vacio">Cargando agenda…</div></section>
      <p class="nota">Los pasos se muestran en orden; el paso resaltado es el siguiente a registrar.</p>
    `);
    this.q("#clinica").addEventListener("change", () => {
      this.cargarRoles();
      this.aplicar();
    });
    this.q("#rol").addEventListener("change", () => this.aplicar());
    this.q("#fecha").addEventListener("change", () => this.aplicar());
    this.q("#paciente").addEventListener("input", () => this.aplicar());
    this.shadowRoot.addEventListener("click", (evento) => {
      const boton = evento.target.closest("button[data-rango]");
      if (!boton) return;
      this.aplicarRango(boton.dataset.rango);
    });
    this.shadowRoot.addEventListener("ver-ticket", (evento) => this.abrirTicket(evento.detail));
    this.cargarRoles();
    this.cargar();
  }

  cargarRoles() {
    const sesion = leerSesion();
    const idClinica = this.q("#clinica").value;
    const roles = (sesion?.roles ?? []).filter(
      (rol) => String(rol.idClinica) === String(idClinica)
    );
    const select = this.q("#rol");
    select.innerHTML =
      `<option value="">Todos mis roles</option>` +
      roles.map((rol) => `<option value="${escapar(rol.idRol)}">${escapar(rol.nombreRol)}</option>`).join("");
    if (roles.length === 0) {
      select.innerHTML = `<option value="">Sin roles en esta clínica</option>`;
      select.disabled = true;
    } else {
      select.disabled = false;
    }
  }

  /** Aplica el rango rapido de fechas: hoy, ayer o la agenda futura. */
  aplicarRango(rango) {
    const hoy = new Date();
    const iso = (fecha) =>
      `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, "0")}-${String(fecha.getDate()).padStart(2, "0")}`;
    const ayer = new Date(hoy.getTime() - 86400000);
    const manana = new Date(hoy.getTime() + 86400000);
    const aviso = this.q("#rango-aviso");
    for (const boton of this.qa("button[data-rango]")) {
      boton.classList.toggle("activo", boton.dataset.rango === rango);
    }
    if (rango === "hoy") {
      this.q("#fecha").value = iso(hoy);
      aviso.textContent = "Mostrando las consultas de hoy.";
    } else if (rango === "pasado") {
      this.q("#fecha").value = iso(ayer);
      aviso.textContent = "Mostrando las consultas de ayer.";
    } else {
      this.q("#fecha").value = iso(manana);
      aviso.textContent = "Mostrando las consultas de manana; puede cambiar la fecha para ver otras.";
    }
    this.aplicar();
  }

  async cargar() {
    try {
      this._datos = await cargarTodo();
      if (this.isConnected) this.aplicar();
    } catch {
      this.q("#lista").innerHTML = `<div class="vacio">No se pudo cargar la agenda. Intente de nuevo.</div>`;
    }
  }

  aplicar() {
    if (!this._datos) return;
    const sesion = leerSesion();
    if (!sesion) return;
    const idClinica = this.q("#clinica").value || null;
    const idRol = this.q("#rol").value || null;
    const fecha = this.q("#fecha").value || hoyISO();
    const paciente = this.q("#paciente").value.trim();
    actualizarSesion({ idClinica: idClinica ?? sesion.idClinica, idRol });
    const citas = citasDeEmpleado(this._datos, { idClinica, idRol, desde: fecha, hasta: fecha, paciente });
    const conteos = { total: citas.length, programada: 0, proceso: 0, completada: 0 };
    for (const cita of citas) {
      const { clave } = estadoDeCita(cita);
      if (conteos[clave] !== undefined) conteos[clave] += 1;
    }
    this.q("#resumen").innerHTML = `
      <div class="indicador"><span>Total</span><strong>${conteos.total}</strong></div>
      <div class="indicador"><span>Programadas</span><strong>${conteos.programada}</strong></div>
      <div class="indicador proceso"><span>En proceso</span><strong>${conteos.proceso}</strong></div>
      <div class="indicador completada"><span>Completadas</span><strong>${conteos.completada}</strong></div>
    `;
    const lista = this.q("#lista");
    lista.innerHTML = "";
    if (citas.length === 0) {
      lista.innerHTML = `<div class="vacio">No hay consultas para la fecha y filtros seleccionados.</div>`;
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
