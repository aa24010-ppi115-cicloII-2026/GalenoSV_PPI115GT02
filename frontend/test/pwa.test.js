import { existsSync, readFileSync } from "node:fs";
import { expect } from "chai";

const manifest = JSON.parse(readFileSync(new URL("../manifest.json", import.meta.url), "utf8"));
const serviceWorker = readFileSync(new URL("../service-worker.js", import.meta.url), "utf8");
const index = readFileSync(new URL("../index.html", import.meta.url), "utf8");

describe("manifest PWA", () => {
  it("declara nombre, nombre corto e identificador", () => {
    expect(manifest.name).to.be.a("string").and.not.empty;
    expect(manifest.short_name).to.be.a("string").and.not.empty;
    expect(manifest.id).to.equal("/");
  });

  it("arranca en index.html y se muestra standalone", () => {
    expect(manifest.start_url).to.equal("./index.html");
    expect(manifest.display).to.equal("standalone");
    expect(manifest.scope).to.equal("./");
  });

  it("incluye íconos de 192 y 512 píxeles existentes", () => {
    const tamanos = manifest.icons.map((icono) => icono.sizes);
    expect(tamanos).to.include("192x192");
    expect(tamanos).to.include("512x512");
    for (const icono of manifest.icons) {
      expect(existsSync(new URL(`../${icono.src}`, import.meta.url)), icono.src).to.be.true;
    }
  });

  it("incluye capturas para escritorio y móvil existentes", () => {
    const formatos = manifest.screenshots.map((captura) => captura.form_factor);
    expect(formatos).to.include("wide");
    expect(formatos).to.include("narrow");
    for (const captura of manifest.screenshots) {
      expect(existsSync(new URL(`../${captura.src}`, import.meta.url)), captura.src).to.be.true;
    }
  });

  it("registra el service worker y precarga los módulos de la SPA", () => {
    expect(index).to.contain('type="module" src="js/app.js"');
    expect(index).to.contain("<app-shell>");
    expect(index).to.contain("<pwa-instalar>");
    expect(serviceWorker).to.match(/const CACHE = "galenosv-v\d+"/);
    expect(serviceWorker).to.contain('"./js/componentes/app-shell.js"');
    expect(serviceWorker).to.contain('"./js/datos.js"');
    expect(serviceWorker).to.contain('"./js/pdf.js"');
    expect(serviceWorker).to.contain('"./js/pendientes.js"');
    expect(serviceWorker).to.contain('addEventListener("fetch"');
    expect(serviceWorker).to.contain('datosPrimero');
    expect(serviceWorker).to.contain('self.skipWaiting()');
    expect(serviceWorker).to.contain('clients.claim()');
  });

  it("tiene colores y orientación definidos", () => {
    expect(manifest.theme_color).to.match(/^#[0-9a-f]{6}$/i);
    expect(manifest.background_color).to.match(/^#[0-9a-f]{6}$/i);
    expect(manifest.orientation).to.equal("portrait-primary");
  });
});
