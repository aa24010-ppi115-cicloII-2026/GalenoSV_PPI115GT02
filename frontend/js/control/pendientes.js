const CLAVE = "galenosv.pendientes";

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

export function encolar(operacion) {
  const pendientes = leer();
  pendientes.push({ ...operacion, id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}` });
  if (!escribir(pendientes)) throw new Error("No se pudo guardar el cambio en este dispositivo.");
  return pendientes.length;
}

export function listarPendientes() {
  return leer();
}

export function quitarPendiente(id) {
  const actuales = leer();
  const pendientes = actuales.filter((pendiente) => pendiente.id !== id);
  if (pendientes.length === actuales.length) return true;
  return escribir(pendientes);
}

let sincronizacion = null;

/**
 * Reintenta las operaciones guardadas mientras la aplicacion estuvo sin conexion.
 * Cada operacion se ejecuta una vez; si falla se conserva para el siguiente intento.
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
