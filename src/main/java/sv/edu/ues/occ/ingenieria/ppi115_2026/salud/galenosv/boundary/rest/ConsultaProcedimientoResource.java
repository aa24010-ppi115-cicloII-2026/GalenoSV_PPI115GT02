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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;

/** Expone operaciones REST de ConsultaProcedimiento siguiendo el contrato de la clase. */
@Path("/consultas-procedimiento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsultaProcedimientoResource extends DefaultResource<ConsultaProcedimiento, ConsultaProcedimientoDTO> {

    @Inject
    private ConsultaProcedimientoDAO dao;

    @Override
    protected DefaultDAO<ConsultaProcedimiento> dao() {
        return dao;
    }
    @Inject
    private ConsultaDAO consultaDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ConsultaProcedimientoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ConsultaProcedimiento entidad = new ConsultaProcedimiento(UUID.randomUUID());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdConsultaProcedimiento().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ConsultaProcedimientoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ConsultaProcedimiento entidad = dao.find(uuid);
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
    protected ConsultaProcedimientoDTO aDTO(ConsultaProcedimiento entidad) {
        ConsultaProcedimientoDTO dto = new ConsultaProcedimientoDTO();
        dto.setIdConsultaProcedimiento(entidad.getIdConsultaProcedimiento() == null ? null : entidad.getIdConsultaProcedimiento().toString());
        dto.setIdProcedimiento(entidad.getIdProcedimiento() == null ? null : entidad.getIdProcedimiento().toString());
        dto.setFechaInicio(entidad.getFechaInicio() == null ? null : entidad.getFechaInicio().toString());
        dto.setFechaFin(entidad.getFechaFin() == null ? null : entidad.getFechaFin().toString());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setIdConsulta(entidad.getIdConsulta() == null ? null : entidad.getIdConsulta().getIdConsulta().toString());
        return dto;
    }

    private void copiar(ConsultaProcedimientoDTO datos, ConsultaProcedimiento entidad) {
        if (datos.getIdProcedimiento() != null) entidad.setIdProcedimiento(RestUtil.uuid(datos.getIdProcedimiento(), "idProcedimiento"));
        if (datos.getFechaInicio() != null) entidad.setFechaInicio(RestUtil.fecha(datos.getFechaInicio(), "fechaInicio"));
        if (datos.getFechaFin() != null) entidad.setFechaFin(RestUtil.fecha(datos.getFechaFin(), "fechaFin"));
        if (datos.getObservaciones() != null) entidad.setObservaciones(datos.getObservaciones().trim());
        if (datos.getIdConsulta() != null && !datos.getIdConsulta().isBlank()) entidad.setIdConsulta(consulta(datos.getIdConsulta()));
    }

    private String validar(ConsultaProcedimientoDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private Consulta consulta(String id) {
        Consulta encontrada = consultaDAO.find(RestUtil.uuid(id, "idConsulta"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idConsulta", "No existe el idConsulta indicado");
        }
        return encontrada;
    }
}
