/** Personas y sus asignaciones: quién es paciente o empleado, en qué clínicas y su edad. */

import { nombreCompleto, normalizarTexto } from "../comun/texto.js";
import { buscarPorId, mismoId } from "./tablas.js";
import { documentoDe } from "./documentos.js";

export function personaDeDocumento(datos, idTipoDocumento, valor) {
  const documento = documentoDe(datos, idTipoDocumento, valor);
  return documento ? buscarPorId(datos, "personas", documento.idPersona) : null;
}

export function rolesDePersona(datos, idPersona) {
  return datos.personasRol
    .filter((personaRol) => mismoId(personaRol.idPersona, idPersona))
    .map((personaRol) => ({
      personaRol,
      rol: buscarPorId(datos, "roles", personaRol.idRol),
      clinica: buscarPorId(datos, "clinicas", personaRol.idClinica),
    }));
}

export function clinicasDePaciente(datos, idPersona) {
  const asignaciones = rolesDePersona(datos, idPersona);
  const pacientes = asignaciones.filter(
    ({ rol }) => normalizarTexto(rol?.nombre) === "paciente" && rol?.activo !== false
  );
  const unicas = [];
  for (const { clinica } of pacientes) {
    if (clinica && !unicas.some((item) => String(item.idClinica) === String(clinica.idClinica))) unicas.push(clinica);
  }
  return unicas;
}

export function empleadoDePersona(datos, idPersona) {
  return rolesDePersona(datos, idPersona).filter(
    ({ rol }) => rol && normalizarTexto(rol.nombre) !== "paciente" && rol.activo !== false
  );
}

export function edadDe(persona) {
  if (!persona?.fechaNacimiento) return null;
  const nacimiento = new Date(persona.fechaNacimiento);
  if (Number.isNaN(nacimiento.getTime())) return null;
  // La fecha de nacimiento llega a medianoche UTC: se compara con el día local de hoy en UTC.
  const ahora = new Date();
  const hoy = new Date(Date.UTC(ahora.getFullYear(), ahora.getMonth(), ahora.getDate()));
  let edad = hoy.getUTCFullYear() - nacimiento.getUTCFullYear();
  const mes = hoy.getUTCMonth() - nacimiento.getUTCMonth();
  if (mes < 0 || (mes === 0 && hoy.getUTCDate() < nacimiento.getUTCDate())) edad -= 1;
  return edad >= 0 ? edad : null;
}

export function pacientesDeClinica(datos, idClinica) {
  const vistos = [];
  for (const personaRol of datos.personasRol) {
    if (String(personaRol.idClinica) !== String(idClinica)) continue;
    const rol = buscarPorId(datos, "roles", personaRol.idRol);
    if (normalizarTexto(rol?.nombre) !== "paciente" || rol?.activo === false) continue;
    const persona = buscarPorId(datos, "personas", personaRol.idPersona);
    if (persona && !vistos.some((item) => mismoId(item.idPersona, persona.idPersona))) vistos.push(persona);
  }
  return vistos.sort((a, b) => nombreCompleto(a).localeCompare(nombreCompleto(b)));
}
