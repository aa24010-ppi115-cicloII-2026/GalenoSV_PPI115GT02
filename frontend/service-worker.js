const CACHE = "galenosv-v4";
const API = "http://localhost:9080";
const RECURSOS = [
  "./",
  "./index.html",
  "./css/principal.css",
  "./js/api.js",
  "./js/base.js",
  "./js/estado.js",
  "./js/datos.js",
  "./js/cie10.js",
  "./js/pdf.js",
  "./js/pendientes.js",
  "./js/app.js",
  "./js/pwa.js",
  "./js/componentes/ui-toast.js",
  "./js/componentes/pwa-instalar.js",
  "./js/componentes/ticket-cita.js",
  "./js/componentes/cita-card.js",
  "./js/componentes/app-login.js",
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

async function cachePrimero(peticion) {
  const guardada = await caches.match(peticion);
  if (guardada) return guardada;
  const respuesta = await fetch(peticion);
  if (respuesta.ok) {
    const cache = await caches.open(CACHE);
    cache.put(peticion, respuesta.clone());
  }
  return respuesta;
}

async function redPrimero(peticion, respaldo = null) {
  try {
    const respuesta = await fetch(peticion);
    if (respuesta.ok) {
      const cache = await caches.open(CACHE);
      cache.put(peticion, respuesta.clone());
    }
    return respuesta;
  } catch {
    const guardada = await caches.match(peticion);
    if (guardada) return guardada;
    if (respaldo) {
      const alterna = await caches.match(respaldo);
      if (alterna) return alterna;
    }
    return new Response(JSON.stringify({ mensaje: "Sin conexión y sin copia local" }), {
      status: 503,
      headers: { "Content-Type": "application/json" },
    });
  }
}

/**
 * Las lecturas de la API se guardan en cache para poder consultar los datos
 * sin conexion. Los cambios (POST, PUT, DELETE) los encola la aplicacion en
 * el dispositivo y se reenvian al recuperar la red.
 */
async function datosPrimero(peticion) {
  try {
    const respuesta = await fetch(peticion);
    if (respuesta.ok) {
      const cache = await caches.open(CACHE);
      cache.put(peticion, respuesta.clone());
    }
    return respuesta;
  } catch {
    const guardada = await caches.match(peticion);
    if (guardada) return guardada;
    return new Response(JSON.stringify([]), {
      status: 200,
      headers: { "Content-Type": "application/json", "X-Sin-Conexion": "true" },
    });
  }
}

self.addEventListener("fetch", (evento) => {
  const peticion = evento.request;
  if (peticion.method !== "GET") return;
  const url = new URL(peticion.url);
  if (url.origin === API) {
    evento.respondWith(datosPrimero(peticion));
    return;
  }
  if (url.origin !== self.location.origin) return;
  if (peticion.mode === "navigate") {
    evento.respondWith(redPrimero(peticion, "./index.html"));
    return;
  }
  evento.respondWith(cachePrimero(peticion));
});
