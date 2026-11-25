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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenTipoExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

/** Expone operaciones REST de ExamenTipoExamen siguiendo el contrato de la clase. */
@Path("/examenes-tipo-examen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamenTipoExamenResource extends DefaultResource<ExamenTipoExamen, ExamenTipoExamenDTO> {

    @Inject
    private ExamenTipoExamenDAO dao;

    @Override
    protected DefaultDAO<ExamenTipoExamen> dao() {
        return dao;
    }
    @Inject
    private ExamenDAO examenDAO;
    @Inject
    private TipoExamenDAO tipoExamenDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ExamenTipoExamenDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");
            if (datos.getIdExamen() != null && datos.getIdTipoExamen() != null) {
                UUID idExamen = RestUtil.uuid(datos.getIdExamen(), "idExamen");
                UUID idTipoExamen = RestUtil.uuid(datos.getIdTipoExamen(), "idTipoExamen");
                if (dao.existeVinculo(idExamen, idTipoExamen, null)) {
                    return RestUtil.error(422, "idTipoExamen", "El examen ya tiene vinculado ese tipo");
                }
            }
            ExamenTipoExamen entidad = new ExamenTipoExamen(UUID.randomUUID());
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdExamenTipoExamen().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ExamenTipoExamenDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ExamenTipoExamen entidad = dao.find(uuid);
            if (entidad == null) return RestUtil.error(404, "id", "No existe el registro");
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");
            if (datos.getIdExamen() != null && datos.getIdTipoExamen() != null) {
                UUID idExamen = RestUtil.uuid(datos.getIdExamen(), "idExamen");
                UUID idTipoExamen = RestUtil.uuid(datos.getIdTipoExamen(), "idTipoExamen");
                if (dao.existeVinculo(idExamen, idTipoExamen, uuid)) {
                    return RestUtil.error(422, "idTipoExamen", "El examen ya tiene vinculado ese tipo");
                }
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
    protected ExamenTipoExamenDTO aDTO(ExamenTipoExamen entidad) {
        ExamenTipoExamenDTO dto = new ExamenTipoExamenDTO();
        dto.setIdExamenTipoExamen(entidad.getIdExamenTipoExamen() == null ? null : entidad.getIdExamenTipoExamen().toString());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setIdExamen(entidad.getIdExamen() == null ? null : entidad.getIdExamen().getIdExamen().toString());
        dto.setIdTipoExamen(entidad.getIdTipoExamen() == null ? null : entidad.getIdTipoExamen().getIdTipoExamen().toString());
        return dto;
    }

    private void copiar(ExamenTipoExamenDTO datos, ExamenTipoExamen entidad) {
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getObservaciones() != null) entidad.setObservaciones(datos.getObservaciones().trim());
        if (datos.getIdExamen() != null && !datos.getIdExamen().isBlank()) entidad.setIdExamen(examen(datos.getIdExamen()));
        if (datos.getIdTipoExamen() != null && !datos.getIdTipoExamen().isBlank()) entidad.setIdTipoExamen(tipoExamen(datos.getIdTipoExamen()));
    }

    private String validar(ExamenTipoExamenDTO datos) {
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
    private TipoExamen tipoExamen(String id) {
        TipoExamen encontrada = tipoExamenDAO.find(RestUtil.uuid(id, "idTipoExamen"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idTipoExamen", "No existe el idTipoExamen indicado");
        }
        return encontrada;
    }
}
