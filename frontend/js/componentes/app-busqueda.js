import { Vista, definirElemento } from "../base.js";
import { escapar, nombreCompleto } from "../texto.js";
import { cargarTodo, citasDePaciente, documentoDePersona, edadDe } from "../datos.js";
import { estaConectado } from "../pendientes.js";
import { mostrarTicket } from "./ticket-cita.js";
import "./asistente-citas.js";

const SIN_RESULTADOS = `<div class="vacio">Complete los pasos del asistente para ver sus citas.</div>`;

/** HU1 · Búsqueda de citas: carga los datos, aloja el asistente y muestra las citas como cards. */
class AppBusqueda extends Vista {

  connectedCallback() {
    this.render(`
      <section class="encabezado">
        <h2>Buscar mis citas</h2>
        <p class="aviso">Siga los pasos para consultar sus citas e imprimir el ticket de cada una.</p>
      </section>
      <asistente-citas></asistente-citas>
      <section id="resultados" aria-live="polite" aria-label="Resultado de la búsqueda">${SIN_RESULTADOS}</section>
    `);
    this._alBuscarCitas ??= (evento) => this.mostrarCitas(evento.detail);
    this._alVerTicket ??= (evento) => mostrarTicket(this.shadowRoot, evento.detail);
    this.shadowRoot.removeEventListener("buscar-citas", this._alBuscarCitas);
    this.shadowRoot.addEventListener("buscar-citas", this._alBuscarCitas);
    this.shadowRoot.removeEventListener("ver-ticket", this._alVerTicket);
    this.shadowRoot.addEventListener("ver-ticket", this._alVerTicket);
    this._alCambiarConexion ??= () => this.actualizarDisponibilidad();
    window.addEventListener("online", this._alCambiarConexion);
    window.addEventListener("offline", this._alCambiarConexion);
    this.actualizarDisponibilidad();
  }

  disconnectedCallback() {
    window.removeEventListener("online", this._alCambiarConexion);
    window.removeEventListener("offline", this._alCambiarConexion);
  }

  get asistente() {
    return this.q("asistente-citas");
  }

  async cargar() {
    try {
      // Sin conexión, el service worker entrega los datos guardados en el dispositivo.
      this._datos = await cargarTodo();
      if (!this.isConnected) return;
      this.asistente.desbloquear();
      this.asistente.datos = this._datos;
    } catch {
      if (!this.isConnected) return;
      this.asistente.errorCarga(
        estaConectado()
          ? "No se pudo conectar con el servicio. Recargue la página."
          : "Sin conexión y sin datos guardados en este dispositivo."
      );
    }
  }

  actualizarDisponibilidad() {
    if (!this.asistente) return;
    this.cargar();
  }

  mostrarCitas({ persona, idClinica, clinica, desde, hasta }) {
    const citas = citasDePaciente(this._datos, { idPersona: persona.idPersona, idClinica, desde, hasta });
    const nombre = nombreCompleto(persona);
    const total = `${citas.length} cita${citas.length === 1 ? "" : "s"}`;
    // Los datos del paciente van una sola vez aquí, no en cada tarjeta.
    const documento = documentoDePersona(this._datos, persona.idPersona);
    const edad = edadDe(persona);
    const datosPaciente = [
      documento && `${documento.tipo} ${documento.valor}`,
      edad !== null && `${edad} años`,
      `${total} en ${clinica?.nombre ?? "la clínica elegida"}`,
    ]
      .filter(Boolean)
      .join(" · ");
    const contenedor = this.q("#resultados");
    contenedor.innerHTML = `
      <div class="resumen">
        <div>
          <h3 id="titulo-resultados" tabindex="-1">${escapar(nombre)}</h3>
          <span class="aviso">${escapar(datosPaciente)}</span>
        </div>
        <span class="separador"></span>
        <button type="button" class="secundario" id="nueva">Nueva búsqueda</button>
      </div>
      <div class="lista" id="lista-citas"></div>
    `;
    contenedor.querySelector("#nueva").addEventListener("click", () => this.nuevaBusqueda());
    const lista = contenedor.querySelector("#lista-citas");
    // Lleva la vista y el lector de pantalla a los resultados, que quedan debajo del asistente.
    contenedor.querySelector("#titulo-resultados").focus();
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

}

definirElemento("app-busqueda", AppBusqueda);
