import { Vista, escapar } from "../base.js";
import { personaDeDocumento, clinicasDePaciente, cumpleFormato } from "../datos.js";
import "./ui-autocompletar.js";

const PASOS = ["Tipo de documento", "Número de documento", "Clínica", "Rango de fechas"];

/**
 * HU1 · Asistente de búsqueda de citas en cuatro pasos:
 * tipo de documento → número → clínica donde es paciente → rango de fechas.
 * No consulta la API: recibe los datos ya cargados en `datos` y emite
 * `buscar-citas` con la persona, la clínica y el rango elegidos.
 */
export class AsistenteCitas extends Vista {
  get estilos() {
    const check = "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M5 12.5l4.5 4.5L19 7.5'/%3E%3C/svg%3E\")";
    return `
      form { display: grid; gap: 1.1rem; padding: 1.35rem 1.5rem 1.25rem; }
      ol.pasos {
        list-style: none; margin: 0 0 0.25rem; padding: 0;
        display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.5rem;
        counter-reset: paso;
      }
      ol.pasos li {
        counter-increment: paso; position: relative;
        display: flex; align-items: center; gap: 0.5rem;
        font-size: 0.8rem; font-weight: 500; color: var(--texto-tenue, #5f7387);
        padding-bottom: 0.65rem;
      }
      ol.pasos li::after {
        content: ""; position: absolute; left: 0; right: 0; bottom: 0; height: 3px; border-radius: 3px;
        background: var(--borde, #d6dee7); transition: background-color 0.25s ease;
      }
      ol.pasos li::before {
        content: counter(paso);
        flex: none; width: 1.65rem; height: 1.65rem; border-radius: 999px;
        display: grid; place-items: center; font-weight: 700; font-size: 0.78rem;
        font-variant-numeric: tabular-nums;
        background: var(--superficie, #fff); color: var(--texto-tenue, #5f7387);
        box-shadow: inset 0 0 0 1.5px var(--borde-fuerte, #b5c3d1);
        transition: background-color 0.25s ease, color 0.25s ease, box-shadow 0.25s ease;
      }
      ol.pasos li.hecho { color: var(--acento, #0f7b6c); }
      ol.pasos li.hecho::after { background: var(--acento, #0f7b6c); }
      ol.pasos li.hecho::before { content: ""; background: var(--acento, #0f7b6c); box-shadow: none; }
      ol.pasos li.hecho .marca { display: block; }
      ol.pasos li[aria-current="step"] { color: var(--texto, #14202b); font-weight: 700; }
      ol.pasos li[aria-current="step"]::after { background: var(--mate, #2c4a63); }
      ol.pasos li[aria-current="step"]::before {
        background: var(--mate, #2c4a63); color: #fff;
        box-shadow: 0 0 0 4px var(--mate-suave, #e9eff4);
      }
      .marca {
        display: none; position: absolute; left: 0.4rem; top: 0.42rem; width: 0.85rem; height: 0.85rem;
        background: #fff; -webkit-mask: ${check} center / contain no-repeat; mask: ${check} center / contain no-repeat;
      }
      section[data-paso] { display: grid; gap: 0.75rem; animation: entrar 0.28s cubic-bezier(0.16, 1, 0.3, 1); }
      section[data-paso] h3 { font-size: 1.12rem; line-height: 1.3; }
      @keyframes entrar { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: none; } }
      .ayuda { font-size: 0.82rem; color: var(--texto-suave, #4d6378); }
      .rango { display: grid; grid-template-columns: 1fr 1fr; gap: 0.9rem; }
      .rango input { font-variant-numeric: tabular-nums; }
      .eleccion { display: flex; flex-wrap: wrap; gap: 0.4rem; }
      .eleccion:empty { display: none; }
      .eleccion span {
        font-size: 0.78rem; font-weight: 500; padding: 0.22rem 0.65rem; border-radius: 999px;
        background: var(--acento-suave, #e6f3f0); color: var(--acento-fuerte, #0b6155);
        box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--acento, #0f7b6c) 20%, transparent);
      }
      .acciones {
        display: flex; gap: 0.6rem; justify-content: flex-end; flex-wrap: wrap;
        padding-top: 1rem; border-top: 1px solid var(--borde, #d6dee7);
      }
      @media (prefers-reduced-motion: reduce) {
        section[data-paso] { animation: none; }
      }
      @media (max-width: 620px) {
        form { padding: 1.1rem 1rem 1rem; }
        ol.pasos li { font-size: 0; gap: 0; justify-content: center; }
        ol.pasos li[aria-current="step"] { font-size: 0.75rem; gap: 0.4rem; justify-content: flex-start; }
        .marca { left: 50%; margin-left: -0.425rem; }
        .rango { grid-template-columns: 1fr; }
        .acciones button { flex: 1; }
      }
    `;
  }

