
import { expect } from "chai";
import { listarTodo } from "../../../js/api.js";
import {
  cargarTodo,
  recargarTablas,
  limpiarCache,
} from "../../../js/datos.js";
import { FUENTES } from "../../../js/recursos.js";

const CLAVE_PENDIENTES = "galenosv.pendientes";

let solicitudes;
let datosServidor;
let almacenamientoOriginal;
let fetchOriginal;

function respuesta(datos, total = datos.length) {
  return new Response(JSON.stringify(datos), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "X-Total-Count": String(total),
    },
  });
}

function instalarServidor() {
  solicitudes = [];

  globalThis.fetch = async (url) => {
    const direccion = new URL(url);
    const ruta = direccion.pathname.split("/").at(-1);
    const first = Number(direccion.searchParams.get("first"));
    const max = Number(direccion.searchParams.get("max"));

    solicitudes.push({ ruta, first, max });

    const registros = datosServidor[ruta] ?? [];

    return respuesta(registros.slice(first, first + max), registros.length);
  };
}

function guardarPendientes(pendientes) {
  globalThis.localStorage.setItem(
    CLAVE_PENDIENTES,
    JSON.stringify(pendientes)
  );
}

describe("listarTodo", () => {
  beforeEach(() => {
    fetchOriginal = globalThis.fetch;
    almacenamientoOriginal = Object.getOwnPropertyDescriptor(
      globalThis,
      "localStorage"
    );

    globalThis.localStorage = {
      getItem: () => null,
      setItem: () => {},
      removeItem: () => {},
    };

    datosServidor = {};
    instalarServidor();
  });

  afterEach(() => {
    globalThis.fetch = fetchOriginal;

    if (almacenamientoOriginal) {
      Object.defineProperty(
        globalThis,
        "localStorage",
        almacenamientoOriginal
      );
    } else {
      delete globalThis.localStorage;
    }
  });

  it("devuelve una lista vacía cuando no existen registros", async () => {
    const resultado = await listarTodo("personas");

    expect(resultado).to.deep.equal([]);
    expect(solicitudes).to.have.lengthOf(1);
  });

  it("recupera todos los registros cuando existen varias páginas", async () => {
    datosServidor.personas = Array.from(
      { length: 205 },
      (_, i) => ({ idPersona: i + 1 })
    );

    const resultado = await listarTodo("personas");

    expect(resultado).to.have.lengthOf(205);
    expect(resultado[0].idPersona).to.equal(1);
    expect(resultado[204].idPersona).to.equal(205);
  });

  it("solicita las páginas utilizando el tamaño configurado", async () => {
    datosServidor.personas = Array.from(
      { length: 205 },
      (_, i) => ({ idPersona: i + 1 })
    );

    await listarTodo("personas");

    expect(solicitudes.map(({ first }) => first))
      .to.deep.equal([0, 100, 200]);

    expect(solicitudes.map(({ max }) => max))
      .to.deep.equal([100, 100, 100]);
  });

  it("lanza un error si una página queda incompleta", async () => {
    globalThis.fetch = async (url) => {
      const direccion = new URL(url);
      const first = Number(direccion.searchParams.get("first"));

      if (first === 0) {
        return respuesta(
          Array.from({ length: 100 }, (_, i) => ({ id: i + 1 })),
          150
        );
      }

      return respuesta([], 150);
    };

    try {
      await listarTodo("personas");
      expect.fail("Debió lanzar un error");
    } catch (error) {
      expect(error.message).to.include("quedó incompleta");
    }
  });
});

