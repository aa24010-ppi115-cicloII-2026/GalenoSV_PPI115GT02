const CACHE = "galenosv-v1";
const API = "http://localhost:9080";
const RECURSOS = [
  "./",
  "./index.html",
  "./css/principal.css",
  "./js/api.js",
  "./js/recursos.js",
  "./js/app.js",
  "./js/pwa.js",
  "./manifest.json",
  "./recursos/imagenes/icono-192.png",
  "./recursos/imagenes/icono-512.png",
  "./recursos/imagenes/icono-maskable-512.png",
];

self.addEventListener("install", (evento) => {
  evento.waitUntil(
    caches.open(CACHE).then((cache) => cache.addAll(RECURSOS)).then(() => self.skipWaiting())
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

self.addEventListener("fetch", (evento) => {
  const peticion = evento.request;
  if (peticion.method !== "GET") return;
  const url = new URL(peticion.url);
  if (url.origin === API) {
    evento.respondWith(redPrimero(peticion));
    return;
  }
  if (url.origin !== self.location.origin) return;
  if (peticion.mode === "navigate") {
    evento.respondWith(redPrimero(peticion, "./index.html"));
    return;
  }
  evento.respondWith(cachePrimero(peticion));
});
