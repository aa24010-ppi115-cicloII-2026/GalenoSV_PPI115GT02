/**
 * Error con que se rechazan las promesas del acceso a datos.
 * Hereda de Error para que el mensaje y la traza se vean igual en la consola.
 */
export default class DefaultError extends Error {
  /**
   * @param {string} [mensaje] texto que se le muestra a la persona
   * @param {{status?: number|null, parametro?: string|null, error?: unknown}} [detalle] estado HTTP
   *   si la API respondió (null si no hubo respuesta), el campo que la API rechazó y el error
   *   original, para quien quiera ver más detalle
   */
  constructor(mensaje = "", { status = null, parametro = null, error = null } = {}) {
    super(mensaje);
    this.name = "DefaultError";
    this.status = status;
    this.parametro = parametro;
    this.error = error;
  }
}
