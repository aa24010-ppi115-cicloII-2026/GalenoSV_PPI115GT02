const REGISTRO = "./service-worker.js";

async function registrar() {
  if (!("serviceWorker" in navigator)) return null;
  try {
    const registro = await navigator.serviceWorker.register(REGISTRO, { scope: "./" });
    return registro;
  } catch (error) {
    console.error("No se pudo registrar el service worker", error);
    return null;
  }
}

if ("serviceWorker" in navigator) {
  registrar();
  if (document.readyState === "complete") registrar();
  else window.addEventListener("load", registrar, { once: true });
}
