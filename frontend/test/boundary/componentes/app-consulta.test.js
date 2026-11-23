import sinon from "sinon";
import { expect } from "chai";
import { stubApi, esperar } from "../../ayudas.js";
import { limpiarCache } from "../../../js/control/repositorio.js";
import { listarPendientes, sincronizar } from "../../../js/control/pendientes.js";
import { ejecutarPendiente } from "../../../js/control/api.js";
import { usarDom, sinRed, conRed, dom, datos, SESION_ENFERMERO } from "../entorno-dom.js";

describe("app-consulta · HU4", () => {
  usarDom();

  /** Abre la consulta y espera a que se pinte. */
  async function abrirConsulta(id = "5000") {
    sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
    const vista = document.createElement("app-consulta");
    vista.setAttribute("id-consulta", id);
    document.body.append(vista);
    await esperar(() => vista.shadowRoot.querySelector(".proc"));
    return vista;
  }

  /** Responde el diálogo de confirmación propio y devuelve su texto. */
  async function responderDialogo(aceptar) {
    await esperar(() => document.querySelector("ui-confirmar"));
    const dialogo = document.querySelector("ui-confirmar");
    const texto = dialogo.shadowRoot.textContent;
    dialogo.shadowRoot.querySelector(`[data-respuesta="${aceptar ? "si" : "no"}"]`).click();
    await esperar(() => !document.querySelector("ui-confirmar"));
    return texto;
  }

  const pasoActualizado = () =>
    fetch.getCalls().filter((llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso"));

  describe("HU4 · Registro de atención", () => {
    it("app-consulta no ofrece retroceder en el primer paso, aunque ya esté iniciado", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      expect(raiz.querySelector("[data-accion='retroceder']")).to.be.null;
      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      expect(raiz.querySelector("[data-accion='retroceder']")).to.be.null;
    });

    it("app-consulta con todos los pasos completados retrocede al último paso, no al penúltimo", async () => {
      datos.consultasProcedimientoPaso.push(
        { idConsultaProcedimientoPaso: 9001, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T09:40:00-06:00", estado: "FINALIZADO" },
        { idConsultaProcedimientoPaso: 9002, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T10:00:00-06:00", estado: "FINALIZADO" }
      );
      const vista = await abrirConsulta();
      // Sin paso en curso no hay formulario de notas: lo escrito no quedaría en ningún paso.
      expect(vista.shadowRoot.querySelector(".paso-panel.completada")).to.exist;
      expect(vista.shadowRoot.querySelector("[data-accion='guardar-notas']")).to.be.null;
      vista.shadowRoot.querySelector("[data-accion='retroceder']").click();
      expect(await responderDialogo(true)).to.contain("Alta");
      await esperar(() => pasoActualizado().length === 1);
      expect(pasoActualizado()[0].args[0]).to.contain("/9002");
    });

    it("app-consulta en el último paso ofrece completarlo en lugar de avanzar", async () => {
      datos.consultasProcedimientoPaso.push(
        { idConsultaProcedimientoPaso: 9001, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T09:40:00-06:00", estado: "FINALIZADO" },
        { idConsultaProcedimientoPaso: 9002, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T10:00:00-06:00", estado: "EN_PROCESO" }
      );
      const vista = await abrirConsulta();
      expect(vista.shadowRoot.querySelector("[data-accion='completar']").textContent).to.equal("Completar último paso");
    });

    it("app-consulta no pierde lo escrito en el registro al iniciar el paso", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      expect(raiz.querySelector(".paso-panel").textContent).to.contain("Paso 1 de 3");
      raiz.querySelector("[data-campo='notas']").value = "Paciente refiere sensibilidad";
      raiz.querySelector("[data-campo='plan']").value = "Control en 15 días";
      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      expect(raiz.querySelector("[data-campo='notas']").value).to.equal("Paciente refiere sensibilidad");
      expect(raiz.querySelector("[data-campo='plan']").value).to.equal("Control en 15 días");
    });

    it("app-consulta destaca «Finalizar consulta» solo cuando todos los pasos están completos", async () => {
      const vista = await abrirConsulta();
      expect(vista.shadowRoot.querySelector("[data-accion='finalizar']").classList.contains("secundario")).to.be.true;

      vista.remove();
      limpiarCache();
      datos.consultasProcedimientoPaso.push(
        { idConsultaProcedimientoPaso: 9001, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T09:40:00-06:00", estado: "FINALIZADO" },
        { idConsultaProcedimientoPaso: 9002, idConsultaProcedimiento: 8000, idPersonaRol: 3000, fechaInicio: "2026-11-12T10:00:00-06:00", estado: "FINALIZADO" }
      );
      const completa = await abrirConsulta();
      expect(completa.shadowRoot.querySelector("[data-accion='finalizar']").classList.contains("secundario")).to.be.false;
    });

    it("app-consulta resume al paciente en el encabezado con su edad y el estado como insignia", async () => {
      datos.personas[0].fechaNacimiento = "1990-05-15T00:00:00-06:00";
      const vista = await abrirConsulta();
      const encabezado = vista.shadowRoot.querySelector(".encabezado");
      expect(encabezado.querySelector("h2").textContent).to.equal("Ana López");
      expect(encabezado.querySelector(".datos").textContent).to.match(/\d+ años · Clínica Central · .+ · Limpieza/);
      expect(encabezado.querySelector(".estado-pill").textContent).to.equal("En proceso");
    });

    it("app-consulta muestra como finalizada una consulta cerrada aunque conserve pasos pendientes", async () => {
      datos.consultas[0].fechaFin = "2026-11-12T12:00:00-06:00";
      const vista = await abrirConsulta();
      const encabezado = vista.shadowRoot.querySelector(".encabezado");

      expect(encabezado.querySelector(".titulo .estado-pill").textContent).to.equal("Finalizada");
      expect(encabezado.querySelector(".fila .estado-pill").textContent).to.contain("Finalizada");
      expect(vista.shadowRoot.querySelector("[data-accion='finalizar']")).to.be.null;
      expect(vista.shadowRoot.textContent).to.contain("modo de solo lectura");
    });

    it("app-consulta pliega los exámenes opcionales, resume las órdenes y muestra «Asignar» solo con exámenes elegidos", async () => {
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        estado: "EN_PROCESO",
      });
      datos.ordenesExamen.push({ idOrdenExamen: 400, idConsultaProcedimientoPaso: 9001, fechaCreacion: "2026-11-12T09:45:00-06:00", indicaciones: "Examen: Radiografía periapical" });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      const examenes = raiz.querySelector("details.examenes");
      expect(examenes.open).to.be.false;
      expect(examenes.querySelector("summary").textContent).to.contain("1 orden");

      examenes.open = true;
      const asignar = () => raiz.querySelector("[data-accion='asignar']").closest(".asignacion");
      expect(asignar().hidden).to.be.true;
      const buscador = raiz.querySelector("[data-examen-buscador]");
      buscador.value = 20;
      buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
      expect(asignar().hidden).to.be.false;

      // Tras asignar, el bloque sigue abierto para ver la orden nueva.
      raiz.querySelector("[data-accion='asignar']").click();
      await esperar(() => raiz.querySelector("details.examenes summary").textContent.includes("2 órdenes"));
      expect(raiz.querySelector("details.examenes").open).to.be.true;
    });

    it("app-consulta busca el diagnóstico CIE-10 escribiendo parte del nombre", async () => {
      const vista = await abrirConsulta();
      const campo = vista.shadowRoot.querySelector("[data-campo='diagnostico']");
      expect(campo.localName).to.equal("ui-autocompletar");
      const entrada = campo.shadowRoot.querySelector("input");
      entrada.value = "dentina";
      entrada.dispatchEvent(new dom.window.Event("input"));
      const opciones = [...campo.shadowRoot.querySelectorAll("li[role=option]")].map((li) => li.textContent);
      expect(opciones).to.deep.equal(["K02.1 · Caries de la dentina"]);
    });

    it("app-consulta busca exámenes por nombre o categoría y permite elegir y quitar varios", async () => {
      datos.examenes.push({ idExamen: 21, nombre: "Hemograma", activo: true });
      datos.examenesTipoExamen.push({ idExamenTipoExamen: 2, idExamen: 21, idTipoExamen: 31 });
      datos.tiposExamen.push({ idTipoExamen: 31, nombre: "Laboratorio" });
      datos.procedimientosPasoExamen.push({ idProcedimientoPasoExamen: 2, idProcedimientoPaso: 7001, idExamen: 21, activo: true });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-examen-buscador]"));

      const buscador = raiz.querySelector("[data-examen-buscador]");
      expect(buscador.localName).to.equal("ui-autocompletar");
      const entrada = buscador.shadowRoot.querySelector("input");
      const buscar = (texto) => {
        entrada.value = texto;
        entrada.dispatchEvent(new dom.window.Event("input"));
        return [...buscador.shadowRoot.querySelectorAll("li[role=option]")].map((li) => li.textContent);
      };
      const elegir = (idExamen) => {
        buscador.value = idExamen;
        buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
      };
      const elegidos = () => [...raiz.querySelectorAll(".elegidos li")].map((li) => li.querySelector("span").textContent);

      expect(buscar("laboratorio")).to.deep.equal(["Hemograma · Laboratorio"]);
      expect(buscar("radio")).to.deep.equal(["Radiografía periapical · Imagenología"]);
      elegir(21);
      elegir(20);
      expect(elegidos()).to.deep.equal(["Hemograma", "Radiografía periapical"]);
      // Lo ya elegido no se vuelve a ofrecer.
      expect(buscador.opciones).to.deep.equal([]);
      expect(buscador.mensaje).to.equal("Ya eligió todos los exámenes disponibles");

      raiz.querySelector('.elegidos button[aria-label="Quitar Hemograma"]').click();
      expect(elegidos()).to.deep.equal(["Radiografía periapical"]);
      expect(buscador.opciones.map((opcion) => opcion.valor)).to.deep.equal([21]);
    });

    it("app-consulta elimina las órdenes de examen seleccionadas del paso en curso tras confirmar, salvo las que ya tienen resultado", async () => {
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        estado: "EN_PROCESO",
      });
      datos.ordenesExamen.push(
        { idOrdenExamen: 400, idConsultaProcedimientoPaso: 9001, fechaCreacion: "2026-11-12T09:45:00-06:00", indicaciones: "Examen: Radiografía periapical" },
        { idOrdenExamen: 401, idConsultaProcedimientoPaso: 9001, fechaCreacion: "2026-11-12T09:46:00-06:00", indicaciones: "Examen: Hemograma" }
      );
      datos.examenesResultado.push({ idExamenResultado: 500, idOrdenExamen: 401, resultado: "Normal" });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      const casillas = () => [...raiz.querySelectorAll('.ordenes input[type="checkbox"]')];
      const boton = () => raiz.querySelector("[data-accion='eliminar-ordenes']");
      await esperar(() => casillas().length === 2);

      // La acción va fuera de la lista y está inactiva hasta seleccionar.
      expect(raiz.querySelector(".ordenes li button")).to.be.null;
      expect(boton().disabled).to.be.true;
      const [sinResultado, conResultado] = casillas();
      expect(conResultado.disabled).to.be.true;
      expect(conResultado.closest("li").textContent).to.contain("Con resultado");
      // La lista no repite «Examen:», igual que el ticket.
      expect(sinResultado.closest("li").textContent).to.contain("Radiografía periapical");
      expect(raiz.querySelector(".ordenes").textContent).not.to.contain("Examen:");

      sinResultado.click();
      expect(boton().disabled).to.be.false;
      expect(boton().textContent).to.contain("(1)");

      const borrados = () => fetch.getCalls().filter((llamada) => llamada.args[1]?.method === "DELETE");
      boton().click();
      expect(await responderDialogo(false)).to.contain("«Radiografía periapical»");
      expect(borrados()).to.have.length(0);
      await esperar(() => !boton().hasAttribute("aria-busy"));

      boton().click();
      expect(await responderDialogo(true)).to.contain("no se puede deshacer");
      await esperar(() => borrados().length === 1);
      expect(borrados()[0].args[0]).to.contain("/ordenes-examen/400");
      await esperar(() => casillas().length === 1);
      expect(raiz.querySelector(".ordenes").textContent).to.contain("Hemograma");
    });

    it("app-consulta finaliza la consulta registrando la fecha de cierre", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='finalizar']"));
      vista.shadowRoot.querySelector("[data-accion='finalizar']").click();
      const texto = await responderDialogo(true);
      expect(texto).to.contain("Finalizar la consulta");
      // La consulta de prueba tiene 2 de 3 pasos sin completar: el diálogo lo advierte.
      expect(texto).to.contain("Quedan 2 pasos sin completar");
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("/consultas/")
        )
      );
      const llamada = fetch
        .getCalls()
        .find((item) => item.args[1]?.method === "PUT" && item.args[0].includes("/consultas/"));
      expect(JSON.parse(llamada.args[1].body).fechaFin).to.be.a("string");
    });

    it("app-consulta muestra qué se registró en cada paso con la misma línea de pasos del dashboard", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector(".proc linea-pasos"));
      // El mismo historial en jerarquía que las cards del dashboard, sin duplicarlo en otro formato.
      expect(vista.shadowRoot.querySelectorAll(".stepper, .registro-pasos")).to.have.length(0);
      const linea = vista.shadowRoot.querySelector(".proc linea-pasos").shadowRoot;
      expect(linea.querySelector(".paso.completado").textContent).to.contain("Evaluación");
      expect(linea.querySelector(".paso.completado details.registro").textContent).to.contain("Elena Martínez");
    });

    it("app-consulta inicia, asigna exámenes y completa un paso", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='iniciar']"));

      vista.shadowRoot.querySelector("[data-accion='iniciar']").click();
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));
      const creacion = fetch.getCalls().find(
        (llamada) => (llamada.args[1]?.method ?? "GET") === "POST" && llamada.args[0].includes("consultas-procedimiento-paso")
      );
      expect(creacion).to.not.be.undefined;
      expect(JSON.parse(creacion.args[1].body).estado).to.equal("EN_PROCESO");

      const buscador = vista.shadowRoot.querySelector("[data-examen-buscador]");
      buscador.value = 20;
      buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
      vista.shadowRoot.querySelector("[data-accion='asignar']").click();
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => (llamada.args[1]?.method ?? "GET") === "POST" && llamada.args[0].includes("ordenes-examen")
        )
      );
      const orden = fetch.getCalls().find((llamada) => llamada.args[0].includes("ordenes-examen") && llamada.args[1]?.method === "POST");
      expect(JSON.parse(orden.args[1].body).indicaciones).to.contain("Radiografía periapical");
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));

      vista.shadowRoot.querySelector("[data-campo='diagnostico']").value = "K02.1 · Caries de la dentina";
      vista.shadowRoot.querySelector("[data-accion='completar']").click();
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
        )
      );
      await esperar(() => vista.shadowRoot.textContent.includes("Paso actual: Alta"));
      expect(vista.shadowRoot.querySelector(".paso-cabecera").textContent).to.contain("Paso 3 de 3");
      // El registro queda en el paso completado y el siguiente empieza sin el diagnóstico anterior.
      const observaciones = JSON.parse(datos.consultasProcedimiento[0].observaciones);
      expect(observaciones.pasos["7001"].diagnostico).to.equal("K02.1 · Caries de la dentina");
      expect(observaciones.diagnostico).to.equal("");
      // El paso de enfermería solo admite notas: sin diagnóstico, plan ni exámenes.
      expect(vista.shadowRoot.querySelector("[data-campo='diagnostico']")).to.be.null;
      expect(vista.shadowRoot.querySelector("[data-campo='plan']")).to.be.null;
      expect(vista.shadowRoot.querySelector("details.examenes")).to.be.null;
      expect(vista.shadowRoot.querySelector("[data-campo='notas']").value).to.equal("");
    });

    it("app-consulta deja un paso en espera cuando el rol de la sesión no es el responsable", async () => {
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3001,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        fechaFin: "2026-11-12T10:00:00-06:00",
        estado: "FINALIZADO",
      });
      const sesion = { ...SESION_ENFERMERO, idRol: 2, roles: [SESION_ENFERMERO.roles[1]] };
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(sesion));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector(".paso-panel"));
      const panel = vista.shadowRoot.querySelector(".paso-panel");
      expect(panel.classList.contains("espera")).to.be.true;
      expect(panel.textContent).to.contain("Paso en espera");
      expect(panel.textContent).to.contain("Enfermero");
      // Sin formulario ni acciones: el paso espera al rol que le corresponde.
      expect(vista.shadowRoot.querySelector("[data-campo='notas']")).to.be.null;
      expect(vista.shadowRoot.querySelector("[data-accion='iniciar']")).to.be.null;
      expect(vista.shadowRoot.querySelector("[data-accion='completar']")).to.be.null;
    });

    it("app-consulta permite a la enfermera registrar solo signos vitales y completar el paso", async () => {
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3001,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        fechaFin: "2026-11-12T10:00:00-06:00",
        estado: "FINALIZADO",
      });
      const sesion = { ...SESION_ENFERMERO, idRol: 3, roles: [SESION_ENFERMERO.roles[0]] };
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(sesion));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='iniciar']"));
      const raiz = vista.shadowRoot;
      expect(raiz.querySelector("[data-campo='notas']")).to.not.be.null;
      expect(raiz.querySelector("[data-campo='diagnostico']")).to.be.null;
      expect(raiz.querySelector("[data-campo='plan']")).to.be.null;
      expect(raiz.querySelector("details.examenes")).to.be.null;
      expect(raiz.querySelector("label[for='notas-8000']").textContent).to.equal("Signos vitales");

      raiz.querySelector("[data-accion='iniciar']").click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      raiz.querySelector("[data-campo='notas']").value = "TA 118/76, FC 72, T 36.5 °C";
      // Sin diagnóstico CIE-10 el paso de enfermería se completa igual.
      raiz.querySelector("[data-accion='completar']").click();
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
        )
      );
      const observaciones = JSON.parse(datos.consultasProcedimiento[0].observaciones);
      expect(observaciones.pasos["7002"].notas).to.contain("TA 118/76");
      expect(observaciones.pasos["7002"].diagnostico).to.equal("");
    });

    it("app-consulta ofrece volver al panel si la consulta no existe y reintentar si falla la carga", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const inexistente = document.createElement("app-consulta");
      inexistente.setAttribute("id-consulta", "9999");
      document.body.append(inexistente);
      await esperar(() => inexistente.shadowRoot.textContent.includes("no existe"));
      const navegaciones = [];
      inexistente.addEventListener("navegar", (evento) => navegaciones.push(evento.detail));
      inexistente.shadowRoot.querySelector("[data-accion='volver']").click();
      expect(navegaciones).to.deep.equal(["dashboard"]);

      limpiarCache();
      fetch.rejects(new TypeError("Failed to fetch"));
      const sinRed = document.createElement("app-consulta");
      sinRed.setAttribute("id-consulta", "5000");
      document.body.append(sinRed);
      await esperar(() => sinRed.shadowRoot.querySelector("[data-accion='reintentar']"));
      sinon.restore();
      stubApi(datos);
      sinRed.shadowRoot.querySelector("[data-accion='reintentar']").click();
      await esperar(() => sinRed.shadowRoot.querySelector(".proc"));
    });

    it("app-consulta ignora el doble clic mientras guarda: un solo paso iniciado", async () => {
      datos.consultasProcedimientoPaso = [];
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      const boton = raiz.querySelector("[data-accion='iniciar']");
      boton.click();
      expect(boton.disabled).to.be.true;
      boton.click();
      await esperar(() => raiz.querySelector("[data-accion='completar']"));
      const altas = fetch.getCalls().filter(
        (llamada) => llamada.args[1]?.method === "POST" && llamada.args[0].includes("consultas-procedimiento-paso")
      );
      expect(altas).to.have.length(1);
      expect(datos.consultasProcedimientoPaso).to.have.length(1);
    });

    it("app-consulta al retroceder muestra en el formulario lo que ya tenía registrado ese paso", async () => {
      datos.consultasProcedimiento[0].observaciones = JSON.stringify({
        notas: "",
        diagnostico: "",
        plan: "",
        pasos: { 7001: { notas: "Placa en molares", diagnostico: "K03.6 · Depósitos en los dientes", plan: "Control" } },
      });
      datos.consultasProcedimientoPaso.push({
        idConsultaProcedimientoPaso: 9001,
        idConsultaProcedimiento: 8000,
        idPersonaRol: 3000,
        fechaInicio: "2026-11-12T09:40:00-06:00",
        estado: "FINALIZADO",
      });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      expect(raiz.querySelector("[data-campo='notas']").value).to.equal("");
      raiz.querySelector("[data-accion='retroceder']").click();
      await responderDialogo(true);
      await esperar(() => raiz.textContent.includes("Paso actual: Profilaxis"));
      expect(raiz.querySelector("[data-campo='notas']").value).to.equal("Placa en molares");
      expect(raiz.querySelector("[data-campo='diagnostico']").value).to.equal("K03.6 · Depósitos en los dientes");
    });

    it("app-consulta sin conexión inicia, asigna y completa un paso, lo muestra en pantalla y lo envía con los mismos ids al volver la red", async () => {
      datos.consultasProcedimientoPaso = [];
      datos.procedimientosPasoExamen.push({
        idProcedimientoPasoExamen: 3,
        idProcedimientoPaso: 7000,
        idExamen: 20,
        activo: true,
      });
      const vista = await abrirConsulta();
      const raiz = vista.shadowRoot;
      await esperar(() => raiz.querySelector("[data-accion='iniciar']"));
      sinRed();
      try {
        raiz.querySelector("[data-accion='iniciar']").click();
        await esperar(() => raiz.querySelector("[data-accion='completar']"));
        await esperar(() => {
          const asignar = raiz.querySelector("[data-accion='asignar']");
          return asignar && !asignar.disabled;
        });

        const buscador = raiz.querySelector("[data-examen-buscador]");
        buscador.value = 20;
        buscador.dispatchEvent(new dom.window.Event("change", { bubbles: true }));
        raiz.querySelector("[data-accion='asignar']").click();
        await esperar(() => raiz.querySelector(".ordenes input[name='orden']"));
        expect(raiz.querySelector(".ordenes").textContent).to.contain("Radiografía periapical");

        raiz.querySelector("[data-campo='diagnostico']").value = "K02.1 · Caries de la dentina";
        raiz.querySelector("[data-accion='completar']").click();
        await esperar(() => raiz.textContent.includes("Paso actual: Profilaxis"));

        const escrituras = fetch.getCalls().filter((llamada) => (llamada.args[1]?.method ?? "GET") !== "GET");
        expect(escrituras).to.have.length(0);
        const cola = listarPendientes();
        const [paso, orden] = cola;
        const idPaso = paso.cuerpo.idConsultaProcedimientoPaso;
        expect(idPaso).to.match(/^[0-9a-f-]{36}$/);
        expect(orden.cuerpo.idOrdenExamen).to.match(/^[0-9a-f-]{36}$/);
        expect(orden.cuerpo.idConsultaProcedimientoPaso).to.equal(idPaso);
        // El cierre del paso apunta al id creado sin conexión, no a uno indefinido.
        expect(cola.map((pendiente) => `${pendiente.metodo} ${pendiente.ruta}`)).to.include(
          `PUT consultas-procedimiento-paso/${idPaso}`
        );

        conRed();
        expect(await sincronizar(ejecutarPendiente)).to.deep.equal({ enviadas: cola.length, pendientes: 0 });
        expect(datos.consultasProcedimientoPaso).to.have.length(1);
        expect(datos.consultasProcedimientoPaso[0]).to.include({ idConsultaProcedimientoPaso: idPaso, estado: "FINALIZADO" });
        expect(datos.ordenesExamen[0].idConsultaProcedimientoPaso).to.equal(idPaso);
      } finally {
        conRed();
        localStorage.clear();
        delete globalThis.localStorage;
      }
    });

    it("app-consulta conserva el foco y lo escrito si la pantalla se actualiza mientras se escribe", async () => {
      const vista = await abrirConsulta();
      const notas = vista.shadowRoot.querySelector('textarea[data-campo="notas"]');
      notas.focus();
      notas.value = "Paciente refiere sensibilidad";
      // Por ejemplo, al enviarse los cambios que estaban pendientes sin conexión.
      dom.window.dispatchEvent(new dom.window.Event("datos-sincronizados"));
      await esperar(() => vista.shadowRoot.querySelector('textarea[data-campo="notas"]') !== notas);
      const nueva = vista.shadowRoot.querySelector('textarea[data-campo="notas"]');
      expect(vista.shadowRoot.activeElement).to.equal(nueva);
      expect(nueva.value).to.equal("Paciente refiere sensibilidad");
    });

    it("app-consulta retrocede al paso anterior tras confirmar en un diálogo propio, sin borrar su registro", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='retroceder']"));
      const anterior = vista.shadowRoot.querySelector("[data-accion='retroceder']");
      anterior.focus();
      anterior.click();
      expect(await responderDialogo(false)).to.contain("Evaluación");
      await esperar(() => !anterior.hasAttribute("aria-busy"));
      expect(pasoActualizado()).to.have.length(0);
      // Al cerrar, el foco vuelve al botón que abrió el diálogo.
      expect(vista.shadowRoot.activeElement).to.equal(anterior);

      vista.shadowRoot.querySelector("[data-accion='retroceder']").click();
      await responderDialogo(true);
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
        )
      );
      const llamada = fetch
        .getCalls()
        .find(
          (item) => item.args[1]?.method === "PUT" && item.args[0].includes("consultas-procedimiento-paso")
        );
      expect(JSON.parse(llamada.args[1].body).estado).to.equal("PENDIENTE");
      expect(
        fetch.getCalls().some((item) => item.args[1]?.method === "DELETE")
      ).to.be.false;
      await esperar(() => vista.shadowRoot.textContent.includes("Paso actual: Evaluación"));
    });

    it("app-consulta guarda notas y no completa el paso sin un diagnóstico CIE-10 del catálogo", async () => {
      sessionStorage.setItem("galenosv.sesion", JSON.stringify(SESION_ENFERMERO));
      const vista = document.createElement("app-consulta");
      vista.setAttribute("id-consulta", "5000");
      document.body.append(vista);
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='guardar-notas']"));

      vista.shadowRoot.querySelector("[data-campo='notas']").value = "Evolución favorable";
      vista.shadowRoot.querySelector("[data-campo='plan']").value = "Control en 7 días";
      vista.shadowRoot.querySelector("[data-accion='guardar-notas']").click();
      await esperar(() =>
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento/")
        )
      );
      const guardado = fetch.getCalls().find(
        (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento/")
      );
      const observaciones = JSON.parse(JSON.parse(guardado.args[1].body).observaciones);
      expect(observaciones.notas).to.equal("Evolución favorable");
      expect(observaciones.plan).to.equal("Control en 7 días");

      vista.shadowRoot.querySelector("[data-accion='iniciar']").click();
      await esperar(() => vista.shadowRoot.querySelector("[data-accion='completar']"));
      vista.shadowRoot.querySelector("[data-campo='diagnostico']").value = "ZZZ.9";
      const notificaciones = [];
      vista.addEventListener("notificar", (evento) => notificaciones.push(evento.detail.mensaje));
      vista.shadowRoot.querySelector("[data-accion='completar']").click();
      await esperar(() => notificaciones.some((mensaje) => mensaje.includes("CIE-10")));
      expect(
        fetch.getCalls().some(
          (llamada) => llamada.args[1]?.method === "PUT" && llamada.args[0].includes("consultas-procedimiento-paso")
        )
      ).to.be.false;
    });
  });
});
