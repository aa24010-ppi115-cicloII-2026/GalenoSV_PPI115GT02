const REGISTRO = "./service-worker.js";

function registrar() {
  if (!("serviceWorker" in navigator)) return Promise.resolve(null);
  return navigator.serviceWorker.register(REGISTRO, { scope: "./" }).catch((error) => {
    console.error("No se pudo registrar el service worker", error);
    return null;
  });
}

/**
 * Cuando se publica una versión nueva, su service worker toma el control de la página abierta,
 * pero esta sigue ejecutando los módulos anteriores. Se recarga una sola vez para usar los nuevos.
 * En la primera instalación no hay versión anterior y no se recarga.
 */
export function recargarAlActualizar(serviceWorker, recargar) {
  if (!serviceWorker.controller) return;
  let recargada = false;
  serviceWorker.addEventListener("controllerchange", () => {
    if (recargada) return;
    recargada = true;
    recargar();
  });
}

if ("serviceWorker" in navigator) {
  recargarAlActualizar(navigator.serviceWorker, () => window.location.reload());
  registrar();
}
