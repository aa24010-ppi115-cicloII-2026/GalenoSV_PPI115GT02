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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/** Expone operaciones REST de MedioContacto siguiendo el contrato de la clase. */
@Path("/medios-contacto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MedioContactoResource extends DefaultResource<MedioContacto, MedioContactoDTO> {

    @Inject
    private MedioContactoDAO dao;

    @Override
    protected DefaultDAO<MedioContacto> dao() {
        return dao;
    }
    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(MedioContactoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            MedioContacto entidad = new MedioContacto(UUID.randomUUID());
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdMedioContacto().toString()).build();
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
    public Response modificar(@PathParam("id") String id, MedioContactoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            MedioContacto entidad = dao.find(uuid);
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
    protected MedioContactoDTO aDTO(MedioContacto entidad) {
        MedioContactoDTO dto = new MedioContactoDTO();
        dto.setIdMedioContacto(entidad.getIdMedioContacto() == null ? null : entidad.getIdMedioContacto().toString());
        dto.setValor(entidad.getValor());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setIdPersona(entidad.getIdPersona() == null ? null : entidad.getIdPersona().getIdPersona().toString());
        dto.setIdTipoMedioContacto(entidad.getIdTipoMedioContacto() == null ? null : entidad.getIdTipoMedioContacto().getIdTipoMedioContacto().toString());
        return dto;
    }

    private void copiar(MedioContactoDTO datos, MedioContacto entidad) {
        if (datos.getValor() != null) entidad.setValor(datos.getValor().trim());
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getIdPersona() != null && !datos.getIdPersona().isBlank()) entidad.setIdPersona(persona(datos.getIdPersona()));
        if (datos.getIdTipoMedioContacto() != null && !datos.getIdTipoMedioContacto().isBlank()) entidad.setIdTipoMedioContacto(tipoMedioContacto(datos.getIdTipoMedioContacto()));
    }

    private String validar(MedioContactoDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private Persona persona(String id) {
        Persona encontrada = personaDAO.find(RestUtil.uuid(id, "idPersona"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idPersona", "No existe el idPersona indicado");
        }
        return encontrada;
    }
    private TipoMedioContacto tipoMedioContacto(String id) {
        TipoMedioContacto encontrada = tipoMedioContactoDAO.find(RestUtil.uuid(id, "idTipoMedioContacto"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idTipoMedioContacto", "No existe el idTipoMedioContacto indicado");
        }
        return encontrada;
    }
}
