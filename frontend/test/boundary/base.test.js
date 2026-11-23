import sinon from "sinon";
import { expect } from "chai";
import { esperar } from "../ayudas.js";
import { usarDom, montar, dom, Vista, definirElemento } from "./entorno-dom.js";

describe("Vista · clase base de los componentes", () => {
  usarDom();

  it("Vista acepta hojas de estilo propias sin depender del layout de GalenoSV", () => {
    class ElementoPortable extends Vista {}
    ElementoPortable.hojasDeEstilo = ["https://estilos.example.com/portable.css"];
    definirElemento("ui-portable-prueba", ElementoPortable);
    const elemento = montar("ui-portable-prueba");
    elemento.render("<p>listo</p>");
    const hojas = [...elemento.shadowRoot.querySelectorAll('link[rel="stylesheet"]')].map((hoja) => hoja.href);
    expect(hojas).to.deep.equal(["https://estilos.example.com/portable.css"]);
    elemento.remove();
  });

  it("render devuelve el foco al elemento equivalente del contenido nuevo, si es uno solo", () => {
    class VistaRepintada extends Vista {}
    definirElemento("prueba-vista-repintada", VistaRepintada);
    const vista = montar("prueba-vista-repintada");
    const raiz = vista.shadowRoot;
    vista.render('<button data-accion="guardar" data-cp="8000">Guardar</button><textarea id="notas-8000"></textarea>');

    raiz.querySelector("textarea").focus();
    vista.render('<button data-accion="guardar" data-cp="8000">Guardar</button><textarea id="notas-8000"></textarea>');
    expect(raiz.activeElement?.id, "por id").to.equal("notas-8000");

    raiz.querySelector("button").focus();
    vista.render('<p>Nuevo</p><button data-accion="guardar" data-cp="8000">Guardar</button>');
    expect(raiz.activeElement?.textContent, "por sus atributos data-*").to.equal("Guardar");

    // Si el equivalente no existe o es ambiguo, no se adivina.
    raiz.querySelector("button").focus();
    vista.render('<button data-accion="guardar" data-cp="8000">A</button><button data-accion="guardar" data-cp="8000">B</button>');
    expect(raiz.activeElement).to.be.null;
  });

  describe("con hojas de estilo compartidas (adoptedStyleSheets)", () => {
    /** Simula un navegador actual: jsdom aún no implementa adoptedStyleSheets. */
    beforeEach(() => {
      Object.defineProperty(dom.window.ShadowRoot.prototype, "adoptedStyleSheets", {
        configurable: true,
        get() {
          return this._adoptadas ?? [];
        },
        set(hojas) {
          this._adoptadas = hojas;
        },
      });
      globalThis.ShadowRoot = dom.window.ShadowRoot;
      globalThis.CSSStyleSheet = dom.window.CSSStyleSheet;
    });

    afterEach(() => {
      delete dom.window.ShadowRoot.prototype.adoptedStyleSheets;
      delete globalThis.ShadowRoot;
      delete globalThis.CSSStyleSheet;
    });

    const cssEn = (href, css) =>
      fetch.withArgs(href).resolves(new Response(css, { status: 200, headers: { "Content-Type": "text/css" } }));

    it("todas las instancias comparten una hoja que se descarga una sola vez, sin <link>", async () => {
      const href = "https://example.test/compartida.css";
      cssEn(href, ":host { display: block; }");
      class VistaCompartida extends Vista {
        static hojasDeEstilo = [href];
        static plantilla = "<p>Hola</p>";
      }
      definirElemento("prueba-vista-compartida", VistaCompartida);
      const [una, otra] = ["a", "b"].map(() => {
        const elemento = montar("prueba-vista-compartida");
        elemento.montarPlantilla();
        return elemento;
      });
      una.render("<p>Otra vez</p>");

      const [hoja] = una.shadowRoot.adoptedStyleSheets;
      expect(otra.shadowRoot.adoptedStyleSheets[0]).to.equal(hoja);
      expect(una.shadowRoot.adoptedStyleSheets).to.have.length(1);
      expect(una.shadowRoot.querySelector("link")).to.be.null;
      expect(fetch.withArgs(href).callCount).to.equal(1);
      await esperar(() => hoja.cssRules.length === 1);
      expect(hoja.cssRules[0].cssText).to.contain("display: block");
    });

    it("precargarEstilos descarga la base y la hoja de cada componente antes de registrarlos", async () => {
      const { precargarEstilos } = await import("../../js/boundary/base.js");
      const base = new URL("../../css/componentes/base.css", import.meta.url).href;
      const propia = new URL("../../css/componentes/prueba-precarga.css", import.meta.url).href;
      cssEn(base, "* { box-sizing: border-box; }");
      cssEn(propia, ".a { color: red; }");
      await precargarEstilos(["prueba-precarga"]);
      expect(fetch.withArgs(propia).callCount).to.equal(1);

      class VistaPrecargada extends Vista {}
      definirElemento("prueba-precarga", VistaPrecargada);
      const elemento = montar("prueba-precarga");
      elemento.render("");
      const reglas = elemento.shadowRoot.adoptedStyleSheets.map((hoja) => hoja.cssRules.length);
      expect(reglas, "las hojas ya llegan con sus reglas").to.deep.equal([1, 1]);
    });

    it("si una hoja no se puede descargar lo avisa en consola sin bloquear la aplicación", async () => {
      const { precargarEstilos } = await import("../../js/boundary/base.js");
      const rota = new URL("../../css/componentes/prueba-rota.css", import.meta.url).href;
      fetch.withArgs(rota).resolves(new Response("", { status: 404 }));
      const consola = sinon.stub(console, "error");
      await precargarEstilos(["prueba-rota"]);
      expect(consola.calledWithMatch(sinon.match("prueba-rota.css"))).to.be.true;
    });
  });
});
