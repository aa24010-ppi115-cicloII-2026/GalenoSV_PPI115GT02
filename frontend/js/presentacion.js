import { fechaCorta, fechaHora } from "./fechas.js";
import { etiquetaPaso, registroVisible } from "./datos.js";
import { nombreCompleto } from "./texto.js";

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
