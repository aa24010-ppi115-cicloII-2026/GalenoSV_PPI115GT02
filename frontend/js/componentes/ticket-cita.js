import { Vista, escapar, fechaHora } from "../base.js";
import { leerObservaciones, motivoDe, estadoDeCita } from "../datos.js";
import { descargarPdf } from "../pdf.js";
import { pedirBinario, estaConectado } from "../api.js";

export function documentoImprimible(cita) {
  const pasos = cita.procedimientos.flatMap(({ procedimiento, pasos: lista }) =>
    lista.map(({ paso, registro, estado }) => ({
      procedimiento: procedimiento?.nombre ?? "Procedimiento",
      nombre: paso.nombre,
      estado,
      fecha: registro?.fechaInicio ? fechaHora(registro.fechaInicio) : "Pendiente",
      observaciones: registro?.observaciones ?? "",
    }))
  );
  const notas = cita.procedimientos
    .map(({ procedimiento, consultaProcedimiento }) => ({
      nombre: procedimiento?.nombre ?? "Procedimiento",
      ...leerObservaciones(consultaProcedimiento?.observaciones),
    }))
    .filter((item) => item.notas || item.diagnostico || item.plan);
  return `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <title>Ticket de cita ${escapar(cita.consulta.idConsulta)}</title>
  <style>
    body { font-family: system-ui, "Segoe UI", sans-serif; margin: 2rem; color: #14202b; }
    header { border-bottom: 3px solid #0f7b6c; padding-bottom: 0.7rem; margin-bottom: 1rem; }
    h1 { font-size: 1.25rem; margin: 0; }
    .codigo { color: #0f7b6c; font-weight: 700; }
    .datos { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem 1rem; font-size: 0.9rem; margin: 1rem 0; }
    .datos span { color: #587089; display: block; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.04em; }
    h2 { font-size: 0.95rem; margin: 1rem 0 0.4rem; color: #2c4a63; }
    table { width: 100%; border-collapse: collapse; font-size: 0.82rem; }
    th, td { text-align: left; border-bottom: 1px solid #d7dee7; padding: 0.35rem 0.4rem; vertical-align: top; }
    th { background: #eef5f2; color: #2c4a63; }
    footer { margin-top: 1.5rem; font-size: 0.72rem; color: #587089; }
    @media print { body { margin: 0.8rem; } }
  </style>
</head>
<body>
  <header>
    <h1>GalenoSV · Comprobante de cita</h1>
    <p>Ticket <span class="codigo">#${escapar(cita.consulta.idConsulta)}</span> · ${escapar(fechaHora(cita.consulta.fechaInicio))}</p>
  </header>
  <section class="datos">
    <div><span>Paciente</span>${escapar(`${cita.paciente?.nombres ?? ""} ${cita.paciente?.apellidos ?? ""}`.trim())}</div>
    <div><span>Clínica</span>${escapar(cita.clinica?.nombre ?? "—")}</div>
    <div><span>Motivo</span>${escapar(motivoDe(cita.consulta))}</div>
    <div><span>Estado</span>${escapar(estadoDeCita(cita).texto)}</div>
  </section>
  <h2>Procedimientos y pasos</h2>
  <table>
    <thead><tr><th>Procedimiento</th><th>Paso</th><th>Estado</th><th>Fecha</th><th>Observaciones</th></tr></thead>
    <tbody>
      ${
        pasos
          .map(
            (paso) => `<tr>
              <td>${escapar(paso.procedimiento)}</td>
              <td>${escapar(paso.nombre)}</td>
              <td>${escapar(paso.estado)}</td>
              <td>${escapar(paso.fecha)}</td>
              <td>${escapar(paso.observaciones || "—")}</td>
            </tr>`
          )
          .join("") || '<tr><td colspan="5">Sin pasos registrados.</td></tr>'
      }
    </tbody>
  </table>
  ${
    notas.length > 0
      ? `<h2>Notas de atención</h2>${notas
          .map(
            (item) => `<div style="font-size:0.85rem;margin-bottom:0.6rem">
              <strong>${escapar(item.nombre)}</strong>
              ${item.diagnostico ? `<p>Diagnóstico CIE-10: ${escapar(item.diagnostico)}</p>` : ""}
              ${item.notas ? `<p>Notas: ${escapar(item.notas)}</p>` : ""}
              ${item.plan ? `<p>Plan: ${escapar(item.plan)}</p>` : ""}
            </div>`
          )
          .join("")}`
      : ""
  }
  <footer>Documento generado desde GalenoSV. Use Imprimir → Guardar como PDF para conservar una copia.</footer>
</body>
</html>`;
}

