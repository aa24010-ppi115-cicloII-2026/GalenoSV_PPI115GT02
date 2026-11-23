import sinon from "sinon";
import { expect } from "chai";
import { datosBase, stubApi } from "../ayudas.js";
import { cargarTodo, limpiarCache, aplicarPendientes } from "../../js/control/repositorio.js";

describe("repositorio.js", () => {
  let datos;

  beforeEach(() => {
    datos = datosBase();
  });

  it("conserva como listas los recursos del backend aunque los objetos tengan otros campos primero", async () => {
    const fuente = datosBase();
    fuente.personasRol = fuente.personasRol.map((item) => ({
      fechaCreacion: "2026-01-01T08:00-06:00",
      idClinica: item.idClinica,
      idPersona: item.idPersona,
      idPersonaRol: item.idPersonaRol,
      idRol: item.idRol,
    }));
    stubApi(fuente);
    try {
      const cargados = await cargarTodo();
      expect(cargados.personasRol).to.have.length(3);
      expect(cargados.personasRol.find((item) => item.idPersonaRol === 1000).idPersona).to.equal(100);
      expect(cargados).not.to.have.property("porId");
    } finally {
      sinon.restore();
      limpiarCache();
    }
  });

  it("muestra sobre los datos del servidor los cambios guardados sin conexión, sin alterar la copia original", () => {
    datos.ordenesExamen.push({ idOrdenExamen: 4000, idConsultaProcedimientoPaso: 9000 });
    const vista = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "consultas-procedimiento-paso",
        cuerpo: { idConsultaProcedimientoPaso: "c1", idConsultaProcedimiento: 8000, estado: "EN_PROCESO" },
      },
      { metodo: "PUT", ruta: "consultas-procedimiento-paso/c1", cuerpo: { estado: "FINALIZADO" } },
      { metodo: "PUT", ruta: "consultas-procedimiento-paso/9000", cuerpo: { observaciones: "Editado" } },
      { metodo: "DELETE", ruta: "ordenes-examen/4000", cuerpo: null },
    ]);
    expect(vista.consultasProcedimientoPaso.find((item) => item.idConsultaProcedimientoPaso === "c1").estado).to.equal("FINALIZADO");
    expect(vista.consultasProcedimientoPaso.find((item) => item.idConsultaProcedimientoPaso === 9000).observaciones).to.equal("Editado");
    expect(vista.ordenesExamen).to.deep.equal([]);
    expect(datos.consultasProcedimientoPaso).to.have.length(1);
    expect(datos.consultasProcedimientoPaso[0].observaciones).to.be.undefined;
    expect(datos.ordenesExamen).to.have.length(1);
    expect(aplicarPendientes(datos, [])).to.equal(datos);
  });

  it("proyecta una alta pendiente con el mismo identificador sin duplicar la fila", () => {
    const pendiente = {
      metodo: "POST",
      ruta: "consultas-procedimiento-paso/pendiente",
      cuerpo: {
        idConsultaProcedimientoPaso: "9000",
        estado: "REVISADO",
      },
    };

    const vista = aplicarPendientes(datos, [pendiente]);

    expect(vista.consultasProcedimientoPaso).to.have.length(1);
    expect(vista.consultasProcedimientoPaso[0]).to.include({
      idConsultaProcedimientoPaso: "9000",
      estado: "REVISADO",
    });
    expect(datos.consultasProcedimientoPaso[0].estado).to.equal("FINALIZADO");
  });
});
