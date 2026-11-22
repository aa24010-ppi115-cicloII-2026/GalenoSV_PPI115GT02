import { Vista, definirElemento } from "../base.js";
import { escapar } from "../texto.js";
import { fechaCalendario, hoyISO } from "../fechas.js";
import {
  personaDeDocumento,
  clinicasDePaciente,
  errorDeFormatoDocumento,
  indicacionesDocumento,
  tiposDocumentoActivos,
  tipoDocumentoElegido,
} from "../datos.js";
import "./ui-autocompletar.js";

const PASOS = ["Tipo de documento", "Número de documento", "Clínica", "Rango de fechas"];

/**
 * HU1 · Asistente de búsqueda de citas en cuatro pasos:
 * tipo de documento → número → clínica donde es paciente → rango de fechas.
 * No consulta la API: recibe los datos ya cargados en `datos` y emite
 * `buscar-citas` con la persona, la clínica y el rango elegidos.
 */
class AsistenteCitas extends Vista {

  connectedCallback() {
    if (this._iniciado) return;
    this._iniciado = true;
    this.paso = 1;
    this.render(`
      <form class="tarjeta" novalidate aria-label="Asistente de búsqueda de citas">
        <ol class="pasos">${PASOS
          .map(
            (nombre) =>
              `<li data-paso="${PASOS.indexOf(nombre) + 1}"><span class="marca" aria-hidden="true"></span><span class="texto"><span class="etiqueta">${escapar(nombre)}</span><span class="valor"></span></span></li>`
          )
          .join("")}</ol>

        <section data-paso="1">
          <h3>¿Con qué documento está registrado?</h3>
          <ui-autocompletar id="tipo" etiqueta="Tipo de documento"></ui-autocompletar>
        </section>

        <section data-paso="2" hidden>
          <h3>Escriba su número de documento</h3>
          <div class="campo">
            <label for="numero" id="etiqueta-numero">Número de documento</label>
            <input id="numero" autocomplete="off" inputmode="text" aria-describedby="formato" />
            <small class="ayuda" id="formato"></small>
          </div>
        </section>

        <section data-paso="3" hidden>
          <h3>Elija la clínica</h3>
          <ui-autocompletar id="clinica" etiqueta="Clínica" descrito-por="ayuda-clinica"></ui-autocompletar>
          <small class="ayuda" id="ayuda-clinica">Solo aparecen las clínicas donde usted está registrado como paciente.</small>
        </section>

        <section data-paso="4" hidden>
          <h3>¿En qué fechas busca?</h3>
          <div class="rango">
            <div class="campo">
              <label for="desde">Desde</label>
              <input type="date" id="desde" value="${hoyISO()}" />
            </div>
            <div class="campo">
              <label for="hasta">Hasta</label>
              <input type="date" id="hasta" value="${hoyISO()}" />
            </div>
          </div>
          <small class="ayuda">Por defecto se buscan las citas de hoy. Cambie las fechas para ver otro rango; deje una vacía para no limitar ese extremo.</small>
        </section>

        <p class="error" id="error" role="alert" hidden></p>
        <div class="acciones">
          <button type="button" class="secundario" id="atras">Atrás</button>
          <button type="button" id="siguiente">Siguiente</button>
          <button type="button" id="buscar">Buscar citas</button>
        </div>
      </form>
    `);
    this.q("#atras").addEventListener("click", () => this.irA(this.paso - 1));
    this.q("#siguiente").addEventListener("click", () => this.avanzar());
    this.q("#buscar").addEventListener("click", () => this.buscar());
    this.q("form").addEventListener("keydown", (evento) => {
      if (evento.key !== "Enter" || evento.target.tagName === "BUTTON") return;
      evento.preventDefault();
      if (this.paso === 4) this.buscar();
      else this.avanzar();
    });
    this.q("#tipo").addEventListener("change", () => {
      this.invalidarIdentidad();
      this.q("#numero").value = "";
      this.mostrarFormato();
    });
    this.q("#numero").addEventListener("input", () => this.invalidarIdentidad());
    // Un error se refiere a lo que se escribió antes: al corregir cualquier dato deja de mostrarse.
    this.q("form").addEventListener("input", () => {
      this.mostrarError("");
      this.pintarEleccion();
    });
    this.q("form").addEventListener("change", () => {
      this.mostrarError("");
      this.pintarEleccion();
    });
    this.mostrarPaso();
    if (this._datos) this.pintarTipos();
  }

