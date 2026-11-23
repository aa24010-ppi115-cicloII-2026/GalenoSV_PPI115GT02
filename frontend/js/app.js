import { COMPONENTES_CON_ESTILO } from "./configuracion-estilos.js";
import { precargarEstilos } from "./boundary/base.js";
import "./pwa.js";

/** Módulos que registran los Web Components, en orden: el shell al final, cuando ya existen sus vistas. */
const COMPONENTES = [
  "./boundary/componentes/reutilizables.js",
  "./boundary/componentes/ticket-cita.js",
  "./boundary/componentes/cita-card.js",
  "./boundary/componentes/asistente-citas.js",
  "./boundary/componentes/app-login.js",
  "./boundary/componentes/app-busqueda.js",
  "./boundary/componentes/app-dashboard.js",
  "./boundary/componentes/app-consulta.js",
  "./boundary/componentes/app-shell.js",
];

// Primero las hojas de estilo y después los componentes: así se pintan con estilo desde el inicio.
precargarEstilos(COMPONENTES_CON_ESTILO).then(() =>
  COMPONENTES.reduce((cadena, modulo) => cadena.then(() => import(modulo)), Promise.resolve())
);
