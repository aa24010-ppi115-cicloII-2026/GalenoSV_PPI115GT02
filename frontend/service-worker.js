const CACHE = "galenosv-v69";
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
  "./js/api.js",
  "./js/base.js",
  "./js/configuracion-estilos.js",
  "./js/texto.js",
  "./js/fechas.js",
  "./js/presentacion.js",
  "./js/presentacion-consulta.js",
  "./js/estado.js",
  "./js/datos.js",
  "./js/cie10.js",
  "./js/pendientes.js",
  "./js/recursos.js",
  "./js/app.js",
  "./js/pwa.js",
  "./js/componentes/ui-toast.js",
  "./js/componentes/pwa-instalar.js",
  "./js/componentes/ticket-cita.js",
  "./js/componentes/cita-card.js",
  "./js/componentes/ui-autocompletar.js",
  "./js/componentes/ui-confirmar.js",
  "./js/componentes/reutilizables.js",
  "./js/componentes/linea-pasos.js",
  "./js/componentes/app-login.js",
  "./js/componentes/asistente-citas.js",
  "./js/componentes/app-busqueda.js",
  "./js/componentes/app-dashboard.js",
  "./js/componentes/app-consulta.js",
  "./js/componentes/app-shell.js",
  "./manifest.json",
  "./recursos/imagenes/icono-192.png",
  "./recursos/imagenes/icono-512.png",
  "./recursos/imagenes/icono-maskable-512.png",
];

self.addEventListener("install", (evento) => {
  evento.waitUntil(
    (async () => {
      const cache = await caches.open(CACHE);
      // Cada recurso se guarda por separado: si uno falla, el resto igual queda disponible.
      await Promise.all(
        RECURSOS.map(async (recurso) => {
          try {
            const respuesta = await fetch(new Request(recurso, { cache: "reload" }));
            if (respuesta.ok) await cache.put(recurso, respuesta);
          } catch (error) {
            console.warn("No se pudo precachear", recurso, error);
          }
        })
      );
      await self.skipWaiting();
    })()
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

async function guardarEnCache(peticion, respuesta) {
  if (!respuesta.ok) return;
  try {
    const cache = await caches.open(CACHE);
    await cache.put(peticion, respuesta.clone());
  } catch (error) {
    // Un fallo de almacenamiento local no debe descartar una respuesta válida de la API.
    console.warn("No se pudo guardar la respuesta en cache", peticion.url, error);
  }
}

async function cachePrimero(peticion) {
  const guardada = await caches.match(peticion);
  if (guardada) return guardada;
  const respuesta = await fetch(peticion);
  await guardarEnCache(peticion, respuesta);
  return respuesta;
}

/**
 * Las lecturas de la API se guardan en cache para poder consultar los datos sin conexion.
 * Los cambios (POST, PUT, DELETE) los encola la aplicacion y se reenvian al recuperar la red.
 * La misma estrategia atiende la navegacion: usa la copia de respaldo sin marcarla como
 * API caida; solo las respuestas de la API se marcan con X-Sin-Conexion.
 */
async function redPrimero(peticion, { alternativa = null, marcarSinConexion = false } = {}) {
  try {
    const respuesta = await fetch(peticion);
    await guardarEnCache(peticion, respuesta);
    return respuesta;
  } catch {
    const guardada =
      (await caches.match(peticion)) ?? (alternativa ? await caches.match(alternativa) : null);
    const cabeceras = new Headers(guardada?.headers ?? { "Content-Type": "application/json" });
    if (marcarSinConexion) cabeceras.set("X-Sin-Conexion", "true");
    if (guardada) {
      return new Response(await guardada.blob(), {
        status: guardada.status,
        headers: cabeceras,
      });
    }
    // Sin copia guardada no se inventa una lista vacía: la vista muestra que no pudo cargar.
    const mensaje = marcarSinConexion
      ? "El servidor de GalenoSV no responde y no hay datos guardados en este dispositivo."
      : "Sin conexión y sin copia local";
    return new Response(JSON.stringify({ mensaje }), { status: 503, headers: cabeceras });
  }
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
