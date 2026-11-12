const CLAVE = "galenosv.pendientes";

export function estaConectado() {
  if (typeof navigator === "undefined") return true;
  return navigator.onLine !== false;
}

function leer() {
  try {
    const crudo = localStorage.getItem(CLAVE);
    return crudo ? JSON.parse(crudo) : [];
  } catch {
    return [];
  }
}

function escribir(pendientes) {
  try {
    localStorage.setItem(CLAVE, JSON.stringify(pendientes));
  } catch {
    // Almacenamiento no disponible: el cambio se pierde, pero la app sigue operando.
  }
}

export function encolar(operacion) {
  const pendientes = leer();
  pendientes.push({ ...operacion, id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}` });
  escribir(pendientes);
  return pendientes.length;
}

export function listarPendientes() {
  return leer();
}

export function quitarPendiente(id) {
  const pendientes = leer().filter((pendiente) => pendiente.id !== id);
  escribir(pendientes);
  return pendientes.length;
}

export function vaciarPendientes() {
  escribir([]);
}

/**
 * Reintenta las operaciones guardadas mientras la aplicacion estuvo sin conexion.
 * Cada operacion se ejecuta una vez; si falla se conserva para el siguiente intento.
 */
export async function sincronizar(ejecutar) {
  if (!estaConectado()) return { enviadas: 0, pendientes: leer().length };
  const pendientes = leer();
  let enviadas = 0;
  for (const pendiente of [...pendientes]) {
    try {
      await ejecutar(pendiente);
      quitarPendiente(pendiente.id);
      enviadas += 1;
    } catch {
      break;
    }
  }
  return { enviadas, pendientes: leer().length };
}
