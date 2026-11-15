import { Vista, escapar } from "../base.js";
import { personaDeDocumento, clinicasDePaciente } from "../datos.js";

const PASOS = ["Tipo de documento", "Número de documento", "Clínica", "Rango de fechas"];

/**
 * HU1 · Asistente de búsqueda de citas en cuatro pasos:
 * tipo de documento → número → clínica donde es paciente → rango de fechas.
 * No consulta la API: recibe los datos ya cargados en `datos` y emite
 * `buscar-citas` con la persona, la clínica y el rango elegidos.
 */
export class AsistenteCitas extends Vista {
  get estilos() {
    return `
      form { display: grid; gap: 1rem; }
      ol.pasos {
        list-style: none; margin: 0; padding: 0;
        display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.5rem;
        counter-reset: paso;
      }
      ol.pasos li {
        counter-increment: paso;
        display: flex; align-items: center; gap: 0.45rem;
        font-size: 0.78rem; color: #587089;
        padding-bottom: 0.55rem; border-bottom: 3px solid var(--borde, #cbd5e1);
      }
      ol.pasos li::before {
        content: counter(paso);
        flex: none; width: 1.6rem; height: 1.6rem; border-radius: 999px;
        display: grid; place-items: center; font-weight: 700;
        background: #eef2f6; color: var(--mate, #2c4a63);
      }
      ol.pasos li.hecho { color: var(--acento, #0f7b6c); border-color: var(--acento, #0f7b6c); }
      ol.pasos li.hecho::before { content: "✓"; background: var(--acento, #0f7b6c); color: #fff; }
      ol.pasos li[aria-current="step"] { color: var(--texto, #14202b); font-weight: 700; border-color: var(--mate, #2c4a63); }
      ol.pasos li[aria-current="step"]::before { background: var(--mate, #2c4a63); color: #fff; }
      [hidden] { display: none !important; }
      section[data-paso] { display: grid; gap: 0.6rem; }
      section[data-paso] h3 { font-size: 1.05rem; }
      .ayuda { font-size: 0.82rem; color: #587089; }
      fieldset { border: none; margin: 0; padding: 0; display: grid; gap: 0.5rem; }
      legend { font-size: 0.8rem; font-weight: 600; color: var(--mate, #2c4a63); margin-bottom: 0.3rem; }
      .opcion {
        display: flex; align-items: center; gap: 0.6rem; cursor: pointer;
        padding: 0.7rem 0.85rem; border: 1px solid var(--borde, #cbd5e1); border-radius: 12px; background: #fff;
      }
      .opcion:has(input:checked) { border-color: var(--acento, #0f7b6c); background: #f0f8f6; }
      .opcion input { accent-color: var(--acento, #0f7b6c); width: 1.05rem; height: 1.05rem; }
      .rango { display: grid; grid-template-columns: 1fr 1fr; gap: 0.8rem; }
      .eleccion { display: flex; flex-wrap: wrap; gap: 0.4rem; }
      .eleccion span {
        font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 999px;
        background: #eef5f2; color: var(--acento, #0f7b6c);
      }
      .acciones { display: flex; gap: 0.6rem; justify-content: flex-end; flex-wrap: wrap; }
      button:focus-visible, .opcion:focus-within { outline: 3px solid color-mix(in srgb, var(--acento, #0f7b6c) 45%, white); outline-offset: 2px; }
      @media (max-width: 620px) {
        ol.pasos li { font-size: 0; gap: 0; justify-content: center; }
        ol.pasos li[aria-current="step"] { font-size: 0.75rem; gap: 0.4rem; }
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
        <ol class="pasos">${PASOS.map((nombre) => `<li>${nombre}</li>`).join("")}</ol>
        <div class="eleccion" aria-label="Datos elegidos"></div>

        <section data-paso="1">
          <h3>¿Con qué documento está registrado?</h3>
          <div class="campo">
            <label for="tipo">Tipo de documento</label>
            <select id="tipo"><option value="">Cargando…</option></select>
          </div>
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
          <fieldset>
            <legend>Clínicas donde usted está registrado como paciente</legend>
            <div id="clinicas"></div>
          </fieldset>
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
    this.q("#tipo").innerHTML =
      `<option value="">Seleccione…</option>` +
      tipos.map((tipo) => `<option value="${escapar(tipo.idTipoDocumento)}">${escapar(tipo.nombre)}</option>`).join("");
  }

  errorCarga(mensaje) {
    this.q("#tipo").innerHTML = `<option value="">No disponible</option>`;
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
      if (!this.q('input[name="clinica"]:checked')) return this.mostrarError("Seleccione una clínica.");
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
    if (tipo?.expresionRegular && !this.cumpleFormato(numero, tipo.expresionRegular)) {
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
    this.q("#clinicas").innerHTML = clinicas
      .map(
        (clinica) => `<label class="opcion"><input type="radio" name="clinica" value="${escapar(clinica.idClinica)}" ${
          clinicas.length === 1 ? "checked" : ""
        } /> ${escapar(clinica.nombre)}</label>`
      )
      .join("");
    return true;
  }

  cumpleFormato(valor, expresion) {
    try {
      return new RegExp(expresion).test(valor);
    } catch {
      return true; // Una expresión mal escrita en el catálogo no debe bloquear al paciente.
    }
  }

  buscar() {
    this.mostrarError("");
    const desde = this.q("#desde").value || null;
    const hasta = this.q("#hasta").value || null;
    if (desde && hasta && desde > hasta) {
      this.mostrarError("La fecha inicial no puede ser posterior a la fecha final.");
      return;
    }
    const idClinica = this.q('input[name="clinica"]:checked')?.value ?? null;
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
    const primerCampo = this.q(`section[data-paso="${this.paso}"]`).querySelector("select, input");
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
    if (this.paso > 3) partes.push(this.q('input[name="clinica"]:checked')?.closest("label").textContent.trim());
    this.q(".eleccion").innerHTML = partes.filter(Boolean).map((parte) => `<span>${escapar(parte)}</span>`).join("");
  }

  /** Vuelve al paso 1 con el formulario vacío. */
  reiniciar() {
    this.q("#tipo").value = "";
    this.q("#numero").value = "";
    this.q("#desde").value = "";
    this.q("#hasta").value = "";
    this.q("#clinicas").innerHTML = "";
    this._persona = null;
    this._clinicas = [];
    this.mostrarFormato();
    this.irA(1);
  }
}

customElements.define("asistente-citas", AsistenteCitas);
