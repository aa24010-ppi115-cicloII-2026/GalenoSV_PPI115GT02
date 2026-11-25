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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenResultado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/** Expone operaciones REST de ExamenResultado siguiendo el contrato de la clase. */
@Path("/examenes-resultado")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamenResultadoResource extends DefaultResource<ExamenResultado, ExamenResultadoDTO> {

    @Inject
    private ExamenResultadoDAO dao;

    @Override
    protected DefaultDAO<ExamenResultado> dao() {
        return dao;
    }
    @Inject
    private OrdenExamenDAO ordenExamenDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ExamenResultadoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ExamenResultado entidad = new ExamenResultado(UUID.randomUUID());
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdExamenResultado().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ExamenResultadoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ExamenResultado entidad = dao.find(uuid);
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
    protected ExamenResultadoDTO aDTO(ExamenResultado entidad) {
        ExamenResultadoDTO dto = new ExamenResultadoDTO();
        dto.setIdExamenResultado(entidad.getIdExamenResultado() == null ? null : entidad.getIdExamenResultado().toString());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setResultado(entidad.getResultado());
        dto.setInterpretacion(entidad.getInterpretacion());
        dto.setRutaAtestado(entidad.getRutaAtestado());
        dto.setIdOrdenExamen(entidad.getIdOrdenExamen() == null ? null : entidad.getIdOrdenExamen().getIdOrdenExamen().toString());
        return dto;
    }

    private void copiar(ExamenResultadoDTO datos, ExamenResultado entidad) {
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getResultado() != null) entidad.setResultado(datos.getResultado().trim());
        if (datos.getInterpretacion() != null) entidad.setInterpretacion(datos.getInterpretacion().trim());
        if (datos.getRutaAtestado() != null) entidad.setRutaAtestado(datos.getRutaAtestado().trim());
        if (datos.getIdOrdenExamen() != null && !datos.getIdOrdenExamen().isBlank()) entidad.setIdOrdenExamen(ordenExamen(datos.getIdOrdenExamen()));
    }

    private String validar(ExamenResultadoDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private OrdenExamen ordenExamen(String id) {
        OrdenExamen encontrada = ordenExamenDAO.find(RestUtil.uuid(id, "idOrdenExamen"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idOrdenExamen", "No existe el idOrdenExamen indicado");
        }
        return encontrada;
    }
}
