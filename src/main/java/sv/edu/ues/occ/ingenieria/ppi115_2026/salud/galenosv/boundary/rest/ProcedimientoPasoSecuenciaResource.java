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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/** Expone operaciones REST de ProcedimientoPasoSecuencia siguiendo el contrato de la clase. */
@Path("/procedimientos-paso-secuencia")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcedimientoPasoSecuenciaResource extends DefaultResource<ProcedimientoPasoSecuencia, ProcedimientoPasoSecuenciaDTO> {

    @Inject
    private ProcedimientoPasoSecuenciaDAO dao;

    @Override
    protected DefaultDAO<ProcedimientoPasoSecuencia> dao() {
        return dao;
    }
    @Inject
    private ProcedimientoPasoDAO procedimientoPasoDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ProcedimientoPasoSecuenciaDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ProcedimientoPasoSecuencia entidad = new ProcedimientoPasoSecuencia(UUID.randomUUID());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdProcedimientoPasoSecuencia().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ProcedimientoPasoSecuenciaDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ProcedimientoPasoSecuencia entidad = dao.find(uuid);
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
    protected ProcedimientoPasoSecuenciaDTO aDTO(ProcedimientoPasoSecuencia entidad) {
        ProcedimientoPasoSecuenciaDTO dto = new ProcedimientoPasoSecuenciaDTO();
        dto.setIdProcedimientoPasoSecuencia(entidad.getIdProcedimientoPasoSecuencia() == null ? null : entidad.getIdProcedimientoPasoSecuencia().toString());
        dto.setIdProcedimientoPasoReferencia(entidad.getIdProcedimientoPasoReferencia() == null ? null : entidad.getIdProcedimientoPasoReferencia().toString());
        dto.setTipoSecuencia(entidad.getTipoSecuencia());
        dto.setIdProcedimientoPaso(entidad.getIdProcedimientoPaso() == null ? null : entidad.getIdProcedimientoPaso().getIdProcedimientoPaso().toString());
        return dto;
    }

    private void copiar(ProcedimientoPasoSecuenciaDTO datos, ProcedimientoPasoSecuencia entidad) {
        if (datos.getIdProcedimientoPasoReferencia() != null) entidad.setIdProcedimientoPasoReferencia(RestUtil.uuid(datos.getIdProcedimientoPasoReferencia(), "idProcedimientoPasoReferencia"));
        if (datos.getTipoSecuencia() != null) entidad.setTipoSecuencia(datos.getTipoSecuencia().trim());
        if (datos.getIdProcedimientoPaso() != null && !datos.getIdProcedimientoPaso().isBlank()) entidad.setIdProcedimientoPaso(procedimientoPaso(datos.getIdProcedimientoPaso()));
    }

    private String validar(ProcedimientoPasoSecuenciaDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private ProcedimientoPaso procedimientoPaso(String id) {
        ProcedimientoPaso encontrada = procedimientoPasoDAO.find(RestUtil.uuid(id, "idProcedimientoPaso"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idProcedimientoPaso", "No existe el idProcedimientoPaso indicado");
        }
        return encontrada;
    }
}
