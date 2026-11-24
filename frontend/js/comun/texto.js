const ENTIDADES_HTML = {
  "&": "&amp;",
  "<": "&lt;",
  ">": "&gt;",
  '"': "&quot;",
  "'": "&#39;",
};

/** Cantidad con la palabra en singular o plural. */
export function cantidad(numero, singular, plural) {
  return `${numero} ${numero === 1 ? singular : plural}`;
}

/** Escapa texto antes de insertarlo como contenido HTML. */
export function escapar(texto) {
  return String(texto ?? "").replace(/[&<>"']/g, (caracter) => ENTIDADES_HTML[caracter]);
}

/** Fragmento de HTML ya armado: al interpolarlo en otra plantilla no se vuelve a escapar. */
class Html {
  constructor(texto) {
    this.texto = texto;
  }

  toString() {
    return this.texto;
  }
}

function aHtml(valor) {
  if (valor instanceof Html) return valor.texto;
  if (Array.isArray(valor)) return valor.map(aHtml).join("");
  if (valor === null || valor === undefined || valor === false) return "";
  return escapar(valor);
}

/**
 * Plantilla de HTML que escapa sola cada dato interpolado.
 *   html`<li>${paciente.nombre}</li>`        → el nombre nunca se interpreta como HTML
 *   html`<ul>${lista.map((x) => html`<li>${x}</li>`)}</ul>`  → los fragmentos y listas se insertan tal cual
 * `null`, `undefined` y `false` no pintan nada, para escribir condiciones como `${cond && html`...`}`.
 */
export function html(partes, ...valores) {
  return new Html(partes.reduce((resultado, parte, indice) => resultado + aHtml(valores[indice - 1]) + parte));
}

/** Quita acentos sin cambiar la posición de los caracteres, para resaltar coincidencias. */
function sinAcentos(valor) {
  return String(valor ?? "").normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

/** Forma de búsqueda: sin acentos, en minúsculas y sin espacios sobrantes. */
export function normalizarTexto(valor) {
  return sinAcentos(valor).toLowerCase().trim();
}

/**
 * Nombres y apellidos de una persona en una sola línea.
 * @param {{nombres?: string, apellidos?: string}|null} persona
 * @returns {string}
 */
export function nombreCompleto(persona) {
  return `${persona?.nombres ?? ""} ${persona?.apellidos ?? ""}`.trim();
}

/** Para el encabezado: primer nombre y primer apellido, sin cortar el nombre completo. */
export function nombreCorto(persona) {
  const primeraPalabra = (valor) => String(valor ?? "").trim().split(/\s+/)[0] ?? "";
  return [primeraPalabra(persona?.nombres), primeraPalabra(persona?.apellidos)].filter(Boolean).join(" ");
}
