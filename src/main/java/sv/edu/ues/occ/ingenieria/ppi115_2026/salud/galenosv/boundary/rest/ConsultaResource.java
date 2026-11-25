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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

/** Expone operaciones REST de Consulta siguiendo el contrato de la clase. */
@Path("/consultas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsultaResource extends DefaultResource<Consulta, ConsultaDTO> {

    @Inject
    private ConsultaDAO dao;

    @Override
    protected DefaultDAO<Consulta> dao() {
        return dao;
    }
    @Inject
    private PersonaRolDAO personaRolDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ConsultaDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            Consulta entidad = new Consulta(UUID.randomUUID());
            entidad.setFechaInicio(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdConsulta().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ConsultaDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            Consulta entidad = dao.find(uuid);
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
    protected ConsultaDTO aDTO(Consulta entidad) {
        ConsultaDTO dto = new ConsultaDTO();
        dto.setIdConsulta(entidad.getIdConsulta() == null ? null : entidad.getIdConsulta().toString());
        dto.setFechaInicio(entidad.getFechaInicio() == null ? null : entidad.getFechaInicio().toString());
        dto.setFechaFin(entidad.getFechaFin() == null ? null : entidad.getFechaFin().toString());
        dto.setReferenciaExterna(entidad.getReferenciaExterna());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setIdPersonaRol(entidad.getIdPersonaRol() == null ? null : entidad.getIdPersonaRol().getIdPersonaRol().toString());
        return dto;
    }

    private void copiar(ConsultaDTO datos, Consulta entidad) {
        if (datos.getFechaInicio() != null) entidad.setFechaInicio(RestUtil.fecha(datos.getFechaInicio(), "fechaInicio"));
        if (datos.getFechaFin() != null) entidad.setFechaFin(RestUtil.fecha(datos.getFechaFin(), "fechaFin"));
        if (datos.getReferenciaExterna() != null) entidad.setReferenciaExterna(datos.getReferenciaExterna().trim());
        if (datos.getObservaciones() != null) entidad.setObservaciones(datos.getObservaciones().trim());
        if (datos.getIdPersonaRol() != null && !datos.getIdPersonaRol().isBlank()) entidad.setIdPersonaRol(personaRol(datos.getIdPersonaRol()));
    }

    private String validar(ConsultaDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private PersonaRol personaRol(String id) {
        PersonaRol encontrada = personaRolDAO.find(RestUtil.uuid(id, "idPersonaRol"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idPersonaRol", "No existe el idPersonaRol indicado");
        }
        return encontrada;
    }
}
