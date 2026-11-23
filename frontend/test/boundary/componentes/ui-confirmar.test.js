import { expect } from "chai";
import { usarDom, dom } from "../entorno-dom.js";

describe("ui-confirmar", () => {
  usarDom();

  it("ui-confirmar enfoca Cancelar, mantiene el foco dentro con Tab y Escape cancela", async () => {
    const { confirmar } = await import("../../../js/boundary/componentes/ui-confirmar.js");
    const respuesta = confirmar({ titulo: "¿Seguro?", aceptar: "Sí" });
    const raiz = document.querySelector("ui-confirmar").shadowRoot;
    expect(raiz.querySelector("[role=alertdialog]").getAttribute("aria-modal")).to.equal("true");
    expect(raiz.activeElement.textContent).to.equal("Cancelar");
    const tecla = (key) => raiz.activeElement.dispatchEvent(new dom.window.KeyboardEvent("keydown", { key, bubbles: true, cancelable: true }));
    tecla("Tab");
    expect(raiz.activeElement.textContent).to.equal("Sí");
    tecla("Tab");
    expect(raiz.activeElement.textContent).to.equal("Cancelar");
    tecla("Escape");
    expect(await respuesta).to.be.false;
    expect(document.querySelector("ui-confirmar")).to.be.null;
  });

  it("ui-confirmar acepta un mensaje con formato además de texto plano", async () => {
    const { confirmar } = await import("../../../js/boundary/componentes/ui-confirmar.js");
    const { fragmento } = await import("../../../js/boundary/base.js");
    const { html } = await import("../../../js/comun/texto.js");
    confirmar({ titulo: "¿Retroceder?", mensaje: fragmento(html`Se reabre <strong>${"Profilaxis <b>"}</strong>.`) });
    const mensaje = document.querySelector("ui-confirmar").shadowRoot.querySelector("#mensaje");
    expect(mensaje.querySelector("strong").textContent).to.equal("Profilaxis <b>");
    expect(mensaje.textContent).to.equal("Se reabre Profilaxis <b>.");

    document.body.innerHTML = "";
    confirmar({ titulo: "¿Seguro?", mensaje: "<em>texto</em>" });
    const plano = document.querySelector("ui-confirmar").shadowRoot.querySelector("#mensaje");
    expect(plano.querySelector("em"), "un texto nunca se interpreta como HTML").to.be.null;
    expect(plano.textContent).to.equal("<em>texto</em>");
  });
});
