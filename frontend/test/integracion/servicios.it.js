/**
 * Pruebas de integración: usan la API real, sin mocks, y solo leen datos.
 * Necesitan el backend levantado (`mvn clean package liberty:run`) y se corren aparte de las
 * pruebas de unidad: `npm run test:integracion`.
 */
import { expect } from "chai";
import { API } from "../../js/control/api.js";
import { servicios } from "../../js/control/servicios.js";
import { RECURSOS } from "../../js/entity/recursos.js";
import DefaultResponse from "../../js/entity/default-response.js";
import DefaultError from "../../js/entity/default-error.js";

describe("servicios contra la API real", function () {
  this.timeout(20000);

  before(function () {
    return fetch(`${API}/roles?first=0&max=1`).catch(() => {
      throw new Error(`El backend no responde en ${API}. Levántelo antes de correr estas pruebas.`);
    });
  });

  it("findRange devuelve un DefaultResponse con la página pedida y el total del servidor", function () {
    return servicios.clinicas.findRange(0, 2).then((respuesta) => {
      expect(respuesta).to.be.instanceOf(DefaultResponse);
      expect(respuesta.datos.length).to.be.at.most(2);
      expect(respuesta.total).to.be.at.least(respuesta.datos.length);
      for (const clinica of respuesta.datos) expect(clinica).to.have.property("idClinica");
    });
  });

  it("findAll trae tantos registros como anuncia X-Total-Count, sin repetir ninguno", function () {
    return Promise.all([servicios.roles.findRange(0, 1), servicios.roles.findAll()]).then(([pagina, todos]) => {
      expect(todos).to.have.length(pagina.total);
      expect(new Set(todos.map((rol) => rol.idRol)).size).to.equal(todos.length);
    });
  });

  it("cada recurso de RECURSOS existe en la API y sus registros traen el campo id declarado", function () {
    const claves = Object.keys(RECURSOS);
    return Promise.all(claves.map((clave) => servicios[clave].findRange(0, 1))).then((respuestas) => {
      respuestas.forEach((respuesta, indice) => {
        const { ruta, id } = RECURSOS[claves[indice]];
        expect(respuesta.total, ruta).to.be.at.least(0);
        if (respuesta.datos.length > 0) expect(respuesta.datos[0], ruta).to.have.property(id);
      });
    });
  });

  it("un rango inválido se rechaza con un DefaultError con el estado que devolvió la API", function () {
    return servicios.roles.findRange(-1, 5).then(
      () => {
        throw new Error("Se esperaba que la API rechazara first=-1");
      },
      (error) => {
        expect(error).to.be.instanceOf(DefaultError);
        expect(error.status).to.be.within(400, 499);
        expect(error.message).to.not.equal("");
      }
    );
  });
});
