const CLAVE = "galenosv.sesion";
const escuchas = new Set();

function emitir() {
  const detalle = leerSesion();
  for (const escucha of [...escuchas]) escucha({ detail: detalle });
}

export function leerSesion() {
  try {
    const guardada = sessionStorage.getItem(CLAVE);
    return guardada ? JSON.parse(guardada) : null;
  } catch {
    return null;
  }
}

export function guardarSesion(sesion) {
  sessionStorage.setItem(CLAVE, JSON.stringify(sesion));
  emitir();
}

export function actualizarSesion(cambios) {
  const actual = leerSesion();
  if (!actual) return;
  guardarSesion({ ...actual, ...cambios });
}

export function cerrarSesion() {
  sessionStorage.removeItem(CLAVE);
  emitir();
}

export function alCambiarSesion(escucha) {
  escuchas.add(escucha);
  return () => escuchas.delete(escucha);
}
