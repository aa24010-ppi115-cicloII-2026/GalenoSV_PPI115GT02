/** Respuesta de una búsqueda paginada: los registros de la página y el total del recurso. */
export default class DefaultResponse {
  /**
   * @param {object[]} [datos] registros de la página
   * @param {number} [total] total de registros según la cabecera X-Total-Count; -1 si aún no se conoce
   */
  constructor(datos = [], total = -1) {
    this.datos = datos;
    this.total = total;
  }
}
