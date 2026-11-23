import { expect } from "chai";
import { datosBase } from "../../ayudas.js";
import { resumenCita } from "../../../js/control/citas.js";
import { usarDom, montar, SESION_ENFERMERO } from "../entorno-dom.js";

describe("cita-card", () => {
  usarDom();

  describe("HU1 · Búsqueda de citas", () => {
    describe("cards de citas", () => {
      it("cita-card emite ver-ticket con su cita", () => {
        const cita = resumenCita(datosBase(), datosBase().consultas.find((item) => String(item.idConsulta) === "5000"));
        const tarjeta = montar("cita-card", { cita, vista: "empleado" });
        const tickets = [];
        tarjeta.addEventListener("ver-ticket", (evento) => tickets.push(evento.detail));
        tarjeta.shadowRoot.querySelector('[data-accion="ticket"]').click();
        expect(tickets[0].consulta.idConsulta).to.equal(5000);
      });

      it("cita-card muestra junto al ticket las acciones que le pasa la pantalla, sin saber cuáles son", () => {
        const cita = resumenCita(datosBase(), datosBase().consultas.find((item) => String(item.idConsulta) === "5000"));
        const tarjeta = document.createElement("cita-card");
        tarjeta.cita = cita;
        tarjeta.innerHTML = '<button slot="acciones">Reprogramar</button>';
        document.body.append(tarjeta);
        const ranura = tarjeta.shadowRoot.querySelector(".acciones slot[name=acciones]");
        expect(ranura.assignedElements().map((boton) => boton.textContent)).to.deep.equal(["Reprogramar"]);

        // Sin acciones de la pantalla solo queda el botón de ticket.
        const sola = montar("cita-card", { cita });
        expect(sola.shadowRoot.querySelector("slot[name=acciones]").assignedElements()).to.have.length(0);
        expect([...sola.shadowRoot.querySelectorAll(".acciones button")].map((b) => b.textContent)).to.deep.equal(["Ver ticket"]);
      });

      it("cita-card muestra los pasos en jerarquía: procedimiento, pasos en orden y lo registrado en cada uno", () => {
        sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
        const base = datosBase();
        base.consultasProcedimiento[0].observaciones = JSON.stringify({ notas: "", pasos: { 7000: { notas: "Encía sana" } } });
        const indice = base;
        const raiz = montar("cita-card", { cita: resumenCita(indice, indice.consultas.find((item) => String(item.idConsulta) === "5000")) }).shadowRoot;

        const procedimiento = raiz.querySelector(".procedimiento");
        expect(procedimiento.querySelector(".nombre-procedimiento").textContent).to.equal("Limpieza dental");
        const linea = procedimiento.querySelector("linea-pasos").shadowRoot;
        const pasos = [...linea.querySelectorAll("ol.pasos > li.paso")];
        expect(pasos.map((paso) => paso.querySelector(".nombre").textContent)).to.deep.equal(["Evaluación", "Profilaxis", "Alta"]);

        const [hecho, actual, pendiente] = pasos;
        expect(hecho.querySelector(".quien").textContent).to.contain("Elena Martínez");
        expect(hecho.querySelector(".quien").textContent).to.contain("12 nov");
        const registro = hecho.querySelector("details.registro");
        expect(registro.querySelector("summary").textContent).to.equal("Ver registro");
        expect(registro.textContent).to.contain("Encía sana");
        // Sin registro todavía no hay nada que ver: solo se indica el estado.
        expect(actual.querySelector("details.registro")).to.be.null;
        expect(actual.querySelector(".quien").textContent).to.equal("Por iniciar");
        expect(pendiente.querySelector("details.registro")).to.be.null;
      });

      it("cita-card marca el paso actual sin iniciar como «Por iniciar» y muestra la fecha de nacimiento correcta", () => {
        const base = datosBase();
        base.personas[0].fechaNacimiento = "1992-09-30T00:00:00Z";
        const indice = base;
        const tarjeta = montar("cita-card", { cita: resumenCita(indice, indice.consultas.find((item) => String(item.idConsulta) === "5000")), vista: "empleado" });
        const actual = tarjeta.shadowRoot.querySelector("linea-pasos").shadowRoot.querySelector(".paso.actual .quien").textContent;
        expect(actual).to.contain("Por iniciar");
        expect(tarjeta.shadowRoot.textContent).to.contain("30 sept 1992");
      });

      it("en la búsqueda del paciente titula con el motivo y no repite datos de la cita", () => {
        sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
        const cita = resumenCita(datosBase(), datosBase().consultas.find((item) => String(item.idConsulta) === "5000"));
        const tarjeta = document.createElement("cita-card");
        tarjeta.setAttribute("vista", "paciente");
        tarjeta.cita = cita;
        document.body.append(tarjeta);
        const raiz = tarjeta.shadowRoot;
        expect(raiz.querySelector("h3").textContent).to.equal("Limpieza");
        expect(raiz.querySelector(".paciente p").textContent).to.equal("Clínica Central");
        // Documento y nacimiento ya están en el encabezado de los resultados: no se repiten en cada cita.
        expect(raiz.querySelector(".general")).to.be.null;

        const enDashboard = montar("cita-card", { cita, vista: "empleado" }).shadowRoot;
        expect(enDashboard.querySelector("h3").textContent).to.equal("Ana López");
      });
    });
  });
});
