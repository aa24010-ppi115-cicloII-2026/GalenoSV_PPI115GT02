import { Vista, definirElemento } from "../base.js";
import { normalizarTexto } from "../../comun/texto.js";

/**
 * Campo de búsqueda con autocompletado para elegir un dato del catálogo (patrón combobox de ARIA).
 *   <ui-autocompletar id="tipo" etiqueta="Tipo de documento" value="1"></ui-autocompletar>
 *   campo.opciones = [{ valor, texto }];   campo.value;   evento "change" al elegir.
 * Mientras no hay opciones muestra `mensaje` (por ejemplo «Cargando…»).
 * Como en un <input>, el atributo `value` es el valor inicial: se elige al llegar las opciones
 * mientras nadie haya elegido otro, y `form.reset()` vuelve a él. `etiqueta` y `descrito-por`
 * pueden cambiar en cualquier momento.
 *
 * Es un control de formulario (form-associated custom element): con `name` envía la opción
 * elegida con el formulario, con `required` queda inválido mientras no se elija una, y responde
 * a `form.reset()` y a `<fieldset disabled>`. Donde el navegador no tiene esa API sigue
 * funcionando igual, solo sin participar en el formulario.
 */
export class UiAutocompletar extends Vista {
  static formAssociated = true;
  static observedAttributes = ["required", "etiqueta", "descrito-por", "value"];
  static delegarFoco = true;

  static plantilla = `
    <div class="campo">
      <label for="entrada" part="etiqueta"></label>
      <div class="caja">
        <input id="entrada" part="entrada" type="text" role="combobox" autocomplete="off" spellcheck="false"
          aria-autocomplete="list" aria-expanded="false" aria-controls="lista" />
        <span class="flecha" aria-hidden="true"></span>
        <ul id="lista" part="lista" role="listbox" hidden></ul>
      </div>
    </div>
  `;

  #iniciado = false;
  #opciones = [];
  #valor = "";
  #mensaje;
  #filtradas = [];
  #activa = -1;
  #entrada = null;
  #lista = null;
  /** API de formularios del navegador (ElementInternals); null donde no existe. */
  #internos = null;
  #deshabilitadoPorFormulario = false;
  /** true desde que se elige una opción o se asigna `value`: el atributo `value` ya no la cambia. */
  #sucio = false;

  constructor() {
    super();
    const internos = this.attachInternals?.();
    if (typeof internos?.setFormValue === "function") this.#internos = internos;
  }

  connectedCallback() {
    if (this.#iniciado) return;
    this.#iniciado = true;
    this.montarPlantilla();
    this.#entrada = this.q("#entrada");
    this.#lista = this.q("#lista");
    this.#pintarAtributos();

    // Los nodos internos viven lo mismo que el componente: sus escuchas no necesitan limpieza.
    this.#entrada.addEventListener("input", () => this.abrir());
    this.#entrada.addEventListener("click", () => this.abrir());
    this.#entrada.addEventListener("keydown", (evento) => this.tecla(evento));
    this.#entrada.addEventListener("blur", () => this.confirmarTexto());
    // mousedown cancela el blur del campo para que el clic llegue a la opción.
    this.#lista.addEventListener("mousedown", (evento) => evento.preventDefault());
    this.#lista.addEventListener("click", (evento) => {
      const item = evento.target.closest("li[data-indice]");
      if (item) this.elegir(this.#filtradas[Number(item.dataset.indice)]);
    });
    this.mensaje = this.#opciones.length ? null : this.#mensaje ?? "Cargando…";
    this.pintarEntrada();
    this.#avisarFormulario();
  }

