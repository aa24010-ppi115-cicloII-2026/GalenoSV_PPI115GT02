const ANCHO = 595;
const ALTO = 842;
const MARGEN = 48;
const INTERLINEA = 15;
const MAX_CARACTERES = 88;
const MAX_LINEAS_PAGINA = 46;

const SUSTITUCIONES = {
  "–": "-",
  "—": "-",
  "“": '"',
  "”": '"',
  "‘": "'",
  "’": "'",
  "…": "...",
  "\t": "    ",
};

export function limpiarTexto(texto) {
  let salida = "";
  for (const caracter of String(texto ?? "")) {
    if (SUSTITUCIONES[caracter]) {
      salida += SUSTITUCIONES[caracter];
    } else if (caracter.charCodeAt(0) >= 32 && caracter.charCodeAt(0) <= 255) {
      salida += caracter;
    } else {
      salida += " ";
    }
  }
  return salida;
}

export function envolver(texto, limite = MAX_CARACTERES) {
  const limpio = limpiarTexto(texto);
  if (!limpio) return [""];
  const palabras = limpio.split(/\s+/);
  const lineas = [];
  let actual = "";
  for (const palabra of palabras) {
    if (!actual) {
      actual = palabra;
    } else if (`${actual} ${palabra}`.length <= limite) {
      actual = `${actual} ${palabra}`;
    } else {
      lineas.push(actual);
      actual = palabra;
    }
    while (actual.length > limite) {
      lineas.push(actual.slice(0, limite));
      actual = actual.slice(limite);
    }
  }
  if (actual) lineas.push(actual);
  return lineas.length > 0 ? lineas : [""];
}

export function escapar(texto) {
  return String(texto ?? "").replace(/\\/g, "\\\\").replace(/\(/g, "\\(").replace(/\)/g, "\\)");
}

export function nombreArchivo(texto) {
  const limpio = limpiarTexto(texto).replace(/[^A-Za-z0-9]+/g, "-").replace(/^-|-$/g, "");
  return `${limpio || "ticket"}.pdf`;
}

function prepararLineas(titulo, lineas) {
  const items = [{ texto: limpiarTexto(titulo), negrita: true, tamano: 16 }];
  items.push({ texto: "=".repeat(60), negrita: false, tamano: 11 });
  for (const linea of lineas) {
    for (const parte of envolver(linea)) {
      items.push({ texto: parte, negrita: false, tamano: 11 });
    }
  }
  const paginas = [];
  for (let indice = 0; indice < items.length; indice += MAX_LINEAS_PAGINA) {
    paginas.push(items.slice(indice, indice + MAX_LINEAS_PAGINA));
  }
  return paginas.length > 0 ? paginas : [[]];
}

export function construirPdf(titulo, lineas = []) {
  const paginas = prepararLineas(titulo, lineas);
  const idPaginas = paginas.map((_, indice) => 3 + indice * 2);
  const idFuenteNormal = 3 + paginas.length * 2;
  const idFuenteNegrita = idFuenteNormal + 1;

  const secuencia = [
    { id: 1, contenido: "<< /Type /Catalog /Pages 2 0 R >>" },
    {
      id: 2,
      contenido: `<< /Type /Pages /Kids [${idPaginas.map((id) => `${id} 0 R`).join(" ")}] /Count ${paginas.length} >>`,
    },
  ];

  paginas.forEach((pagina, indice) => {
    const idPagina = idPaginas[indice];
    const idContenido = idPagina + 1;
    let flujo = "BT\n";
    let y = ALTO - MARGEN - 20;
    for (const item of pagina) {
      flujo += `${item.negrita ? "/F2" : "/F1"} ${item.tamano} Tf\n`;
      flujo += `1 0 0 1 ${MARGEN} ${y} Tm\n`;
      flujo += `(${escapar(item.texto)}) Tj\n`;
      y -= item.negrita ? INTERLINEA + 6 : INTERLINEA;
    }
    flujo += "ET\n";
    secuencia.push({
      id: idPagina,
      contenido: `<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${ANCHO} ${ALTO}] /Resources << /Font << /F1 ${idFuenteNormal} 0 R /F2 ${idFuenteNegrita} 0 R >> >> /Contents ${idContenido} 0 R >>`,
    });
    secuencia.push({
      id: idContenido,
      contenido: `<< /Length ${flujo.length} >>\nstream\n${flujo}endstream`,
    });
  });

  secuencia.push({
    id: idFuenteNormal,
    contenido: "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>",
  });
  secuencia.push({
    id: idFuenteNegrita,
    contenido: "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>",
  });
  secuencia.sort((a, b) => a.id - b.id);

  const cuerpo = [];
  let desplazamiento = 0;
  const escribir = (texto) => {
    cuerpo.push(texto);
    desplazamiento += texto.length;
  };

  escribir("%PDF-1.4\n%âãÏÓ\n");
  const inicioXref = desplazamiento;
  const offsets = [];
  for (const item of secuencia) {
    offsets[item.id] = desplazamiento;
    escribir(`${item.id} 0 obj\n${item.contenido}\nendobj\n`);
  }
  const totalObjetos = secuencia[secuencia.length - 1].id;
  escribir(
    `xref\n0 ${totalObjetos + 1}\n0000000000 65535 f \n${offsets
      .slice(1)
      .map((valor) => `${String(valor).padStart(10, "0")} 00000 n \n`)
      .join("")}`
  );
  escribir(`trailer\n<< /Size ${totalObjetos + 1} /Root 1 0 R >>\nstartxref\n${inicioXref}\n%%EOF\n`);

  const texto = cuerpo.join("");
  const bytes = new Uint8Array(texto.length);
  for (let indice = 0; indice < texto.length; indice++) {
    bytes[indice] = texto.charCodeAt(indice) & 0xff;
  }
  return bytes;
}

export function descargarPdf(nombre, titulo, lineas) {
  const bytes = construirPdf(titulo, lineas);
  const blob = new Blob([bytes], { type: "application/pdf" });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement("a");
  enlace.href = url;
  enlace.download = nombreArchivo(nombre);
  document.body.append(enlace);
  enlace.click();
  enlace.remove();
  setTimeout(() => URL.revokeObjectURL(url), 2000);
  return bytes.length;
}