  set datos(valor) {
    this._datos = valor;
    if (this._iniciado) this.pintarTipos();
  }

  get datos() {
    return this._datos;
  }

  pintarTipos() {
    const tipos = tiposDocumentoActivos(this._datos ?? {});
    this.q("#tipo").opciones = tipos.map((tipo) => ({ valor: tipo.idTipoDocumento, texto: tipo.nombre }));
  }

  errorCarga(mensaje) {
    this.q("#tipo").mensaje = "No disponible";
    this.mostrarError(mensaje);
  }

  desbloquear() {
    this.qa("input, button").forEach((elemento) => { elemento.disabled = false; });
    this.q("form").removeAttribute("aria-disabled");
    this.pintarTipos();
    this.mostrarError("");
  }

  tipoElegido() {
    return tipoDocumentoElegido(this._datos, this.q("#tipo").value);
  }

  mostrarFormato() {
    const tipo = this.tipoElegido();
    const formato = indicacionesDocumento(tipo);
    const nombre = tipo?.nombre ?? "documento";
    const etiquetaNumero = `Número de ${nombre}`;
    this.q("#formato").textContent = formato.texto;
    this.q("#numero").placeholder = formato.ejemplo;
    this.q("#etiqueta-numero").textContent = etiquetaNumero;
    this.q('section[data-paso="2"] h3').textContent = `Escriba su ${etiquetaNumero.charAt(0).toLowerCase()}${etiquetaNumero.slice(1)}`;
    this.q('ol.pasos li[data-paso="2"] .etiqueta').textContent = etiquetaNumero;
    this.pintarEleccion();
  }

  /** Al modificar el documento se invalidan la persona y las clínicas del valor anterior. */
  invalidarIdentidad() {
    this._persona = null;
    this._clinicas = [];
    this._numeroValidado = null;
    this._tipoValidado = null;
    this.q("#clinica").opciones = [];
  }

  mostrarError(mensaje) {
    const error = this.q("#error");
    error.textContent = mensaje;
    error.hidden = !mensaje;
  }

  /** Valida el paso actual y, si todo está bien, pasa al siguiente. */
  avanzar() {
    this.mostrarError("");
    if (this.paso === 1) {
      if (!this.tipoElegido()) return this.mostrarError("Seleccione el tipo de documento.");
      return this.irA(2);
    }
    if (this.paso === 2) return this.validarDocumento() && this.irA(3);
    if (this.paso === 3) {
      if (!this.q("#clinica").value) return this.mostrarError("Seleccione una clínica.");
      return this.irA(4);
    }
  }

  validarDocumento() {
    const tipo = this.tipoElegido();
    const numero = this.q("#numero").value.trim();
    if (!numero) {
      this.mostrarError(`Escriba el número de ${tipo?.nombre ?? "documento"}.`);
      return false;
    }
    const errorFormato = errorDeFormatoDocumento(tipo, numero);
    if (errorFormato) {
      this.mostrarError(errorFormato);
      return false;
    }

    const persona = personaDeDocumento(this._datos, tipo.idTipoDocumento, numero);
    const clinicas = persona
      ? clinicasDePaciente(this._datos, persona.idPersona).sort((a, b) => String(a.nombre).localeCompare(String(b.nombre)))
      : [];
    // Mismo mensaje si el documento no existe o no es de un paciente: la pantalla es pública.
    if (clinicas.length === 0) {
      this.mostrarError("No hay citas registradas con ese documento. Verifique el tipo y el número.");
      return false;
    }
    this._persona = persona;
    this._clinicas = clinicas;
    this._numeroValidado = numero;
    this._tipoValidado = tipo.idTipoDocumento;
    const campoClinica = this.q("#clinica");
    campoClinica.opciones = clinicas.map((clinica) => ({ valor: clinica.idClinica, texto: clinica.nombre }));
    if (clinicas.length === 1) campoClinica.value = clinicas[0].idClinica;
    return true;
  }

