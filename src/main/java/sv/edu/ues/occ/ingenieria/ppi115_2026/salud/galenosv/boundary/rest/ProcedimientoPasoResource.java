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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/** Expone operaciones REST de ProcedimientoPaso siguiendo el contrato de la clase. */
@Path("/procedimientos-paso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedimientoPasoResource extends DefaultResource<ProcedimientoPaso, ProcedimientoPasoDTO> {

    @Inject
    private ProcedimientoPasoDAO dao;

    @Override
    protected DefaultDAO<ProcedimientoPaso> dao() {
        return dao;
    }
    @Inject
    private ProcedimientoDAO procedimientoDAO;
    @Inject
    private RolDAO rolDAO;

    @Context
    private UriInfo uriInfo;

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

    @Override
    protected ProcedimientoPasoDTO aDTO(ProcedimientoPaso entidad) {
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
