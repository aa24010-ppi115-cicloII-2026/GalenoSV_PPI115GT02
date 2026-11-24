const CACHE = "galenosv-v77";
const API = "http://localhost:9080";
const RECURSOS = [
  "./",
  "./index.html",
  "./css/principal.css",
  "./css/ticket-documento.css",
  "./css/componentes/base.css",
  "./css/componentes/app-busqueda.css",
  "./css/componentes/app-consulta.css",
  "./css/componentes/app-dashboard.css",
  "./css/componentes/app-login.css",
  "./css/componentes/app-shell.css",
  "./css/componentes/asistente-citas.css",
  "./css/componentes/cita-card.css",
  "./css/componentes/linea-pasos.css",
  "./css/componentes/pwa-instalar.css",
  "./css/componentes/ticket-cita.css",
  "./css/componentes/ui-autocompletar.css",
  "./css/componentes/ui-confirmar.css",
  "./css/componentes/ui-toast.css",
  "./js/app.js",
  "./js/configuracion-estilos.js",
  "./js/pwa.js",
  "./js/comun/fechas.js",
  "./js/comun/texto.js",
  "./js/entity/cie10.js",
  "./js/entity/observaciones.js",
  "./js/entity/recursos.js",
  "./js/entity/default-error.js",
  "./js/entity/default-response.js",
  "./js/control/api.js",
  "./js/control/default-service.js",
  "./js/control/servicios.js",
  "./js/control/citas.js",
  "./js/control/documentos.js",
  "./js/control/pendientes.js",
  "./js/control/personas.js",
  "./js/control/repositorio.js",
  "./js/control/sesion.js",
  "./js/control/tema.js",
  "./js/control/tablas.js",
  "./js/boundary/base.js",
  "./js/boundary/presentacion-consulta.js",
  "./js/boundary/presentacion.js",
  "./js/boundary/componentes/app-busqueda.js",
  "./js/boundary/componentes/app-consulta.js",
  "./js/boundary/componentes/app-dashboard.js",
  "./js/boundary/componentes/app-login.js",
  "./js/boundary/componentes/app-shell.js",
  "./js/boundary/componentes/asistente-citas.js",
  "./js/boundary/componentes/cita-card.js",
  "./js/boundary/componentes/linea-pasos.js",
  "./js/boundary/componentes/pwa-instalar.js",
  "./js/boundary/componentes/reutilizables.js",
  "./js/boundary/componentes/ticket-cita.js",
  "./js/boundary/componentes/ui-autocompletar.js",
  "./js/boundary/componentes/ui-confirmar.js",
  "./js/boundary/componentes/ui-toast.js",
  "./manifest.json",
  "./recursos/imagenes/icono-192.png",
  "./recursos/imagenes/icono-512.png",
  "./recursos/imagenes/icono-maskable-512.png",
];

self.addEventListener("install", (evento) => {
  evento.waitUntil(
    caches
      .open(CACHE)
      .then((cache) =>
        // Cada recurso se guarda por separado: si uno falla, el resto igual queda disponible.
        Promise.all(
          RECURSOS.map((recurso) =>
            fetch(new Request(recurso, { cache: "reload" }))
              .then((respuesta) => (respuesta.ok ? cache.put(recurso, respuesta) : undefined))
              .catch((error) => console.warn("No se pudo precachear", recurso, error))
          )
        )
      )
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (evento) => {
  evento.waitUntil(
    caches
      .keys()
      .then((claves) => Promise.all(claves.filter((clave) => clave !== CACHE).map((clave) => caches.delete(clave))))
      .then(() => self.clients.claim())
  );
});

function guardarEnCache(peticion, respuesta) {
  if (!respuesta.ok) return Promise.resolve();
  return caches
    .open(CACHE)
    .then((cache) => cache.put(peticion, respuesta.clone()))
    .catch((error) => {
      // Un fallo de almacenamiento local no debe descartar una respuesta válida de la API.
      console.warn("No se pudo guardar la respuesta en cache", peticion.url, error);
    });
}

/** Pide a la red y guarda la respuesta; se cumple con la respuesta original. */
function pedirYGuardar(peticion) {
  return fetch(peticion).then((respuesta) => guardarEnCache(peticion, respuesta).then(() => respuesta));
}

function cachePrimero(peticion) {
  return caches.match(peticion).then((guardada) => guardada ?? pedirYGuardar(peticion));
}

/**
 * Las lecturas de la API se guardan en caché para poder consultar los datos sin conexión.
 * Los cambios (POST, PUT, DELETE) los encola la aplicación y se reenvían al recuperar la red.
 * La misma estrategia atiende la navegacion: usa la copia de respaldo sin marcarla como
 * API caida; solo las respuestas de la API se marcan con X-Sin-Conexion.
 */
function redPrimero(peticion, { alternativa = null, marcarSinConexion = false } = {}) {
  return pedirYGuardar(peticion).catch(() =>
    caches
      .match(peticion)
      .then((guardada) => guardada ?? (alternativa ? caches.match(alternativa) : null))
      .then((guardada) => {
        const cabeceras = new Headers(guardada?.headers ?? { "Content-Type": "application/json" });
        if (marcarSinConexion) cabeceras.set("X-Sin-Conexion", "true");
        if (guardada) {
          return guardada.blob().then(
            (cuerpo) => new Response(cuerpo, { status: guardada.status, headers: cabeceras })
          );
        }
        // Sin copia guardada no se inventa una lista vacía: la vista muestra que no pudo cargar.
        const mensaje = marcarSinConexion
          ? "El servidor de GalenoSV no responde y no hay datos guardados en este dispositivo."
          : "Sin conexión y sin copia local";
        return new Response(JSON.stringify({ mensaje }), { status: 503, headers: cabeceras });
      })
  );
}

self.addEventListener("fetch", (evento) => {
  const peticion = evento.request;
  if (peticion.method !== "GET") return;
  const url = new URL(peticion.url);
  if (url.origin === API) {
    evento.respondWith(redPrimero(peticion, { marcarSinConexion: true }));
    return;
  }
  if (url.origin !== self.location.origin) return;
  if (peticion.mode === "navigate") {
    evento.respondWith(redPrimero(peticion, { alternativa: "./index.html" }));
    return;
  }
  evento.respondWith(cachePrimero(peticion));
});