export function lineasTicket(cita) {
  const lineas = [];
  const paciente = `${cita.paciente?.nombres ?? ""} ${cita.paciente?.apellidos ?? ""}`.trim() || "-";
  lineas.push(`Paciente: ${paciente}`);
  lineas.push(`Clinica: ${cita.clinica?.nombre ?? "-"}`);
  lineas.push(`Fecha de la cita: ${fechaHora(cita.consulta.fechaInicio)}`);
  lineas.push(`Motivo: ${motivoDe(cita.consulta)}`);
  lineas.push(`Estado: ${estadoDeCita(cita).texto}`);
  lineas.push(`Ticket: #${cita.consulta.idConsulta}`);
  lineas.push("");
  lineas.push("PROCEDIMIENTOS Y PASOS");
  for (const { procedimiento, pasos } of cita.procedimientos) {
    lineas.push(`- ${procedimiento?.nombre ?? "Procedimiento"}`);
    for (const { paso, registro, persona, estado, notas } of pasos) {
      const fecha = registro?.fechaInicio ? fechaHora(registro.fechaInicio) : "Pendiente";
      const quien = persona ? `${persona.nombres} ${persona.apellidos}` : "Sin asignar";
      lineas.push(`   ${paso.nombre} | ${estado} | ${fecha} | ${quien}`);
      if (notas?.notas) lineas.push(`      Notas: ${notas.notas}`);
      if (notas?.diagnostico) lineas.push(`      Diagnostico CIE-10: ${notas.diagnostico}`);
      if (notas?.plan) lineas.push(`      Plan: ${notas.plan}`);
    }
  }
  const atenciones = cita.procedimientos
    .map(({ procedimiento, notas }) => ({ procedimiento, notas }))
    .filter(({ notas }) => notas && (notas.notas || notas.diagnostico || notas.plan));
  if (atenciones.length > 0) {
    lineas.push("");
    lineas.push("NOTAS DE ATENCION");
    for (const { procedimiento, notas } of atenciones) {
      lineas.push(`- ${procedimiento?.nombre ?? "Procedimiento"}`);
      if (notas.diagnostico) lineas.push(`   Diagnostico CIE-10: ${notas.diagnostico}`);
      if (notas.notas) lineas.push(`   Notas: ${notas.notas}`);
      if (notas.plan) lineas.push(`   Plan: ${notas.plan}`);
    }
  }
  lineas.push("");
  lineas.push("Documento generado desde GalenoSV (aplicacion web progresiva).");
  return lineas;
}

export class TicketCita extends Vista {
  get estilos() {
    return `
      .fondo {
        position: fixed; inset: 0; background: rgba(20, 32, 43, 0.55);
        display: grid; place-items: center; padding: 1rem; z-index: 60;
      }
      .hoja {
        background: #fff; border-radius: 18px; padding: 1.4rem;
        width: min(680px, 100%); max-height: 90vh; overflow: auto;
        box-shadow: 0 24px 60px rgba(20, 32, 43, 0.35);
      }
      header { border-bottom: 3px solid #0f7b6c; padding-bottom: 0.6rem; margin-bottom: 0.8rem; }
      h2 { font-size: 1.15rem; }
      .codigo { color: #0f7b6c; font-weight: 700; }
      .datos { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem 1rem; font-size: 0.88rem; margin: 0.8rem 0; }
      .datos span { color: #587089; display: block; font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.04em; }
      table { width: 100%; border-collapse: collapse; font-size: 0.78rem; }
      th, td { text-align: left; border-bottom: 1px solid #d7dee7; padding: 0.35rem; vertical-align: top; }
      th { background: #eef5f2; color: #2c4a63; }
      .acciones { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
      @media print {
        .fondo { position: static; background: none; padding: 0; }
        .acciones { display: none; }
        .hoja { box-shadow: none; max-height: none; }
      }
    `;
  }

  set cita(valor) {
    this._cita = valor;
    if (this.isConnected) this.pintar();
  }

  get cita() {
    return this._cita;
  }

  disconnectedCallback() {
    document.removeEventListener("keydown", this._tecla);
  }

  connectedCallback() {
    if (!this._escucha) {
      this._escucha = (evento) => {
        const boton = evento.target.closest("button[data-accion]");
        if (!boton) return;
        if (boton.dataset.accion === "cerrar") this.remove();
        if (boton.dataset.accion === "imprimir") this.imprimir();
        if (boton.dataset.accion === "descargar") this.descargar();
      };
      this.shadowRoot.addEventListener("click", this._escucha);
      this._tecla = (evento) => {
        if (evento.key === "Escape") this.remove();
      };
      document.addEventListener("keydown", this._tecla);
    }
    this.pintar();
    this.q("button[data-accion='cerrar']")?.focus();
  }

