package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;

/**
 * Operaciones REST que comparten todos los recursos, como DefaultDAO en el acceso a datos:
 * OPTIONS para CORS, listado paginado, busqueda y eliminacion por UUID. Cada recurso pone
 * su DAO, la conversion a DTO, y sus propias altas y modificaciones.
 *
 * @param <E> entidad
 * @param <D> DTO con que la entidad viaja en JSON
 */
public abstract class DefaultResource<E, D> {

    /** DAO de la entidad del recurso. */
    protected abstract DefaultDAO<E> dao();

    /** Convierte la entidad en el DTO que se envia en JSON. */
    protected abstract D aDTO(E entidad);

    @OPTIONS
    public Response opciones() {
        return Response.noContent().build();
    }

    /** Lista registros paginados; X-Total-Count evita una peticion extra de conteo. */
    @GET
    public Response listar(@QueryParam("first") Integer first, @QueryParam("max") Integer max) {
        RestUtil.Rango rango = RestUtil.rango(first, max);
        if (rango == null) {
            return RestUtil.error(422, "first/max", "first debe ser >= 0 y max entre 1 y 100");
        }
        try {
            List<D> datos = dao().findRange(rango.inicio(), rango.limite()).stream().map(this::aDTO).toList();
            return Response.ok(datos).header("X-Total-Count", dao().count()).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Busca un registro por su UUID. */
    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") String id) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            E entidad = dao().find(uuid);
            return entidad == null ? RestUtil.error(404, "id", "No existe el registro")
                    : Response.ok(aDTO(entidad)).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Elimina el registro indicado en la ruta; si otros registros lo usan responde 409. */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") String id) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            E entidad = dao().find(uuid);
            if (entidad == null) return RestUtil.error(404, "id", "No existe el registro");
            dao().eliminar(entidad);
            return Response.noContent().build();
        } catch (RuntimeException ex) {
            if (RestUtil.esConflicto(ex)) {
                return RestUtil.error(409, "id", "El registro esta relacionado y no se puede eliminar");
            }
            return RestUtil.error(500, "", "No se pudo eliminar el registro");
        }
    }
}
