import { Vista, definirElemento, fragmento } from "../base.js";
import { html, cantidad, nombreCompleto } from "../../comun/texto.js";
import { cargarTodo } from "../../control/repositorio.js";
import { citasDePaciente } from "../../control/citas.js";
import { documentoDePersona } from "../../control/documentos.js";
import { edadDe } from "../../control/personas.js";
import { estaConectado } from "../../control/pendientes.js";
import { mostrarTicket } from "./ticket-cita.js";
import "./asistente-citas.js";

const SIN_RESULTADOS = `<div class="vacio">Complete los pasos del asistente para ver sus citas.</div>`;

/** HU1 · Búsqueda de citas: carga los datos, aloja el asistente y muestra las citas como cards. */
export default class AppBusqueda extends Vista {
  static plantilla = `
    <section class="encabezado">
      <h2>Buscar mis citas</h2>
      <p class="aviso">Siga los pasos para consultar sus citas e imprimir el ticket de cada una.</p>
    </section>
    <asistente-citas></asistente-citas>
    <section id="resultados" aria-live="polite" aria-label="Resultado de la búsqueda">${SIN_RESULTADOS}</section>
  `;

  #datos = null;

  connectedCallback() {
    this.montarPlantilla();
    this.escuchar(this.shadowRoot, "buscar-citas", (evento) => this.mostrarCitas(evento.detail));
    this.escuchar(this.shadowRoot, "ver-ticket", (evento) => mostrarTicket(this.shadowRoot, evento.detail));
    this.escuchar(window, "online", () => this.actualizarDisponibilidad());
    this.escuchar(window, "offline", () => this.actualizarDisponibilidad());
    this.actualizarDisponibilidad();
  }

  get asistente() {
    return this.q("asistente-citas");
  }

  cargar() {
    // Sin conexión, el service worker entrega los datos guardados en el dispositivo.
    return cargarTodo()
      .then((datos) => {
        this.#datos = datos;
        if (!this.isConnected) return;
        this.asistente.desbloquear();
        this.asistente.datos = this.#datos;
      })
      .catch(() => {
        if (!this.isConnected) return;
        this.asistente.errorCarga(
          estaConectado()
            ? "No se pudo conectar con el servicio. Recargue la página."
            : "Sin conexión y sin datos guardados en este dispositivo."
        );
      });
  }

  actualizarDisponibilidad() {
    if (!this.asistente) return;
    this.cargar();
  }

  /** Datos del paciente que van una sola vez sobre la lista, no en cada tarjeta. */
  resumenPaciente(persona, total, clinica) {
    const documento = documentoDePersona(this.#datos, persona.idPersona);
    const edad = edadDe(persona);
    return [
      documento && `${documento.tipo} ${documento.valor}`,
      edad !== null && `${edad} años`,
      `${cantidad(total, "cita", "citas")} en ${clinica?.nombre ?? "la clínica elegida"}`,
    ]
      .filter(Boolean)
      .join(" · ");
  }

  mostrarCitas({ persona, idClinica, clinica, desde, hasta }) {
    const citas = citasDePaciente(this.#datos, { idPersona: persona.idPersona, idClinica, desde, hasta });
    const contenedor = this.q("#resultados");
    contenedor.replaceChildren(
      fragmento(html`
        <div class="resumen">
          <div>
            <h3 id="titulo-resultados" tabindex="-1">${nombreCompleto(persona)}</h3>
            <span class="aviso">${this.resumenPaciente(persona, citas.length, clinica)}</span>
          </div>
          <span class="separador"></span>
          <button type="button" class="secundario" id="nueva">Nueva búsqueda</button>
        </div>
        <div class="lista" id="lista-citas">
          ${citas.length === 0 && html`<div class="vacio">No hay citas para la clínica y las fechas elegidas.</div>`}
        </div>
      `)
    );
    contenedor.querySelector("#nueva").addEventListener("click", () => this.nuevaBusqueda());
    // Lleva la vista y el lector de pantalla a los resultados, que quedan debajo del asistente.
    contenedor.querySelector("#titulo-resultados").focus();
    const tarjetas = citas.map((cita) => {
      const tarjeta = document.createElement("cita-card");
      tarjeta.setAttribute("vista", "paciente");
      tarjeta.cita = cita;
      return tarjeta;
    });
    contenedor.querySelector("#lista-citas").append(...tarjetas);
  }

  nuevaBusqueda() {
    this.q("ticket-cita")?.remove();
    this.q("#resultados").replaceChildren(fragmento(SIN_RESULTADOS));
    this.asistente.reiniciar();
  }
}

definirElemento("app-busqueda", AppBusqueda);
