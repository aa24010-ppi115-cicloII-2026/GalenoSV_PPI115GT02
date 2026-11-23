function interpretar(valor) {
  if (valor === null || valor === undefined || valor === "") return null;
  const fecha = new Date(valor);
  return Number.isNaN(fecha.getTime()) ? null : fecha;
}

export function fechaHora(valor) {
  const fecha = interpretar(valor);
  if (!fecha) return "—";
  return fecha.toLocaleString("es-SV", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

export function fechaCorta(valor) {
  const fecha = interpretar(valor);
  if (!fecha) return "—";
  return fecha.toLocaleDateString("es-SV", { day: "numeric", month: "short", year: "numeric" });
}

/** Las fechas de nacimiento se guardan a medianoche UTC en la API. */
export function fechaCalendario(valor) {
  const fecha = interpretar(valor);
  if (!fecha) return "—";
  return fecha.toLocaleDateString("es-SV", {
    day: "numeric",
    month: "short",
    year: "numeric",
    timeZone: "UTC",
  });
}

/** Fecha local en formato YYYY-MM-DD, sin depender de la zona UTC. */
export function fechaISO(fecha = new Date()) {
  const mes = String(fecha.getMonth() + 1).padStart(2, "0");
  const dia = String(fecha.getDate()).padStart(2, "0");
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function hoyISO() {
  return fechaISO();
}

export function ahoraISO() {
  const fecha = new Date();
  const zona = -fecha.getTimezoneOffset();
  const signo = zona >= 0 ? "+" : "-";
  const horas = String(Math.floor(Math.abs(zona) / 60)).padStart(2, "0");
  const minutos = String(Math.abs(zona) % 60).padStart(2, "0");
  const local = new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().slice(0, 19);
  return `${local}${signo}${horas}:${minutos}`;
}
