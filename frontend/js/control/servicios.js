import DefaultService from "./default-service.js";
import { RECURSOS } from "../entity/recursos.js";

/**
 * Un servicio por recurso de la API, con la misma clave que usa la app para sus tablas:
 *   servicios.clinicas.findAll();   servicios.ordenesExamen.eliminar(id);
 * Todos usan DefaultService tal cual, porque ningún recurso necesita un comportamiento propio.
 * Si alguno lo necesitara, bastaría una subclase (`class ConsultaService extends DefaultService`).
 */
export const servicios = Object.fromEntries(
  Object.entries(RECURSOS).map(([clave, { ruta }]) => [clave, new DefaultService(ruta)])
);
