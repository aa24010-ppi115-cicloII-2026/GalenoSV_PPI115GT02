import { Vista, definirElemento } from "../base.js";
import { escapar, normalizarTexto } from "../texto.js";

/**
 * Campo de búsqueda con autocompletado para elegir un dato del catálogo (patrón combobox de ARIA).
 *   <ui-autocompletar id="tipo" etiqueta="Tipo de documento"></ui-autocompletar>
 *   campo.opciones = [{ valor, texto }];   campo.value;   evento "change" al elegir.
 * Mientras no hay opciones muestra `mensaje` (por ejemplo «Cargando…»).
 */
export class UiAutocompletar extends Vista {

  connectedCallback() {
    if (this._iniciado) return;
    this._iniciado = true;
    this._opciones ??= [];
    this._valor ??= "";
    this.render(`
      <div class="campo">
        <label for="entrada">${escapar(this.getAttribute("etiqueta") ?? "")}</label>
        <div class="caja">
          <input id="entrada" type="text" role="combobox" autocomplete="off" spellcheck="false"
            aria-autocomplete="list" aria-expanded="false" aria-controls="lista" />
          <span class="flecha" aria-hidden="true"></span>
          <ul id="lista" role="listbox" aria-label="${escapar(this.getAttribute("etiqueta") ?? "Opciones")}" hidden></ul>
        </div>
      </div>
    `);
    const entrada = this.entrada;
    if (this.hasAttribute("descrito-por")) entrada.setAttribute("aria-describedby", this.getAttribute("descrito-por"));
    entrada.addEventListener("input", () => this.abrir());
    entrada.addEventListener("click", () => this.abrir());
    entrada.addEventListener("keydown", (evento) => this.tecla(evento));
    entrada.addEventListener("blur", () => this.confirmarTexto());
    const lista = this.q("#lista");
    // mousedown cancela el blur del campo para que el clic llegue a la opción.
    lista.addEventListener("mousedown", (evento) => evento.preventDefault());
    lista.addEventListener("click", (evento) => {
      const item = evento.target.closest("li[data-indice]");
      if (item) this.elegir(this._filtradas[Number(item.dataset.indice)]);
    });
    this.mensaje = this._opciones.length ? null : this._mensaje ?? "Cargando…";
    this.pintarEntrada();
  }

  get entrada() {
    return this.q("#entrada");
  }

  set opciones(lista) {
    this._opciones = lista ?? [];
    if (!this._opciones.some((opcion) => String(opcion.valor) === String(this._valor))) this._valor = "";
    if (this._iniciado) {
      this.mensaje = null;
      this.cerrar();
      this.pintarEntrada();
    }
  }

  get opciones() {
    return this._opciones ?? [];
  }

  get value() {
    return this._valor ?? "";
  }

  set value(valor) {
    const opcion = this.opciones.find((item) => String(item.valor) === String(valor));
    this._valor = opcion ? opcion.valor : "";
    if (this._iniciado) this.pintarEntrada();
  }

  /** Texto de la opción elegida, o cadena vacía. */
  get textoElegido() {
    return this.opciones.find((item) => String(item.valor) === String(this._valor))?.texto ?? "";
  }

  /** Deshabilita el campo mostrando un aviso en lugar de opciones; null lo habilita. */
  set mensaje(texto) {
    this._mensaje = texto;
    if (!this._iniciado) return;
    if (texto) this.cerrar();
    this.entrada.disabled = Boolean(texto);
    this.entrada.placeholder = texto ?? "Escriba para buscar…";
  }

  get mensaje() {
    return this._mensaje ?? null;
  }

  focus() {
    this.entrada?.focus();
  }

  pintarEntrada() {
    this.entrada.value = this.textoElegido;
  }

  abrir() {
    if (this.entrada.disabled) return;
    const texto = this.entrada.value;
    // Si el texto es el de la opción ya elegida se muestran todas, como un select.
    const consulta = texto === this.textoElegido ? "" : normalizarTexto(texto.trim());
    this._filtradas = this.opciones.filter((opcion) => normalizarTexto(opcion.texto).includes(consulta));
    this._activa = this._filtradas.length === 1 ? 0 : -1;
    const lista = this.q("#lista");
    lista.innerHTML =
      this._filtradas
        .map((opcion, indice) => {
          const elegida = String(opcion.valor) === String(this._valor);
          return `<li id="op-${indice}" role="option" data-indice="${indice}" aria-selected="${elegida}">${this.resaltar(opcion.texto, consulta)}</li>`;
        })
        .join("") || `<li class="vacia" role="option" aria-disabled="true">Sin resultados</li>`;
    lista.hidden = false;
    this.entrada.setAttribute("aria-expanded", "true");
    this.marcarActiva();
  }

  cerrar() {
    this.q("#lista").hidden = true;
    this.entrada.setAttribute("aria-expanded", "false");
    this.entrada.removeAttribute("aria-activedescendant");
  }

  get abierta() {
    return !this.q("#lista").hidden;
  }

  resaltar(texto, consulta) {
    const inicio = consulta ? normalizarTexto(texto).indexOf(consulta) : -1;
    if (inicio < 0) return escapar(texto);
    const fin = inicio + consulta.length;
    return `${escapar(texto.slice(0, inicio))}<mark>${escapar(texto.slice(inicio, fin))}</mark>${escapar(texto.slice(fin))}`;
  }

  marcarActiva() {
    this.qa("li[data-indice]").forEach((item, indice) => item.classList.toggle("activa", indice === this._activa));
    if (this._activa >= 0) {
      this.entrada.setAttribute("aria-activedescendant", `op-${this._activa}`);
      this.q(`#op-${this._activa}`)?.scrollIntoView?.({ block: "nearest" });
    } else {
      this.entrada.removeAttribute("aria-activedescendant");
    }
  }

  tecla(evento) {
    const total = this._filtradas?.length ?? 0;
    if (evento.key === "ArrowDown" || evento.key === "ArrowUp") {
      evento.preventDefault();
      if (!this.abierta) return this.abrir();
      if (total === 0) return undefined;
      const paso = evento.key === "ArrowDown" ? 1 : -1;
      this._activa = this._activa < 0
        ? (paso > 0 ? 0 : total - 1)
        : (this._activa + paso + total) % total;
      return this.marcarActiva();
    }
    if (evento.key === "Enter" && this.abierta) {
      // Con la lista abierta, Enter elige y no debe enviar el formulario ni avanzar de paso.
      evento.preventDefault();
      evento.stopPropagation();
      if (this._activa >= 0) this.elegir(this._filtradas[this._activa]);
      return undefined;
    }
    if (evento.key === "Escape" && this.abierta) {
      evento.stopPropagation();
      this.cerrar();
      this.pintarEntrada();
    }
    return undefined;
  }

  elegir(opcion) {
    if (!opcion) return;
    const cambio = String(opcion.valor) !== String(this._valor);
    this._valor = opcion.valor;
    this.pintarEntrada();
    this.cerrar();
    if (cambio) this.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
  }

  /** Al salir del campo: acepta un texto que coincide exactamente con una opción; si no, restaura la elegida. */
  confirmarTexto() {
    if (!this.abierta) return;
    const texto = normalizarTexto(this.entrada.value.trim());
    const exacta = this.opciones.find((opcion) => normalizarTexto(opcion.texto) === texto);
    if (exacta) return this.elegir(exacta);
    if (!texto && this._valor !== "") {
      this._valor = "";
      this.cerrar();
      this.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
      return undefined;
    }
    this.cerrar();
    this.pintarEntrada();
    return undefined;
  }
}

definirElemento("ui-autocompletar", UiAutocompletar);
