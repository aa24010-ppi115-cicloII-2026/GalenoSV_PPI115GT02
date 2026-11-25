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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/** Expone operaciones REST de ProcedimientoPasoExamen siguiendo el contrato de la clase. */
@Path("/procedimientos-paso-examen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedimientoPasoExamenResource extends DefaultResource<ProcedimientoPasoExamen, ProcedimientoPasoExamenDTO> {

    @Inject
    private ProcedimientoPasoExamenDAO dao;

    @Override
    protected DefaultDAO<ProcedimientoPasoExamen> dao() {
        return dao;
    }
    @Inject
    private ExamenDAO examenDAO;
    @Inject
    private ProcedimientoPasoDAO procedimientoPasoDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ProcedimientoPasoExamenDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ProcedimientoPasoExamen entidad = new ProcedimientoPasoExamen(UUID.randomUUID());
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            entidad.setActivo(Boolean.TRUE);
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdProcedimientoPasoExamen().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ProcedimientoPasoExamenDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ProcedimientoPasoExamen entidad = dao.find(uuid);
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
    protected ProcedimientoPasoExamenDTO aDTO(ProcedimientoPasoExamen entidad) {
        ProcedimientoPasoExamenDTO dto = new ProcedimientoPasoExamenDTO();
        dto.setIdProcedimientoPasoExamen(entidad.getIdProcedimientoPasoExamen() == null ? null : entidad.getIdProcedimientoPasoExamen().toString());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setActivo(entidad.getActivo());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setIdExamen(entidad.getIdExamen() == null ? null : entidad.getIdExamen().getIdExamen().toString());
        dto.setIdProcedimientoPaso(entidad.getIdProcedimientoPaso() == null ? null : entidad.getIdProcedimientoPaso().getIdProcedimientoPaso().toString());
        return dto;
    }

    private void copiar(ProcedimientoPasoExamenDTO datos, ProcedimientoPasoExamen entidad) {
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getActivo() != null) entidad.setActivo(datos.getActivo());
        if (datos.getObservaciones() != null) entidad.setObservaciones(datos.getObservaciones().trim());
        if (datos.getIdExamen() != null && !datos.getIdExamen().isBlank()) entidad.setIdExamen(examen(datos.getIdExamen()));
        if (datos.getIdProcedimientoPaso() != null && !datos.getIdProcedimientoPaso().isBlank()) entidad.setIdProcedimientoPaso(procedimientoPaso(datos.getIdProcedimientoPaso()));
    }

    private String validar(ProcedimientoPasoExamenDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private Examen examen(String id) {
        Examen encontrada = examenDAO.find(RestUtil.uuid(id, "idExamen"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idExamen", "No existe el idExamen indicado");
        }
        return encontrada;
    }
    private ProcedimientoPaso procedimientoPaso(String id) {
        ProcedimientoPaso encontrada = procedimientoPasoDAO.find(RestUtil.uuid(id, "idProcedimientoPaso"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idProcedimientoPaso", "No existe el idProcedimientoPaso indicado");
        }
        return encontrada;
    }
}
