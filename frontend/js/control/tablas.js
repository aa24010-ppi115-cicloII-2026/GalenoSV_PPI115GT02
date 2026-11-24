/** Búsquedas por identificador sobre las tablas descargadas de la API. */

import { CAMPO_ID } from "../entity/recursos.js";

/**
 * Índices de cada tabla: lista → campo → valor (como texto) → posiciones de sus filas.
 * Se arman la primera vez que se busca por ese campo y se reutilizan mientras exista la lista.
 * Las tablas descargadas no se modifican en su lugar (aplicarPendientes trabaja sobre copias),
 * así que un índice no queda desactualizado; con WeakMap se libera junto con su lista.
 */
const indices = new WeakMap();

function indice(lista, campo) {
  let porCampo = indices.get(lista);
  if (!porCampo) {
    porCampo = new Map();
    indices.set(lista, porCampo);
  }
  let grupos = porCampo.get(campo);
  if (!grupos) {
    grupos = new Map();
    lista.forEach((fila, posicion) => {
      const valor = fila?.[campo];
      if (valor === null || valor === undefined) return;
      const clave = String(valor);
      const grupo = grupos.get(clave);
      if (grupo) grupo.push(posicion);
      else grupos.set(clave, [posicion]);
    });
    porCampo.set(campo, grupos);
  }
  return grupos;
}

/**
 * Filas cuyo campo coincide con alguno de los valores, en el orden de la tabla. Equivale a
 * `lista.filter((fila) => mismoId(fila[campo], valor))`, pero recorre la tabla una sola vez
 * aunque se consulte muchas veces.
 * @param {object[]|undefined} lista tabla descargada de la API
 * @param {string} campo por ejemplo "idConsulta"
 * @param {*|Array} valores un valor o una lista de valores; null y undefined no coinciden con nada
 * @returns {object[]} una lista nueva
 */
export function filasDonde(lista, campo, valores) {
  if (!lista) return [];
  const grupos = indice(lista, campo);
  const buscados = new Set(
    (Array.isArray(valores) ? valores : [valores]).filter((valor) => valor !== null && valor !== undefined).map(String)
  );
  const posiciones = [...buscados].flatMap((clave) => grupos.get(clave) ?? []);
  if (buscados.size > 1) posiciones.sort((a, b) => a - b);
  return posiciones.map((posicion) => lista[posicion]);
}

/**
 * Busca un registro en la lista recibida del backend usando su identificador.
 * @param {object} datos tablas de la API
 * @param {string} clave tabla, por ejemplo "personas"
 * @param {*} id
 * @returns {object|null} el primero con ese id, o null
 */
export function buscarPorId(datos, clave, id) {
  if (id === null || id === undefined) return null;
  const lista = datos[clave];
  if (!lista) return null;
  const posicion = indice(lista, CAMPO_ID[clave]).get(String(id))?.[0];
  return posicion === undefined ? null : lista[posicion];
}

/**
 * Compara dos identificadores como texto; null o undefined nunca coinciden.
 * @returns {boolean}
 */
export function mismoId(a, b) {
  return a !== null && a !== undefined && b !== null && b !== undefined && String(a) === String(b);
}
