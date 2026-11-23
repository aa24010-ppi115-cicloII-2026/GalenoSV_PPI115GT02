/**
 * Repositorio de la aplicación: descarga todas las tablas de la API una sola vez, las guarda
 * en memoria y les aplica los cambios hechos sin conexión.
 */

import { listarTodo } from "./api.js";
import { listarPendientes } from "./pendientes.js";
import { FUENTES, CAMPO_ID, CLAVE_DE_RUTA } from "../entity/recursos.js";

/**
 * Muestra sobre los datos del servidor los cambios guardados sin conexión, para que la pantalla
 * refleje lo hecho antes de sincronizar. Trabaja sobre copias: los datos originales no cambian.
 */
export function aplicarPendientes(datos, pendientes) {
  if (pendientes.length === 0) return datos;
  const tablas = { ...datos };
  for (const { metodo, ruta, cuerpo } of pendientes) {
    const [recurso, id] = ruta.split("/");
    const clave = CLAVE_DE_RUTA[recurso];
    if (!clave) continue;
    const campo = CAMPO_ID[clave];
    if (tablas[clave] === datos[clave]) tablas[clave] = [...(datos[clave] ?? [])];
    const lista = tablas[clave];
    const indice = lista.findIndex((item) => String(item[campo]) === id);
    // Un alta sin id del cliente no se puede referenciar todavía; se verá al sincronizar.
    if (metodo === "POST" && cuerpo?.[campo]) {
      const existente = lista.findIndex((item) => String(item[campo]) === String(cuerpo[campo]));
      if (existente >= 0) lista[existente] = { ...lista[existente], ...cuerpo };
      else lista.push({ ...cuerpo });
    }
    else if (metodo === "PUT" && indice >= 0) lista[indice] = { ...lista[indice], ...cuerpo };
    else if (metodo === "DELETE" && indice >= 0) lista.splice(indice, 1);
  }
  return tablas;
}

let cache = null;

let carga = null;

let revisionCache = 0;

/** Pide una tabla completa y la devuelve como par [clave, filas]. */
function tabla(clave) {
  return listarTodo(FUENTES[clave]).then((filas) => [clave, filas]);
}

/**
 * Todas las tablas de la API, con los cambios pendientes sin conexión ya aplicados.
 * Las llamadas simultáneas comparten una sola descarga; `recargar` descarta la caché.
 * @returns {Promise<object>} un objeto con una lista por tabla
 */
export function cargarTodo({ recargar = false } = {}) {
  return new Promise((resolve) => {
    if (recargar) {
      revisionCache += 1;
      cache = null;
      carga = null;
    }
    if (cache) {
      resolve(aplicarPendientes(cache, listarPendientes()));
      return;
    }
    if (!carga) {
      const revision = revisionCache;
      carga = Promise.all(Object.keys(FUENTES).map(tabla)).then((entradas) => {
        const datos = Object.fromEntries(entradas);
        if (revision === revisionCache) cache = datos;
        return datos;
      });
    }
    const solicitud = carga;
    const revision = revisionCache;
    resolve(
      solicitud
        // Si la caché se descartó mientras se descargaba, se vuelve a pedir.
        .then((datos) => (revision !== revisionCache ? cargarTodo() : aplicarPendientes(datos, listarPendientes())))
        .finally(() => {
          if (carga === solicitud) carga = null;
        })
    );
  });
}

/**
 * Vuelve a pedir solo las tablas indicadas y las combina con la caché.
 * @param {string[]} claves nombres de FUENTES, por ejemplo ["consultasProcedimientoPaso"]
 * @returns {Promise<object>} se rechaza si alguna clave no existe
 */
export function recargarTablas(claves) {
  return new Promise((resolve) => {
    const desconocidas = claves.filter((clave) => !Object.hasOwn(FUENTES, clave));
    if (desconocidas.length) throw new Error(`No se pueden recargar estos recursos: ${desconocidas.join(", ")}.`);
    if (claves.length === 0) {
      resolve(cargarTodo());
      return;
    }
    const recargar = () => {
      const revision = revisionCache;
      const base = cache;
      return Promise.all(claves.map(tabla)).then((entradas) => {
        if (revision !== revisionCache) return cargarTodo();
        cache = { ...(cache ?? base), ...Object.fromEntries(entradas) };
        return aplicarPendientes(cache, listarPendientes());
      });
    };
    resolve(cache ? recargar() : cargarTodo().then(recargar));
  });
}

export function limpiarCache() {
  revisionCache += 1;
  cache = null;
  carga = null;
}
