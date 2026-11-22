
import { expect } from "chai";
import { aplicarPendientes } from "../../../js/datos.js";

const crearDatos = () => ({
  clinicas: [
    { idClinica: 1, nombre: "Central", direccion: "Santa Ana" },
    { idClinica: 2, nombre: "Norte", direccion: "Ahuachapán" },
  ],
  personas: [
    { idPersona: 10, nombres: "Ana" },
  ],
});

describe("aplicarPendientes", () => {
  it("devuelve los mismos datos si no hay operaciones pendientes", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, []);

    expect(resultado).to.equal(datos);
  });

  // POST

  it("agrega un registro nuevo mediante POST", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "clinicas/3",
        cuerpo: { idClinica: 3, nombre: "Occidente" },
      },
    ]);

    expect(resultado.clinicas).to.have.lengthOf(3);
    expect(resultado.clinicas[2].nombre).to.equal("Occidente");
  });

  it("actualiza el registro si el POST contiene un identificador existente", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "clinicas/1",
        cuerpo: { idClinica: 1, nombre: "Central actualizada" },
      },
    ]);

    expect(resultado.clinicas).to.have.lengthOf(2);
    expect(resultado.clinicas[0].nombre).to.equal("Central actualizada");
  });

  it("no agrega registros POST sin identificador", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "clinicas/",
        cuerpo: { nombre: "Nueva clínica" },
      },
    ]);

    expect(resultado.clinicas).to.have.lengthOf(2);
  });

  // PUT

  it("actualiza un registro existente mediante PUT", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "PUT",
        ruta: "clinicas/1",
        cuerpo: { nombre: "Central modificada" },
      },
    ]);

    expect(resultado.clinicas[0]).to.deep.equal({
      idClinica: 1,
      nombre: "Central modificada",
      direccion: "Santa Ana",
    });
  });

  it("conserva las propiedades que no se modifican mediante PUT", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "PUT",
        ruta: "clinicas/1",
        cuerpo: { nombre: "Nueva Central" },
      },
    ]);

    expect(resultado.clinicas[0].direccion).to.equal("Santa Ana");
  });

  it("no modifica registros cuando el PUT apunta a un identificador inexistente", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "PUT",
        ruta: "clinicas/999",
        cuerpo: { nombre: "Inexistente" },
      },
    ]);

    expect(resultado.clinicas).to.have.lengthOf(2);
  });

  // DELETE

  it("elimina un registro existente mediante DELETE", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "DELETE",
        ruta: "clinicas/1",
      },
    ]);

    expect(resultado.clinicas).to.have.lengthOf(1);
    expect(resultado.clinicas[0].idClinica).to.equal(2);
  });

  it("no modifica la tabla cuando DELETE apunta a un registro inexistente", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "DELETE",
        ruta: "clinicas/999",
      },
    ]);

    expect(resultado.clinicas).to.have.lengthOf(2);
  });

  // Rutas y operaciones desconocidas

  it("ignora rutas que no pertenecen a los recursos conocidos", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "DELETE",
        ruta: "recurso-desconocido/1",
      },
    ]);

    expect(resultado.clinicas).to.deep.equal(datos.clinicas);
    expect(resultado.personas).to.deep.equal(datos.personas);
  });

  it("ignora métodos HTTP que no están contemplados", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "PATCH",
        ruta: "clinicas/1",
        cuerpo: { nombre: "Modificada" },
      },
    ]);

    expect(resultado.clinicas).to.deep.equal(datos.clinicas);
  });

  // Inmutabilidad

  it("no modifica el objeto original al agregar registros", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "clinicas/3",
        cuerpo: { idClinica: 3, nombre: "Nueva" },
      },
    ]);

    expect(datos.clinicas).to.have.lengthOf(2);
    expect(resultado.clinicas).to.have.lengthOf(3);
    expect(resultado).to.not.equal(datos);
  });

  it("no modifica el objeto original al actualizar registros", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "PUT",
        ruta: "clinicas/1",
        cuerpo: { nombre: "Modificada" },
      },
    ]);

    expect(datos.clinicas[0].nombre).to.equal("Central");
    expect(resultado.clinicas[0].nombre).to.equal("Modificada");
  });

  it("no modifica el objeto original al eliminar registros", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "DELETE",
        ruta: "clinicas/1",
      },
    ]);

    expect(datos.clinicas).to.have.lengthOf(2);
    expect(resultado.clinicas).to.have.lengthOf(1);
  });

  it("conserva las referencias de las tablas que no fueron modificadas", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "DELETE",
        ruta: "clinicas/1",
      },
    ]);

    expect(resultado.personas).to.equal(datos.personas);
  });

  it("aplica varias operaciones pendientes en orden", () => {
    const datos = crearDatos();

    const resultado = aplicarPendientes(datos, [
      {
        metodo: "POST",
        ruta: "clinicas/3",
        cuerpo: { idClinica: 3, nombre: "Occidente" },
      },
      {
        metodo: "PUT",
        ruta: "clinicas/1",
        cuerpo: { nombre: "Central actualizada" },
      },
      {
        metodo: "DELETE",
        ruta: "clinicas/2",
      },
    ]);

    expect(resultado.clinicas).to.deep.equal([
      { idClinica: 1, nombre: "Central actualizada", direccion: "Santa Ana" },
      { idClinica: 3, nombre: "Occidente" },
    ]);
  });
});
