import sinon from "sinon";
import { expect } from "chai";
import { esperar } from "../../ayudas.js";
import { cargarTodo } from "../../../js/control/repositorio.js";
import { usarDom, montar, sinRed, dom, datos } from "../entorno-dom.js";

describe("app-busqueda · HU1", () => {
  usarDom();

  describe("HU1 · Búsqueda de citas", () => {
    describe("asistente de búsqueda", () => {
      // El asistente vive dentro de app-busqueda; estas ayudas lo recorren como lo haría el paciente.
      async function abrirAsistente() {
        const busqueda = montar("app-busqueda");
        await esperar(() => busqueda.shadowRoot.querySelector("asistente-citas")?.shadowRoot?.querySelector("#tipo").opciones.length > 0);
        return { busqueda, raiz: busqueda.shadowRoot.querySelector("asistente-citas").shadowRoot };
      }

      const pasoVisible = (raiz) => raiz.querySelector("section[data-paso]:not([hidden])").dataset.paso;

      const error = (raiz) => (raiz.querySelector("#error").hidden ? "" : raiz.querySelector("#error").textContent);

      const clic = (raiz, selector) => raiz.querySelector(selector).click();

      async function hastaPaso(raiz, paso) {
        await esperar(() => pasoVisible(raiz) === paso);
      }

      async function completarDocumento(raiz, numero) {
        raiz.querySelector("#tipo").value = "1";
        clic(raiz, "#siguiente");
        await hastaPaso(raiz, "2");
        raiz.querySelector("#numero").value = numero;
        clic(raiz, "#siguiente");
      }

      it("empieza en el paso 1 y no avanza sin elegir el tipo de documento", async () => {
        const { raiz } = await abrirAsistente();
        expect(pasoVisible(raiz)).to.equal("1");
        expect(raiz.querySelector('ol.pasos li[aria-current="step"]').textContent).to.contain("Tipo de documento");
        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("1");
        expect(error(raiz)).to.contain("tipo de documento");
      });

      it("muestra el tipo y su número debajo de la etiqueta de cada paso", async () => {
        const { raiz } = await abrirAsistente();
        const tipo = raiz.querySelector("#tipo");
        tipo.value = "1";
        tipo.dispatchEvent(new dom.window.Event("change", { bubbles: true }));

        expect(raiz.querySelector("#etiqueta-numero").textContent).to.equal("Número de DUI");
        expect(raiz.querySelector('ol.pasos li[data-paso="2"] .etiqueta').textContent).to.equal("Número de DUI");
        expect(raiz.querySelector('ol.pasos li[data-paso="1"] .valor').textContent).to.equal("DUI");

        const numero = raiz.querySelector("#numero");
        numero.value = "11111111-1";
        numero.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
        expect(raiz.querySelector('ol.pasos li[data-paso="2"] .valor').textContent).to.equal("11111111-1");
      });

      it("valida el número con el formato del tipo de documento y que esté registrado", async () => {
        datos.tiposDocumento[0].expresionRegular = "^[0-9]{8}-[0-9]$";
        datos.tiposDocumento[0].indicaciones = "8 dígitos, guion y dígito verificador";
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "123");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.contain("8 dígitos, guion y dígito verificador");

        // Al corregir el número el error anterior desaparece.
        const numero = raiz.querySelector("#numero");
        numero.value = "99999999-9";
        numero.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
        expect(raiz.querySelector("#error").hidden).to.be.true;

        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.contain("No hay citas registradas con ese documento");
      });

      it("no distingue un documento inexistente de uno sin clínicas como paciente", async () => {
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "33333333-3");
        const desconocido = error(raiz);
        raiz.querySelector("#numero").value = "99999999-9";
        clic(raiz, "#siguiente");
        expect(desconocido).to.equal(error(raiz));
      });

      it("lista solo las clínicas donde es paciente y permite volver al paso anterior", async () => {
        datos.clinicas.push({ idClinica: 20, nombre: "Clínica Norte" }, { idClinica: 30, nombre: "Clínica Oeste" });
        datos.personasRol.push(
          { idPersonaRol: 1001, idPersona: 100, idRol: 2, idClinica: 20 },
          { idPersonaRol: 1002, idPersona: 100, idRol: 1, idClinica: 30 }
        );
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        const clinicas = raiz.querySelector("#clinica").opciones.map((opcion) => opcion.texto);
        expect(clinicas).to.deep.equal(["Clínica Central", "Clínica Oeste"]);

        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("3");
        expect(error(raiz)).to.contain("clínica");

        clic(raiz, "#atras");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(raiz.querySelector("#numero").value).to.equal("11111111-1");
      });

      it("al retroceder hasta el tipo y cambiarlo obliga a escribir y validar el nuevo documento", async () => {
        datos.tiposDocumento.push({ idTipoDocumento: 2, nombre: "Pasaporte" });
        datos.documentos.push({ idDocumento: 4, idPersona: 200, idTipoDocumento: 2, valor: "AB123" });
        const { raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        await hastaPaso(raiz, "4");

        clic(raiz, "#atras");
        await hastaPaso(raiz, "3");
        clic(raiz, "#atras");
        await hastaPaso(raiz, "2");
        clic(raiz, "#atras");
        await hastaPaso(raiz, "1");

        const tipo = raiz.querySelector("#tipo");
        tipo.value = 2;
        tipo.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
        expect(raiz.querySelector("#numero").value).to.equal("");
        expect(raiz.querySelector("#clinica").value).to.equal("");

        clic(raiz, "#siguiente");
        await hastaPaso(raiz, "2");
        clic(raiz, "#siguiente");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.contain("Escriba el número de Pasaporte");
      });

      it("busca por clínica y rango de fechas, con las citas de hoy por defecto", async () => {
        // Hoy es el 12 de noviembre: la paciente tiene una cita hoy y otra el 5 de enero.
        const reloj = sinon.useFakeTimers({ now: new Date("2026-11-12T08:00:00-06:00"), toFake: ["Date"] });
        try {
          const { busqueda, raiz } = await abrirAsistente();
          await completarDocumento(raiz, "11111111-1");
          await hastaPaso(raiz, "3");
          expect(raiz.querySelector("#clinica").textoElegido).to.equal("Clínica Central");
          clic(raiz, "#siguiente");
          expect(pasoVisible(raiz)).to.equal("4");
          expect(raiz.querySelector("#desde").value).to.equal("2026-11-12");
          expect(raiz.querySelector("#hasta").value).to.equal("2026-11-12");

          raiz.querySelector("#desde").value = "2026-12-01";
          raiz.querySelector("#hasta").value = "2026-11-01";
          clic(raiz, "#buscar");
          expect(error(raiz)).to.contain("fecha inicial");
          expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);

          // Con las dos fechas vacías se buscan las de hoy, no todas.
          raiz.querySelector("#desde").value = "";
          raiz.querySelector("#hasta").value = "";
          clic(raiz, "#buscar");
          await esperar(() => busqueda.shadowRoot.querySelector(".resumen"));
          expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(1);

          // Con una sola fecha, el otro extremo queda abierto.
          raiz.querySelector("#desde").value = "2026-01-01";
          clic(raiz, "#buscar");
          await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 2);

          raiz.querySelector("#desde").value = "2026-11-01";
          clic(raiz, "#buscar");
          await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 1);
          const tarjeta = busqueda.shadowRoot.querySelector("cita-card").shadowRoot;
          // La búsqueda del paciente no agrega acciones de empleado a la tarjeta.
          expect(busqueda.shadowRoot.querySelector('cita-card [slot="acciones"]')).to.be.null;
          expect(tarjeta.textContent).to.contain("Clínica Central");
          expect(tarjeta.textContent).to.contain("Limpieza dental");
          expect(tarjeta.querySelector(".fecha").getAttribute("aria-label")).to.contain("12 nov 2026");
        } finally {
          reloj.restore();
        }
      });

      it("informa cuando no hay citas en la clínica y las fechas elegidas", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        raiz.querySelector("#desde").value = "2027-01-01";
        raiz.querySelector("#hasta").value = "";
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelector(".resumen"));
        expect(busqueda.shadowRoot.querySelector(".resumen").textContent).to.contain("0 citas");
        expect(busqueda.shadowRoot.querySelector("#lista-citas").textContent).to.contain("No hay citas para la clínica y las fechas elegidas");
        expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);
      });

      it("avanza con Enter y pide el número cuando se deja vacío", async () => {
        const { raiz } = await abrirAsistente();
        const enter = (selector) =>
          raiz.querySelector(selector).dispatchEvent(new dom.window.KeyboardEvent("keydown", { key: "Enter", bubbles: true }));
        raiz.querySelector("#tipo").value = "1";
        enter("#tipo");
        expect(pasoVisible(raiz)).to.equal("2");

        enter("#numero");
        expect(pasoVisible(raiz)).to.equal("2");
        expect(error(raiz)).to.equal("Escriba el número de DUI.");

        raiz.querySelector("#numero").value = "11111111-1";
        enter("#numero");
        expect(pasoVisible(raiz)).to.equal("3");
      });

      it("avisa al paciente cuando no se pueden cargar los datos del servicio", async () => {
        fetch.rejects(new TypeError("Failed to fetch"));
        const busqueda = montar("app-busqueda");
        const raiz = busqueda.shadowRoot.querySelector("asistente-citas").shadowRoot;
        await esperar(() => !raiz.querySelector("#error").hidden);
        expect(error(raiz)).to.contain("No se pudo conectar con el servicio");
        expect(raiz.querySelector("#tipo").mensaje).to.equal("No disponible");
      });

      it("sin conexión usa los datos guardados en el dispositivo", async () => {
        const datosGuardados = await cargarTodo();
        const llamadasAntes = fetch.callCount;
        sinRed();
        fetch.rejects(new TypeError("Failed to fetch"));
        const busqueda = montar("app-busqueda");
        const asistente = busqueda.shadowRoot.querySelector("asistente-citas");
        await esperar(() => asistente.shadowRoot.querySelector("#tipo").opciones.length > 0);
        expect(busqueda.shadowRoot.querySelector("h2").textContent).to.equal("Buscar mis citas");
        expect(asistente.datos).to.equal(datosGuardados);
        expect(fetch.callCount).to.equal(llamadasAntes);
        expect(asistente.shadowRoot.querySelector("#error").hidden).to.be.true;
        expect([...asistente.shadowRoot.querySelectorAll("input, button")].some((elemento) => elemento.disabled)).to.be.false;
      });

      it("ticket-cita mantiene el foco dentro del comprobante y lo devuelve a «Ver ticket» al cerrar", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        // Todo el año, para encontrar las dos citas de la paciente.
        raiz.querySelector("#desde").value = "2026-01-01";
        raiz.querySelector("#hasta").value = "2026-12-31";
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelector("cita-card"));
        const verTicket = busqueda.shadowRoot.querySelector("cita-card").shadowRoot.querySelector('[data-accion="ticket"]');
        verTicket.focus();
        verTicket.click();
        await esperar(() => busqueda.shadowRoot.querySelector("ticket-cita"));
        const ticket = busqueda.shadowRoot.querySelector("ticket-cita").shadowRoot;
        const botones = [...ticket.querySelectorAll("button")];
        expect(ticket.activeElement).to.equal(botones[0]);
        const tecla = (key, shiftKey = false) =>
          ticket.activeElement.dispatchEvent(new dom.window.KeyboardEvent("keydown", { key, shiftKey, bubbles: true, composed: true, cancelable: true }));
        tecla("Tab", true);
        expect(ticket.activeElement).to.equal(botones.at(-1));
        tecla("Tab");
        expect(ticket.activeElement).to.equal(botones[0]);
        tecla("Escape");
        expect(busqueda.shadowRoot.querySelector("ticket-cita")).to.be.null;
        expect(verTicket.getRootNode().activeElement).to.equal(verTicket);
      });

      it("abre el ticket de una cita encontrada y permite iniciar una nueva búsqueda", async () => {
        const { busqueda, raiz } = await abrirAsistente();
        await completarDocumento(raiz, "11111111-1");
        await hastaPaso(raiz, "3");
        clic(raiz, "#siguiente");
        // Todo el año, para encontrar las dos citas de la paciente.
        raiz.querySelector("#desde").value = "2026-01-01";
        raiz.querySelector("#hasta").value = "2026-12-31";
        clic(raiz, "#buscar");
        await esperar(() => busqueda.shadowRoot.querySelectorAll("cita-card").length === 2);
        // Los datos del paciente se muestran una sola vez y la vista se lleva a los resultados.
        const titulo = busqueda.shadowRoot.querySelector("#titulo-resultados");
        expect(titulo.textContent).to.equal("Ana López");
        expect(busqueda.shadowRoot.querySelector(".resumen").textContent).to.contain("DUI 11111111-1");
        expect(busqueda.shadowRoot.activeElement).to.equal(titulo);
        busqueda.shadowRoot.querySelector("cita-card").shadowRoot.querySelector('button[data-accion="ticket"]').click();
        await esperar(() => busqueda.shadowRoot.querySelector("ticket-cita"));
        expect(busqueda.shadowRoot.querySelector("ticket-cita").shadowRoot.textContent).to.contain("Comprobante de cita");

        busqueda.shadowRoot.querySelector("#nueva").click();
        expect(pasoVisible(raiz)).to.equal("1");
        // La nueva búsqueda vuelve a proponer las citas de hoy.
        const hoy = new Date();
        const hoyISO = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, "0")}-${String(hoy.getDate()).padStart(2, "0")}`;
        expect([raiz.querySelector("#desde").value, raiz.querySelector("#hasta").value]).to.deep.equal([hoyISO, hoyISO]);
        expect(busqueda.shadowRoot.querySelectorAll("cita-card")).to.have.length(0);
      });
    });
  });
});
