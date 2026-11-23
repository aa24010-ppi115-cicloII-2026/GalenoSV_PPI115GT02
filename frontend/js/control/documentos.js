/** Tipos de documento de identidad: catálogo activo, formato esperado y documento de cada persona. */

import { normalizarTexto } from "../comun/texto.js";
import { buscarPorId, mismoId } from "./tablas.js";

export function documentoDe(datos, idTipoDocumento, valor) {
  return datos.documentos.find(
    (documento) =>
      mismoId(documento.idTipoDocumento, idTipoDocumento) &&
      normalizarTexto(documento.valor) === normalizarTexto(valor)
  );
}

export function tiposDocumentoActivos(datos) {
  return [...(datos.tiposDocumento ?? [])]
    .filter((tipo) => tipo.activo !== false)
    .sort((a, b) => String(a.nombre).localeCompare(String(b.nombre)));
}

export function tipoDocumentoElegido(datos, id) {
  return (datos?.tiposDocumento ?? []).find((tipo) => mismoId(tipo.idTipoDocumento, id)) ?? null;
}

export function indicacionesDocumento(tipo) {
  return {
    texto: tipo?.indicaciones || (tipo?.expresionRegular ? `Formato: ${tipo.expresionRegular}` : ""),
    ejemplo: tipo?.nombre === "DUI" ? "Ej. 00000000-0" : "",
  };
}

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

export function errorDeFormatoDocumento(tipo, valor) {
  return cumpleFormato(tipo, valor) ? "" : mensajeFormatoDocumento(tipo);
}

export function documentoDePersona(datos, idPersona) {
  const documentos = datos.documentos.filter((documento) => mismoId(documento.idPersona, idPersona));
  if (documentos.length === 0) return null;
  const documento = documentos[0];
  const tipo = buscarPorId(datos, "tiposDocumento", documento.idTipoDocumento);
  return {
    idDocumento: documento.idDocumento,
    valor: documento.valor,
    tipo: tipo?.nombre ?? "Documento",
  };
}