  pintar() {
    const cita = this._cita;
    if (!cita) {
      this.render("");
      return;
    }
    const pasos = cita.procedimientos.flatMap(({ procedimiento, pasos: lista }) =>
      lista.map(({ paso, registro, estado }) => ({ paso, registro, estado, procedimiento }))
    );
    const notas = cita.procedimientos
      .map(({ procedimiento, consultaProcedimiento }) => ({
        nombre: procedimiento?.nombre ?? "Procedimiento",
        ...leerObservaciones(consultaProcedimiento?.observaciones),
      }))
      .filter((item) => item.notas || item.diagnostico || item.plan);
    this.render(`
      <div class="fondo" role="dialog" aria-modal="true" aria-label="Ticket de la cita">
        <div class="hoja">
          <header>
            <h2>GalenoSV · Comprobante de cita</h2>
            <p class="aviso">Ticket <span class="codigo">#${escapar(cita.consulta.idConsulta)}</span> · ${escapar(fechaHora(cita.consulta.fechaInicio))}</p>
          </header>
          <div class="datos">
            <div><span>Paciente</span>${escapar(`${cita.paciente?.nombres ?? ""} ${cita.paciente?.apellidos ?? ""}`.trim() || "—")}</div>
            <div><span>Clínica</span>${escapar(cita.clinica?.nombre ?? "—")}</div>
            <div><span>Motivo</span>${escapar(motivoDe(cita.consulta))}</div>
            <div><span>Estado</span>${escapar(estadoDeCita(cita).texto)}</div>
          </div>
          <table>
            <thead><tr><th>Procedimiento</th><th>Paso</th><th>Estado</th><th>Fecha</th><th>Observaciones</th></tr></thead>
            <tbody>
              ${
                pasos
                  .map(
                    ({ paso, registro, estado, procedimiento }) => `<tr>
                      <td>${escapar(procedimiento?.nombre ?? "—")}</td>
                      <td>${escapar(paso.nombre)}</td>
                      <td>${escapar(estado)}</td>
                      <td>${escapar(registro?.fechaInicio ? fechaHora(registro.fechaInicio) : "Pendiente")}</td>
                      <td>${escapar(registro?.observaciones || "—")}</td>
                    </tr>`
                  )
                  .join("") || '<tr><td colspan="5">Sin pasos registrados.</td></tr>'
              }
            </tbody>
          </table>
          ${
            notas.length > 0
              ? `<div class="notas">${notas
                  .map(
                    (item) => `<div><strong>${escapar(item.nombre)}</strong>
                      ${item.diagnostico ? `<p>CIE-10: ${escapar(item.diagnostico)}</p>` : ""}
                      ${item.notas ? `<p>Notas: ${escapar(item.notas)}</p>` : ""}
                      ${item.plan ? `<p>Plan: ${escapar(item.plan)}</p>` : ""}
                    </div>`
                  )
                  .join("")}</div>`
              : ""
          }
          <p class="aviso" style="margin-top:0.8rem;font-size:0.75rem">Use Imprimir → Guardar como PDF para conservar una copia del ticket.</p>
          <div class="acciones">
            <button type="button" class="secundario" data-accion="cerrar">Cerrar</button>
            <button type="button" class="secundario" data-accion="imprimir">Imprimir</button>
            <button type="button" data-accion="descargar">Descargar PDF</button>
          </div>
        </div>
      </div>
    `);
  }

  async descargar() {
    if (!this._cita) return;
    const idConsulta = this._cita.consulta.idConsulta;
    try {
      if (estaConectado()) {
        const tamano = await pedirBinario(
          `tickets/consultas/${idConsulta}`,
          `ticket-${idConsulta}.pdf`
        );
        this.notificar(`Ticket PDF descargado (${tamano} bytes).`, "ok");
        return;
      }
      const tamano = descargarPdf(
        `ticket-${idConsulta}`,
        "GalenoSV - Comprobante de cita",
        lineasTicket(this._cita)
      );
      this.notificar(`Ticket PDF generado sin conexión (${tamano} bytes).`, "ok");
    } catch (error) {
      try {
        const tamano = descargarPdf(
          `ticket-${idConsulta}`,
          "GalenoSV - Comprobante de cita",
          lineasTicket(this._cita)
        );
        this.notificar(`Ticket PDF generado en el dispositivo (${tamano} bytes).`, "ok");
      } catch (fallo) {
        this.notificar(`No se pudo generar el PDF: ${fallo.message}`, "error");
      }
    }
  }

  imprimir() {
    if (!this._cita) return;
    const ventana = window.open("", "_blank");
    if (!ventana) {
      this.notificar("Permita las ventanas emergentes para imprimir el ticket.", "error");
      return;
    }
    ventana.document.write(documentoImprimible(this._cita));
    ventana.document.close();
    ventana.focus();
    ventana.print();
  }
}

customElements.define("ticket-cita", TicketCita);
