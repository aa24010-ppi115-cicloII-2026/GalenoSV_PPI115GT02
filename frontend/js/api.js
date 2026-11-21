import { encolar, estaConectado } from "./pendientes.js";

export const API = "http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api";

async function errorDe(respuesta) {
  let mensaje = `Error ${respuesta.status}`;
  const parametro = respuesta.headers.get("X-Parametro-Error");
  try {
    const cuerpo = await respuesta.json();
    mensaje = cuerpo.mensaje || cuerpo.message || mensaje;
  } catch {
    mensaje = `${mensaje} ${respuesta.statusText}`.trim();
  }
  if (parametro) mensaje = `${mensaje} (${parametro})`;
  const error = new Error(mensaje);
  error.status = respuesta.status;
  return error;
}

let servicioCaido = false;

/** Hay red, pero la API no respondió: la app trabaja con lo guardado en el dispositivo. */
export function servicioNoResponde() {
  return servicioCaido;
}

function marcarServicio(caido) {
  if (caido === servicioCaido) return;
  servicioCaido = caido;
  if (typeof window !== "undefined") window.dispatchEvent(new Event("servicio-cambio"));
}

async function pedir(ruta, opciones = {}) {
  const { headers: cabecerasPersonalizadas = {}, ...peticion } = opciones;
  const cabeceras = { Accept: "application/json", ...cabecerasPersonalizadas };
  if (peticion.body !== undefined && !Object.keys(cabeceras).some((clave) => clave.toLowerCase() === "content-type")) {
    cabeceras["Content-Type"] = "application/json";
  }
  let respuesta;
  try {
    respuesta = await fetch(`${API}/${ruta}`, { ...peticion, headers: cabeceras });
  } catch {
    marcarServicio(true);
    throw new Error("No se pudo contactar al servidor de GalenoSV. Intente de nuevo en unos minutos.");
  }
  // El service worker marca así lo que respondió desde el dispositivo porque la API no contestó.
  marcarServicio(respuesta.headers.get("X-Sin-Conexion") === "true");
  if (!respuesta.ok) throw await errorDe(respuesta);
  return respuesta;
}

export async function listar(ruta, { first = 0, max = 20 } = {}) {
  const respuesta = await pedir(`${ruta}?first=${first}&max=${max}`);
  const cantidadTotal = respuesta.headers.get("X-Total-Count");
  const total = Number(cantidadTotal);
  if (!/^\d+$/.test(cantidadTotal ?? "") || !Number.isSafeInteger(total)) {
    throw new Error(`La respuesta de ${ruta} no incluye un X-Total-Count válido.`);
  }
  const datos = await respuesta.json();
  if (!Array.isArray(datos)) throw new Error(`La respuesta de ${ruta} no contiene una lista.`);
  return {
    datos,
    total,
  };
}

export async function listarTodo(ruta) {
  const tamano = 100;
  let first = 0;
  let total = Infinity;
  const datos = [];
  while (first < total) {
    const pagina = await listar(ruta, { first, max: tamano });
    datos.push(...pagina.datos);
    total = pagina.total;
    first += pagina.datos.length;
    if (pagina.datos.length === 0 && first < total) {
      throw new Error(`La respuesta de ${ruta} quedó incompleta antes de llegar a ${total} registros.`);
    }
  }
  return datos;
}

export async function comprobarServicio() {
  if (!estaConectado()) return false;
  try {
    await listar("roles", { max: 1 });
    return !servicioNoResponde();
  } catch {
    return false;
  }
}

async function mutar(metodo, ruta, cuerpo = null) {
  if (!estaConectado()) {
    encolar({ metodo, ruta, cuerpo });
    return null;
  }
  const opciones = { method: metodo };
  if (metodo !== "DELETE") opciones.body = JSON.stringify(cuerpo ?? {});
  try {
    const respuesta = await pedir(ruta, opciones);
    return metodo === "POST" ? respuesta.headers.get("Location") : null;
  } catch (error) {
    if (error.status) throw error;
    encolar({ metodo, ruta, cuerpo });
    return null;
  }
}

export function crear(ruta, datos) {
  return mutar("POST", ruta, datos);
}

export function actualizar(ruta, id, datos) {
  return mutar("PUT", `${ruta}/${id}`, datos);
}

export function eliminar(ruta, id) {
  return mutar("DELETE", `${ruta}/${id}`);
}

/**
 * Reenvia una operacion guardada en la cola sin conexion.
 * La ruta ya trae el identificador completo cuando el metodo lo requiere.
 */
export async function ejecutarPendiente(pendiente) {
  const { metodo, ruta, cuerpo } = pendiente;
  if (!["POST", "PUT", "DELETE"].includes(metodo)) throw new Error(`Método pendiente no admitido: ${metodo}`);
  const opciones = { method: metodo };
  if (metodo !== "DELETE") opciones.body = JSON.stringify(cuerpo ?? {});
  await pedir(ruta, opciones);
}
