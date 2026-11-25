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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/** Expone operaciones REST de TipoMedioContacto siguiendo el contrato de la clase. */
@Path("/tipos-medio-contacto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoMedioContactoResource extends DefaultResource<TipoMedioContacto, TipoMedioContactoDTO> {

    @Inject
    private TipoMedioContactoDAO dao;

    @Override
    protected DefaultDAO<TipoMedioContacto> dao() {
        return dao;
    }

    @Context
    private UriInfo uriInfo;

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(TipoMedioContactoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            TipoMedioContacto entidad = new TipoMedioContacto(UUID.randomUUID());
            entidad.setActivo(Boolean.TRUE);
            entidad.setExpresionRegular(".*");
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdTipoMedioContacto().toString()).build();
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
    public Response modificar(@PathParam("id") String id, TipoMedioContactoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            TipoMedioContacto entidad = dao.find(uuid);
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
    protected TipoMedioContactoDTO aDTO(TipoMedioContacto entidad) {
        TipoMedioContactoDTO dto = new TipoMedioContactoDTO();
        dto.setIdTipoMedioContacto(entidad.getIdTipoMedioContacto() == null ? null : entidad.getIdTipoMedioContacto().toString());
        dto.setNombre(entidad.getNombre());
        dto.setIndicaciones(entidad.getIndicaciones());
        dto.setExpresionRegular(entidad.getExpresionRegular());
        dto.setActivo(entidad.getActivo());
        return dto;
    }

    private void copiar(TipoMedioContactoDTO datos, TipoMedioContacto entidad) {
        if (datos.getNombre() != null) entidad.setNombre(datos.getNombre().trim());
        if (datos.getIndicaciones() != null) entidad.setIndicaciones(datos.getIndicaciones().trim());
        if (datos.getExpresionRegular() != null) entidad.setExpresionRegular(datos.getExpresionRegular().trim());
        if (datos.getActivo() != null) entidad.setActivo(datos.getActivo());
    }

    private String validar(TipoMedioContactoDTO datos) {
        if (datos == null) return "body";
        if (datos.getNombre() == null || datos.getNombre().isBlank()) return "nombre";
        if (datos.getNombre().trim().length() > 155) return "nombre";
        return null;
    }
}
