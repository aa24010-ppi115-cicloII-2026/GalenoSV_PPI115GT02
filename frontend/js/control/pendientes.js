const CLAVE = "galenosv.pendientes";

/**
 * El navegador dice que hay red. No garantiza que el servidor responda (ver comprobarServicio).
 * @returns {boolean}
 */
export function estaConectado() {
  if (typeof navigator === "undefined") return true;
  return navigator.onLine !== false;
}

function leer() {
  try {
    const crudo = localStorage.getItem(CLAVE);
    const pendientes = crudo ? JSON.parse(crudo) : [];
    return Array.isArray(pendientes) ? pendientes : [];
  } catch {
    return [];
  }
}

function escribir(pendientes) {
  try {
    localStorage.setItem(CLAVE, JSON.stringify(pendientes));
  } catch {
    return false;
  }
  // Avisa a la interfaz (aviso de conexión) que cambió la cantidad de pendientes.
  if (typeof window !== "undefined") window.dispatchEvent(new Event("pendientes-cambio"));
  return true;
}

/**
 * Guarda un cambio en la cola del dispositivo para enviarlo cuando vuelva la conexión.
 * @param {{metodo: string, ruta: string, cuerpo: object|null}} operacion
 * @returns {number} cantidad de cambios en la cola
 * @throws {Error} si el navegador no permite guardar
 */
export function encolar(operacion) {
  const pendientes = leer();
  pendientes.push({ ...operacion, id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}` });
  if (!escribir(pendientes)) throw new Error("No se pudo guardar el cambio en este dispositivo.");
  return pendientes.length;
}

/**
 * Cambios guardados en la cola, en el orden en que se hicieron.
 * @returns {object[]}
 */
export function listarPendientes() {
  return leer();
}

/**
 * Quita de la cola un cambio ya enviado.
 * @param {string} id id del cambio en la cola
 * @returns {boolean} false si no se pudo guardar la cola actualizada
 */
export function quitarPendiente(id) {
  const actuales = leer();
  const pendientes = actuales.filter((pendiente) => pendiente.id !== id);
  if (pendientes.length === actuales.length) return true;
  return escribir(pendientes);
}

let sincronizacion = null;

/**
 * Reintenta las operaciones guardadas mientras la aplicación estuvo sin conexión.
 * Cada operación se ejecuta una vez; si falla se conserva para el siguiente intento.
 */
export function sincronizar(ejecutar) {
  if (!estaConectado()) return Promise.resolve({ enviadas: 0, pendientes: leer().length });
  if (sincronizacion) return sincronizacion;
  sincronizacion = vaciarCola(ejecutar).finally(() => {
    sincronizacion = null;
  });
  return sincronizacion;
}

/** Envía la cola en orden, una operación a la vez; se detiene en la primera que falle. */
function vaciarCola(ejecutar) {
  const resultado = (enviadas) => ({ enviadas, pendientes: leer().length });
  const enviar = ([pendiente, ...resto], enviadas) => {
    if (!pendiente) return Promise.resolve(resultado(enviadas));
    return new Promise((resolve) => resolve(ejecutar(pendiente)))
      .then(() => (quitarPendiente(pendiente.id) ? enviar(resto, enviadas + 1) : resultado(enviadas)))
      .catch(() => resultado(enviadas));
  };
  return enviar(leer(), 0);
}
