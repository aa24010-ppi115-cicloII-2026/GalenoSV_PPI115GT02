import { Vista, escapar } from "../base.js";
import { cargarTodo, citasDePaciente } from "../datos.js";
import "./asistente-citas.js";

const SIN_RESULTADOS = `<div class="vacio">Complete los pasos del asistente para ver sus citas.</div>`;

/** HU1 · Búsqueda de citas: carga los datos, aloja el asistente y muestra las citas como cards. */
export class AppBusqueda extends Vista {
  get estilos() {
    return `
      .encabezado { display: grid; gap: 0.4rem; margin: 0 0 1.25rem; }
      .encabezado h2 { font-size: clamp(1.45rem, 1.2rem + 1vw, 1.85rem); line-height: 1.2; letter-spacing: -0.02em; }
      .encabezado .aviso { max-width: 60ch; }
      #resultados { margin-top: 2rem; display: grid; gap: 1rem; }
      .resumen { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; padding-bottom: 0.75rem; border-bottom: 1px solid var(--borde, #d6dee7); }
      .resumen > div { display: grid; gap: 0.1rem; }
      .resumen strong { font-size: 1.15rem; letter-spacing: -0.01em; }
      .lista { display: grid; gap: 1rem; }
    `;
  }

  connectedCallback() {
    this.render(`
      <section class="encabezado">
        <h2>Buscar mis citas</h2>
        <p class="aviso">Siga los pasos para consultar sus citas y descargar el ticket de cada una.</p>
      </section>
      <asistente-citas></asistente-citas>
      <section id="resultados" aria-live="polite" aria-label="Resultado de la búsqueda">${SIN_RESULTADOS}</section>
    `);
    this.shadowRoot.addEventListener("buscar-citas", (evento) => this.mostrarCitas(evento.detail));
    this.shadowRoot.addEventListener("ver-ticket", (evento) => this.abrirTicket(evento.detail));
    this.cargar();
  }

  get asistente() {
    return this.q("asistente-citas");
  }

  async cargar() {
    try {
      this._datos = await cargarTodo();
      this.asistente.datos = this._datos;
    } catch {
      this.asistente.errorCarga("No se pudo conectar con el servicio. Recargue la página.");
    }
  }

  mostrarCitas({ persona, idClinica, clinica, desde, hasta }) {
    const citas = citasDePaciente(this._datos, { idPersona: persona.idPersona, idClinica, desde, hasta });
    const nombre = `${persona.nombres} ${persona.apellidos}`.trim();
    const total = `${citas.length} cita${citas.length === 1 ? "" : "s"}`;
    const contenedor = this.q("#resultados");
    contenedor.innerHTML = `
      <div class="resumen">
        <div>
          <strong>${escapar(nombre)}</strong>
          <span class="aviso">${total} en ${escapar(clinica?.nombre ?? "la clínica elegida")}</span>
        </div>
        <span class="separador"></span>
        <button type="button" class="secundario" id="nueva">Nueva búsqueda</button>
      </div>
      <div class="lista" id="lista-citas"></div>
    `;
    contenedor.querySelector("#nueva").addEventListener("click", () => this.nuevaBusqueda());
    const lista = contenedor.querySelector("#lista-citas");
    if (citas.length === 0) {
      lista.innerHTML = `<div class="vacio">No hay citas para la clínica y las fechas elegidas.</div>`;
      return;
    }
    for (const cita of citas) {
      const tarjeta = document.createElement("cita-card");
      tarjeta.setAttribute("vista", "paciente");
      tarjeta.cita = cita;
      lista.append(tarjeta);
    }
  }

  nuevaBusqueda() {
    this.q("ticket-cita")?.remove();
    this.q("#resultados").innerHTML = SIN_RESULTADOS;
    this.asistente.reiniciar();
  }

  abrirTicket(cita) {
    this.q("ticket-cita")?.remove();
    const ticket = document.createElement("ticket-cita");
    ticket.cita = cita;
    this.shadowRoot.append(ticket);
  }
}

customElements.define("app-busqueda", AppBusqueda);
