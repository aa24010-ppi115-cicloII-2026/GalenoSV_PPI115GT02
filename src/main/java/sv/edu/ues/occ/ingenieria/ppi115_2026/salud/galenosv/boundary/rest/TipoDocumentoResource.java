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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;

/** Expone operaciones REST del catalogo de tipos de documento. */
@Path("/tipos-documento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoDocumentoResource {

    @Inject
    private TipoDocumentoDAO dao;

    @Context
    private UriInfo uriInfo;

    @OPTIONS
    public Response opciones() {
        return Response.noContent().build();
    }

    /** Lista registros paginados; X-Total-Count permite paginar sin una segunda peticion. */
    @GET
    public Response listar(@QueryParam("first") Integer first, @QueryParam("max") Integer max) {
        int inicio = first == null ? 0 : first;
        int limite = max == null ? 20 : max;
        if (inicio < 0 || limite < 1 || limite > 100) {
            return error(422, "first/max", "first debe ser >= 0 y max entre 1 y 100");
        }
        try {
            List<TipoDocumentoDTO> datos = dao.findRange(inicio, limite).stream().map(this::aDTO).toList();
            return Response.ok(datos).header("X-Total-Count", dao.count()).build();
        } catch (RuntimeException ex) {
            return error(500, "", "No se pudo consultar el catalogo");
        }
    }

    /** Busca un tipo por UUID. */
    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") String id) {
        UUID uuid = parsearId(id);
        if (uuid == null) return error(422, "id", "El UUID no es valido");
        try {
            TipoDocumento tipo = dao.find(uuid);
            return tipo == null ? error(404, "id", "No existe el tipo de documento")
                    : Response.ok(aDTO(tipo)).build();
        } catch (RuntimeException ex) {
            return error(500, "", "No se pudo consultar el tipo de documento");
        }
    }

    /** Crea un tipo y devuelve 201 junto a Location apuntando al recurso creado. */
    @POST
    public Response crear(TipoDocumentoDTO datos) {
        String problema = validar(datos);
        if (problema != null) return error(422, problema, "Revise los datos enviados");
        try {
            if (dao.existeNombre(datos.getNombre(), null)) {
                return error(422, "nombre", "Ya existe un tipo con ese nombre");
            }
            TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
            copiar(datos, tipo);
            dao.crear(tipo);
            URI location = uriInfo.getAbsolutePathBuilder().path(tipo.getIdTipoDocumento().toString()).build();
            return Response.created(location).build();
        } catch (RuntimeException ex) {
            return error(500, "", "No se pudo crear el tipo de documento");
        }
    }

    /** Actualiza campos editables del tipo identificado en la ruta. */
    @PUT
    @Path("/{id}")
    public Response modificar(@PathParam("id") String id, TipoDocumentoDTO datos) {
        UUID uuid = parsearId(id);
        if (uuid == null) return error(422, "id", "El UUID no es valido");
        String problema = validar(datos);
        if (problema != null) return error(422, problema, "Revise los datos enviados");
        try {
            TipoDocumento tipo = dao.find(uuid);
            if (tipo == null) return error(404, "id", "No existe el tipo de documento");
            if (dao.existeNombre(datos.getNombre(), uuid)) {
                return error(422, "nombre", "Ya existe un tipo con ese nombre");
            }
            copiar(datos, tipo);
            dao.modificar(tipo);
            return Response.noContent().build();
        } catch (RuntimeException ex) {
            return error(500, "", "No se pudo modificar el tipo de documento");
        }
    }

    /** Elimina el tipo identificado en la ruta. */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") String id) {
        UUID uuid = parsearId(id);
        if (uuid == null) return error(422, "id", "El UUID no es valido");
        try {
            TipoDocumento tipo = dao.find(uuid);
            if (tipo == null) return error(404, "id", "No existe el tipo de documento");
            dao.eliminar(tipo);
            return Response.noContent().build();
        } catch (IllegalStateException ex) {
            return error(409, "id", "El tipo esta relacionado con documentos y no se puede eliminar");
        } catch (RuntimeException ex) {
            return error(500, "", "No se pudo eliminar el tipo de documento");
        }
    }

    private TipoDocumentoDTO aDTO(TipoDocumento tipo) {
        TipoDocumentoDTO dto = new TipoDocumentoDTO();
        dto.setIdTipoDocumento(tipo.getIdTipoDocumento().toString());
        dto.setNombre(tipo.getNombre());
        dto.setIndicaciones(tipo.getIndicaciones());
        dto.setExpresionRegular(tipo.getExpresionRegular());
        dto.setActivo(tipo.getActivo());
        return dto;
    }

    private void copiar(TipoDocumentoDTO datos, TipoDocumento tipo) {
        tipo.setNombre(datos.getNombre().trim());
        tipo.setIndicaciones(datos.getIndicaciones());
        tipo.setExpresionRegular(datos.getExpresionRegular());
        tipo.setActivo(datos.getActivo());
    }

    private String validar(TipoDocumentoDTO datos) {
        if (datos == null) return "body";
        if (datos.getNombre() == null || datos.getNombre().isBlank() || datos.getNombre().trim().length() > 155) return "nombre";
        if (datos.getActivo() == null) return "activo";
        return null;
    }

    private UUID parsearId(String id) {
        try { return UUID.fromString(id); } catch (IllegalArgumentException | NullPointerException ex) { return null; }
    }

    private Response error(int estado, String parametro, String mensaje) {
        Response.ResponseBuilder respuesta = Response.status(estado).entity(java.util.Map.of("mensaje", mensaje));
        if (parametro != null && !parametro.isBlank()) respuesta.header("X-Parametro-Error", parametro);
        return respuesta.build();
    }
}
