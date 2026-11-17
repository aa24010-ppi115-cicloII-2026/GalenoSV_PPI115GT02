package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/** Expone operaciones REST de ProcedimientoPaso siguiendo el contrato de la clase. */
@Path("/procedimientos-paso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedimientoPasoResource {

    @Inject
    private ProcedimientoPasoDAO dao;
    @Inject
    private ProcedimientoDAO procedimientoDAO;
    @Inject
    private RolDAO rolDAO;

    @Context
    private UriInfo uriInfo;

    @OPTIONS
    public Response opciones() {
        return Response.noContent().build();
    }

    /** Lista registros paginados; X-Total-Count evita una peticion extra de conteo. */
    @GET
    public Response listar(@QueryParam("first") Integer first, @QueryParam("max") Integer max) {
        int inicio = first == null ? 0 : first;
        int limite = max == null ? 20 : max;
        if (inicio < 0 || limite < 1 || limite > 100) {
            return RestUtil.error(422, "first/max", "first debe ser >= 0 y max entre 1 y 100");
        }
        try {
            List<ProcedimientoPasoDTO> datos = dao.findRange(inicio, limite).stream().map(this::aDTO).toList();
            return Response.ok(datos).header("X-Total-Count", dao.count()).build();
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
            ProcedimientoPaso entidad = dao.find(uuid);
            return entidad == null ? RestUtil.error(404, "id", "No existe el registro")
                    : Response.ok(aDTO(entidad)).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ProcedimientoPasoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ProcedimientoPaso entidad = new ProcedimientoPaso(UUID.randomUUID());
            entidad.setIndicaFin(Boolean.FALSE);
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdProcedimientoPaso().toString()).build();
            return Response.created(location).build();
        } catch (ParametroInvalidoException ex) {
            return RestUtil.error(422, ex.getParametro(), ex.getMessage());
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo crear el registro");
        }
    }

    /** Actualiza los campos enviados del registro indicado en la ruta. */
    @PUT
    @Path("/{id}")
    public Response modificar(@PathParam("id") String id, ProcedimientoPasoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ProcedimientoPaso entidad = dao.find(uuid);
            if (entidad == null) return RestUtil.error(404, "id", "No existe el registro");
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            copiar(datos, entidad);
            dao.modificar(entidad);
            return Response.noContent().build();
        } catch (ParametroInvalidoException ex) {
            return RestUtil.error(422, ex.getParametro(), ex.getMessage());
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo modificar el registro");
        }
    }

    /** Elimina el registro indicado en la ruta. */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") String id) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ProcedimientoPaso entidad = dao.find(uuid);
            if (entidad == null) return RestUtil.error(404, "id", "No existe el registro");
            dao.eliminar(entidad);
            return Response.noContent().build();
        } catch (RuntimeException ex) {
            if (RestUtil.esConflicto(ex)) {
                return RestUtil.error(409, "id", "El registro esta relacionado y no se puede eliminar");
            }
            return RestUtil.error(500, "", "No se pudo eliminar el registro");
        }
    }

    private ProcedimientoPasoDTO aDTO(ProcedimientoPaso entidad) {
        ProcedimientoPasoDTO dto = new ProcedimientoPasoDTO();
        dto.setIdProcedimientoPaso(entidad.getIdProcedimientoPaso() == null ? null : entidad.getIdProcedimientoPaso().toString());
        dto.setNombre(entidad.getNombre());
        dto.setIndicaFin(entidad.getIndicaFin());
        dto.setIdProcedimiento(entidad.getIdProcedimiento() == null ? null : entidad.getIdProcedimiento().getIdProcedimiento().toString());
        dto.setIdRol(entidad.getIdRol() == null ? null : entidad.getIdRol().getIdRol().toString());
        return dto;
    }

    private void copiar(ProcedimientoPasoDTO datos, ProcedimientoPaso entidad) {
        if (datos.getNombre() != null) entidad.setNombre(datos.getNombre().trim());
        if (datos.getIndicaFin() != null) entidad.setIndicaFin(datos.getIndicaFin());
        if (datos.getIdProcedimiento() != null && !datos.getIdProcedimiento().isBlank()) entidad.setIdProcedimiento(procedimiento(datos.getIdProcedimiento()));
        if (datos.getIdRol() != null && !datos.getIdRol().isBlank()) entidad.setIdRol(rol(datos.getIdRol()));
    }

    private String validar(ProcedimientoPasoDTO datos) {
        if (datos == null) return "body";
        if (datos.getNombre() == null || datos.getNombre().isBlank()) return "nombre";
        if (datos.getNombre().trim().length() > 155) return "nombre";
        return null;
    }

    private Procedimiento procedimiento(String id) {
        Procedimiento encontrada = procedimientoDAO.find(RestUtil.uuid(id, "idProcedimiento"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idProcedimiento", "No existe el idProcedimiento indicado");
        }
        return encontrada;
    }
    private Rol rol(String id) {
        Rol encontrada = rolDAO.find(RestUtil.uuid(id, "idRol"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idRol", "No existe el idRol indicado");
        }
        return encontrada;
    }
}