  buscar() {
    this.mostrarError("");
    const tipo = this.tipoElegido();
    const numero = this.q("#numero").value.trim();
    if (!this._persona || numero !== this._numeroValidado || String(tipo?.idTipoDocumento) !== String(this._tipoValidado)) {
      this.irA(2);
      this.mostrarError("Vuelva a validar el número de documento antes de buscar.");
      return;
    }
    let desde = this.q("#desde").value || null;
    let hasta = this.q("#hasta").value || null;
    // Sin ninguna fecha se buscan las citas de hoy, igual que el valor inicial del formulario.
    if (!desde && !hasta) desde = hasta = hoyISO();
    if (desde && hasta && desde > hasta) {
      this.mostrarError("La fecha inicial no puede ser posterior a la fecha final.");
      return;
    }
    const idClinica = this.q("#clinica").value || null;
    const clinica = this._clinicas?.find((item) => String(item.idClinica) === String(idClinica)) ?? null;
    this.dispatchEvent(
      new CustomEvent("buscar-citas", {
        bubbles: true,
        composed: true,
        detail: { persona: this._persona, idClinica: clinica?.idClinica ?? idClinica, clinica, desde, hasta },
      })
    );
  }

  irA(paso) {
    this.paso = Math.min(4, Math.max(1, paso));
    this.mostrarError("");
    this.mostrarPaso();
    const primerCampo = this.q(`section[data-paso="${this.paso}"]`).querySelector("ui-autocompletar, input");
    primerCampo?.focus();
  }

  mostrarPaso() {
    for (const seccion of this.qa("section[data-paso]")) {
      seccion.hidden = Number(seccion.dataset.paso) !== this.paso;
    }
    this.qa("ol.pasos li").forEach((li, indice) => {
      li.classList.toggle("hecho", indice + 1 < this.paso);
      if (indice + 1 === this.paso) li.setAttribute("aria-current", "step");
      else li.removeAttribute("aria-current");
    });
    this.q("#atras").hidden = this.paso === 1;
    this.q("#siguiente").hidden = this.paso === 4;
    this.q("#buscar").hidden = this.paso !== 4;
    this.pintarEleccion();
  }

  rangoElegido() {
    const desde = this.q("#desde").value;
    const hasta = this.q("#hasta").value;
    if (!desde && !hasta) return "";
    if (desde && hasta && desde === hasta) return fechaCalendario(desde);
    return [desde, hasta].filter(Boolean).map(fechaCalendario).join(" – ");
  }

  pintarEleccion() {
    const valores = [
      this.tipoElegido()?.nombre,
      this.q("#numero").value.trim(),
      this.q("#clinica").textoElegido,
      this.rangoElegido(),
    ];
    this.qa("ol.pasos li").forEach((li, indice) => {
      li.querySelector(".valor").textContent = valores[indice] ?? "";
    });
  }

  /** Vuelve al paso 1 con el formulario vacío. */
  reiniciar() {
    this.q("#tipo").value = "";
    this.q("#numero").value = "";
    this.q("#desde").value = hoyISO();
    this.q("#hasta").value = hoyISO();
    this.q("#clinica").opciones = [];
    this._persona = null;
    this._clinicas = [];
    this._numeroValidado = null;
    this._tipoValidado = null;
    this.mostrarFormato();
    this.irA(1);
  }
}

definirElemento("asistente-citas", AsistenteCitas);
