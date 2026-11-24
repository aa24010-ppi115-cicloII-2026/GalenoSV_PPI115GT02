/*
 * Tema claro u oscuro de las páginas JSF, como en el frontend (frontend/js/control/tema.js):
 * empieza en claro, la elección se recuerda en el dispositivo y se aplica con data-tema en
 * <html>. Se carga en <head> para aplicarla antes de pintar y que no destelle el claro.
 * El botón del encabezado lleva data-alternar-tema.
 */
(() => {
  const CLAVE = "galenosv.tema";
  const raiz = document.documentElement;

  /** Tema guardado; si el navegador bloquea el almacenamiento, el que tiene la página. */
  const leer = () => {
    try {
      return localStorage.getItem(CLAVE) === "oscuro" ? "oscuro" : "claro";
    } catch {
      return raiz.dataset.tema === "oscuro" ? "oscuro" : "claro";
    }
  };

  const aplicar = (tema) => {
    raiz.dataset.tema = tema;
    document.querySelectorAll("[data-alternar-tema]").forEach((boton) => {
      boton.setAttribute("aria-pressed", String(tema === "oscuro"));
    });
  };

  const sincronizar = () => aplicar(leer());

  // update="@all" del cambio de idioma reemplaza <head> y <body> por ajax y vuelve a ejecutar
  // este archivo: los escuchas ya existen, solo falta marcar el botón nuevo cuando llegue el
  // <body>, que se reemplaza justo después del <head>.
  if (window.galenosvTemaListo) {
    setTimeout(sincronizar);
    return;
  }
  window.galenosvTemaListo = true;

  sincronizar();
  // El botón aún no existe mientras se carga el <head>.
  document.addEventListener("DOMContentLoaded", sincronizar);

  document.addEventListener("click", (evento) => {
    if (!evento.target.closest("[data-alternar-tema]")) return;
    const tema = leer() === "oscuro" ? "claro" : "oscuro";
    try {
      localStorage.setItem(CLAVE, tema);
    } catch {
      // Sin almacenamiento la elección dura hasta recargar, pero se aplica.
    }
    aplicar(tema);
  });

  // Otra pestaña cambió el tema.
  window.addEventListener("storage", (evento) => {
    if (evento.key === CLAVE || evento.key === null) sincronizar();
  });
})();
