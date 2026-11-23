import { encolar, estaConectado } from "./pendientes.js";

export const API = "http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api";

/**
 * Error con el mensaje que envió la API. `status` lo distingue de una falla de red.
 * @param {Response} respuesta respuesta con estado distinto de 2xx
 * @returns {Promise<Error>} se cumple siempre con el error armado
 */
function errorDe(respuesta) {
  const base = `Error ${respuesta.status}`;
  const parametro = respuesta.headers.get("X-Parametro-Error");
  return respuesta
    .json()
    .then((cuerpo) => cuerpo.mensaje || cuerpo.message || base)
    .catch(() => `${base} ${respuesta.statusText}`.trim())
    .then((mensaje) => {
      const error = new Error(parametro ? `${mensaje} (${parametro})` : mensaje);
      error.status = respuesta.status;
      return error;
    });
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

/**
 * Petición HTTP a la API con las cabeceras JSON.
 * @param {string} ruta recurso relativo a la API, con su query string
 * @param {RequestInit} [opciones] método, cuerpo y cabeceras extra
 * @returns {Promise<Response>} se cumple con una respuesta 2xx; se rechaza si no hubo
 *   respuesta (servidor caído) o con el error de la API (con `status`)
 */
function pedir(ruta, opciones = {}) {
  const { headers: cabecerasPersonalizadas = {}, ...peticion } = opciones;
  const cabeceras = { Accept: "application/json", ...cabecerasPersonalizadas };
  if (peticion.body !== undefined && !Object.keys(cabeceras).some((clave) => clave.toLowerCase() === "content-type")) {
    cabeceras["Content-Type"] = "application/json";
  }
  return new Promise((resolve) => resolve(fetch(`${API}/${ruta}`, { ...peticion, headers: cabeceras })))
    .catch(() => {
      marcarServicio(true);
      throw new Error("No se pudo contactar al servidor de GalenoSV. Intente de nuevo en unos minutos.");
    })
    .then((respuesta) => {
      // El service worker marca así lo que respondió desde el dispositivo porque la API no contestó.
      marcarServicio(respuesta.headers.get("X-Sin-Conexion") === "true");
      if (respuesta.ok) return respuesta;
      return errorDe(respuesta).then((error) => {
        throw error;
      });
    });
}

/**
 * Busca un conjunto de registros del recurso usando paginación.
 * @param {string} ruta recurso de la API, por ejemplo "clinicas"
 * @param {{first?: number, max?: number}} [rango] primer registro y cantidad máxima a obtener
 * @returns {Promise<{datos: object[], total: number}>} los registros de la página y el total
 *   de la cabecera X-Total-Count; se rechaza si falta el total o el cuerpo no es una lista
 */
export function listar(ruta, { first = 0, max = 20 } = {}) {
  return pedir(`${ruta}?first=${first}&max=${max}`).then((respuesta) => {
    const cantidadTotal = respuesta.headers.get("X-Total-Count");
    const total = Number(cantidadTotal);
    if (!/^\d+$/.test(cantidadTotal ?? "") || !Number.isSafeInteger(total)) {
      throw new Error(`La respuesta de ${ruta} no incluye un X-Total-Count válido.`);
    }
    return respuesta.json().then((datos) => {
      if (!Array.isArray(datos)) throw new Error(`La respuesta de ${ruta} no contiene una lista.`);
      return { datos, total };
    });
  });
}

/**
 * Todos los registros del recurso, pidiendo página tras página hasta llegar al total.
 * @param {string} ruta recurso de la API
 * @returns {Promise<object[]>} se rechaza si una página llega vacía antes del total
 */
export function listarTodo(ruta) {
  const tamano = 100;
  const datos = [];
  const pedirDesde = (first) =>
    listar(ruta, { first, max: tamano }).then((pagina) => {
      datos.push(...pagina.datos);
      const leidos = first + pagina.datos.length;
      if (pagina.datos.length === 0 && leidos < pagina.total) {
        throw new Error(`La respuesta de ${ruta} quedó incompleta antes de llegar a ${pagina.total} registros.`);
      }
      return leidos < pagina.total ? pedirDesde(leidos) : datos;
    });
  return pedirDesde(0);
}

/**
 * Comprueba si el servidor responde de verdad (no la copia del service worker).
 * @returns {Promise<boolean>}
 */
export function comprobarServicio() {
  if (!estaConectado()) return Promise.resolve(false);
  return listar("roles", { max: 1 })
    .then(() => !servicioNoResponde())
    .catch(() => false);
}

/**
 * Envía un cambio a la API; sin conexión, o si el servidor no responde, lo guarda en la cola.
 * @returns {Promise<string|null>} la cabecera Location de un POST, o null; se rechaza solo
 *   si la API contestó con un error (el cambio no se encola)
 */
function mutar(metodo, ruta, cuerpo = null) {
  return new Promise((resolve) => {
    if (!estaConectado()) {
      encolar({ metodo, ruta, cuerpo });
      resolve(null);
      return;
    }
    const opciones = { method: metodo };
    if (metodo !== "DELETE") opciones.body = JSON.stringify(cuerpo ?? {});
    resolve(
      pedir(ruta, opciones)
        .then((respuesta) => (metodo === "POST" ? respuesta.headers.get("Location") : null))
        .catch((error) => {
          if (error.status) throw error;
          encolar({ metodo, ruta, cuerpo });
          return null;
        })
    );
  });
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
export function ejecutarPendiente(pendiente) {
  return new Promise((resolve) => {
    const { metodo, ruta, cuerpo } = pendiente;
    if (!["POST", "PUT", "DELETE"].includes(metodo)) throw new Error(`Método pendiente no admitido: ${metodo}`);
    const opciones = { method: metodo };
    if (metodo !== "DELETE") opciones.body = JSON.stringify(cuerpo ?? {});
    resolve(pedir(ruta, opciones).then(() => undefined));
  });
}
