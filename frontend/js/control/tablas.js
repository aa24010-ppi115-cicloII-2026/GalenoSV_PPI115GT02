/** Búsquedas por identificador sobre las tablas descargadas de la API. */

import { CAMPO_ID } from "../entity/recursos.js";

/** Busca un registro en la lista recibida del backend usando su identificador. */
export function buscarPorId(datos, clave, id) {
  if (id === null || id === undefined) return null;
  const campo = CAMPO_ID[clave];
  return (datos[clave] ?? []).find((item) => String(item[campo]) === String(id)) ?? null;
}

export function mismoId(a, b) {
  return a !== null && a !== undefined && b !== null && b !== undefined && String(a) === String(b);
}
