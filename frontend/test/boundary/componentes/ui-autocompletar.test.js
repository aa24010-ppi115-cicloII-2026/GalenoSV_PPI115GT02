import { expect } from "chai";
import sinon from "sinon";
import { usarDom, montar, dom } from "../entorno-dom.js";

describe("ui-autocompletar", () => {
  usarDom();

  describe("ui-autocompletar · buscador de datos del catálogo", () => {
    const OPCIONES = [
      { valor: 10, texto: "Clínica Central" },
      { valor: 20, texto: "Clínica Municipal Chalchuapa" },
      { valor: 30, texto: "Policlínica Oeste" },
    ];

    function abrirCampo() {
      const campo = montar("ui-autocompletar", { opciones: OPCIONES });
      campo.setAttribute("etiqueta", "Clínica");
      const entrada = campo.shadowRoot.querySelector("input");
      const escribir = (texto) => {
        entrada.value = texto;
        entrada.dispatchEvent(new dom.window.Event("input"));
      };
      const tecla = (key) => {
        const evento = new dom.window.KeyboardEvent("keydown", { key, bubbles: true, composed: true, cancelable: true });
        entrada.dispatchEvent(evento);
        return evento;
      };
      const visibles = () => [...campo.shadowRoot.querySelectorAll("li[role=option]")].map((li) => li.textContent);
      return { campo, entrada, escribir, tecla, visibles };
    }

    it("filtra mientras se escribe, sin distinguir mayúsculas ni acentos, y resalta la coincidencia", () => {
      const { campo, escribir, visibles } = abrirCampo();
      escribir("CLINICA");
      expect(visibles()).to.deep.equal(["Clínica Central", "Clínica Municipal Chalchuapa", "Policlínica Oeste"]);
      escribir("chal");
      expect(visibles()).to.deep.equal(["Clínica Municipal Chalchuapa"]);
      expect(campo.shadowRoot.querySelector("mark").textContent).to.equal("Chal");
      escribir("zzz");
      expect(visibles()).to.deep.equal(["Sin resultados"]);
    });

    it("se elige con flechas y Enter, emite change y Enter no llega al formulario", () => {
      const { campo, entrada, escribir, tecla } = abrirCampo();
      const cambios = [];
      const enFormulario = [];
      campo.addEventListener("change", () => cambios.push(campo.value));
      document.body.addEventListener("keydown", (evento) => enFormulario.push(evento.key));
      escribir("clinica");
      tecla("ArrowDown");
      tecla("ArrowDown");
      expect(entrada.getAttribute("aria-activedescendant")).to.equal("op-1");
      tecla("Enter");
      expect(campo.value).to.equal(20);
      expect(campo.textoElegido).to.equal("Clínica Municipal Chalchuapa");
      expect(entrada.value).to.equal("Clínica Municipal Chalchuapa");
      expect(entrada.getAttribute("aria-expanded")).to.equal("false");
      expect(cambios).to.deep.equal([20]);
      expect(enFormulario).to.not.include("Enter");
    });

    it("Escape cierra la lista y conserva la opción elegida", () => {
      const { campo, entrada, escribir, tecla } = abrirCampo();
      campo.value = 10;
      escribir("poli");
      tecla("Escape");
      expect(entrada.getAttribute("aria-expanded")).to.equal("false");
      expect(entrada.value).to.equal("Clínica Central");
      expect(campo.value).to.equal(10);
    });

    it("al salir acepta un texto exacto, restaura uno incompleto y vacía el valor si se borra", () => {
      const { campo, entrada, escribir } = abrirCampo();
      const salir = () => entrada.dispatchEvent(new dom.window.FocusEvent("blur"));
      escribir("policlinica oeste");
      salir();
      expect(campo.value).to.equal(30);

      escribir("muni");
      salir();
      expect(campo.value).to.equal(30);
      expect(entrada.value).to.equal("Policlínica Oeste");

      escribir("");
      salir();
      expect(campo.value).to.equal("");
    });

    it("muestra un aviso y se deshabilita mientras no hay opciones", () => {
      const campo = montar("ui-autocompletar");
      const entrada = campo.shadowRoot.querySelector("input");
      expect(entrada.disabled).to.be.true;
      expect(entrada.placeholder).to.equal("Cargando…");
      campo.opciones = OPCIONES;
      expect(entrada.disabled).to.be.false;
      campo.mensaje = "No disponible";
      expect(entrada.disabled).to.be.true;
      expect(entrada.placeholder).to.equal("No disponible");
    });
  });

  describe("como control de formulario (form-associated custom element)", () => {
    const OPCIONES = [
      { valor: 10, texto: "Clínica Central" },
      { valor: 20, texto: "Clínica Municipal Chalchuapa" },
    ];

    /**
     * jsdom no implementa la parte de formularios de ElementInternals (setFormValue, setValidity):
     * se reemplaza por un doble para comprobar qué le informa el control al formulario.
     */
    function conInternos() {
      const internos = { setFormValue: sinon.spy(), setValidity: sinon.spy() };
      sinon.stub(dom.window.HTMLElement.prototype, "attachInternals").returns(internos);
      return internos;
    }

    function crearCampo({ requerido = false } = {}) {
      const campo = document.createElement("ui-autocompletar");
      campo.setAttribute("etiqueta", "Clínica");
      campo.setAttribute("name", "clinica");
      if (requerido) campo.setAttribute("required", "");
      campo.opciones = OPCIONES;
      document.body.append(campo);
      return { campo, entrada: campo.shadowRoot.querySelector("input") };
    }

    it("se declara asociado a formularios y envía la opción elegida, o vacío al borrarla", () => {
      const internos = conInternos();
      const { campo, entrada } = crearCampo();
      expect(customElements.get("ui-autocompletar").formAssociated).to.be.true;
      expect(internos.setFormValue.lastCall.args).to.deep.equal([""]);

      entrada.value = "chal";
      entrada.dispatchEvent(new dom.window.Event("input"));
      campo.shadowRoot.querySelector("li[data-indice]").click();
      expect(campo.value).to.equal(20);
      expect(internos.setFormValue.lastCall.args).to.deep.equal(["20"]);

      entrada.dispatchEvent(new dom.window.Event("click"));
      entrada.value = "";
      entrada.dispatchEvent(new dom.window.Event("blur"));
      expect(campo.value).to.equal("");
      expect(internos.setFormValue.lastCall.args).to.deep.equal([""]);
    });

    it("con required queda inválido hasta elegir una opción, con un mensaje y el foco en la entrada", () => {
      const internos = conInternos();
      const { campo, entrada } = crearCampo({ requerido: true });
      const [estado, mensaje, ancla] = internos.setValidity.lastCall.args;
      expect(estado).to.deep.equal({ valueMissing: true });
      expect(mensaje).to.equal("Elija una opción de «Clínica».");
      expect(ancla).to.equal(entrada);

      campo.value = 10;
      expect(internos.setValidity.lastCall.args).to.deep.equal([{}]);

      campo.value = "";
      expect(internos.setValidity.lastCall.args[0]).to.deep.equal({ valueMissing: true });
      campo.removeAttribute("required");
      expect(internos.setValidity.lastCall.args).to.deep.equal([{}]);
    });

    it("al reiniciar el formulario queda sin opción y un fieldset deshabilitado bloquea la entrada", () => {
      const internos = conInternos();
      const { campo, entrada } = crearCampo();
      campo.value = 20;

      campo.formResetCallback();
      expect(campo.value).to.equal("");
      expect(entrada.value).to.equal("");
      expect(internos.setFormValue.lastCall.args).to.deep.equal([""]);

      campo.formDisabledCallback(true);
      expect(entrada.disabled).to.be.true;
      expect(campo.hasAttribute("disabled"), "no se marca el propio elemento").to.be.false;
      campo.formDisabledCallback(false);
      expect(entrada.disabled).to.be.false;

      // Un aviso propio (por ejemplo «No disponible») mantiene la entrada bloqueada.
      campo.mensaje = "No disponible";
      campo.formDisabledCallback(false);
      expect(entrada.disabled).to.be.true;
    });

    it("donde el navegador no tiene la API de formularios sigue funcionando sin ella", () => {
      sinon.stub(dom.window.HTMLElement.prototype, "attachInternals").returns({});
      const { campo, entrada } = crearCampo({ requerido: true });
      campo.value = 20;
      expect(campo.value).to.equal(20);
      expect(entrada.value).to.equal("Clínica Municipal Chalchuapa");
    });
  });

  describe("atributos y foco", () => {
    const OPCIONES = [
      { valor: 10, texto: "Clínica Central" },
      { valor: 20, texto: "Clínica Municipal Chalchuapa" },
    ];

    it("etiqueta y descrito-por se pueden cambiar después de montarlo", () => {
      const campo = montar("ui-autocompletar");
      campo.setAttribute("etiqueta", "Clínica");
      campo.setAttribute("descrito-por", "ayuda");
      const raiz = campo.shadowRoot;
      expect(raiz.querySelector("label").textContent).to.equal("Clínica");
      expect(raiz.querySelector("#lista").getAttribute("aria-label")).to.equal("Clínica");
      expect(raiz.querySelector("input").getAttribute("aria-describedby")).to.equal("ayuda");

      campo.removeAttribute("descrito-por");
      expect(raiz.querySelector("input").hasAttribute("aria-describedby")).to.be.false;
    });

    it("value en el marcado elige esa opción al llegar las opciones, como el valor inicial de un input", () => {
      const campo = document.createElement("ui-autocompletar");
      campo.setAttribute("value", "20");
      document.body.append(campo);
      expect(campo.value, "sin opciones todavía no hay nada elegido").to.equal("");

      campo.opciones = OPCIONES;
      expect(campo.value).to.equal(20);
      expect(campo.shadowRoot.querySelector("input").value).to.equal("Clínica Municipal Chalchuapa");

      // Una elección de la persona o del código manda sobre el valor inicial.
      campo.value = 10;
      campo.setAttribute("value", "20");
      campo.opciones = [...OPCIONES];
      expect(campo.value).to.equal(10);

      // Al reiniciar el formulario vuelve al valor inicial, no a vacío.
      campo.formResetCallback();
      expect(campo.value).to.equal(20);
      campo.setAttribute("value", "10");
      expect(campo.value, "sin elección propia sigue al atributo").to.equal(10);
    });

    it("delega el foco en su campo interno, también desde un <label> externo", () => {
      const adjuntar = sinon.spy(dom.window.HTMLElement.prototype, "attachShadow");
      montar("ui-autocompletar");
      montar("ui-toast");
      expect(adjuntar.firstCall.args[0]).to.deep.equal({ mode: "open", delegatesFocus: true });
      expect(adjuntar.secondCall.args[0], "los demás componentes no lo necesitan").to.deep.equal({ mode: "open", delegatesFocus: false });
    });
  });
});
