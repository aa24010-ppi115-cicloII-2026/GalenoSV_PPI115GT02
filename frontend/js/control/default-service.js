import { pedir, mutar } from "./api.js";
import DefaultResponse from "../entity/default-response.js";
import DefaultError from "../entity/default-error.js";

/** Registros que pide findAll en cada página. */
const TAMANO_PAGINA = 100;

/**
 * Acceso a un recurso REST de la API, con los mismos métodos que DefaultDAO en el backend.
 * Cada recurso es una instancia con su ruta (ver servicios.js); todo devuelve promesas que se
 * rechazan con un DefaultError.
 */
export default class DefaultService {
  /**
   * @param {string} ruta recurso relativo a la API, por ejemplo "clinicas"
   */
  constructor(ruta) {
    this.ruta = ruta;
  }

  /**
   * Busca un conjunto de registros en el repositorio usando paginación.
   * @param {number} [first] primer registro a obtener
   * @param {number} [max] cantidad máxima de registros a obtener
   * @returns {Promise<DefaultResponse>} se resuelve con los registros y el total de X-Total-Count;
   *   se rechaza con un DefaultError si la API falla, falta el total o el cuerpo no es una lista
   */
  findRange(first = 0, max = 20) {
    return pedir(`${this.ruta}?first=${first}&max=${max}`).then((respuesta) => {
      const cabecera = respuesta.headers.get("X-Total-Count");
      const total = Number(cabecera);
      if (!/^\d+$/.test(cabecera ?? "") || !Number.isSafeInteger(total)) {
        throw new DefaultError(`La respuesta de ${this.ruta} no incluye un X-Total-Count válido.`);
      }
      return respuesta
        .json()
        .catch((error) => {
          throw new DefaultError(`Error al procesar los datos de respuesta de ${this.ruta}.`, { error });
        })
        .then((datos) => {
          if (!Array.isArray(datos)) throw new DefaultError(`La respuesta de ${this.ruta} no contiene una lista.`);
          return new DefaultResponse(datos, total);
        });
    });
  }

  /**
   * Todos los registros del recurso, pidiendo página tras página hasta llegar al total.
   * @returns {Promise<object[]>} se rechaza con un DefaultError si una página llega vacía antes del total
   */
  findAll() {
    const datos = [];
    const pedirDesde = (first) =>
      this.findRange(first, TAMANO_PAGINA).then((pagina) => {
        datos.push(...pagina.datos);
        const leidos = first + pagina.datos.length;
        if (pagina.datos.length === 0 && leidos < pagina.total) {
          throw new DefaultError(`La respuesta de ${this.ruta} quedó incompleta antes de llegar a ${pagina.total} registros.`);
        }
        return leidos < pagina.total ? pedirDesde(leidos) : datos;
      });
    return pedirDesde(0);
  }

  /**
   * Crea un registro. Sin conexión, o si el servidor no responde, queda en la cola de pendientes.
   * @param {object} datos registro a crear, con su id generado en el cliente
   * @returns {Promise<string|null>} la cabecera Location, o null si quedó en la cola
   */
  crear(datos) {
    return mutar("POST", this.ruta, datos);
  }

  /**
   * Modifica un registro; sin conexión queda en la cola de pendientes.
   * @param {string} id identificador del registro
   * @param {object} datos campos a guardar
   * @returns {Promise<null>}
   */
  modificar(id, datos) {
    return mutar("PUT", `${this.ruta}/${id}`, datos);
  }

  /**
   * Elimina un registro; sin conexión queda en la cola de pendientes.
   * @param {string} id identificador del registro
   * @returns {Promise<null>}
   */
  eliminar(id) {
    return mutar("DELETE", `${this.ruta}/${id}`);
  }
}
