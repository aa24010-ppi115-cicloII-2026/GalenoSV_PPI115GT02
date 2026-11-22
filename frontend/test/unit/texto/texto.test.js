
import { expect } from "chai";
import {
    cantidad,
    escapar,
    normalizarTexto,
    nombreCompleto,
    nombreCorto,
} from "../../../js/texto.js";

describe("Utilidades de texto", () => {

    describe("cantidad", () => {
        it("usa singular cuando la cantidad es uno", () => {
            expect(cantidad(1, "cita", "citas")).to.equal("1 cita");
        });

        it("usa plural para otras cantidades", () => {
            expect(cantidad(5, "cita", "citas")).to.equal("5 citas");
        });

        it("utiliza plural para cero", () => {
            expect(cantidad(0, "cita", "citas")).to.equal("0 citas");
        });
    });

    describe("escapar", () => {
        it("escapa todos los caracteres especiales HTML", () => {
            expect(escapar(`<div title="O'Hara">A & B</div>`))
                .to.equal("&lt;div title=&quot;O&#39;Hara&quot;&gt;A &amp; B&lt;/div&gt;");
        });

        it("devuelve una cadena vacía para null y undefined", () => {
            expect(escapar(null)).to.equal("");
            expect(escapar(undefined)).to.equal("");
        });

        it("convierte números a texto", () => {
            expect(escapar(123)).to.equal("123");
        });

        it("no modifica texto sin caracteres especiales", () => {
            expect(escapar("GalenoSV")).to.equal("GalenoSV");
        });
    });

    describe("normalizarTexto", () => {
        it("elimina acentos y convierte a minúsculas", () => {
            expect(normalizarTexto("ÁRBOL MÉDICO"))
                .to.equal("arbol medico");
        });

        it("elimina espacios al inicio y al final", () => {
            expect(normalizarTexto("  Ana García  "))
                .to.equal("ana garcia");
        });

        it("devuelve una cadena vacía para null y undefined", () => {
            expect(normalizarTexto(null)).to.equal("");
            expect(normalizarTexto(undefined)).to.equal("");
        });

        it("convierte números a texto", () => {
            expect(normalizarTexto(123)).to.equal("123");
        });
    });

    describe("nombreCompleto", () => {
        it("combina nombres y apellidos", () => {
            expect(nombreCompleto({
                nombres: "Ana María",
                apellidos: "García López",
            })).to.equal("Ana María García López");
        });

        it("elimina espacios sobrantes", () => {
            expect(nombreCompleto({
                nombres: " Ana ",
                apellidos: " García ",
            })).to.equal("Ana   García");
        });

        it("tolera propiedades ausentes", () => {
            expect(nombreCompleto({ nombres: "Ana" })).to.equal("Ana");
            expect(nombreCompleto({ apellidos: "García" })).to.equal("García");
        });

        it("tolera una persona nula o indefinida", () => {
            expect(nombreCompleto(null)).to.equal("");
            expect(nombreCompleto(undefined)).to.equal("");
        });
    });

    describe("nombreCorto", () => {
        it("obtiene el primer nombre y el primer apellido", () => {
            expect(nombreCorto({
                nombres: "Ana María",
                apellidos: "García López",
            })).to.equal("Ana García");
        });

        it("elimina espacios sobrantes", () => {
            expect(nombreCorto({
                nombres: "  Ana   María ",
                apellidos: " García   López ",
            })).to.equal("Ana García");
        });

        it("funciona cuando falta el apellido", () => {
            expect(nombreCorto({ nombres: "Ana María" }))
                .to.equal("Ana");
        });

        it("funciona cuando falta el nombre", () => {
            expect(nombreCorto({ apellidos: "García López" }))
                .to.equal("García");
        });

        it("devuelve una cadena vacía para una persona sin datos", () => {
            expect(nombreCorto({})).to.equal("");
            expect(nombreCorto(null)).to.equal("");
        });
    });
});
