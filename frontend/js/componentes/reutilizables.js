/**
 * Punto de entrada de los controles de interfaz que no dependen del modelo clínico de GalenoSV.
 * Importarlo registra los elementos y exporta sus clases para poder usar nombres propios.
 */
export { Vista, definirElemento, configurarEstilos, modalConFoco } from "../base.js";
export { UiAutocompletar } from "./ui-autocompletar.js";
export { UiConfirmar, confirmar } from "./ui-confirmar.js";
export { UiToast } from "./ui-toast.js";
export { PwaInstalar } from "./pwa-instalar.js";
export { LineaPasos } from "./linea-pasos.js";
