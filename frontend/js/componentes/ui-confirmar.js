import { Vista, escapar } from "../base.js";

/**
 * Diálogo modal de confirmación propio (reemplaza window.confirm).
 *   if (await confirmar({ titulo, mensaje, aceptar: "Retroceder" })) { ... }
 * Con `peligro: true` el botón de aceptar se muestra como acción destructiva.
 * Enfoca «Cancelar», cierra con Escape y devuelve el foco al elemento que lo abrió.
 */
export class UiConfirmar extends Vista {
  get estilos() {
    return `
      .fondo {
        position: fixed; inset: 0; z-index: 100; display: grid; place-items: center; padding: 1rem;
        background: color-mix(in srgb, var(--mate-fuerte, #1f3a50) 45%, transparent);
      }
      .caja {
        width: min(100%, 26rem); display: grid; gap: 0.7rem; padding: 1.35rem 1.35rem 1.1rem;
        background: var(--superficie, #fff); border-radius: var(--radio-grande, 14px); box-shadow: var(--sombra-alta);
      }
      h2 { font-size: 1.1rem; letter-spacing: -0.01em; }
      p { color: var(--texto-suave, #4d6378); font-size: 0.92rem; }
      .botones { display: flex; justify-content: flex-end; gap: 0.5rem; flex-wrap: wrap; margin-top: 0.4rem; }
    `;
  }

  abrir({ titulo, mensaje = "", aceptar = "Aceptar", cancelar = "Cancelar", peligro = false }) {
    // El botón que abrió el diálogo suele estar dentro de otros Shadow DOM.
    let previo = document.activeElement;
    while (previo?.shadowRoot?.activeElement) previo = previo.shadowRoot.activeElement;
    this._previo = previo;
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
      const cerrar = (respuesta) => {
        this.remove();
        this._previo?.focus?.();
        resolver(respuesta);
      };
      this.q(".botones").addEventListener("click", (evento) => {
        const boton = evento.target.closest("[data-respuesta]");
        if (boton) cerrar(boton.dataset.respuesta === "si");
      });
      this.shadowRoot.addEventListener("keydown", (evento) => {
        if (evento.key === "Escape") cerrar(false);
        // Mantiene el foco dentro del diálogo: solo hay dos botones.
        if (evento.key === "Tab") {
          evento.preventDefault();
          const [no, si] = this.qa("button");
          (this.shadowRoot.activeElement === no ? si : no).focus();
        }
      });
      this.q('[data-respuesta="no"]').focus();
    });
  }
}

customElements.define("ui-confirmar", UiConfirmar);

/** Muestra el diálogo y resuelve true si se acepta. */
export function confirmar(opciones) {
  const dialogo = document.createElement("ui-confirmar");
  document.body.append(dialogo);
  return dialogo.abrir(opciones);
}
