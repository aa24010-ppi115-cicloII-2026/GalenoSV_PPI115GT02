import { Vista, definirElemento, modalConFoco } from "../base.js";

/**
 * Diálogo modal de confirmación propio (reemplaza window.confirm).
 *   confirmar({ titulo, mensaje, aceptar: "Retroceder" }).then((aceptado) => { if (aceptado) ... });
 * `mensaje` es texto, o nodos cuando necesita formato: fragmento(html`Se borrará <strong>${nombre}</strong>.`)
 * Con `peligro: true` el botón de aceptar se muestra como acción destructiva.
 * Enfoca «Cancelar», cierra con Escape y devuelve el foco al elemento que lo abrió.
 */
export default class UiConfirmar extends Vista {
  static plantilla = `
    <div class="fondo">
      <div class="caja" part="dialogo" role="alertdialog" aria-modal="true" aria-labelledby="titulo" aria-describedby="mensaje">
        <h2 id="titulo"></h2>
        <div id="mensaje" class="mensaje"></div>
        <div class="botones">
          <button type="button" class="secundario" part="boton cancelar" data-respuesta="no"></button>
          <button type="button" part="boton aceptar" data-respuesta="si"></button>
        </div>
      </div>
    </div>
  `;

  abrir({ titulo, mensaje = "", aceptar = "Aceptar", cancelar = "Cancelar", peligro = false }) {
    this.montarPlantilla();
    this.q("#titulo").textContent = titulo;
    const contenido = this.q("#mensaje");
    if (mensaje instanceof Node) contenido.replaceChildren(mensaje);
    else contenido.textContent = mensaje;
    const botonCancelar = this.q('[data-respuesta="no"]');
    const botonAceptar = this.q('[data-respuesta="si"]');
    botonCancelar.textContent = cancelar;
    botonAceptar.textContent = aceptar;
    botonAceptar.classList.toggle("peligro", peligro);

    return new Promise((resolver) => {
      const cerrar = (respuesta) => {
        liberar();
        this.remove();
        resolver(respuesta);
      };
      const liberar = modalConFoco(this, () => cerrar(false));
      this.escuchar(this.q(".botones"), "click", (evento) => {
        const boton = evento.target.closest("[data-respuesta]");
        if (boton) cerrar(boton.dataset.respuesta === "si");
      });
      botonCancelar.focus();
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
