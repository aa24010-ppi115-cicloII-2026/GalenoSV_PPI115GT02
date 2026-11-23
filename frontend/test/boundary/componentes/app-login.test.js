import { expect } from "chai";
import { esperar } from "../../ayudas.js";
import { usarDom, montar, dom, datos } from "../entorno-dom.js";

describe("app-login · HU2", () => {
  usarDom();

  describe("HU2 · Login", () => {
    async function abrirLogin() {
      const login = montar("app-login");
      await esperar(() => login.shadowRoot.querySelector("#tipo").opciones.length > 0);
      return login;
    }

    function enviar(login, tipo, numero) {
      login.shadowRoot.querySelector("#tipo").value = tipo;
      login.shadowRoot.querySelector("#numero").value = numero;
      login.shadowRoot.querySelector("#formulario").dispatchEvent(
        new dom.window.Event("submit", { bubbles: true, cancelable: true })
      );
    }

    const errorLogin = (login) => login.shadowRoot.querySelector("#error");

    it("app-login valida el documento y crea la sesión sin autorregistro", async () => {
      const login = montar("app-login");
      const navegaciones = [];
      login.addEventListener("navegar", (evento) => navegaciones.push(evento.detail));
      await esperar(() => login.shadowRoot.querySelector("#tipo").opciones.length > 0);
      login.shadowRoot.querySelector("#tipo").value = "1";
      login.shadowRoot.querySelector("#numero").value = " 33333333-3 ";
      login.shadowRoot.querySelector("#formulario").dispatchEvent(
        new dom.window.Event("submit", { bubbles: true, cancelable: true })
      );
      await esperar(() => sessionStorage.getItem("galenosv.sesion") && navegaciones.length > 0);
      const sesion = JSON.parse(sessionStorage.getItem("galenosv.sesion"));
      expect(sesion.idPersona).to.equal(300);
      expect(sesion.nombre).to.equal("Elena Martínez");
      expect(sesion.nombreCorto).to.equal("Elena Martínez");
      expect(sesion.roles[0].nombreRol).to.equal("Enfermero");
      expect(navegaciones).to.include("dashboard");
    });

    it("app-login rechaza documentos desconocidos y muestra el error", async () => {
      const login = montar("app-login");
      await esperar(() => login.shadowRoot.querySelector("#tipo").opciones.length > 0);
      login.shadowRoot.querySelector("#tipo").value = "1";
      login.shadowRoot.querySelector("#numero").value = "99999999-9";
      login.shadowRoot.querySelector("#formulario").dispatchEvent(
        new dom.window.Event("submit", { bubbles: true, cancelable: true })
      );
      await esperar(() => !login.shadowRoot.querySelector("#error").hidden);
      expect(login.shadowRoot.querySelector("#error").textContent).to.equal(
        "No hay un empleado registrado con ese documento. Verifique el tipo y el número."
      );
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
    });

    it("app-login rechaza a quien solo es paciente con el mismo mensaje, sin revelar que el documento existe", async () => {
      const login = await abrirLogin();
      enviar(login, "1", "11111111-1");
      await esperar(() => !errorLogin(login).hidden);
      expect(errorLogin(login).textContent).to.equal(
        "No hay un empleado registrado con ese documento. Verifique el tipo y el número."
      );
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
    });

    it("app-login oculta el error en cuanto se corrige el número y no sugiere un documento real", async () => {
      const login = await abrirLogin();
      enviar(login, "1", "11111111-1");
      await esperar(() => !errorLogin(login).hidden);
      const numero = login.shadowRoot.querySelector("#numero");
      numero.value = "33333333-3";
      numero.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
      expect(errorLogin(login).hidden).to.be.true;
      expect(numero.placeholder).to.not.match(/[1-9]/);
    });

    it("app-login pide tipo y número, y valida el formato antes de buscar al empleado", async () => {
      datos.tiposDocumento[0].expresionRegular = "^[0-9]{8}-[0-9]$";
      datos.tiposDocumento[0].indicaciones = "8 dígitos, guion y dígito verificador";
      const login = await abrirLogin();
      enviar(login, "", "");
      expect(errorLogin(login).textContent).to.equal("Seleccione el tipo de documento e ingrese el número.");
      expect(errorLogin(login).getAttribute("role")).to.equal("alert");

      enviar(login, "1", "123");
      await esperar(() => errorLogin(login).textContent.includes("formato"));
      expect(errorLogin(login).textContent).to.contain("8 dígitos, guion y dígito verificador");
      expect(sessionStorage.getItem("galenosv.sesion")).to.be.null;
    });

    it("app-login avisa cuando no puede cargar los datos del servicio", async () => {
      fetch.rejects(new TypeError("Failed to fetch"));
      const login = montar("app-login");
      await esperar(() => !errorLogin(login).hidden);
      expect(errorLogin(login).textContent).to.contain("No se pudo conectar con el servicio");
      expect(login.shadowRoot.querySelector("#tipo").mensaje).to.equal("No se pudo cargar");
    });

    it("app-login no ofrece recuperar contraseña ni autorregistro", async () => {
      const login = await abrirLogin();
      expect(login.shadowRoot.textContent).to.not.match(/contraseña|Solicitar acceso/i);
      expect(login.shadowRoot.querySelector("#ir-buscar")).to.not.be.null;
    });
  });
});
