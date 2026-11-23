import { expect } from "chai";
import { usarDom, montar, dom } from "../entorno-dom.js";

describe("reutilizables · punto de entrada de los controles", () => {
  usarDom();

  it("el punto de entrada permite registrar un control con un nombre propio y conserva su CSS", async () => {
    const { UiAutocompletar, definirElemento } = await import("galenosv-frontend/componentes");
    class BuscadorLocal extends UiAutocompletar {}
    const nombre = "galenosv-prueba-buscador";
    const registrado = definirElemento(nombre, BuscadorLocal);
    const repetido = definirElemento(nombre, class extends UiAutocompletar {});
    const buscador = montar(nombre, { opciones: [{ valor: "a", texto: "Alfa" }] });

    expect(registrado).to.equal(BuscadorLocal);
    expect(repetido).to.equal(BuscadorLocal);
    expect(buscador.shadowRoot.querySelector("input")).to.exist;
    expect(buscador.shadowRoot.querySelectorAll('link[rel="stylesheet"]')[1].href).to.contain("ui-autocompletar.css");
  });

  it("los controles reutilizables exponen sus piezas con part para darles estilo desde fuera", async () => {
    await import("../../../js/boundary/componentes/linea-pasos.js");
    const partes = (elemento) => [...elemento.shadowRoot.querySelectorAll("[part]")].map((nodo) => nodo.getAttribute("part"));

    const buscador = montar("ui-autocompletar", { opciones: [{ valor: "a", texto: "Alfa" }] });
    buscador.shadowRoot.querySelector("input").dispatchEvent(new dom.window.Event("click"));
    expect(partes(buscador)).to.include.members(["etiqueta", "entrada", "lista", "opcion"]);

    const pasos = montar("linea-pasos", { pasos: [{ nombre: "Limpieza", estado: "actual" }] });
    expect(partes(pasos)).to.include.members(["paso actual", "numero"]);

    const avisos = montar("ui-toast");
    avisos.mostrar("Guardado", { tipo: "ok" });
    expect(partes(avisos)).to.deep.equal(["aviso ok"]);

    const { confirmar } = await import("../../../js/boundary/componentes/ui-confirmar.js");
    confirmar({ titulo: "¿Seguro?" });
    expect(partes(document.querySelector("ui-confirmar"))).to.include.members(["dialogo", "boton cancelar", "boton aceptar"]);

    const instalar = montar("pwa-instalar");
    expect(partes(instalar)).to.include.members(["boton-abrir", "tarjeta", "boton-instalar"]);
  });
});
