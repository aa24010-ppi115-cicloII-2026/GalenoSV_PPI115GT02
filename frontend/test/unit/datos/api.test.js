
import { expect } from "chai";
import {
    API,
    listar,
    crear,
    actualizar,
    eliminar,
    comprobarServicio,
    servicioNoResponde,
    ejecutarPendiente,
} from "../../../js/api.js";

const CLAVE = "galenosv.pendientes";

let fetchOriginal;
let navigatorOriginal;
let windowOriginal;
let storageOriginal;

let solicitudes;
let almacenamiento;
let eventos;
let conectado;
let fallaAlGuardar;

function respuesta(datos = [], opciones = {}) {
    const {
        status = 200,
        headers = {},
        statusText = "",
    } = opciones;

    return new Response(JSON.stringify(datos), {
        status,
        statusText,
        headers: {
            "Content-Type": "application/json",
            ...headers,
        },
    });
}

function instalarFetch(implementacion) {
    globalThis.fetch = async (url, opciones = {}) => {
        solicitudes.push({ url, opciones });
        return implementacion(url, opciones);
    };
}

function leerPendientes() {
    const valor = almacenamiento.get(CLAVE);
    return valor ? JSON.parse(valor) : [];
}

function configurarConexion(valor) {
    conectado = valor;

    Object.defineProperty(globalThis, "navigator", {
        configurable: true,
        value: { onLine: valor },
    });
}

