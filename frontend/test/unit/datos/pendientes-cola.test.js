
import { expect } from "chai";
import {
    estaConectado,
    encolar,
    listarPendientes,
    quitarPendiente,
    sincronizar,
} from "../../../js/pendientes.js";

const CLAVE = "galenosv.pendientes";

describe("Cola de operaciones pendientes", () => {
    let almacenamiento;
    let eventos;
    let fallarLectura;
    let fallarEscritura;
    let llamadasEscritura;
    let descriptoresOriginales;

    const guardarGlobal = (nombre, valor) => {
        Object.defineProperty(globalThis, nombre, {
            configurable: true,
            writable: true,
            value: valor,
        });
    };

    const guardarPendientes = (pendientes) => {
        almacenamiento.set(CLAVE, JSON.stringify(pendientes));
    };

    const obtenerGuardados = () => {
        const datos = almacenamiento.get(CLAVE);
        return datos ? JSON.parse(datos) : [];
    };

    const crearPendiente = (id, metodo = "POST") => ({
        id,
        metodo,
        ruta: "personas/1",
        cuerpo: { nombres: "Ana" },
    });

    beforeEach(() => {
        descriptoresOriginales = {};

        for (const nombre of ["localStorage", "navigator", "window"]) {
            descriptoresOriginales[nombre] =
                Object.getOwnPropertyDescriptor(globalThis, nombre);
        }

        almacenamiento = new Map();
        eventos = [];
        fallarLectura = false;
        fallarEscritura = false;
        llamadasEscritura = 0;

        guardarGlobal("localStorage", {
            getItem(clave) {
                if (fallarLectura) {
                    throw new Error("Error de lectura");
                }

                return almacenamiento.get(clave) ?? null;
            },

            setItem(clave, valor) {
                llamadasEscritura++;

                if (fallarEscritura) {
                    throw new Error("Error de escritura");
                }

                almacenamiento.set(clave, valor);
            },
        });

        guardarGlobal("navigator", { onLine: true });

        guardarGlobal("window", {
            dispatchEvent(evento) {
                eventos.push(evento.type);
                return true;
            },
        });
    });

    afterEach(() => {
        for (const nombre of ["localStorage", "navigator", "window"]) {
            const descriptor = descriptoresOriginales[nombre];

            if (descriptor) {
                Object.defineProperty(globalThis, nombre, descriptor);
            } else {
                delete globalThis[nombre];
            }
        }
    });

    // --------------------------------------------------
    // estaConectado
    // --------------------------------------------------

    describe("estaConectado", () => {
        it("devuelve true cuando navigator no existe", () => {
            guardarGlobal("navigator", undefined);

            expect(estaConectado()).to.equal(true);
        });

        it("devuelve true cuando el navegador está conectado", () => {
            navigator.onLine = true;

            expect(estaConectado()).to.equal(true);
        });

        it("devuelve false cuando el navegador está desconectado", () => {
            navigator.onLine = false;

            expect(estaConectado()).to.equal(false);
        });

        it("considera conectado el navegador si onLine no es false", () => {
            navigator.onLine = undefined;

            expect(estaConectado()).to.equal(true);
        });
    });

    // --------------------------------------------------
    // leer / listarPendientes
    // --------------------------------------------------

    describe("listarPendientes", () => {
        it("devuelve una lista vacía cuando no hay datos guardados", () => {
            expect(listarPendientes()).to.deep.equal([]);
        });

        it("recupera las operaciones almacenadas", () => {
            const pendientes = [
                crearPendiente("1"),
                crearPendiente("2", "PUT"),
            ];

            guardarPendientes(pendientes);

            expect(listarPendientes()).to.deep.equal(pendientes);
        });

        it("devuelve una lista vacía cuando el JSON está dañado", () => {
            almacenamiento.set(CLAVE, "{ JSON inválido");

            expect(listarPendientes()).to.deep.equal([]);
        });

        it("devuelve una lista vacía cuando el contenido no es un array", () => {
            almacenamiento.set(CLAVE, JSON.stringify({ id: 1 }));

            expect(listarPendientes()).to.deep.equal([]);
        });

        it("devuelve una lista vacía si falla la lectura del almacenamiento", () => {
            fallarLectura = true;

            expect(listarPendientes()).to.deep.equal([]);
        });
    });

    // --------------------------------------------------
    // encolar
    // --------------------------------------------------

    describe("encolar", () => {
        it("agrega una operación y devuelve la cantidad de pendientes", () => {
            const cantidad = encolar({
                metodo: "POST",
                ruta: "personas",
                cuerpo: { nombres: "Ana" },
            });

            expect(cantidad).to.equal(1);
            expect(listarPendientes()).to.have.lengthOf(1);
        });

        it("genera un identificador para cada operación", () => {
            encolar({
                metodo: "POST",
                ruta: "personas",
                cuerpo: {},
            });

            const [pendiente] = listarPendientes();

            expect(pendiente.id).to.be.a("string");
            expect(pendiente.id).to.match(/^\d+-[a-z0-9]+$/);
        });

        it("conserva las operaciones anteriores al agregar una nueva", () => {
            encolar({
                metodo: "POST",
                ruta: "personas",
                cuerpo: { nombres: "Ana" },
            });

            encolar({
                metodo: "PUT",
                ruta: "personas/1",
                cuerpo: { nombres: "María" },
            });

            expect(listarPendientes()).to.have.lengthOf(2);
        });

        it("emite el evento pendientes-cambio al guardar", () => {
            encolar({
                metodo: "POST",
                ruta: "personas",
                cuerpo: {},
            });

            expect(eventos).to.deep.equal(["pendientes-cambio"]);
        });

        it("lanza un error si no puede guardar la operación", () => {
            fallarEscritura = true;

            expect(() => encolar({
                metodo: "POST",
                ruta: "personas",
                cuerpo: {},
            })).to.throw(
                "No se pudo guardar el cambio en este dispositivo."
            );
        });

        it("no emite el evento si falla el almacenamiento", () => {
            fallarEscritura = true;

            try {
                encolar({
                    metodo: "POST",
                    ruta: "personas",
                    cuerpo: {},
                });
            } catch {
                // El error es el comportamiento esperado.
            }

            expect(eventos).to.deep.equal([]);
        });

        it("permite guardar operaciones cuando window no existe", () => {
            guardarGlobal("window", undefined);

            expect(encolar({
                metodo: "POST",
                ruta: "personas",
                cuerpo: {},
            })).to.equal(1);
        });
    });

    // --------------------------------------------------
    // quitarPendiente
    // --------------------------------------------------

    describe("quitarPendiente", () => {
        it("elimina una operación existente", () => {
            guardarPendientes([
                crearPendiente("1"),
                crearPendiente("2"),
            ]);

            const resultado = quitarPendiente("1");

            expect(resultado).to.equal(true);
            expect(listarPendientes()).to.deep.equal([
                crearPendiente("2"),
            ]);
        });

        it("devuelve true si el identificador no existe", () => {
            guardarPendientes([crearPendiente("1")]);

            const escriturasAntes = llamadasEscritura;

            const resultado = quitarPendiente("999");

            expect(resultado).to.equal(true);
            expect(llamadasEscritura).to.equal(escriturasAntes);
            expect(listarPendientes()).to.have.lengthOf(1);
        });

        it("emite un evento al eliminar una operación", () => {
            guardarPendientes([crearPendiente("1")]);

            quitarPendiente("1");

            expect(eventos).to.deep.equal(["pendientes-cambio"]);
        });

        it("devuelve false si no puede guardar la eliminación", () => {
            guardarPendientes([crearPendiente("1")]);
            fallarEscritura = true;

            const resultado = quitarPendiente("1");

            expect(resultado).to.equal(false);
        });

        it("conserva la operación si falla su eliminación", () => {
            guardarPendientes([crearPendiente("1")]);
            fallarEscritura = true;

            quitarPendiente("1");

            expect(obtenerGuardados()).to.have.lengthOf(1);
        });
    });

    // --------------------------------------------------
    // sincronizar
    // --------------------------------------------------

    describe("sincronizar", () => {
        it("no ejecuta operaciones cuando no hay conexión", async () => {
            guardarPendientes([crearPendiente("1")]);
            navigator.onLine = false;

            let ejecutadas = 0;

            const resultado = await sincronizar(async () => {
                ejecutadas++;
            });

            expect(ejecutadas).to.equal(0);
            expect(resultado).to.deep.equal({
                enviadas: 0,
                pendientes: 1,
            });
        });

        it("devuelve cero operaciones cuando la cola está vacía", async () => {
            const resultado = await sincronizar(async () => {});

            expect(resultado).to.deep.equal({
                enviadas: 0,
                pendientes: 0,
            });
        });

        it("envía y elimina todas las operaciones correctamente", async () => {
            guardarPendientes([
                crearPendiente("1"),
                crearPendiente("2", "PUT"),
                crearPendiente("3", "DELETE"),
            ]);

            const ejecutadas = [];

            const resultado = await sincronizar(async (pendiente) => {
                ejecutadas.push(pendiente.id);
            });

            expect(ejecutadas).to.deep.equal(["1", "2", "3"]);
            expect(resultado).to.deep.equal({
                enviadas: 3,
                pendientes: 0,
            });
            expect(listarPendientes()).to.deep.equal([]);
        });

        it("conserva las operaciones restantes si una falla", async () => {
            guardarPendientes([
                crearPendiente("1"),
                crearPendiente("2"),
                crearPendiente("3"),
            ]);

            const ejecutadas = [];

            const resultado = await sincronizar(async (pendiente) => {
                ejecutadas.push(pendiente.id);

                if (pendiente.id === "2") {
                    throw new Error("Error del servidor");
                }
            });

            expect(ejecutadas).to.deep.equal(["1", "2"]);
            expect(resultado).to.deep.equal({
                enviadas: 1,
                pendientes: 2,
            });

            expect(listarPendientes().map((p) => p.id))
                .to.deep.equal(["2", "3"]);
        });

        it("detiene la sincronización si no puede eliminar una operación", async () => {
            guardarPendientes([
                crearPendiente("1"),
                crearPendiente("2"),
            ]);

            const ejecutadas = [];

            fallarEscritura = true;

            const resultado = await sincronizar(async (pendiente) => {
                ejecutadas.push(pendiente.id);
            });

            expect(ejecutadas).to.deep.equal(["1"]);
            expect(resultado).to.deep.equal({
                enviadas: 0,
                pendientes: 2,
            });
        });

        it("ejecuta las operaciones en el orden almacenado", async () => {
            guardarPendientes([
                crearPendiente("primera"),
                crearPendiente("segunda"),
                crearPendiente("tercera"),
            ]);

            const orden = [];

            await sincronizar(async (pendiente) => {
                orden.push(pendiente.id);
            });

            expect(orden).to.deep.equal([
                "primera",
                "segunda",
                "tercera",
            ]);
        });

        it("evita ejecutar dos sincronizaciones simultáneas", async () => {
            guardarPendientes([
                crearPendiente("1"),
                crearPendiente("2"),
            ]);

            let liberar;
            let llamadas = 0;

            const bloqueo = new Promise((resolve) => {
                liberar = resolve;
            });

            const primera = sincronizar(async () => {
                llamadas++;
                await bloqueo;
            });

            const segunda = sincronizar(async () => {
                llamadas++;
            });

            expect(llamadas).to.equal(1);

            liberar();

            const resultados = await Promise.all([primera, segunda]);

            expect(llamadas).to.equal(2);
            expect(resultados[0]).to.deep.equal({
                enviadas: 2,
                pendientes: 0,
            });
            expect(resultados[1]).to.deep.equal(resultados[0]);
        });

        it("permite volver a sincronizar después de finalizar", async () => {
            guardarPendientes([crearPendiente("1")]);

            await sincronizar(async () => {});

            guardarPendientes([crearPendiente("2")]);

            const ejecutadas = [];

            const resultado = await sincronizar(async (pendiente) => {
                ejecutadas.push(pendiente.id);
            });

            expect(ejecutadas).to.deep.equal(["2"]);
            expect(resultado.enviadas).to.equal(1);
        });
    });
});