  connectedCallback() {
    if (this._iniciado) return;
    this._iniciado = true;
    this.paso = 1;
    this.render(`
      <form class="tarjeta" novalidate aria-label="Asistente de búsqueda de citas">
        <ol class="pasos">${PASOS.map((nombre) => `<li><span class="marca" aria-hidden="true"></span>${nombre}</li>`).join("")}</ol>
        <div class="eleccion" aria-label="Datos elegidos"></div>

        <section data-paso="1">
          <h3>¿Con qué documento está registrado?</h3>
          <ui-autocompletar id="tipo" etiqueta="Tipo de documento"></ui-autocompletar>
        </section>

        <section data-paso="2" hidden>
          <h3>Escriba su número de documento</h3>
          <div class="campo">
            <label for="numero">Número de documento</label>
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
              <input type="date" id="desde" />
            </div>
            <div class="campo">
              <label for="hasta">Hasta</label>
              <input type="date" id="hasta" />
            </div>
          </div>
          <small class="ayuda">Deje las fechas vacías para ver todas sus citas, pasadas y futuras.</small>
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
    this.q("#tipo").addEventListener("change", () => this.mostrarFormato());
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
    const tipos = [...(this._datos?.tiposDocumento ?? [])]
      .filter((tipo) => tipo.activo !== false)
      .sort((a, b) => String(a.nombre).localeCompare(String(b.nombre)));
    this.q("#tipo").opciones = tipos.map((tipo) => ({ valor: tipo.idTipoDocumento, texto: tipo.nombre }));
  }

  errorCarga(mensaje) {
    this.q("#tipo").mensaje = "No disponible";
    this.mostrarError(mensaje);
  }

  tipoElegido() {
    const id = this.q("#tipo").value;
    return (this._datos?.tiposDocumento ?? []).find((tipo) => String(tipo.idTipoDocumento) === String(id)) ?? null;
  }

  mostrarFormato() {
    const tipo = this.tipoElegido();
    const ayuda = tipo?.indicaciones || (tipo?.expresionRegular ? `Formato: ${tipo.expresionRegular}` : "");
    this.q("#formato").textContent = ayuda;
    this.q("#numero").placeholder = tipo?.nombre === "DUI" ? "Ej. 01234567-8" : "";
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
    return undefined;
  }

  validarDocumento() {
    const tipo = this.tipoElegido();
    const numero = this.q("#numero").value.trim();
    if (!numero) {
      this.mostrarError("Escriba el número de documento.");
      return false;
    }
    if (!cumpleFormato(tipo, numero)) {
      this.mostrarError(`El número no tiene el formato esperado: ${tipo.indicaciones || tipo.expresionRegular}.`);
      return false;
    }
    const persona = personaDeDocumento(this._datos, tipo.idTipoDocumento, numero);
    if (!persona) {
      this.mostrarError("El documento no está registrado en GalenoSV. Verifique el tipo y el número.");
      return false;
    }
    const clinicas = clinicasDePaciente(this._datos, persona.idPersona).sort((a, b) =>
      String(a.nombre).localeCompare(String(b.nombre))
    );
    if (clinicas.length === 0) {
      this.mostrarError("El documento no tiene clínicas registradas como paciente.");
      return false;
    }
    this._persona = persona;
    this._clinicas = clinicas;
    const campoClinica = this.q("#clinica");
    campoClinica.opciones = clinicas.map((clinica) => ({ valor: clinica.idClinica, texto: clinica.nombre }));
    if (clinicas.length === 1) campoClinica.value = clinicas[0].idClinica;
    return true;
  }

  buscar() {
    this.mostrarError("");
    const desde = this.q("#desde").value || null;
    const hasta = this.q("#hasta").value || null;
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

  pintarEleccion() {
    const partes = [];
    if (this.paso > 1) partes.push(this.tipoElegido()?.nombre);
    if (this.paso > 2) partes.push(this.q("#numero").value.trim());
    if (this.paso > 3) partes.push(this.q("#clinica").textoElegido);
    this.q(".eleccion").innerHTML = partes.filter(Boolean).map((parte) => `<span>${escapar(parte)}</span>`).join("");
  }

  /** Vuelve al paso 1 con el formulario vacío. */
  reiniciar() {
    this.q("#tipo").value = "";
    this.q("#numero").value = "";
    this.q("#desde").value = "";
    this.q("#hasta").value = "";
    this.q("#clinica").opciones = [];
    this._persona = null;
    this._clinicas = [];
    this.mostrarFormato();
    this.irA(1);
  }
}

customElements.define("asistente-citas", AsistenteCitas);
