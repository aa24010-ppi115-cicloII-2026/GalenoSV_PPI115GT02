import sinon from "sinon";
import { expect } from "chai";
import { esperar } from "../../ayudas.js";
import { leerSesion } from "../../../js/control/sesion.js";
import { limpiarCache } from "../../../js/control/repositorio.js";
import { usarDom, montar, dom, datos, SESION_ENFERMERO } from "../entorno-dom.js";

describe("app-dashboard · HU3", () => {
  usarDom();

  describe("HU3 · Dashboard", () => {
    const tarjetas = (raiz) => [...raiz.querySelectorAll("#lista cita-card")];

    const idsDe = (raiz) => tarjetas(raiz).map((tarjeta) => tarjeta.cita.consulta.idConsulta);

    const cambiar = (campo, valor) => {
      campo.value = valor;
      campo.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
    };

    const rango = (raiz, desde, hasta) => {
      raiz.querySelector("#desde").value = desde;
      cambiar(raiz.querySelector("#hasta"), hasta);
    };

    async function abrirPanel(hoy = "2026-11-12T08:00:00-06:00") {
      // Solo se congela la fecha: los temporizadores siguen reales para esperar el render.
      reloj = sinon.useFakeTimers({ now: new Date(hoy), toFake: ["Date"] });
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const panel = montar("app-dashboard");
      await esperar(() => panel.shadowRoot.querySelectorAll("#resumen:not([aria-busy]) .indicador").length === 4);
      return panel.shadowRoot;
    }

    let reloj;

    afterEach(() => reloj?.restore());

    it("preselecciona la primera clínica y su rol cuando el empleado trabaja en varias sedes", async () => {
      datos.clinicas.push({ idClinica: 20, nombre: "Clínica Norte" });
      const sesion = {
        ...SESION_ENFERMERO,
        idClinica: "",
        idRol: "",
        clinicas: [
          { idClinica: 10, nombre: "Clínica Central" },
          { idClinica: 20, nombre: "Clínica Norte" },
        ],
        roles: [
          SESION_ENFERMERO.roles[0],
          { idPersonaRol: 3002, idRol: 2, nombreRol: "Odontólogo", idClinica: 20, nombreClinica: "Clínica Norte" },
        ],
      };
      reloj = sinon.useFakeTimers({ now: new Date("2026-11-12T08:00:00-06:00"), toFake: ["Date"] });
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(sesion));
      const raiz = montar("app-dashboard").shadowRoot;
      await esperar(() => raiz.querySelector("#lista").getAttribute("aria-live") === "polite" && !raiz.querySelector("#resumen[aria-busy]"));
      expect(raiz.querySelector("#clinica").value).to.equal(10);
      expect(raiz.querySelector("#rol").value).to.equal(3);
    });

    it("reserva el resumen y describe el rango mientras carga, para que la agenda no salte", async () => {
      reloj = sinon.useFakeTimers({ now: new Date("2026-11-12T08:00:00-06:00"), toFake: ["Date"] });
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const raiz = montar("app-dashboard").shadowRoot;
      // Antes de que lleguen los datos: mismo lugar ocupado, con «—» en lugar de los conteos.
      expect(raiz.querySelector("#resumen").getAttribute("aria-busy")).to.equal("true");
      expect([...raiz.querySelectorAll("#resumen strong")].map((n) => n.textContent)).to.deep.equal(["—", "—", "—", "—"]);
      expect(raiz.querySelector("#rango-aviso").textContent).to.contain("Consultas de hoy");
      await esperar(() => !raiz.querySelector("#resumen").hasAttribute("aria-busy"));
      expect(raiz.querySelector("#resumen strong").textContent).not.to.equal("—");
    });

    it("cuenta las citas sin pasos como programadas para que el resumen cuadre con el total", async () => {
      const raiz = await abrirPanel();
      await esperar(() => [...raiz.querySelectorAll("#resumen strong")].some((n) => n.textContent !== "—"));
      expect([...raiz.querySelectorAll("#resumen strong")].map((n) => n.textContent)).to.deep.equal(["1", "1", "0", "0"]);
    });

    it("si la carga falla quita el estado ocupado y ofrece reintentar", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      fetch.rejects(new TypeError("Failed to fetch"));
      const raiz = montar("app-dashboard").shadowRoot;
      await esperar(() => raiz.querySelector("#reintentar"));
      expect(raiz.querySelector("#resumen").hasAttribute("aria-busy")).to.be.false;
      expect(raiz.querySelector("#lista").textContent).to.contain("No se pudo cargar la agenda");
    });

    it("no muestra consultas sin un rol elegido en la clínica", async () => {
      sessionStorage.setItem(
        "galenosv.sesion",
        JSON.stringify({
          ...SESION_ENFERMERO,
          clinicas: [{ idClinica: 20, nombre: "Clínica sin rol" }],
          idClinica: 20,
          idRol: null,
        })
      );
      const raiz = montar("app-dashboard").shadowRoot;
      await esperar(() => !raiz.querySelector("#resumen").hasAttribute("aria-busy"));
      expect(tarjetas(raiz)).to.have.length(0);
      expect(raiz.querySelector("#lista").textContent).to.contain("Seleccione una clínica y un rol");
      expect([...raiz.querySelectorAll("#resumen strong")].map((item) => item.textContent)).to.deep.equal(["0", "0", "0", "0"]);
    });

    it("conserva las fechas y el paciente elegidos al volver de una consulta", async () => {
      const raiz = await abrirPanel();
      raiz.querySelector('[data-rango="pasadas"]').click();
      const paciente = raiz.querySelector("#paciente");
      paciente.value = 200;
      paciente.dispatchEvent(new dom.window.Event("change"));
      raiz.host.remove();

      const panel = montar("app-dashboard");
      await esperar(() => panel.shadowRoot.querySelectorAll("#resumen:not([aria-busy]) .indicador").length === 4);
      const nueva = panel.shadowRoot;
      expect(nueva.querySelector("#desde").value).to.equal("");
      expect(nueva.querySelector("#hasta").value).to.equal("2026-11-11");
      expect(nueva.querySelector('[data-rango="pasadas"]').getAttribute("aria-pressed")).to.equal("true");
      expect(String(nueva.querySelector("#paciente").value)).to.equal("200");
    });

    it("muestra por defecto las consultas de hoy del rol elegido", async () => {
      const raiz = await abrirPanel();
      expect(raiz.querySelector("#desde").value).to.equal("2026-11-12");
      expect(raiz.querySelector("#hasta").value).to.equal("2026-11-12");
      expect(raiz.querySelector('[data-rango="hoy"]').getAttribute("aria-pressed")).to.equal("true");
      expect(raiz.querySelector("#rango-aviso").textContent).to.contain("hoy");
      await esperar(() => tarjetas(raiz).length === 1);
      expect(idsDe(raiz)).to.deep.equal([5002]);
    });

    it("pone en cada tarjeta «Atender consulta» o «Ver atención» y lleva al registro de atención", async () => {
      const accion = (raiz) => tarjetas(raiz)[0].querySelector('[slot="acciones"]');
      let raiz = await abrirPanel();
      await esperar(() => tarjetas(raiz).length === 1);
      expect(accion(raiz).textContent).to.equal("Atender consulta");
      const rutas = [];
      raiz.host.addEventListener("navegar", (evento) => rutas.push(evento.detail));
      accion(raiz).click();
      expect(rutas).to.deep.equal(["consulta/5002"]);

      // Con todos los pasos completos ya no hay nada que atender: solo revisar.
      document.body.innerHTML = "";
      reloj.restore();
      limpiarCache();
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9100,
        idConsultaProcedimiento: 8001,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T11:05:00-06:00",
        fechaFin: "2026-11-12T11:40:00-06:00",
        estado: "FINALIZADO",
      });
      raiz = await abrirPanel();
      await esperar(() => tarjetas(raiz).length === 1);
      expect(accion(raiz).textContent).to.equal("Ver atención");
    });

    it("cambia de rol y muestra solo las consultas con pasos de ese rol", async () => {
      const raiz = await abrirPanel();
      await esperar(() => tarjetas(raiz).length === 1);
      cambiar(raiz.querySelector("#rol"), "2");
      await esperar(() => tarjetas(raiz).length === 1);
      expect(tarjetas(raiz)[0].shadowRoot.textContent).to.contain("Limpieza dental");
    });

    it("filtra consultas pasadas, la más reciente primero", async () => {
      const raiz = await abrirPanel("2026-11-20T08:00:00-06:00");
      await esperar(() => raiz.querySelector("#lista .vacio"));
      raiz.querySelector('[data-rango="pasadas"]').click();
      await esperar(() => tarjetas(raiz).length === 1);
      expect(raiz.querySelector("#desde").value).to.equal("");
      expect(raiz.querySelector("#hasta").value).to.equal("2026-11-19");
      expect(idsDe(raiz)).to.deep.equal([5002]);
      expect(raiz.querySelector('[data-rango="pasadas"]').getAttribute("aria-pressed")).to.equal("true");
    });

    it("filtra consultas próximas desde mañana sin límite final", async () => {
      const raiz = await abrirPanel("2026-11-01T08:00:00-06:00");
      raiz.querySelector('[data-rango="proximas"]').click();
      await esperar(() => tarjetas(raiz).length === 1);
      expect(raiz.querySelector("#desde").value).to.equal("2026-11-02");
      expect(raiz.querySelector("#hasta").value).to.equal("");
      expect(idsDe(raiz)).to.deep.equal([5002]);
    });

    it("permite un rango libre de fechas y avisa si el inicio es posterior al fin", async () => {
      const raiz = await abrirPanel("2026-12-01T08:00:00-06:00");
      rango(raiz, "2026-11-01", "2026-11-30");
      await esperar(() => tarjetas(raiz).length === 1);
      expect(raiz.querySelector('[aria-pressed="true"]')).to.be.null;
      expect(raiz.querySelector("#rango-aviso").textContent).to.contain("1 nov 2026");

      rango(raiz, "2026-11-30", "2026-11-01");
      await esperar(() => raiz.querySelector("#lista [role=alert]"));
      expect(tarjetas(raiz)).to.have.length(0);
      expect(raiz.querySelector("#lista").textContent).to.contain("posterior");
      expect(raiz.querySelector("#rango-aviso").textContent).to.equal("");
      expect(leerSesion().filtros).to.deep.equal({ desde: "2026-11-01", hasta: "2026-11-30", idPaciente: null });
    });

    it("usa buscadores para clínica, rol y paciente, y filtra por el paciente elegido", async () => {
      const raiz = await abrirPanel();
      await esperar(() => raiz.querySelector("#paciente").opciones.length > 0);
      expect(raiz.querySelector("#clinica").textoElegido).to.equal("Clínica Central");
      expect(raiz.querySelector("#rol").textoElegido).to.equal("Enfermero");
      expect(raiz.querySelector("#paciente").opciones.map((opcion) => opcion.texto)).to.deep.equal([
        "Ana López · DUI 11111111-1",
        "Carlos Pérez · DUI 22222222-2",
      ]);
      await esperar(() => tarjetas(raiz).length === 1);
      cambiar(raiz.querySelector("#paciente"), 200);
      await esperar(() => tarjetas(raiz).length === 1);
      expect(tarjetas(raiz)[0].shadowRoot.textContent).to.contain("Carlos Pérez");
    });
  });
});
