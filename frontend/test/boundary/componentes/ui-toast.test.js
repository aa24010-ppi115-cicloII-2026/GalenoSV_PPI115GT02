import { expect } from "chai";
import { usarDom, montar, dom } from "../entorno-dom.js";

describe("ui-toast", () => {
  usarDom();

  it("ui-toast muestra las notificaciones recibidas del documento", async () => {
    const toast = montar("ui-toast");
    document.dispatchEvent(
      new dom.window.CustomEvent("notificar", { detail: { mensaje: "Guardado correcto", tipo: "ok" } })
    );
    expect(toast.shadowRoot.querySelector(".aviso").textContent).to.equal("Guardado correcto");
  });

  it("ui-toast expone una API local configurable además del evento de notificación", () => {
    const toast = montar("ui-toast");
    const aviso = toast.mostrar("Mensaje de otra aplicación", { tipo: "error" });
    expect(aviso.classList.contains("error")).to.be.true;
    expect(aviso.textContent).to.equal("Mensaje de otra aplicación");
  });

  it("ui-toast limita los avisos visibles para no tapar la pantalla", () => {
    const toast = montar("ui-toast");
    ["uno", "dos", "tres", "cuatro", "cinco"].forEach((mensaje) => toast.mostrar(mensaje));
    const visibles = [...toast.shadowRoot.querySelectorAll(".aviso")].map((aviso) => aviso.textContent);
    expect(visibles).to.deep.equal(["tres", "cuatro", "cinco"]);
  });
});
