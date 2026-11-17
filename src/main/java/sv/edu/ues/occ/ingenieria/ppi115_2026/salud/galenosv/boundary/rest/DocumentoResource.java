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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;

/** Expone operaciones REST de Documento siguiendo el contrato de la clase. */
@Path("/documentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentoResource {

    @Inject
    private DocumentoDAO dao;
    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private TipoDocumentoDAO tipoDocumentoDAO;

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
            List<DocumentoDTO> datos = dao.findRange(inicio, limite).stream().map(this::aDTO).toList();
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
            Documento entidad = dao.find(uuid);
            return entidad == null ? RestUtil.error(404, "id", "No existe el registro")
                    : Response.ok(aDTO(entidad)).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(DocumentoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            Documento entidad = new Documento(UUID.randomUUID());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdDocumento().toString()).build();
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
    public Response modificar(@PathParam("id") String id, DocumentoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            Documento entidad = dao.find(uuid);
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
            Documento entidad = dao.find(uuid);
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

    private DocumentoDTO aDTO(Documento entidad) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setIdDocumento(entidad.getIdDocumento() == null ? null : entidad.getIdDocumento().toString());
        dto.setValor(entidad.getValor());
        dto.setRutaFisica(entidad.getRutaFisica());
        dto.setIdPersona(entidad.getIdPersona() == null ? null : entidad.getIdPersona().getIdPersona().toString());
        dto.setIdTipoDocumento(entidad.getIdTipoDocumento() == null ? null : entidad.getIdTipoDocumento().getIdTipoDocumento().toString());
        return dto;
    }

    private void copiar(DocumentoDTO datos, Documento entidad) {
        if (datos.getValor() != null) entidad.setValor(datos.getValor().trim());
        if (datos.getRutaFisica() != null) entidad.setRutaFisica(datos.getRutaFisica().trim());
        if (datos.getIdPersona() != null && !datos.getIdPersona().isBlank()) entidad.setIdPersona(persona(datos.getIdPersona()));
        if (datos.getIdTipoDocumento() != null && !datos.getIdTipoDocumento().isBlank()) entidad.setIdTipoDocumento(tipoDocumento(datos.getIdTipoDocumento()));
    }

    private String validar(DocumentoDTO datos) {
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
    private TipoDocumento tipoDocumento(String id) {
        TipoDocumento encontrada = tipoDocumentoDAO.find(RestUtil.uuid(id, "idTipoDocumento"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idTipoDocumento", "No existe el idTipoDocumento indicado");
        }
        return encontrada;
    }
}
