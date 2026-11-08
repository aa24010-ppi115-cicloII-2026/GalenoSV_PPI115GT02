package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

/** Expone operaciones REST de ConsultaProcedimientoPaso siguiendo el contrato de la clase. */
@Path("/consultas-procedimiento-paso")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsultaProcedimientoPasoResource {

    @Inject
    private ConsultaProcedimientoPasoDAO dao;
    @Inject
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject
    private PersonaRolDAO personaRolDAO;

    @Context
    private UriInfo uriInfo;

    @OPTIONS
    public Response opciones() {
        return Response.noContent().build();
    }

    /** Lista registros paginados; X-Total-Count evita una peticion extra de conteo. */
    @GET
    public Response listar(@QueryParam("first") Integer first, @QueryParam("max") Integer max) {
        int inicio = first == null ? 0 : first;
        int limite = max == null ? 20 : max;
        if (inicio < 0 || limite < 1 || limite > 100) {
            return RestUtil.error(422, "first/max", "first debe ser >= 0 y max entre 1 y 100");
        }
        try {
            List<ConsultaProcedimientoPasoDTO> datos = dao.findRange(inicio, limite).stream().map(this::aDTO).toList();
            return Response.ok(datos).header("X-Total-Count", dao.count()).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Busca un registro por su UUID. */
    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") String id) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ConsultaProcedimientoPaso entidad = dao.find(uuid);
            return entidad == null ? RestUtil.error(404, "id", "No existe el registro")
                    : Response.ok(aDTO(entidad)).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ConsultaProcedimientoPasoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ConsultaProcedimientoPaso entidad = new ConsultaProcedimientoPaso(UUID.randomUUID());
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

    /** Elimina el registro indicado en la ruta. */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") String id) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ConsultaProcedimientoPaso entidad = dao.find(uuid);
            if (entidad == null) return RestUtil.error(404, "id", "No existe el registro");
            dao.eliminar(entidad);
            return Response.noContent().build();
        } catch (RuntimeException ex) {
            if (RestUtil.esConflicto(ex)) {
                return RestUtil.error(409, "id", "El registro esta relacionado y no se puede eliminar");
            }
            return RestUtil.error(500, "", "No se pudo eliminar el registro");
        }
    }

    private ConsultaProcedimientoPasoDTO aDTO(ConsultaProcedimientoPaso entidad) {
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