describe("caché de cargarTodo", () => {
  beforeEach(() => {
    fetchOriginal = globalThis.fetch;
    almacenamientoOriginal = Object.getOwnPropertyDescriptor(
      globalThis,
      "localStorage"
    );

    const almacenamiento = new Map();

    globalThis.localStorage = {
      getItem: (clave) => almacenamiento.get(clave) ?? null,
      setItem: (clave, valor) => almacenamiento.set(clave, valor),
      removeItem: (clave) => almacenamiento.delete(clave),
    };

    datosServidor = {};
    for (const ruta of Object.values(FUENTES)) {
      datosServidor[ruta] = [];
    }

    instalarServidor();
    limpiarCache();
  });

  afterEach(() => {
    limpiarCache();
    globalThis.fetch = fetchOriginal;

    if (almacenamientoOriginal) {
      Object.defineProperty(
        globalThis,
        "localStorage",
        almacenamientoOriginal
      );
    } else {
      delete globalThis.localStorage;
    }
  });

  it("carga todas las fuentes en la primera solicitud", async () => {
    const resultado = await cargarTodo();

    expect(Object.keys(resultado)).to.have.lengthOf(
      Object.keys(FUENTES).length
    );

    expect(solicitudes).to.have.lengthOf(
      Object.keys(FUENTES).length
    );
  });

  it("reutiliza la caché en solicitudes posteriores", async () => {
    datosServidor.personas = [{ idPersona: 1 }];

    const primeraCarga = await cargarTodo();
    const cantidadSolicitudes = solicitudes.length;

    datosServidor.personas = [{ idPersona: 2 }];

    const segundaCarga = await cargarTodo();

    expect(segundaCarga.personas).to.deep.equal([
      { idPersona: 1 },
    ]);

    expect(solicitudes).to.have.lengthOf(cantidadSolicitudes);
    expect(segundaCarga).to.equal(primeraCarga);
  });

  it("comparte una misma carga entre solicitudes simultáneas", async () => {
    const [primera, segunda] = await Promise.all([
      cargarTodo(),
      cargarTodo(),
    ]);

    expect(primera).to.equal(segunda);

    expect(solicitudes).to.have.lengthOf(
      Object.keys(FUENTES).length
    );
  });

  it("recarga únicamente las tablas solicitadas", async () => {
    datosServidor.personas = [{ idPersona: 1 }];
    datosServidor.roles = [{ idRol: 1 }];

    const inicial = await cargarTodo();
    const solicitudesIniciales = solicitudes.length;

    datosServidor.personas = [{ idPersona: 2 }];

    const actualizada = await recargarTablas(["personas"]);

    expect(actualizada.personas).to.deep.equal([
      { idPersona: 2 },
    ]);

    expect(actualizada.roles).to.deep.equal(inicial.roles);

    expect(solicitudes).to.have.lengthOf(
      solicitudesIniciales + 1
    );

    expect(solicitudes.at(-1).ruta).to.equal("personas");
  });

  it("rechaza recursos desconocidos al recargar", async () => {
    try {
      await recargarTablas(["recurso-inexistente"]);
      expect.fail("Debió lanzar un error");
    } catch (error) {
      expect(error.message).to.include("No se pueden recargar");
    }
  });

  it("no realiza solicitudes al recargar una lista vacía si ya hay caché", async () => {
    await cargarTodo();

    const cantidadSolicitudes = solicitudes.length;

    await recargarTablas([]);

    expect(solicitudes).to.have.lengthOf(cantidadSolicitudes);
  });

  it("vuelve a cargar los datos después de limpiar la caché", async () => {
    datosServidor.personas = [{ idPersona: 1 }];

    await cargarTodo();
    const cantidadInicial = solicitudes.length;

    datosServidor.personas = [{ idPersona: 2 }];

    limpiarCache();

    const resultado = await cargarTodo();

    expect(resultado.personas).to.deep.equal([
      { idPersona: 2 },
    ]);

    expect(solicitudes).to.have.lengthOf(
      cantidadInicial * 2
    );
  });

  it("aplica los pendientes locales sobre los datos cargados", async () => {
    datosServidor.personas = [{ idPersona: 1, nombres: "Ana" }];

    guardarPendientes([
      {
        metodo: "POST",
        ruta: "personas",
        cuerpo: {
          idPersona: 2,
          nombres: "Carlos",
        },
      },
    ]);

    const resultado = await cargarTodo();

    expect(resultado.personas).to.deep.equal([
      { idPersona: 1, nombres: "Ana" },
      { idPersona: 2, nombres: "Carlos" },
    ]);
  });

  it("conserva los datos originales al aplicar pendientes", async () => {
    datosServidor.personas = [
      { idPersona: 1, nombres: "Ana" },
    ];

    guardarPendientes([
      {
        metodo: "PUT",
        ruta: "personas/1",
        cuerpo: { nombres: "Ana María" },
      },
    ]);

    const resultado = await cargarTodo();

    expect(resultado.personas[0].nombres).to.equal("Ana María");
    expect(datosServidor.personas[0].nombres).to.equal("Ana");
  });


  it("ignora una carga antigua si se limpia la caché mientras está pendiente", async () => {
    datosServidor.personas = [
      { idPersona: 1, nombres: "Datos antiguos" },
    ];

    const respuestasAntiguas = [];

    globalThis.fetch = async (url) => {
      const direccion = new URL(url);
      const ruta = direccion.pathname.split("/").at(-1);
      const first = Number(direccion.searchParams.get("first"));
      const max = Number(direccion.searchParams.get("max"));

      const registros = [...(datosServidor[ruta] ?? [])];

      return new Promise((resolve) => {
        respuestasAntiguas.push(() => {
          resolve(
            respuesta(
              registros.slice(first, first + max),
              registros.length
            )
          );
        });
      });
    };

    const cargaAntigua = cargarTodo();

    // Esperamos a que comiencen todas las solicitudes antiguas.
    while (respuestasAntiguas.length < Object.keys(FUENTES).length) {
      await new Promise((resolve) => setTimeout(resolve, 0));
    }

    limpiarCache();

    datosServidor.personas = [
      { idPersona: 2, nombres: "Datos nuevos" },
    ];

    // La nueva carga utiliza respuestas inmediatas.
    globalThis.fetch = async (url) => {
      const direccion = new URL(url);
      const ruta = direccion.pathname.split("/").at(-1);
      const first = Number(direccion.searchParams.get("first"));
      const max = Number(direccion.searchParams.get("max"));

      const registros = datosServidor[ruta] ?? [];

      return respuesta(
        registros.slice(first, first + max),
        registros.length
      );
    };

    const cargaNueva = await cargarTodo();

    // Ahora terminan las solicitudes de la carga antigua.
    respuestasAntiguas.forEach((liberar) => liberar());

    const resultadoAntiguo = await cargaAntigua;

    expect(cargaNueva.personas).to.deep.equal([
      { idPersona: 2, nombres: "Datos nuevos" },
    ]);

    expect(resultadoAntiguo.personas).to.deep.equal([
      { idPersona: 2, nombres: "Datos nuevos" },
    ]);
  });

  it("ignora una recarga selectiva antigua si se limpia la caché durante su ejecución", async () => {
    datosServidor.personas = [
      { idPersona: 1, nombres: "Original" },
    ];

    await cargarTodo();

    datosServidor.personas = [
      { idPersona: 1, nombres: "Recarga antigua" },
    ];

    let liberarRecargaAntigua;

    globalThis.fetch = async (url) => {
      const direccion = new URL(url);
      const ruta = direccion.pathname.split("/").at(-1);
      const first = Number(direccion.searchParams.get("first"));
      const max = Number(direccion.searchParams.get("max"));

      const registros = [...(datosServidor[ruta] ?? [])];

      return new Promise((resolve) => {
        liberarRecargaAntigua = () => {
          resolve(
            respuesta(
              registros.slice(first, first + max),
              registros.length
            )
          );
        };
      });
    };

    const recargaAntigua = recargarTablas(["personas"]);

    while (!liberarRecargaAntigua) {
      await new Promise((resolve) => setTimeout(resolve, 0));
    }

    limpiarCache();

    datosServidor.personas = [
      { idPersona: 1, nombres: "Datos nuevos" },
    ];

    globalThis.fetch = async (url) => {
      const direccion = new URL(url);
      const ruta = direccion.pathname.split("/").at(-1);
      const first = Number(direccion.searchParams.get("first"));
      const max = Number(direccion.searchParams.get("max"));

      const registros = datosServidor[ruta] ?? [];

      return respuesta(
        registros.slice(first, first + max),
        registros.length
      );
    };

    const cargaNueva = await cargarTodo();

    liberarRecargaAntigua();

    const resultadoRecarga = await recargaAntigua;

    expect(cargaNueva.personas).to.deep.equal([
      { idPersona: 1, nombres: "Datos nuevos" },
    ]);

    expect(resultadoRecarga.personas).to.deep.equal([
      { idPersona: 1, nombres: "Datos nuevos" },
    ]);
  });

});
