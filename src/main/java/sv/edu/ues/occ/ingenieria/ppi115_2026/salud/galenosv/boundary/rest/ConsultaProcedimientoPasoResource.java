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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

/** Expone operaciones REST de ConsultaProcedimientoPaso siguiendo el contrato de la clase. */
@Path("/consultas-procedimiento-paso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsultaProcedimientoPasoResource extends DefaultResource<ConsultaProcedimientoPaso, ConsultaProcedimientoPasoDTO> {

    @Inject
    private ConsultaProcedimientoPasoDAO dao;

    @Override
    protected DefaultDAO<ConsultaProcedimientoPaso> dao() {
        return dao;
    }
    @Inject
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject
    private PersonaRolDAO personaRolDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ConsultaProcedimientoPasoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            UUID id = RestUtil.idNuevo(datos.getIdConsultaProcedimientoPaso(), "idConsultaProcedimientoPaso");
            if (dao.find(id) != null) {
                // Reintento de un cambio ya guardado: se responde igual, sin duplicarlo.
                return Response.created(uriInfo.getAbsolutePathBuilder().path(id.toString()).build()).build();
            }
            ConsultaProcedimientoPaso entidad = new ConsultaProcedimientoPaso(id);
            entidad.setFechaInicio(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdConsultaProcedimientoPaso().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ConsultaProcedimientoPasoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ConsultaProcedimientoPaso entidad = dao.find(uuid);
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
    protected ConsultaProcedimientoPasoDTO aDTO(ConsultaProcedimientoPaso entidad) {
        ConsultaProcedimientoPasoDTO dto = new ConsultaProcedimientoPasoDTO();
        dto.setIdConsultaProcedimientoPaso(entidad.getIdConsultaProcedimientoPaso() == null ? null : entidad.getIdConsultaProcedimientoPaso().toString());
        dto.setFechaInicio(entidad.getFechaInicio() == null ? null : entidad.getFechaInicio().toString());
        dto.setFechaFin(entidad.getFechaFin() == null ? null : entidad.getFechaFin().toString());
        dto.setEstado(entidad.getEstado());
        dto.setIdConsultaProcedimiento(entidad.getIdConsultaProcedimiento() == null ? null : entidad.getIdConsultaProcedimiento().getIdConsultaProcedimiento().toString());
        dto.setIdPersonaRol(entidad.getIdPersonaRol() == null ? null : entidad.getIdPersonaRol().getIdPersonaRol().toString());
        return dto;
    }

    private void copiar(ConsultaProcedimientoPasoDTO datos, ConsultaProcedimientoPaso entidad) {
        if (datos.getFechaInicio() != null) entidad.setFechaInicio(RestUtil.fecha(datos.getFechaInicio(), "fechaInicio"));
        if (datos.getFechaFin() != null) entidad.setFechaFin(RestUtil.fecha(datos.getFechaFin(), "fechaFin"));
        if (datos.getEstado() != null) entidad.setEstado(datos.getEstado().trim());
        if (datos.getIdConsultaProcedimiento() != null && !datos.getIdConsultaProcedimiento().isBlank()) entidad.setIdConsultaProcedimiento(consultaProcedimiento(datos.getIdConsultaProcedimiento()));
        if (datos.getIdPersonaRol() != null && !datos.getIdPersonaRol().isBlank()) entidad.setIdPersonaRol(personaRol(datos.getIdPersonaRol()));
    }

    private String validar(ConsultaProcedimientoPasoDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private ConsultaProcedimiento consultaProcedimiento(String id) {
        ConsultaProcedimiento encontrada = consultaProcedimientoDAO.find(RestUtil.uuid(id, "idConsultaProcedimiento"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idConsultaProcedimiento", "No existe el idConsultaProcedimiento indicado");
        }
        return encontrada;
    }
    private PersonaRol personaRol(String id) {
        PersonaRol encontrada = personaRolDAO.find(RestUtil.uuid(id, "idPersonaRol"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idPersonaRol", "No existe el idPersonaRol indicado");
        }
        return encontrada;
    }
}
