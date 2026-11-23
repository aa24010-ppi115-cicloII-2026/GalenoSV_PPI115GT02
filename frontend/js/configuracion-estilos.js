import { configurarEstilos } from "./boundary/base.js";

/** Componentes con hoja propia en css/componentes/ (el archivo lleva el nombre del elemento). */
export const COMPONENTES_CON_ESTILO = [
  "app-shell",
  "app-busqueda",
  "asistente-citas",
  "app-login",
  "app-dashboard",
  "app-consulta",
  "cita-card",
  "linea-pasos",
  "ticket-cita",
  "ui-autocompletar",
  "ui-confirmar",
  "ui-toast",
  "pwa-instalar",
];

configurarEstilos({
  base: new URL("../css/componentes/base.css", import.meta.url).href,
  directorio: new URL("../css/componentes/", import.meta.url).href,
});
