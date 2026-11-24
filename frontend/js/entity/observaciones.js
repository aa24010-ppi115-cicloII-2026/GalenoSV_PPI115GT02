/**
 * Formato del campo `observaciones` de consulta-procedimiento: un JSON con el borrador del paso
 * en curso (notas, diagnóstico, plan) y lo registrado en cada paso completado.
 */

export function leerObservaciones(valor) {
  try {
    const datos = JSON.parse(valor);
    if (datos && typeof datos === "object" && !Array.isArray(datos)) {
      return {
        notas: datos.notas ?? "",
        diagnostico: datos.diagnostico ?? "",
        plan: datos.plan ?? "",
        pasos: datos.pasos && typeof datos.pasos === "object" ? datos.pasos : {},
      };
    }
  } catch {
    // Texto libre guardado por versiones anteriores del sistema.
  }
  return { notas: valor ?? "", diagnostico: "", plan: "", pasos: {} };
}

/**
 * Convierte las observaciones al JSON que se guarda en la API.
 * @param {{notas?: string, diagnostico?: string, plan?: string, pasos?: object}} observaciones
 * @returns {string}
 */
export function serializarObservaciones({ notas = "", diagnostico = "", plan = "", pasos = {} }) {
  return JSON.stringify({ notas, diagnostico, plan, pasos });
}