  attributeChangedCallback(nombre) {
    if (nombre === "value" && !this.#sucio) this.#aplicarValorInicial();
    if (this.#iniciado && (nombre === "etiqueta" || nombre === "descrito-por")) this.#pintarAtributos();
    this.#avisarFormulario();
  }

  /** El formulario se reinició: vuelve al valor inicial (atributo `value`), como un campo nativo. */
  formResetCallback() {
    this.#sucio = false;
    this.#aplicarValorInicial();
    if (this.#iniciado) this.cerrar();
  }

  /** Un `<fieldset disabled>` deshabilita la entrada interna, no el propio elemento. */
  formDisabledCallback(deshabilitado) {
    this.#deshabilitadoPorFormulario = deshabilitado;
    if (!this.#iniciado) return;
    if (deshabilitado) this.cerrar();
    this.#entrada.disabled = deshabilitado || Boolean(this.#mensaje);
  }

  get entrada() {
    return this.#entrada;
  }

  set opciones(lista) {
    this.#opciones = lista ?? [];
    const deseado = this.#sucio ? this.#valor : this.getAttribute("value") ?? "";
    this.#cambiarValor(this.#buscar(deseado)?.valor ?? "");
    if (this.#iniciado) {
      this.mensaje = null;
      this.cerrar();
      this.pintarEntrada();
    }
  }

  get opciones() {
    return this.#opciones;
  }

  get value() {
    return this.#valor;
  }

  set value(valor) {
    this.#sucio = true;
    this.#cambiarValor(this.#buscar(valor)?.valor ?? "");
    if (this.#iniciado) this.pintarEntrada();
  }

  /** Texto de la opción elegida, o cadena vacía. */
  get textoElegido() {
    return this.#buscar(this.#valor)?.texto ?? "";
  }

  /** Deshabilita el campo mostrando un aviso en lugar de opciones; null lo habilita. */
  set mensaje(texto) {
    this.#mensaje = texto;
    if (!this.#iniciado) return;
    if (texto) this.cerrar();
    this.#entrada.disabled = Boolean(texto) || this.#deshabilitadoPorFormulario;
    this.#entrada.placeholder = texto ?? "Escriba para buscar…";
  }

  get mensaje() {
    return this.#mensaje ?? null;
  }

  get abierta() {
    return !this.#lista.hidden;
  }

  #pintarAtributos() {
    const etiqueta = this.getAttribute("etiqueta");
    this.q("label").textContent = etiqueta ?? "";
    this.#lista.setAttribute("aria-label", etiqueta ?? "Opciones");
    const descripcion = this.getAttribute("descrito-por");
    if (descripcion) this.#entrada.setAttribute("aria-describedby", descripcion);
    else this.#entrada.removeAttribute("aria-describedby");
  }

  #aplicarValorInicial() {
    this.#cambiarValor(this.#buscar(this.getAttribute("value") ?? "")?.valor ?? "");
    if (this.#iniciado) this.pintarEntrada();
  }

  #cambiarValor(valor) {
    this.#valor = valor;
    this.#avisarFormulario();
  }

  /** Informa al formulario la opción elegida y si falta elegir una cuando es obligatoria. */
  #avisarFormulario() {
    if (!this.#internos) return;
    this.#internos.setFormValue(String(this.#valor));
    if (this.hasAttribute("required") && this.#valor === "") {
      const mensaje = `Elija una opción de «${this.getAttribute("etiqueta") ?? "la lista"}».`;
      this.#internos.setValidity({ valueMissing: true }, mensaje, this.#entrada ?? undefined);
    } else {
      this.#internos.setValidity({});
    }
  }

  #buscar(valor) {
    return this.#opciones.find((opcion) => String(opcion.valor) === String(valor));
  }

  pintarEntrada() {
    this.#entrada.value = this.textoElegido;
  }

  abrir() {
    if (this.#entrada.disabled) return;
    const texto = this.#entrada.value;
    // Si el texto es el de la opción ya elegida se muestran todas, como un select.
    const consulta = texto === this.textoElegido ? "" : normalizarTexto(texto.trim());
    this.#filtradas = this.#opciones.filter((opcion) => normalizarTexto(opcion.texto).includes(consulta));
    this.#activa = this.#filtradas.length === 1 ? 0 : -1;
    const items = this.#filtradas.map((opcion, indice) => {
      const item = document.createElement("li");
      item.id = `op-${indice}`;
      item.setAttribute("role", "option");
      item.setAttribute("part", "opcion");
      item.dataset.indice = indice;
      item.setAttribute("aria-selected", String(String(opcion.valor) === String(this.#valor)));
      item.append(...this.resaltar(opcion.texto, consulta));
      return item;
    });
    if (items.length === 0) {
      const vacia = document.createElement("li");
      vacia.className = "vacia";
      vacia.setAttribute("role", "option");
      vacia.setAttribute("aria-disabled", "true");
      vacia.textContent = "Sin resultados";
      items.push(vacia);
    }
    this.#lista.replaceChildren(...items);
    this.#lista.hidden = false;
    this.#entrada.setAttribute("aria-expanded", "true");
    this.marcarActiva();
  }

  cerrar() {
    this.#lista.hidden = true;
    this.#entrada.setAttribute("aria-expanded", "false");
    this.#entrada.removeAttribute("aria-activedescendant");
  }

  /** Partes del texto con la coincidencia dentro de <mark>; se insertan como nodos, nunca como HTML. */
  resaltar(texto, consulta) {
    const inicio = consulta ? normalizarTexto(texto).indexOf(consulta) : -1;
    if (inicio < 0) return [texto];
    const fin = inicio + consulta.length;
    const marca = document.createElement("mark");
    marca.textContent = texto.slice(inicio, fin);
    return [texto.slice(0, inicio), marca, texto.slice(fin)];
  }

  marcarActiva() {
    this.qa("li[data-indice]").forEach((item, indice) => item.classList.toggle("activa", indice === this.#activa));
    if (this.#activa >= 0) {
      this.#entrada.setAttribute("aria-activedescendant", `op-${this.#activa}`);
      this.q(`#op-${this.#activa}`)?.scrollIntoView?.({ block: "nearest" });
    } else {
      this.#entrada.removeAttribute("aria-activedescendant");
    }
  }

  tecla(evento) {
    const total = this.#filtradas.length;
    if (evento.key === "ArrowDown" || evento.key === "ArrowUp") {
      evento.preventDefault();
      if (!this.abierta) return this.abrir();
      if (total === 0) return undefined;
      const paso = evento.key === "ArrowDown" ? 1 : -1;
      this.#activa = this.#activa < 0
        ? (paso > 0 ? 0 : total - 1)
        : (this.#activa + paso + total) % total;
      return this.marcarActiva();
    }
    if (evento.key === "Enter" && this.abierta) {
      // Con la lista abierta, Enter elige y no debe enviar el formulario ni avanzar de paso.
      evento.preventDefault();
      evento.stopPropagation();
      if (this.#activa >= 0) this.elegir(this.#filtradas[this.#activa]);
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
    const cambio = String(opcion.valor) !== String(this.#valor);
    this.#sucio = true;
    this.#cambiarValor(opcion.valor);
    this.pintarEntrada();
    this.cerrar();
    if (cambio) this.#avisarCambio();
  }

  /** Al salir del campo: acepta un texto que coincide exactamente con una opción; si no, restaura la elegida. */
  confirmarTexto() {
    if (!this.abierta) return;
    const texto = normalizarTexto(this.#entrada.value.trim());
    const exacta = this.#opciones.find((opcion) => normalizarTexto(opcion.texto) === texto);
    if (exacta) {
      this.elegir(exacta);
      return;
    }
    this.cerrar();
    if (!texto && this.#valor !== "") {
      this.#sucio = true;
      this.#cambiarValor("");
      this.#avisarCambio();
      return;
    }
    this.pintarEntrada();
  }

  #avisarCambio() {
    this.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
  }
}

definirElemento("ui-autocompletar", UiAutocompletar);
