import { fechaCorta, fechaHora } from "../comun/fechas.js";
import { nombreCompleto } from "../comun/texto.js";

const ETIQUETAS_PASO = { completado: "Completado", actual: "En curso", pendiente: "Pendiente" };

/**
 * Registro que se muestra de un paso. Un paso pendiente puede conservar el registro de antes de
 * retroceder (para retomarlo al avanzar), pero no se muestra: no tiene fecha ni responsable vigentes.
 */
export function registroVisible({ estado, registro }) {
  return estado === "pendiente" ? null : registro ?? null;
}

/**
 * Texto del estado de un paso; el paso actual que aún no se inicia dice «Por iniciar».
 * @param {string} estado "completado", "actual" o "pendiente"
 * @param {boolean} [iniciado]
 * @returns {string}
 */
export function etiquetaPaso(estado, iniciado = true) {
  if (estado === "actual" && !iniciado) return "Por iniciar";
  return ETIQUETAS_PASO[estado] ?? String(estado ?? "");
}

/** Resumen en una línea de lo registrado en un paso: diagnóstico, notas y plan. */
export function notaDelPaso({ notas = "", diagnostico = "", plan = "" } = {}) {
  return [diagnostico && `CIE-10: ${diagnostico}`, notas && `Notas: ${notas}`, plan && `Plan: ${plan}`]
    .filter(Boolean)
    .join(" | ");
}

/**
 * Motivo de la consulta en una línea de hasta 120 caracteres.
 * @param {object} consulta
 * @returns {string}
 */
export function motivoDe(consulta) {
  const texto = String(consulta?.motivo || consulta?.observaciones || "").trim();
  if (!texto) return "Consulta registrada";
  return texto.length > 120 ? `${texto.slice(0, 117)}…` : texto;
}

/** Adapta el modelo clínico de un paso al contrato de presentación de <linea-pasos>. */
export function pasosParaPresentacion(pasos) {
  return pasos.map((item) => {
    const registro = registroVisible(item);
    const persona = item.persona ? nombreCompleto(item.persona) : "Sin asignar";
    const detalles = registro
      ? [
          { etiqueta: "Realizó", valor: persona },
          item.rol?.nombre && { etiqueta: "Rol", valor: item.rol.nombre },
          registro.fechaInicio && { etiqueta: "Inicio", valor: fechaHora(registro.fechaInicio) },
          registro.fechaFin && { etiqueta: "Fin", valor: fechaHora(registro.fechaFin) },
          { etiqueta: "Notas", valor: item.notas?.notas || "Sin notas en este paso." },
          item.notas?.diagnostico && { etiqueta: "CIE-10", valor: item.notas.diagnostico },
          item.notas?.plan && { etiqueta: "Plan", valor: item.notas.plan },
        ].filter(Boolean)
      : [];
    return {
      nombre: item.paso.nombre,
      estado: item.estado,
      etiqueta: etiquetaPaso(item.estado, Boolean(registro)),
      resumen: registro ? `${persona} · ${fechaCorta(registro.fechaInicio)}` : "",
      textoDetalles: "Ver registro",
      detalles,
    };
  });
}
