function interpretar(valor) {
  if (valor === null || valor === undefined || valor === "") return null;
  const fecha = new Date(valor);
  return Number.isNaN(fecha.getTime()) ? null : fecha;
}

const DIA_MES_ANIO = { day: "numeric", month: "short", year: "numeric" };

/** Opciones de cada formato de fecha que usa la aplicación (locale es-SV). */
const OPCIONES = {
  fechaHora: { ...DIA_MES_ANIO, hour: "numeric", minute: "2-digit" },
  fecha: DIA_MES_ANIO,
  calendario: { ...DIA_MES_ANIO, timeZone: "UTC" },
  mes: { month: "short" },
  dia: { day: "2-digit" },
  hora: { hour: "numeric", minute: "2-digit" },
};

const formateadores = new Map();

/**
 * Formateador de un formato, creado la primera vez que se usa y reutilizado después:
 * `toLocaleString` arma uno nuevo en cada llamada, y MDN recomienda reutilizar un
 * Intl.DateTimeFormat cuando se formatean muchas fechas. Crearlo al usarlo, y no al cargar el
 * módulo, evita cargar los datos del idioma durante el arranque. El texto es el mismo.
 */
function formateador(nombre) {
  if (!formateadores.has(nombre)) formateadores.set(nombre, new Intl.DateTimeFormat("es-SV", OPCIONES[nombre]));
  return formateadores.get(nombre);
}

function formatear(nombre, valor) {
  const fecha = interpretar(valor);
  return fecha ? formateador(nombre).format(fecha) : "—";
}

/**
 * Fecha y hora legibles en español de El Salvador, o «—» si no hay fecha válida.
 * @param {string|Date|null} valor
 * @returns {string}
 */
export function fechaHora(valor) {
  return formatear("fechaHora", valor);
}

/**
 * Fecha legible sin hora, o «—» si no hay fecha válida.
 * @param {string|Date|null} valor
 * @returns {string}
 */
export function fechaCorta(valor) {
  return formatear("fecha", valor);
}

/** Las fechas de nacimiento se guardan a medianoche UTC en la API. */
export function fechaCalendario(valor) {
  return formatear("calendario", valor);
}

/**
 * Partes de la fecha para el bloque de calendario de una tarjeta, o null si no es válida.
 * @param {string|Date|null} valor
 * @returns {{mes: string, dia: string, hora: string}|null} mes abreviado sin punto, día con dos cifras y hora
 */
export function partesCalendario(valor) {
  const fecha = interpretar(valor);
  if (!fecha) return null;
  return {
    mes: formateador("mes").format(fecha).replace(".", ""),
    dia: formateador("dia").format(fecha),
    hora: formateador("hora").format(fecha),
  };
}

/** Fecha local en formato YYYY-MM-DD, sin depender de la zona UTC. */
export function fechaISO(fecha = new Date()) {
  const mes = String(fecha.getMonth() + 1).padStart(2, "0");
  const dia = String(fecha.getDate()).padStart(2, "0");
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

/**
 * Fecha local de hoy en formato YYYY-MM-DD.
 * @returns {string}
 */
export function hoyISO() {
  return fechaISO();
}

/**
 * Fecha y hora local con su zona (por ejemplo 2026-11-23T10:30:00-06:00), como la espera la API.
 * @returns {string}
 */
export function ahoraISO() {
  const fecha = new Date();
  const zona = -fecha.getTimezoneOffset();
  const signo = zona >= 0 ? "+" : "-";
  const horas = String(Math.floor(Math.abs(zona) / 60)).padStart(2, "0");
  const minutos = String(Math.abs(zona) % 60).padStart(2, "0");
  const local = new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().slice(0, 19);
  return `${local}${signo}${horas}:${minutos}`;
}
