const CLAVE = "galenosv.sesion";
const escuchas = new Set();

function emitir() {
  const detalle = leerSesion();
  for (const escucha of [...escuchas]) escucha(detalle);
}

/**
 * Sesión del empleado en esta pestaña.
 * @returns {object|null}
 */
export function leerSesion() {
  try {
    const guardada = sessionStorage.getItem(CLAVE);
    return guardada ? JSON.parse(guardada) : null;
  } catch {
    return null;
  }
}

/**
 * Guarda la sesión y avisa a quien escuche los cambios.
 * @param {object} sesion
 */
export function guardarSesion(sesion) {
  sessionStorage.setItem(CLAVE, JSON.stringify(sesion));
  emitir();
}

/**
 * Cambia algunos campos de la sesión, si hay una abierta.
 * @param {object} cambios
 */
export function actualizarSesion(cambios) {
  const actual = leerSesion();
  if (!actual) return;
  guardarSesion({ ...actual, ...cambios });
}

/** Cierra la sesión y avisa a quien escuche los cambios. */
export function cerrarSesion() {
  sessionStorage.removeItem(CLAVE);
  emitir();
}

/**
 * Escucha los cambios de sesión.
 * @param {(sesion: object|null) => void} escucha
 * @returns {() => boolean} deja de escuchar
 */
export function alCambiarSesion(escucha) {
  escuchas.add(escucha);
  return () => escuchas.delete(escucha);
}
