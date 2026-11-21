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

/** Quita acentos sin cambiar la posición de los caracteres, para resaltar coincidencias. */
function sinAcentos(valor) {
  return String(valor ?? "").normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

/** Forma de búsqueda: sin acentos, en minúsculas y sin espacios sobrantes. */
export function normalizarTexto(valor) {
  return sinAcentos(valor).toLowerCase().trim();
}

export function nombreCompleto(persona) {
  return `${persona?.nombres ?? ""} ${persona?.apellidos ?? ""}`.trim();
}

/** Para el encabezado: primer nombre y primer apellido, sin cortar el nombre completo. */
export function nombreCorto(persona) {
  const primeraPalabra = (valor) => String(valor ?? "").trim().split(/\s+/)[0] ?? "";
  return [primeraPalabra(persona?.nombres), primeraPalabra(persona?.apellidos)].filter(Boolean).join(" ");
}
