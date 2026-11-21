import { Vista, definirElemento, modalConFoco } from "../base.js";
import { escapar } from "../texto.js";

/**
 * Diálogo modal de confirmación propio (reemplaza window.confirm).
 *   if (await confirmar({ titulo, mensaje, aceptar: "Retroceder" })) { ... }
 * Con `peligro: true` el botón de aceptar se muestra como acción destructiva.
 * Enfoca «Cancelar», cierra con Escape y devuelve el foco al elemento que lo abrió.
 */
export class UiConfirmar extends Vista {

  abrir({ titulo, mensaje = "", aceptar = "Aceptar", cancelar = "Cancelar", peligro = false }) {
    this.render(`
      <div class="fondo">
        <div class="caja" role="alertdialog" aria-modal="true" aria-labelledby="titulo" aria-describedby="mensaje">
          <h2 id="titulo">${escapar(titulo)}</h2>
          <p id="mensaje">${escapar(mensaje)}</p>
          <div class="botones">
            <button type="button" class="secundario" data-respuesta="no">${escapar(cancelar)}</button>
            <button type="button" ${peligro ? 'class="peligro"' : ""} data-respuesta="si">${escapar(aceptar)}</button>
          </div>
        </div>
      </div>
    `);
    return new Promise((resolver) => {
      let liberar;
      const cerrar = (respuesta) => {
        liberar?.();
        this.remove();
        resolver(respuesta);
      };
      liberar = modalConFoco(this, () => cerrar(false));
      this.q(".botones").addEventListener("click", (evento) => {
        const boton = evento.target.closest("[data-respuesta]");
        if (boton) cerrar(boton.dataset.respuesta === "si");
      });
      this.q('[data-respuesta="no"]').focus();
    });
  }
}

definirElemento("ui-confirmar", UiConfirmar);

/** Muestra el diálogo y resuelve true si se acepta. */
export function confirmar(opciones) {
  const dialogo = document.createElement("ui-confirmar");
  document.body.append(dialogo);
  return dialogo.abrir(opciones);
}
