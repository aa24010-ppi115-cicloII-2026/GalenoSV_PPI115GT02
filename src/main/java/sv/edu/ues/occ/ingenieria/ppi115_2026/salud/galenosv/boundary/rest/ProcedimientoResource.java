package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;

/** Expone operaciones REST de Procedimiento siguiendo el contrato de la clase. */
@Path("/procedimientos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedimientoResource extends DefaultResource<Procedimiento, ProcedimientoDTO> {

    @Inject
    private ProcedimientoDAO dao;

    @Override
    protected DefaultDAO<Procedimiento> dao() {
        return dao;
    }

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ProcedimientoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");
            if (dao.existeNombre(datos.getNombre(), null)) {
                return RestUtil.error(422, "nombre", "Ya existe un registro con ese nombre");
            }
            Procedimiento entidad = new Procedimiento(UUID.randomUUID());
            entidad.setActivo(Boolean.TRUE);
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdProcedimiento().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ProcedimientoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            Procedimiento entidad = dao.find(uuid);
            if (entidad == null) return RestUtil.error(404, "id", "No existe el registro");
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");
            if (dao.existeNombre(datos.getNombre(), uuid)) {
                return RestUtil.error(422, "nombre", "Ya existe un registro con ese nombre");
            }
            copiar(datos, entidad);
            dao.modificar(entidad);
            return Response.noContent().build();
        } catch (ParametroInvalidoException ex) {
            return RestUtil.error(422, ex.getParametro(), ex.getMessage());
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo modificar el registro");
        }
    }

    @Override
    protected ProcedimientoDTO aDTO(Procedimiento entidad) {
        ProcedimientoDTO dto = new ProcedimientoDTO();
        dto.setIdProcedimiento(entidad.getIdProcedimiento() == null ? null : entidad.getIdProcedimiento().toString());
        dto.setNombre(entidad.getNombre());
        dto.setActivo(entidad.getActivo());
        dto.setObservaciones(entidad.getObservaciones());
        return dto;
    }

    private void copiar(ProcedimientoDTO datos, Procedimiento entidad) {
        if (datos.getNombre() != null) entidad.setNombre(datos.getNombre().trim());
        if (datos.getActivo() != null) entidad.setActivo(datos.getActivo());
        if (datos.getObservaciones() != null) entidad.setObservaciones(datos.getObservaciones().trim());
    }

    private String validar(ProcedimientoDTO datos) {
        if (datos == null) return "body";
        if (datos.getNombre() == null || datos.getNombre().isBlank()) return "nombre";
        if (datos.getNombre().trim().length() > 155) return "nombre";
        return null;
    }
}
