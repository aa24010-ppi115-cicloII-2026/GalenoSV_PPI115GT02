/**
 * Punto de entrada de los controles de interfaz que no dependen del modelo clínico de GalenoSV.
 * Importarlo registra los elementos y exporta sus clases para poder usar nombres propios.
 */
export { Vista, definirElemento, configurarEstilos, modalConFoco } from "../base.js";
export { default as UiAutocompletar } from "./ui-autocompletar.js";
export { default as UiConfirmar, confirmar } from "./ui-confirmar.js";
export { default as UiToast } from "./ui-toast.js";
export { default as PwaInstalar } from "./pwa-instalar.js";
export { default as LineaPasos } from "./linea-pasos.js";