describe("API", () => {
    beforeEach(async () => {
        fetchOriginal = globalThis.fetch;
        navigatorOriginal = Object.getOwnPropertyDescriptor(
            globalThis,
            "navigator"
        );
        windowOriginal = Object.getOwnPropertyDescriptor(
            globalThis,
            "window"
        );
        storageOriginal = Object.getOwnPropertyDescriptor(
            globalThis,
            "localStorage"
        );

        solicitudes = [];
        almacenamiento = new Map();
        eventos = [];
        fallaAlGuardar = false;

        configurarConexion(true);

        globalThis.localStorage = {
            getItem(clave) {
                return almacenamiento.get(clave) ?? null;
            },

            setItem(clave, valor) {
                if (fallaAlGuardar) {
                    throw new Error("Almacenamiento no disponible");
                }

                almacenamiento.set(clave, String(valor));
            },

            removeItem(clave) {
                almacenamiento.delete(clave);
            },
        };

        globalThis.window = {
            dispatchEvent(evento) {
                eventos.push(evento.type);
                return true;
            },
        };

        instalarFetch(() =>
            respuesta([], {
                headers: {
                    "X-Sin-Conexion": "false",
                    "X-Total-Count": "0",
                },
            })
        );

        // Restablece el estado interno del servicio entre pruebas.
        await listar("reset");
        solicitudes = [];
        eventos = [];
    });

    afterEach(() => {
        globalThis.fetch = fetchOriginal;

        if (navigatorOriginal) {
            Object.defineProperty(
                globalThis,
                "navigator",
                navigatorOriginal
            );
        } else {
            delete globalThis.navigator;
        }

        if (windowOriginal) {
            Object.defineProperty(globalThis, "window", windowOriginal);
        } else {
            delete globalThis.window;
        }

        if (storageOriginal) {
            Object.defineProperty(
                globalThis,
                "localStorage",
                storageOriginal
            );
        } else {
            delete globalThis.localStorage;
        }
    });

    describe("listar", () => {
        it("consulta una ruta con los parámetros predeterminados", async () => {
            instalarFetch(() => respuesta([{ id: 1 }], {
                headers: { "X-Total-Count": "1" },
            }));

            const resultado = await listar("personas");

            expect(resultado).to.deep.equal({
                datos: [{ id: 1 }],
                total: 1,
            });

            expect(solicitudes[0].url).to.equal(
                `${API}/personas?first=0&max=20`
            );
        });

        it("permite personalizar first y max", async () => {
            instalarFetch(() => respuesta([], {
                headers: { "X-Total-Count": "0" },
            }));

            await listar("personas", { first: 40, max: 10 });

            expect(solicitudes[0].url).to.include(
                "personas?first=40&max=10"
            );
        });

        it("rechaza una respuesta sin X-Total-Count", async () => {
            instalarFetch(() => respuesta([]));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.include(
                    "X-Total-Count válido"
                );
            }
        });

        it("rechaza un X-Total-Count que no sea numérico", async () => {
            for (const valor of ["abc", "-1", "1.5", ""]) {
                instalarFetch(() => respuesta([], {
                    headers: { "X-Total-Count": valor },
                }));

                try {
                    await listar("personas");
                    expect.fail(`Debió rechazar ${valor}`);
                } catch (error) {
                    expect(error.message).to.include(
                        "X-Total-Count válido"
                    );
                }
            }
        });

        it("rechaza un total que excede el rango de enteros seguros", async () => {
            instalarFetch(() => respuesta([], {
                headers: {
                    "X-Total-Count": "9007199254740992",
                },
            }));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.include(
                    "X-Total-Count válido"
                );
            }
        });

        it("rechaza una respuesta cuyo JSON no es una lista", async () => {
            instalarFetch(() => respuesta({ id: 1 }, {
                headers: { "X-Total-Count": "1" },
            }));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.include(
                    "no contiene una lista"
                );
            }
        });
    });

    describe("pedir y encabezados HTTP", () => {
        it("envía Accept: application/json por defecto", async () => {
            instalarFetch(() => respuesta([], {
                headers: {
                    "X-Total-Count": "0",
                },
            }));

            await listar("personas");

            expect(
                solicitudes[0].opciones.headers.Accept
            ).to.equal("application/json");
        });

        it("envía Content-Type cuando existe un cuerpo", async () => {
            instalarFetch(() => respuesta(null, {
                headers: { Location: "/personas/1" },
            }));

            await crear("personas", { nombres: "Ana" });

            expect(
                solicitudes[0].opciones.headers["Content-Type"]
            ).to.equal("application/json");
        });


    });

    describe("errores HTTP", () => {
        it("utiliza el mensaje recibido en el servidor", async () => {
            instalarFetch(() => respuesta(
                { mensaje: "Persona no encontrada" },
                { status: 404 }
            ));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.equal(
                    "Persona no encontrada"
                );
                expect(error.status).to.equal(404);
            }
        });

        it("utiliza message si el cuerpo no contiene mensaje", async () => {
            instalarFetch(() => respuesta(
                { message: "Acceso denegado" },
                { status: 403 }
            ));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.equal("Acceso denegado");
                expect(error.status).to.equal(403);
            }
        });

        it("incluye el parámetro que causó el error", async () => {
            instalarFetch(() => respuesta(
                { mensaje: "Valor inválido" },
                {
                    status: 400,
                    headers: {
                        "X-Parametro-Error": "fechaNacimiento",
                    },
                }
            ));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.equal(
                    "Valor inválido (fechaNacimiento)"
                );
            }
        });

        it("utiliza el estado HTTP cuando el cuerpo no es JSON", async () => {
            instalarFetch(() => new Response("Error interno", {
                status: 500,
                statusText: "Internal Server Error",
            }));

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.include("Error 500");
                expect(error.status).to.equal(500);
            }
        });

        it("no convierte un error HTTP en una operación pendiente", async () => {
            instalarFetch(() => respuesta(
                { mensaje: "No autorizado" },
                { status: 401 }
            ));

            try {
                await crear("personas", { nombres: "Ana" });
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.status).to.equal(401);
                expect(leerPendientes()).to.have.lengthOf(0);
            }
        });
    });

    describe("estado del servicio", () => {
        it("marca el servicio como caído si fetch falla", async () => {
            instalarFetch(() => {
                throw new Error("Error de red");
            });

            try {
                await listar("personas");
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.include(
                    "No se pudo contactar al servidor"
                );
                expect(servicioNoResponde()).to.equal(true);
            }
        });

        it("marca el servicio como caído si X-Sin-Conexion es true", async () => {
            instalarFetch(() => respuesta([], {
                headers: {
                    "X-Sin-Conexion": "true",
                    "X-Total-Count": "0",
                },
            }));

            await listar("personas");

            expect(servicioNoResponde()).to.equal(true);
        });

        it("restablece el estado cuando la API vuelve a responder", async () => {
            // 1. Simular que la API no responde desde el dispositivo
            instalarFetch(() => respuesta([], {
                headers: {
                    "X-Sin-Conexion": "true",
                    "X-Total-Count": "0",
                },
            }));

            await listar("personas");

            expect(servicioNoResponde()).to.equal(true);

            // 2. Simular que la API vuelve a responder
            instalarFetch(() => respuesta([], {
                headers: {
                    "X-Sin-Conexion": "false",
                    "X-Total-Count": "0",
                },
            }));

            await listar("personas");

            expect(servicioNoResponde()).to.equal(false);
        });

        it("emite un evento solo cuando cambia el estado del servicio", async () => {
            instalarFetch(() => respuesta([], {
                headers: {
                    "X-Sin-Conexion": "true",
                    "X-Total-Count": "0",
                },
            }));

            await listar("personas");
            await listar("personas");

            instalarFetch(() => respuesta([], {
                headers: { "X-Sin-Conexion": "false",
                      "X-Total-Count": "0",
                 },
            }));

            await listar("personas");

            expect(eventos).to.deep.equal([
                "servicio-cambio",
                "servicio-cambio",
            ]);
        });

        it("comprobarServicio devuelve false sin conexión", async () => {
            configurarConexion(false);

            const resultado = await comprobarServicio();

            expect(resultado).to.equal(false);
            expect(solicitudes).to.have.lengthOf(0);
        });

        it("comprobarServicio comprueba la API cuando hay conexión", async () => {
            instalarFetch(() => respuesta([], {
                headers: { "X-Total-Count": "0" },
            }));

            const resultado = await comprobarServicio();

            expect(resultado).to.equal(true);
            expect(solicitudes[0].url).to.include("/roles?first=0&max=1");
        });

        it("comprobarServicio devuelve false si la API no responde", async () => {
            instalarFetch(() => {
                throw new Error("Sin conexión");
            });

            const resultado = await comprobarServicio();

            expect(resultado).to.equal(false);
        });
    });

    describe("operaciones CRUD", () => {
        it("crear envía POST y devuelve Location", async () => {
            instalarFetch(() => respuesta(null, {
                headers: { Location: "/personas/15" },
            }));

            const resultado = await crear("personas", {
                nombres: "Ana",
            });

            expect(resultado).to.equal("/personas/15");
            expect(solicitudes[0].opciones.method).to.equal("POST");
            expect(JSON.parse(solicitudes[0].opciones.body))
                .to.deep.equal({ nombres: "Ana" });
        });

        it("actualizar envía PUT al recurso identificado", async () => {
            instalarFetch(() => respuesta(null));

            const resultado = await actualizar(
                "personas",
                15,
                { nombres: "Ana María" }
            );

            expect(resultado).to.equal(null);
            expect(solicitudes[0].url).to.include("/personas/15");
            expect(solicitudes[0].opciones.method).to.equal("PUT");
            expect(JSON.parse(solicitudes[0].opciones.body))
                .to.deep.equal({ nombres: "Ana María" });
        });

        it("eliminar envía DELETE sin cuerpo", async () => {
            instalarFetch(() => respuesta(null));

            const resultado = await eliminar("personas", 15);

            expect(resultado).to.equal(null);
            expect(solicitudes[0].url).to.include("/personas/15");
            expect(solicitudes[0].opciones.method).to.equal("DELETE");
            expect(solicitudes[0].opciones.body).to.equal(undefined);
        });

        it("encola POST cuando el dispositivo está sin conexión", async () => {
            configurarConexion(false);

            const resultado = await crear("personas", {
                nombres: "Ana",
            });

            expect(resultado).to.equal(null);
            expect(solicitudes).to.have.lengthOf(0);

            const pendientes = leerPendientes();

            expect(pendientes).to.have.lengthOf(1);
            expect(pendientes[0]).to.include({
                metodo: "POST",
                ruta: "personas",
            });

            expect(pendientes[0].cuerpo).to.deep.equal({
                nombres: "Ana",
            });

            expect(pendientes[0].id).to.be.a("string");
        });

        it("encola PUT cuando el dispositivo está sin conexión", async () => {
            configurarConexion(false);

            await actualizar("personas", 15, {
                nombres: "Ana",
            });

            expect(leerPendientes()[0]).to.include({
                metodo: "PUT",
                ruta: "personas/15",
            });
        });

        it("encola DELETE cuando el dispositivo está sin conexión", async () => {
            configurarConexion(false);

            await eliminar("personas", 15);

            expect(leerPendientes()[0]).to.include({
                metodo: "DELETE",
                ruta: "personas/15",
            });

            expect(leerPendientes()[0].cuerpo).to.equal(null);
        });

        it("encola la operación cuando falla la conexión durante el envío", async () => {
            instalarFetch(() => {
                throw new Error("Error de red");
            });

            const resultado = await crear("personas", {
                nombres: "Ana",
            });

            expect(resultado).to.equal(null);
            expect(leerPendientes()).to.have.lengthOf(1);
            expect(leerPendientes()[0].metodo).to.equal("POST");
        });

        it("no encola una operación si el servidor devuelve un error HTTP", async () => {
            instalarFetch(() => respuesta(
                { mensaje: "Conflicto" },
                { status: 409 }
            ));

            try {
                await actualizar("personas", 1, { nombres: "Ana" });
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.status).to.equal(409);
                expect(leerPendientes()).to.have.lengthOf(0);
            }
        });

        it("lanza un error si no puede guardar una operación pendiente", async () => {
            configurarConexion(false);
            fallaAlGuardar = true;

            try {
                await crear("personas", { nombres: "Ana" });
                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.equal(
                    "No se pudo guardar el cambio en este dispositivo."
                );
            }
        });
    });

    describe("ejecutarPendiente", () => {
        it("reenvía una operación POST", async () => {
            instalarFetch(() => respuesta(null));

            await ejecutarPendiente({
                metodo: "POST",
                ruta: "personas",
                cuerpo: { nombres: "Ana" },
            });

            expect(solicitudes[0].opciones.method).to.equal("POST");
            expect(JSON.parse(solicitudes[0].opciones.body))
                .to.deep.equal({ nombres: "Ana" });
        });

        it("reenvía una operación PUT", async () => {
            instalarFetch(() => respuesta(null));

            await ejecutarPendiente({
                metodo: "PUT",
                ruta: "personas/1",
                cuerpo: { nombres: "Ana" },
            });

            expect(solicitudes[0].url).to.include("/personas/1");
            expect(solicitudes[0].opciones.method).to.equal("PUT");
        });

        it("reenvía una operación DELETE sin cuerpo", async () => {
            instalarFetch(() => respuesta(null));

            await ejecutarPendiente({
                metodo: "DELETE",
                ruta: "personas/1",
            });

            expect(solicitudes[0].opciones.method).to.equal("DELETE");
            expect(solicitudes[0].opciones.body).to.equal(undefined);
        });

        it("rechaza métodos no admitidos", async () => {
            try {
                await ejecutarPendiente({
                    metodo: "PATCH",
                    ruta: "personas/1",
                });

                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.message).to.include(
                    "Método pendiente no admitido"
                );

                expect(solicitudes).to.have.lengthOf(0);
            }
        });

        it("propaga los errores HTTP al reenviar una operación", async () => {
            instalarFetch(() => respuesta(
                { mensaje: "Registro inválido" },
                { status: 400 }
            ));

            try {
                await ejecutarPendiente({
                    metodo: "POST",
                    ruta: "personas",
                    cuerpo: {},
                });

                expect.fail("Debió lanzar un error");
            } catch (error) {
                expect(error.status).to.equal(400);
            }
        });
    });
});
