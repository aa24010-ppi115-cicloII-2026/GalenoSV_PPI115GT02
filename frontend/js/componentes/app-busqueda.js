import { Vista, escapar } from "../base.js";
import { cargarTodo, citasDePaciente } from "../datos.js";
import "./asistente-citas.js";

const SIN_RESULTADOS = `<div class="vacio">Complete los pasos del asistente para ver sus citas.</div>`;

/** HU1 · Búsqueda de citas: carga los datos, aloja el asistente y muestra las citas como cards. */
export class AppBusqueda extends Vista {
  get estilos() {
    return `
      .encabezado { display: grid; gap: 0.25rem; margin: 0.5rem 0 1rem; }
      .encabezado h2 { font-size: 1.4rem; }
      #resultados { margin-top: 1.4rem; display: grid; gap: 0.9rem; }
      .resumen { display: flex; align-items: baseline; gap: 0.5rem; flex-wrap: wrap; }
      .resumen strong { font-size: 1.05rem; }
      .lista { display: grid; gap: 0.85rem; }
    `;
  }

  connectedCallback() {
    this.render(`
      <section class="encabezado">
        <span class="etiqueta">Pacientes</span>
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
        <strong>${escapar(nombre)}</strong>
        <span class="aviso">${total} en ${escapar(clinica?.nombre ?? "la clínica elegida")}</span>
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
