/**
 * Transporte HTTP hacia la API REST: cabeceras, errores, aviso de servidor caído y cola de
 * cambios sin conexión. Los recursos se usan a través de DefaultService (ver servicios.js).
 */

import { encolar, estaConectado } from "./pendientes.js";
import DefaultError from "../entity/default-error.js";

/** Raíz de la API REST del backend. */
export const API = "http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api";

const METODOS_DE_CAMBIO = ["POST", "PUT", "DELETE"];

/**
 * Causa de la falla dicha con palabras, para cuando la API no manda su propio mensaje.
 * El código HTTP no se muestra: queda en `status` del error.
 * @param {number} estado
 * @returns {string}
 */
function causaSegunEstado(estado) {
  if (estado === 404) return "No se encontró el registro solicitado.";
  if (estado === 409) return "La operación no se puede completar porque hay registros relacionados.";
  if (estado >= 500) return "El servidor no pudo completar la operación. Intente de nuevo en unos minutos.";
  return "Revise los datos ingresados.";
}

/**
 * Error con el mensaje que envió la API (su campo `mensaje`, escrito para el usuario) o, si no
 * lo trae, con la causa según el estado. `message` no se usa: ahí ponen su texto técnico el
 * servidor y las librerías. `status` lo distingue de una falla de red.
 * @param {Response} respuesta respuesta con estado distinto de 2xx
 * @returns {Promise<DefaultError>} se cumple siempre con el error armado
 */
function errorDe(respuesta) {
  const causa = causaSegunEstado(respuesta.status);
  const parametro = respuesta.headers.get("X-Parametro-Error");
  return respuesta
    .json()
    .then((cuerpo) => (typeof cuerpo?.mensaje === "string" && cuerpo.mensaje.trim() ? cuerpo.mensaje : causa))
    .catch(() => causa)
    .then((mensaje) => new DefaultError(mensaje, { status: respuesta.status, parametro }));
}

let servicioCaido = false;

/**
 * Hay red, pero la API no respondió: la app trabaja con lo guardado en el dispositivo.
 * @returns {boolean}
 */
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
 * @returns {Promise<Response>} se cumple con una respuesta 2xx; se rechaza con un DefaultError
 *   sin `status` si no hubo respuesta (servidor caído) o con el estado que devolvió la API
 */
export function pedir(ruta, opciones = {}) {
  const { headers: cabecerasPersonalizadas = {}, ...peticion } = opciones;
  const cabeceras = { Accept: "application/json", ...cabecerasPersonalizadas };
  if (peticion.body !== undefined && !Object.keys(cabeceras).some((clave) => clave.toLowerCase() === "content-type")) {
    cabeceras["Content-Type"] = "application/json";
  }
  return new Promise((resolve) => resolve(fetch(`${API}/${ruta}`, { ...peticion, headers: cabeceras })))
    .catch((error) => {
      marcarServicio(true);
      throw new DefaultError("No se pudo contactar al servidor de GalenoSV. Intente de nuevo en unos minutos.", { error });
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

/** Opciones de fetch para un cambio: DELETE va sin cuerpo. */
function opcionesDeCambio(metodo, cuerpo) {
  return metodo === "DELETE" ? { method: metodo } : { method: metodo, body: JSON.stringify(cuerpo ?? {}) };
}

/**
 * Comprueba si el servidor responde de verdad (no la copia del service worker).
 * @returns {Promise<boolean>}
 */
export function comprobarServicio() {
  if (!estaConectado()) return Promise.resolve(false);
  return pedir("roles?first=0&max=1")
    .then(() => !servicioNoResponde())
    .catch(() => false);
}

/**
 * Envía un cambio a la API; sin conexión, o si el servidor no responde, lo guarda en la cola.
 * @param {"POST"|"PUT"|"DELETE"} metodo
 * @param {string} ruta recurso, con el id cuando el método lo requiere
 * @param {object|null} [cuerpo]
 * @returns {Promise<string|null>} la cabecera Location de un POST, o null; se rechaza solo
 *   si la API contestó con un error (el cambio no se encola)
 */
export function mutar(metodo, ruta, cuerpo = null) {
  return new Promise((resolve) => {
    if (!estaConectado()) {
      encolar({ metodo, ruta, cuerpo });
      resolve(null);
      return;
    }
    resolve(
      pedir(ruta, opcionesDeCambio(metodo, cuerpo))
        .then((respuesta) => (metodo === "POST" ? respuesta.headers.get("Location") : null))
        .catch((error) => {
          if (error.status) throw error;
          encolar({ metodo, ruta, cuerpo });
          return null;
        })
    );
  });
}

/**
 * Reenvía un cambio guardado en la cola sin conexión. La ruta ya trae el identificador
 * cuando el método lo requiere.
 * @param {{metodo: string, ruta: string, cuerpo: object|null}} pendiente
 * @returns {Promise<void>} se rechaza con un DefaultError si la API no lo acepta
 */
export function ejecutarPendiente(pendiente) {
  return new Promise((resolve) => {
    const { metodo, ruta, cuerpo } = pendiente;
    if (!METODOS_DE_CAMBIO.includes(metodo)) throw new DefaultError(`Método pendiente no admitido: ${metodo}`);
    resolve(pedir(ruta, opcionesDeCambio(metodo, cuerpo)).then(() => undefined));
  });
}
