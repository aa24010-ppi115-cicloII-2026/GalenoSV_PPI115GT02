import { existsSync, readFileSync } from "node:fs";
import vm from "node:vm";
import { expect } from "chai";

const manifest = JSON.parse(readFileSync(new URL("../manifest.json", import.meta.url), "utf8"));
const serviceWorker = readFileSync(new URL("../service-worker.js", import.meta.url), "utf8");
const index = readFileSync(new URL("../index.html", import.meta.url), "utf8");
const cssComponentes = [
  "base", "app-busqueda", "app-consulta", "app-dashboard", "app-login", "app-shell",
  "asistente-citas", "cita-card", "linea-pasos", "pwa-instalar", "ticket-cita",
  "ui-autocompletar", "ui-confirmar", "ui-toast",
];

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
<<<<<<< HEAD
    expect(serviceWorker).to.match(/const CACHE = "galenosv-v\d+"/);
=======
>>>>>>> v2modificada
    expect(serviceWorker).to.contain('"./js/componentes/app-shell.js"');
    expect(serviceWorker).to.contain('"./js/componentes/asistente-citas.js"');
    expect(serviceWorker).to.contain('"./js/componentes/ui-autocompletar.js"');
    expect(serviceWorker).to.contain('"./js/componentes/ui-confirmar.js"');
    expect(serviceWorker).to.contain('"./js/componentes/linea-pasos.js"');
<<<<<<< HEAD
=======
    expect(serviceWorker).to.contain('"./js/componentes/reutilizables.js"');
    expect(serviceWorker).to.contain('"./js/presentacion.js"');
>>>>>>> v2modificada
    expect(serviceWorker).to.contain('"./js/datos.js"');
    expect(serviceWorker).to.contain('"./js/texto.js"');
    expect(serviceWorker).to.contain('"./js/fechas.js"');
    expect(serviceWorker).to.contain('"./js/pendientes.js"');
    expect(serviceWorker).to.contain('"./js/recursos.js"');
    expect(serviceWorker).to.contain('addEventListener("fetch"');
    expect(serviceWorker).to.contain('marcarSinConexion: true');
    expect(serviceWorker).to.contain('cabeceras.set("X-Sin-Conexion", "true")');
    expect(serviceWorker).to.contain("status: 503");
    expect(serviceWorker).to.contain('self.skipWaiting()');
    expect(serviceWorker).to.contain('clients.claim()');
  });

  it("usa hojas externas por componente y las precarga para la primera visita sin conexión", () => {
    for (const nombre of cssComponentes) {
      expect(existsSync(new URL(`../css/componentes/${nombre}.css`, import.meta.url)), nombre).to.be.true;
      expect(serviceWorker).to.contain(`./css/componentes/${nombre}.css`);
    }
  });

  it("tiene colores y orientación definidos", () => {
    expect(manifest.theme_color).to.match(/^#[0-9a-f]{6}$/i);
    expect(manifest.background_color).to.match(/^#[0-9a-f]{6}$/i);
    expect(manifest.orientation).to.equal("portrait-primary");
  });
});

describe("actualización de la PWA", () => {
  const swFalso = (controller) => {
    const oyentes = [];
    return {
      controller,
      addEventListener: (tipo, oyente) => oyentes.push({ tipo, oyente }),
      emitir: (tipo) => oyentes.filter((o) => o.tipo === tipo).forEach((o) => o.oyente()),
    };
  };

  it("recarga una sola vez cuando una versión nueva toma el control de la página", async () => {
    const { recargarAlActualizar } = await import("../js/pwa.js");
    const sw = swFalso({});
    let recargas = 0;
    recargarAlActualizar(sw, () => recargas++);
    sw.emitir("controllerchange");
    sw.emitir("controllerchange");
    expect(recargas).to.equal(1);
  });

  it("no recarga en la primera instalación, cuando aún no había versión anterior", async () => {
    const { recargarAlActualizar } = await import("../js/pwa.js");
    const sw = swFalso(null);
    let recargas = 0;
    recargarAlActualizar(sw, () => recargas++);
    sw.emitir("controllerchange");
    expect(recargas).to.equal(0);
  });
});

describe("comportamiento offline del service worker", () => {
  function cargarServiceWorker(obtener, respuestaCacheada = null, coincidencias = null) {
    const oyentes = {};
    const guardadas = [];
    const cache = {
      put: async (peticion, respuesta) => guardadas.push({ peticion, respuesta }),
    };
    const contexto = {
      Request,
      Response,
      Headers,
      URL,
      console: { warn() {} },
      fetch: obtener,
      caches: {
        match: async (peticion) => coincidencias ? coincidencias(peticion) : respuestaCacheada,
        open: async () => cache,
        keys: async () => [],
        delete: async () => true,
      },
      self: {
        location: { origin: "http://localhost:3000" },
        addEventListener: (tipo, oyente) => { oyentes[tipo] = oyente; },
        skipWaiting: async () => {},
        clients: { claim: async () => {} },
      },
    };
    vm.runInNewContext(serviceWorker, contexto);
    return { oyentes, guardadas };
  }

  async function resolverPeticion(oyente, peticion) {
    let respuesta;
    oyente({ request: peticion, respondWith: (promesa) => { respuesta = promesa; } });
    return respuesta;
  }

  it("devuelve la copia cacheada y marca que la respuesta no proviene del servidor", async () => {
    const cacheada = new Response(JSON.stringify([{ idRol: 1 }]), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
    const { oyentes } = cargarServiceWorker(async () => { throw new Error("sin red"); }, cacheada);

    const respuesta = await resolverPeticion(
      oyentes.fetch,
      new Request("http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api/roles?first=0&max=100")
    );

    expect(respuesta.status).to.equal(200);
    expect(respuesta.headers.get("X-Sin-Conexion")).to.equal("true");
    expect(await respuesta.json()).to.deep.equal([{ idRol: 1 }]);
  });

  it("devuelve 503 cuando la API falla y no existe una copia local", async () => {
    const { oyentes } = cargarServiceWorker(async () => { throw new Error("sin red"); });

    const respuesta = await resolverPeticion(
      oyentes.fetch,
      new Request("http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api/roles?first=0&max=100")
    );

    expect(respuesta.status).to.equal(503);
    expect(await respuesta.json()).to.have.property("mensaje");
  });

  it("guarda una respuesta exitosa de la API para futuras consultas", async () => {
    const respuestaApi = new Response("[]", { status: 200 });
    const { oyentes, guardadas } = cargarServiceWorker(async () => respuestaApi.clone());

    const respuesta = await resolverPeticion(
      oyentes.fetch,
      new Request("http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api/roles?first=0&max=100")
    );

    expect(respuesta.status).to.equal(200);
    expect(guardadas).to.have.length(1);
  });

  it("usa index.html como respaldo de la navegación sin marcar la API como caída", async () => {
    const pagina = new Response("<html>GalenoSV</html>", { status: 200 });
    const { oyentes } = cargarServiceWorker(
      async () => { throw new Error("sin red"); },
      null,
      (peticion) => String(peticion) === "./index.html" ? pagina.clone() : null
    );

    const respuesta = await resolverPeticion(
      oyentes.fetch,
      { url: "http://localhost:3000/#/dashboard", method: "GET", mode: "navigate" }
    );

    expect(respuesta.status).to.equal(200);
    expect(await respuesta.text()).to.equal("<html>GalenoSV</html>");
    expect(respuesta.headers.get("X-Sin-Conexion")).to.be.null;
  });
});
