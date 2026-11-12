import { Vista, escapar, hoyISO } from "../base.js";
import { cargarTodo, personaDeDocumento, clinicasDePaciente, citasDePaciente } from "../datos.js";

export class AppBusqueda extends Vista {
  get estilos() {
    return `
      .encabezado { display: grid; gap: 0.25rem; margin-bottom: 1rem; }
      .encabezado h2 { font-size: 1.4rem; }
      form { display: grid; gap: 0.8rem; }
      .rango { display: grid; grid-template-columns: 1fr 1fr; gap: 0.8rem; }
      #resultados { margin-top: 1.4rem; display: grid; gap: 0.9rem; }
      .resumen { display: flex; align-items: baseline; gap: 0.5rem; flex-wrap: wrap; }
      .resumen strong { font-size: 1.05rem; }
      .lista { display: grid; gap: 0.85rem; }
      @media (max-width: 620px) { .rango { grid-template-columns: 1fr; } }
    `;
  }

  connectedCallback() {
    this.render(`
      <section class="encabezado">
        <span class="etiqueta">Pacientes</span>
        <h2>Buscar mis citas</h2>
        <p class="aviso">Consulte sus citas por documento, clínica y rango de fechas. Puede descargar el ticket de cada cita.</p>
      </section>
      <form class="tarjeta" id="formulario" novalidate>
        <div class="rejilla tres">
          <div class="campo">
            <label for="tipo">Tipo de documento</label>
            <select id="tipo" required><option value="">Cargando…</option></select>
          </div>
          <div class="campo">
            <label for="numero">Número de documento</label>
            <input id="numero" autocomplete="off" placeholder="Ej. 01234567-8" required />
          </div>
          <div class="campo">
            <label for="clinica">Clínica</label>
            <select id="clinica" disabled><option value="">Escriba su documento para ver las clínicas</option></select>
            <small class="aviso" id="clinica-aviso">Solo se muestran las clínicas donde usted está registrado como paciente.</small>
          </div>
        </div>
        <div class="rango">
          <div class="campo">
            <label for="desde">Desde</label>
            <input type="date" id="desde" />
          </div>
          <div class="campo">
            <label for="hasta">Hasta</label>
            <input type="date" id="hasta" />
          </div>
        </div>
        <p class="error" id="error" hidden></p>
        <div class="fila">
          <button type="submit" id="buscar">Buscar citas</button>
          <button type="button" class="secundario" id="limpiar">Limpiar</button>
        </div>
      </form>
      <section id="resultados" aria-live="polite" aria-label="Resultado de la búsqueda">
        <div class="vacio">Ingrese su documento para ver las citas registradas.</div>
      </section>
    `);
    this.q("#hasta").value = hoyISO();
    this.q("#formulario").addEventListener("submit", (evento) => {
      evento.preventDefault();
      this.buscar();
    });
    this.q("#limpiar").addEventListener("click", () => {
      this.q("#numero").value = "";
      this.actualizarClinicas();
      this.q("#desde").value = "";
      this.q("#hasta").value = "";
      this.mostrarError("");
      this.q("#resultados").innerHTML = `<div class="vacio">Ingrese su documento para ver las citas registradas.</div>`;
    });
    this.shadowRoot.addEventListener("ver-ticket", (evento) => this.abrirTicket(evento.detail));
    this.q("#tipo").addEventListener("change", () => this.actualizarClinicas());
    this.q("#numero").addEventListener("input", () => this.actualizarClinicas());
    this.cargar();
  }

  async cargar() {
    try {
      this._datos = await cargarTodo();
      const tipos = [...this._datos.tiposDocumento].sort((a, b) =>
        String(a.nombre).localeCompare(String(b.nombre))
      );
      this.q("#tipo").innerHTML =
        `<option value="">Seleccione…</option>` +
        tipos
          .map((item) => `<option value="${escapar(item.idTipoDocumento)}">${escapar(item.nombre)}</option>`)
          .join("");
      this.actualizarClinicas();
    } catch {
      this.q("#tipo").innerHTML = `<option value="">No disponible</option>`;
      this.mostrarError("No se pudo conectar con el servicio. Recargue la página.");
    }
  }

