import sinon from "sinon";
import { expect } from "chai";
import { esperar } from "../../ayudas.js";
import { usarDom, montar, dom } from "../entorno-dom.js";

describe("pwa-instalar", () => {
  usarDom();

  /** Monta el componente y simula que el navegador ofrece instalar la PWA. */
  async function ofrecerInstalacion(userChoice = Promise.resolve({ outcome: "accepted" })) {
    const instalar = montar("pwa-instalar");
    await esperar(() => instalar.shadowRoot.querySelector(".tarjeta"));
    const raiz = instalar.shadowRoot;
    expect(raiz.querySelector("#abrir").hidden).to.be.true;
    expect(raiz.querySelector(".tarjeta").hidden).to.be.true;
    const evento = new dom.window.Event("beforeinstallprompt");
    evento.prompt = sinon.spy();
    evento.userChoice = userChoice;
    dom.window.dispatchEvent(evento);
    await esperar(() => !raiz.querySelector("#abrir").hidden);
    return { raiz, evento };
  }

  it("pwa-instalar muestra solo un botón discreto hasta que el usuario lo pulsa", async () => {
    const { raiz, evento } = await ofrecerInstalacion();
    const abrir = raiz.querySelector("#abrir");
    expect(raiz.querySelector(".tarjeta").hidden).to.be.true;
    expect(abrir.getAttribute("aria-label")).to.equal("Instalar la aplicación");
    expect(abrir.getAttribute("aria-expanded")).to.equal("false");

    abrir.click();
    expect(raiz.querySelector(".tarjeta").hidden).to.be.false;
    expect(abrir.hidden).to.be.true;
    expect(abrir.getAttribute("aria-expanded")).to.equal("true");
    expect(raiz.activeElement).to.equal(raiz.querySelector("#instalar"));
    expect(evento.prompt.called).to.be.false;
  });

  it("pwa-instalar permite definir el nombre y la descripción del proyecto anfitrión", async () => {
    const instalar = document.createElement("pwa-instalar");
    instalar.setAttribute("nombre", "Clínica <Local>");
    instalar.setAttribute("descripcion", "Instalación propia");
    document.body.append(instalar);
    await esperar(() => instalar.shadowRoot.querySelector(".texto strong"));
    expect(instalar.shadowRoot.querySelector(".texto strong").textContent).to.equal("Instale Clínica <Local>");
    expect(instalar.shadowRoot.querySelector(".texto span").textContent).to.equal("Instalación propia");
    expect(instalar.shadowRoot.querySelector(".texto script")).to.be.null;
  });

  it("pwa-instalar abre la instalación del navegador y luego desaparece", async () => {
    const { raiz, evento } = await ofrecerInstalacion();
    raiz.querySelector("#abrir").click();
    raiz.querySelector("#instalar").click();
    await esperar(() => evento.prompt.called && raiz.querySelector(".tarjeta").hidden);
    expect(raiz.querySelector("#abrir").hidden).to.be.true;
  });

  it("pwa-instalar vuelve a plegarse en el botón con «Ahora no»", async () => {
    const { raiz, evento } = await ofrecerInstalacion();
    raiz.querySelector("#abrir").click();
    raiz.querySelector('[aria-label="Ahora no"]').click();
    expect(raiz.querySelector(".tarjeta").hidden).to.be.true;
    expect(raiz.querySelector("#abrir").hidden).to.be.false;
    expect(raiz.activeElement).to.equal(raiz.querySelector("#abrir"));
    expect(evento.prompt.called).to.be.false;
  });

  it("pwa-instalar se oculta cuando la aplicación ya quedó instalada", async () => {
    const { raiz } = await ofrecerInstalacion();
    dom.window.dispatchEvent(new dom.window.Event("appinstalled"));
    expect(raiz.querySelector("#abrir").hidden).to.be.true;
    expect(raiz.querySelector(".tarjeta").hidden).to.be.true;
  });

  it("pwa-instalar actualiza su texto si cambian nombre o descripcion", () => {
    const instalar = montar("pwa-instalar");
    const texto = () => instalar.shadowRoot.querySelector(".texto").textContent;
    expect(texto()).to.equal("Instale GalenoSVAcceso directo y uso sin conexión");
    instalar.setAttribute("nombre", "Agenda");
    instalar.setAttribute("descripcion", "Sin conexión");
    expect(texto()).to.equal("Instale AgendaSin conexión");
  });
});
