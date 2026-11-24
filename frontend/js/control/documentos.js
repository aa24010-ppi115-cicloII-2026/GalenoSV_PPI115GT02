/** Tipos de documento de identidad: catálogo activo, formato esperado y documento de cada persona. */

import { normalizarTexto } from "../comun/texto.js";
import { buscarPorId, filasDonde, mismoId } from "./tablas.js";

/**
 * Documento con ese tipo y número, sin distinguir mayúsculas ni tildes.
 * @param {object} datos tablas de la API
 * @param {string} idTipoDocumento
 * @param {string} valor
 * @returns {object|undefined}
 */
export function documentoDe(datos, idTipoDocumento, valor) {
  return datos.documentos.find(
    (documento) =>
      mismoId(documento.idTipoDocumento, idTipoDocumento) &&
      normalizarTexto(documento.valor) === normalizarTexto(valor)
  );
}

/**
 * Tipos de documento activos, ordenados por nombre.
 * @param {object} datos tablas de la API
 * @returns {object[]}
 */
export function tiposDocumentoActivos(datos) {
  return [...(datos.tiposDocumento ?? [])]
    .filter((tipo) => tipo.activo !== false)
    .sort((a, b) => String(a.nombre).localeCompare(String(b.nombre)));
}

/**
 * Tipo de documento por su id.
 * @param {object} datos tablas de la API
 * @param {string} id
 * @returns {object|null}
 */
export function tipoDocumentoElegido(datos, id) {
  return (datos?.tiposDocumento ?? []).find((tipo) => mismoId(tipo.idTipoDocumento, id)) ?? null;
}

/**
 * Ayuda para escribir el número: las indicaciones del catálogo o su formato, y un ejemplo para el DUI.
 * @param {object|null} tipo
 * @returns {{texto: string, ejemplo: string}}
 */
export function indicacionesDocumento(tipo) {
  return {
    texto: tipo?.indicaciones || (tipo?.expresionRegular ? `Formato: ${tipo.expresionRegular}` : ""),
    ejemplo: tipo?.nombre === "DUI" ? "Ej. 00000000-0" : "",
  };
}

/**
 * Mensaje de error cuando el número no tiene el formato del tipo de documento.
 * @param {object|null} tipo
 * @returns {string}
 */
export function mensajeFormatoDocumento(tipo) {
  const detalle = tipo?.indicaciones || tipo?.expresionRegular || "el formato del documento";
  return `El número${tipo?.nombre ? ` de ${tipo.nombre}` : ""} no tiene el formato esperado: ${detalle}.`;
}

/**
 * Valida el número contra la expresión regular del tipo de documento.
 * Un tipo sin expresión, o con una mal escrita en el catálogo, no bloquea al usuario.
 */
export function cumpleFormato(tipo, valor) {
  if (!tipo?.expresionRegular) return true;
  try {
    return new RegExp(tipo.expresionRegular).test(valor);
  } catch {
    return true;
  }
}

/**
 * Error de formato del número, o cadena vacía si es válido.
 * @param {object|null} tipo
 * @param {string} valor
 * @returns {string}
 */
export function errorDeFormatoDocumento(tipo, valor) {
  return cumpleFormato(tipo, valor) ? "" : mensajeFormatoDocumento(tipo);
}

/**
 * Primer documento de una persona, con el nombre de su tipo.
 * @param {object} datos tablas de la API
 * @param {string} idPersona
 * @returns {{idDocumento: string, valor: string, tipo: string}|null}
 */
export function documentoDePersona(datos, idPersona) {
  const documentos = filasDonde(datos.documentos, "idPersona", idPersona);
  if (documentos.length === 0) return null;
  const documento = documentos[0];
  const tipo = buscarPorId(datos, "tiposDocumento", documento.idTipoDocumento);
  return {
    idDocumento: documento.idDocumento,
    valor: documento.valor,
    tipo: tipo?.nombre ?? "Documento",
  };
}