  mostrarError(mensaje) {
    const error = this.q("#error");
    error.textContent = mensaje;
    error.hidden = !mensaje;
  }

  async actualizarClinicas() {
    const select = this.q("#clinica");
    const aviso = this.q("#clinica-aviso");
    const idTipoDocumento = this.q("#tipo").value;
    const numero = this.q("#numero").value.trim();
    if (!idTipoDocumento || !numero) {
      select.disabled = true;
      select.innerHTML = `<option value="">Escriba su documento para ver las clínicas</option>`;
      aviso.textContent = "Solo se muestran las clínicas donde usted está registrado como paciente.";
      return [];
    }
    try {
      const datos = this._datos ?? (this._datos = await cargarTodo());
      const persona = personaDeDocumento(datos, idTipoDocumento, numero);
      if (!persona) {
        select.disabled = true;
        select.innerHTML = `<option value="">Documento no registrado</option>`;
        aviso.textContent = "No encontramos ese documento en el sistema.";
        return [];
      }
      const clinicas = clinicasDePaciente(datos, persona.idPersona).sort((a, b) =>
        String(a.nombre).localeCompare(String(b.nombre))
      );
      select.disabled = clinicas.length === 0;
      select.innerHTML =
        `<option value="">Todas mis clínicas</option>` +
        clinicas
          .map((item) => `<option value="${escapar(item.idClinica)}">${escapar(item.nombre)}</option>`)
          .join("");
      aviso.textContent =
        clinicas.length > 0
          ? `Clinicas donde esta registrado: ${clinicas.length}.`
          : "No tiene clínicas registradas como paciente.";
      return clinicas;
    } catch {
      select.disabled = true;
      return [];
    }
  }

  async buscar() {
    this.mostrarError("");
    const idTipoDocumento = this.q("#tipo").value;
    const numero = this.q("#numero").value.trim();
    await this.actualizarClinicas();
    const idClinica = this.q("#clinica").value || null;
    const desde = this.q("#desde").value || null;
    const hasta = this.q("#hasta").value || null;
    if (!idTipoDocumento || !numero) {
      this.mostrarError("Seleccione el tipo de documento e ingrese el número.");
      return;
    }
    if (desde && hasta && desde > hasta) {
      this.mostrarError("La fecha inicial no puede ser posterior a la final.");
      return;
    }
    const boton = this.q("#buscar");
    boton.disabled = true;
    boton.textContent = "Buscando…";
    try {
      const datos = this._datos ?? (this._datos = await cargarTodo());
      const persona = personaDeDocumento(datos, idTipoDocumento, numero);
      if (!persona) {
        this.mostrarError("No encontramos una persona con ese documento.");
        this.q("#resultados").innerHTML = `<div class="vacio">Verifique el tipo y número de documento.</div>`;
        return;
      }
      const citas = citasDePaciente(datos, { idPersona: persona.idPersona, idClinica, desde, hasta });
      this.pintarResultados(persona, citas);
    } catch {
      this.mostrarError("No fue posible consultar las citas. Intente de nuevo.");
    } finally {
      boton.disabled = false;
      boton.textContent = "Buscar citas";
    }
  }

  pintarResultados(persona, citas) {
    const contenedor = this.q("#resultados");
    const nombre = `${persona.nombres} ${persona.apellidos}`.trim();
    contenedor.innerHTML = `
      <div class="resumen">
        <strong>${escapar(nombre)}</strong>
        <span class="aviso">${citas.length} cita${citas.length === 1 ? "" : "s"} encontrada${citas.length === 1 ? "" : "s"}</span>
      </div>
      <div class="lista" id="lista-citas"></div>
    `;
    const lista = contenedor.querySelector("#lista-citas");
    if (citas.length === 0) {
      lista.innerHTML = `<div class="vacio">No hay citas para los filtros seleccionados.</div>`;
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

customElements.define("app-busqueda", AppBusqueda);
