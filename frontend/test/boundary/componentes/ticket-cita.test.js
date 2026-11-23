import sinon from "sinon";
import { expect } from "chai";
import { datosBase } from "../../ayudas.js";
import { consultaCompleta, resumenCita } from "../../../js/control/citas.js";
import { usarDom, montar, dom, documentoImprimible } from "../entorno-dom.js";

describe("ticket-cita", () => {
  usarDom();

  describe("HU1 · Búsqueda de citas", () => {
    describe("comprobante de cita", () => {
      const citaDePrueba = () => resumenCita(datosBase(), datosBase().consultas.find((item) => String(item.idConsulta) === "5000"));

      it("el comprobante impreso incluye paciente, código, pasos y lo registrado", () => {
        const cita = consultaCompleta(datosBase(), 5000);
        const html = documentoImprimible(cita);
        for (const texto of ["Ana López", "#5000", "Evaluación", "Caries de la dentina", "Imprimir"]){
          expect(html).to.contain(texto);
        }
      });

      it("el ticket muestra lo registrado en cada paso y no el borrador del paso en curso", () => {
        const base = datosBase();
        base.consultasProcedimiento[0].observaciones = JSON.stringify({
          notas: "Borrador sin guardar",
          diagnostico: "K00.0 · Anodoncia",
          plan: "",
          pasos: { 7000: { notas: "Encía sana", diagnostico: "K02.1 · Caries de la dentina", plan: "" } },
        });
        const indice = base;
        const cita = resumenCita(indice, indice.consultas.find((item) => String(item.idConsulta) === "5000"));
        const ticket = montar("ticket-cita", { cita }).shadowRoot;
        const primeraFila = ticket.querySelector("tbody tr td:last-child").textContent;
        expect(primeraFila).to.equal("CIE-10: K02.1 · Caries de la dentina | Notas: Encía sana");
        for (const texto of [ticket.textContent, documentoImprimible(cita)]) {
          expect(texto).to.contain("Caries de la dentina");
          expect(texto).to.not.contain("Anodoncia");
          expect(texto).to.not.contain("Borrador sin guardar");
        }
      });

      it("el ticket incluye las órdenes de examen del procedimiento sin repetir la palabra «Examen»", () => {
        const base = datosBase();
        base.ordenesExamen.push({
          idOrdenExamen: 400,
          idConsultaProcedimientoPaso: 9000,
          fechaCreacion: "2026-11-12T09:20:00-06:00",
          indicaciones: "Examen: Radiografía periapical — Sector inferior",
        });
        const indice = base;
        const cita = resumenCita(indice, indice.consultas.find((item) => String(item.idConsulta) === "5000"));
        const filas = [...montar("ticket-cita", { cita }).shadowRoot.querySelectorAll("tbody tr")].map((fila) =>
          [...fila.cells].map((celda) => celda.textContent)
        );
        expect(filas.at(-1).slice(1, 3)).to.deep.equal(["Examen ordenado", "Ordenado"]);
        expect(filas.at(-1)[4]).to.equal("Radiografía periapical — Sector inferior");
        expect(documentoImprimible(cita)).to.contain("Radiografía periapical — Sector inferior");
      });

      it("en pantallas angostas cada celda del ticket lleva su etiqueta para leerse como tarjeta", () => {
        const ticket = montar("ticket-cita", { cita: citaDePrueba() }).shadowRoot;
        const etiquetas = [...ticket.querySelector("tbody tr").cells].map((celda) => celda.dataset.etiqueta);
        expect(etiquetas).to.deep.equal(["Procedimiento", "Paso", "Estado", "Fecha", "Observaciones"]);
      });

      it("un paso pendiente que conserva su registro de antes de retroceder no muestra fecha ni responsable", async () => {
        const cita = citaDePrueba();
        const [primero] = cita.procedimientos[0].pasos;
        // Mismo registro que el paso 1, pero el paso quedó pendiente tras retroceder.
        const pendiente = { ...primero, estado: "pendiente" };
        const conPendiente = { ...cita, procedimientos: [{ ...cita.procedimientos[0], pasos: [pendiente] }] };

        const fila = montar("ticket-cita", { cita: conPendiente }).shadowRoot.querySelector("tbody tr");
        expect([...fila.cells].map((celda) => celda.textContent).slice(2, 4)).to.deep.equal(["Pendiente", "Pendiente"]);
        const { pasosParaPresentacion } = await import("../../../js/boundary/presentacion.js");
        const linea = montar("linea-pasos", { pasos: pasosParaPresentacion([pendiente]) }).shadowRoot;
        expect(linea.querySelector(".quien").textContent).to.equal("Pendiente");
        expect(linea.querySelector("details.registro")).to.be.null;
      });

      it("ticket-cita muestra un código corto del ticket en lugar del identificador completo", () => {
        const cita = resumenCita(datosBase(), datosBase().consultas.find((item) => String(item.idConsulta) === "5000"));
        cita.consulta = { ...cita.consulta, idConsulta: "9fcdae6b-1100-401c-881a-892650484990" };
        const ticket = montar("ticket-cita", { cita });
        expect(ticket.shadowRoot.querySelector(".codigo").textContent).to.equal("#9FCDAE6B");
        expect(ticket.shadowRoot.textContent).to.not.contain("892650484990");
      });

      it("ticket-cita ofrece cerrar e imprimir como únicas acciones", () => {
        const ticket = montar("ticket-cita", { cita: citaDePrueba() });
        const acciones = [...ticket.shadowRoot.querySelectorAll(".acciones button")];
        expect(acciones.map((boton) => boton.dataset.accion)).to.deep.equal(["cerrar", "imprimir"]);
      });

      it("ticket-cita imprime el comprobante en una ventana nueva y avisa si el navegador la bloquea", () => {
        const ventana = { document: { replaceChildren: sinon.spy() }, focus: sinon.spy(), print: sinon.spy() };
        const abrir = sinon.stub(dom.window, "open").returns(ventana);
        const ticket = montar("ticket-cita", { cita: citaDePrueba() });
        const avisos = [];
        ticket.addEventListener("notificar", (evento) => avisos.push(evento.detail.mensaje));
        ticket.shadowRoot.querySelector("[data-accion='imprimir']").click();
        const nodos = ventana.document.replaceChildren.firstCall.args;
        expect(nodos.some((nodo) => nodo.textContent?.includes("GalenoSV · Comprobante de cita"))).to.be.true;
        expect(ventana.print.calledOnce).to.be.true;

        abrir.returns(null);
        ticket.shadowRoot.querySelector("[data-accion='imprimir']").click();
        expect(avisos).to.deep.equal(["Permita las ventanas emergentes para imprimir el ticket."]);
      });

      it("ticket-cita muestra los estados de los pasos con texto legible", () => {
        const cita = resumenCita(datosBase(), datosBase().consultas.find((item) => String(item.idConsulta) === "5000"));
        const ticket = montar("ticket-cita", { cita });
        const estados = [...ticket.shadowRoot.querySelectorAll("tbody td:nth-child(3)")].map((td) => td.textContent);
        expect(estados).to.deep.equal(["Completado", "Por iniciar", "Pendiente"]);
      });
    });
  });
});
