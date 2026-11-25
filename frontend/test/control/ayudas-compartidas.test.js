import { expect } from "chai";
import { mismoId } from "../../js/control/tablas.js";
import { conteoDePasos, estadoDeCita } from "../../js/control/citas.js";
import { esRolPaciente } from "../../js/control/personas.js";
import { cantidad } from "../../js/comun/texto.js";

describe("ayudas compartidas de control", () => {
  it("mismoId compara como texto sin convertir la ausencia en cero", () => {
    expect(mismoId(0, "0")).to.equal(true);
    expect(mismoId("10", 10)).to.equal(true);
    expect(mismoId(null, null)).to.equal(false);
    expect(mismoId(undefined, 0)).to.equal(false);
    expect(mismoId("", 0)).to.equal(false);
  });

  it("conteoDePasos cuenta solo los completados y conserva el total", () => {
    expect(conteoDePasos([])).to.deep.equal({ total: 0, completados: 0 });
    expect(conteoDePasos([
      { estado: "completado" },
      { estado: "en_proceso" },
      { estado: "COMPLETADO" },
      { estado: "completado", registro: {} },
    ])).to.deep.equal({ total: 4, completados: 2 });
  });

  it("estadoDeCita usa el mismo conteo para clasificar la agenda", () => {
    const cita = (pasos, consulta = {}) => ({ consulta, procedimientos: [{ pasos }] });

    expect(estadoDeCita(cita([])).clave).to.equal("sin-pasos");
    expect(estadoDeCita(cita([{ estado: "pendiente" }])).clave).to.equal("programada");
    expect(estadoDeCita(cita([{ estado: "completado" }, { estado: "pendiente" }])).clave).to.equal("proceso");
    expect(estadoDeCita(cita([{ estado: "completado" }])).clave).to.equal("completada");
    expect(estadoDeCita(cita([{ estado: "pendiente" }], { fechaFin: "2026-11-12" })).clave).to.equal("completada");
  });

  it("esRolPaciente normaliza acentos, mayusculas y espacios sin ignorar activo", () => {
    expect(esRolPaciente({ nombre: " PÁCIENTE ", activo: true })).to.equal(true);
    expect(esRolPaciente({ nombre: "Paciente" })).to.equal(true);
    expect(esRolPaciente({ nombre: "Paciente", activo: false })).to.equal(false);
    expect(esRolPaciente({ nombre: "Enfermero" })).to.equal(false);
    expect(esRolPaciente(null)).to.equal(false);
  });

  it("cantidad reserva el singular para uno", () => {
    expect(cantidad(0, "paso", "pasos")).to.equal("0 pasos");
    expect(cantidad(1, "paso", "pasos")).to.equal("1 paso");
    expect(cantidad(2, "paso", "pasos")).to.equal("2 pasos");
  });
});
