import { expect } from "chai";
import { construirPdf, envolver, limpiarTexto, escapar, nombreArchivo } from "../js/pdf.js";

const decodificar = (bytes) => Buffer.from(bytes).toString("latin1");

describe("pdf.js", () => {
  it("genera un PDF con cabecera, objetos, xref y cierre", () => {
    const bytes = construirPdf("GalenoSV - Ticket", ["Paciente: Ana Lopez"]);
    const texto = decodificar(bytes);
    expect(texto.startsWith("%PDF-1.4")).to.be.true;
    expect(texto).to.contain("/Type /Catalog");
    expect(texto).to.contain("/Type /Pages");
    expect(texto).to.contain("/Type /Page");
    expect(texto).to.contain("BaseFont /Helvetica");
    expect(texto).to.contain("xref");
    expect(texto).to.contain("trailer");
    expect(texto.trimEnd().endsWith("%%EOF")).to.be.true;
  });

  it("incluye el título y las líneas solicitadas", () => {
    const texto = decodificar(construirPdf("Comprobante de cita", ["Linea uno", "Linea dos"]));
    expect(texto).to.contain("(Comprobante de cita) Tj");
    expect(texto).to.contain("(Linea uno) Tj");
    expect(texto).to.contain("(Linea dos) Tj");
  });

  it("pagina cuando hay demasiadas líneas", () => {
    const lineas = Array.from({ length: 120 }, (_, indice) => `Linea ${indice}`);
    const texto = decodificar(construirPdf("Titulo", lineas));
    const paginas = (texto.match(/\/Type \/Page[^s]/g) ?? []).length;
    expect(paginas).to.be.greaterThan(1);
    expect(texto).to.contain("/Count 3");
  });

  it("escapa los caracteres especiales del contenido", () => {
    expect(escapar("a (b) c")).to.equal("a \\(b\\) c");
    expect(escapar("a\\b")).to.equal("a\\\\b");
  });

  it("envuelve los textos largos en varias líneas", () => {
    const lineas = envolver("palabra ".repeat(40), 30);
    expect(lineas.length).to.be.greaterThan(1);
    for (const linea of lineas) expect(linea.length).to.be.at.most(30);
  });

  it("normaliza caracteres no soportados por la tipografía base", () => {
    expect(limpiarTexto("Cita — prueba")).to.contain("-");
    expect(limpiarTexto(" emojis 😀 ")).to.not.contain("😀");
  });

  it("arma un nombre de archivo seguro", () => {
    expect(nombreArchivo("Ticket 5000")).to.equal("Ticket-5000.pdf");
    expect(nombreArchivo("")).to.equal("ticket.pdf");
  });
});
