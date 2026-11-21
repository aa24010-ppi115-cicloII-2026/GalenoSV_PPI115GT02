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
export async function sincronizar(ejecutar) {
  if (!estaConectado()) return { enviadas: 0, pendientes: leer().length };
  if (sincronizacion) return sincronizacion;
  sincronizacion = vaciarCola(ejecutar).finally(() => {
    sincronizacion = null;
  });
  return sincronizacion;
}

async function vaciarCola(ejecutar) {
  let enviadas = 0;
  for (const pendiente of leer()) {
    try {
      await ejecutar(pendiente);
      if (!quitarPendiente(pendiente.id)) break;
      enviadas += 1;
    } catch {
      break;
    }
  }
  return { enviadas, pendientes: leer().length };
}
