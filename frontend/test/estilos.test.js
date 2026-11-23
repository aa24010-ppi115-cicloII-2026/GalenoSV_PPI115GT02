import { readFileSync, readdirSync } from "node:fs";
import { expect } from "chai";
import { usarDom } from "./boundary/entorno-dom.js";

const principal = readFileSync(new URL("../css/principal.css", import.meta.url), "utf8");

/** Nombres de las variables de color declaradas en un bloque de principal.css. */
function coloresDe(selector) {
  const inicio = principal.indexOf(`${selector} {`);
  const bloque = principal.slice(inicio, principal.indexOf("}", inicio));
  return [...bloque.matchAll(/(--[\w-]+):\s*#[0-9a-f]{3,8};/gi)].map(([, nombre]) => nombre);
}

describe("estilos · tema oscuro", () => {
  it("cada color del tema claro tiene su valor en el tema oscuro y no se sigue al sistema", () => {
    const claros = coloresDe(":root");
    const oscuros = coloresDe(':root[data-tema="oscuro"]');
    expect(claros).to.have.length.above(15);
    expect(oscuros).to.have.members(claros);
    expect(principal).to.match(/:root\[data-tema="oscuro"\] \{[^}]*color-scheme: dark;/);
    expect(principal).not.to.contain("prefers-color-scheme");
  });

  describe("precarga de hojas", () => {
    // configuracion-estilos.js registra las rutas en la clase base, que necesita un DOM.
    usarDom();

    it("la precarga incluye la hoja de cada componente que existe en css/componentes", async () => {
      const { COMPONENTES_CON_ESTILO } = await import("../js/configuracion-estilos.js");
      const archivos = readdirSync(new URL("../css/componentes/", import.meta.url))
        .filter((archivo) => archivo.endsWith(".css") && archivo !== "base.css")
        .map((archivo) => archivo.replace(/\.css$/, ""));
      expect(COMPONENTES_CON_ESTILO).to.have.members(archivos);
    });
  });
});
