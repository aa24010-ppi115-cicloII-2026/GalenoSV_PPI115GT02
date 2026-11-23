/**
 * Tema claro u oscuro que elige la persona con el botón del encabezado. No sigue el tema del
 * sistema: la aplicación empieza en claro y la elección se recuerda en el dispositivo.
 *
 * El tema se aplica con el atributo data-tema de <html>; principal.css define los colores del
 * tema oscuro para ese atributo. index.html lo aplica antes de pintar para que no destelle.
 */
export const CLAVE_TEMA = "galenosv.tema";
export const TEMA_POR_DEFECTO = "claro";

/** Respaldo por si el navegador bloquea el almacenamiento: el tema se cambia igual. */
let enMemoria = TEMA_POR_DEFECTO;

const valido = (valor) => (valor === "claro" || valor === "oscuro" ? valor : null);

/** Tema elegido, o el claro si nunca se eligió uno. */
export function temaActual() {
  try {
    enMemoria = valido(localStorage.getItem(CLAVE_TEMA)) ?? TEMA_POR_DEFECTO;
  } catch {
    // Sin almacenamiento se usa la última elección de esta página.
  }
  return enMemoria;
}

export function aplicarTema(tema) {
  document.documentElement.dataset.tema = tema;
}

export function elegirTema(tema) {
  enMemoria = valido(tema) ?? TEMA_POR_DEFECTO;
  try {
    localStorage.setItem(CLAVE_TEMA, enMemoria);
  } catch {
    // Sin almacenamiento la elección dura hasta recargar, pero se aplica.
  }
  aplicarTema(enMemoria);
  return enMemoria;
}

export function alternarTema() {
  return elegirTema(temaActual() === "oscuro" ? "claro" : "oscuro");
}

/** Avisa cuando se elige otro tema en otra pestaña. Devuelve la función que deja de escuchar. */
export function alCambiarTema(avisar) {
  const alCambiarAlmacen = (evento) => {
    if (evento.key !== CLAVE_TEMA && evento.key !== null) return;
    const tema = temaActual();
    aplicarTema(tema);
    avisar(tema);
  };
  window.addEventListener("storage", alCambiarAlmacen);
  return () => window.removeEventListener("storage", alCambiarAlmacen);
}
