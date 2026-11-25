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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/** Expone operaciones REST de OrdenExamen siguiendo el contrato de la clase. */
@Path("/ordenes-examen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenExamenResource extends DefaultResource<OrdenExamen, OrdenExamenDTO> {

    @Inject
    private OrdenExamenDAO dao;

    @Override
    protected DefaultDAO<OrdenExamen> dao() {
        return dao;
    }
    @Inject
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(OrdenExamenDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            UUID id = RestUtil.idNuevo(datos.getIdOrdenExamen(), "idOrdenExamen");
            if (dao.find(id) != null) {
                // Reintento de un cambio ya guardado: se responde igual, sin duplicarlo.
                return Response.created(uriInfo.getAbsolutePathBuilder().path(id.toString()).build()).build();
            }
            OrdenExamen entidad = new OrdenExamen(id);
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdOrdenExamen().toString()).build();
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
    public Response modificar(@PathParam("id") String id, OrdenExamenDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            OrdenExamen entidad = dao.find(uuid);
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
    protected OrdenExamenDTO aDTO(OrdenExamen entidad) {
        OrdenExamenDTO dto = new OrdenExamenDTO();
        dto.setIdOrdenExamen(entidad.getIdOrdenExamen() == null ? null : entidad.getIdOrdenExamen().toString());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setIndicaciones(entidad.getIndicaciones());
        dto.setIdConsultaProcedimientoPaso(entidad.getIdConsultaProcedimientoPaso() == null ? null : entidad.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso().toString());
        return dto;
    }

    private void copiar(OrdenExamenDTO datos, OrdenExamen entidad) {
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getIndicaciones() != null) entidad.setIndicaciones(datos.getIndicaciones().trim());
        if (datos.getIdConsultaProcedimientoPaso() != null && !datos.getIdConsultaProcedimientoPaso().isBlank()) entidad.setIdConsultaProcedimientoPaso(consultaProcedimientoPaso(datos.getIdConsultaProcedimientoPaso()));
    }

    private String validar(OrdenExamenDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private ConsultaProcedimientoPaso consultaProcedimientoPaso(String id) {
        ConsultaProcedimientoPaso encontrada = consultaProcedimientoPasoDAO.find(RestUtil.uuid(id, "idConsultaProcedimientoPaso"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idConsultaProcedimientoPaso", "No existe el idConsultaProcedimientoPaso indicado");
        }
        return encontrada;
    }
}
