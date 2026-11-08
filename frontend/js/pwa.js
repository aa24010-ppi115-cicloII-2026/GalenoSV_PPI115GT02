const botonInstalar = document.querySelector("#instalar");
let eventoInstalacion = null;

window.addEventListener("beforeinstallprompt", (evento) => {
  evento.preventDefault();
  eventoInstalacion = evento;
  botonInstalar.hidden = false;
});

botonInstalar.addEventListener("click", async () => {
  if (!eventoInstalacion) return;
  eventoInstalacion.prompt();
  await eventoInstalacion.userChoice;
  eventoInstalacion = null;
  botonInstalar.hidden = true;
});

window.addEventListener("appinstalled", () => {
  eventoInstalacion = null;
  botonInstalar.hidden = true;
});

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("./service-worker.js").catch((error) => {
      console.error("No se pudo registrar el service worker", error);
    });
  });
}
