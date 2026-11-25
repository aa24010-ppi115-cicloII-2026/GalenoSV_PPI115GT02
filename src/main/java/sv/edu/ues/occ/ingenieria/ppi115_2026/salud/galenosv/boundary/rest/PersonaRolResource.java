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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/** Expone operaciones REST de PersonaRol siguiendo el contrato de la clase. */
@Path("/personas-rol")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonaRolResource extends DefaultResource<PersonaRol, PersonaRolDTO> {

    @Inject
    private PersonaRolDAO dao;

    @Override
    protected DefaultDAO<PersonaRol> dao() {
        return dao;
    }
    @Inject
    private ClinicaDAO clinicaDAO;
    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private RolDAO rolDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(PersonaRolDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            PersonaRol entidad = new PersonaRol(UUID.randomUUID());
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdPersonaRol().toString()).build();
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
    public Response modificar(@PathParam("id") String id, PersonaRolDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            PersonaRol entidad = dao.find(uuid);
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
    protected PersonaRolDTO aDTO(PersonaRol entidad) {
        PersonaRolDTO dto = new PersonaRolDTO();
        dto.setIdPersonaRol(entidad.getIdPersonaRol() == null ? null : entidad.getIdPersonaRol().toString());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setIdClinica(entidad.getIdClinica() == null ? null : entidad.getIdClinica().getIdClinica().toString());
        dto.setIdPersona(entidad.getIdPersona() == null ? null : entidad.getIdPersona().getIdPersona().toString());
        dto.setIdRol(entidad.getIdRol() == null ? null : entidad.getIdRol().getIdRol().toString());
        return dto;
    }

    private void copiar(PersonaRolDTO datos, PersonaRol entidad) {
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getIdClinica() != null && !datos.getIdClinica().isBlank()) entidad.setIdClinica(clinica(datos.getIdClinica()));
        if (datos.getIdPersona() != null && !datos.getIdPersona().isBlank()) entidad.setIdPersona(persona(datos.getIdPersona()));
        if (datos.getIdRol() != null && !datos.getIdRol().isBlank()) entidad.setIdRol(rol(datos.getIdRol()));
    }

    private String validar(PersonaRolDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private Clinica clinica(String id) {
        Clinica encontrada = clinicaDAO.find(RestUtil.uuid(id, "idClinica"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idClinica", "No existe el idClinica indicado");
        }
        return encontrada;
    }
    private Persona persona(String id) {
        Persona encontrada = personaDAO.find(RestUtil.uuid(id, "idPersona"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idPersona", "No existe el idPersona indicado");
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
