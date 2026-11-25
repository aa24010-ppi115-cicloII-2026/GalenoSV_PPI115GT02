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
public class DocumentoResource extends DefaultResource<Documento, DocumentoDTO> {

    @Inject
    private DocumentoDAO dao;

    @Override
    protected DefaultDAO<Documento> dao() {
        return dao;
    }
    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private TipoDocumentoDAO tipoDocumentoDAO;

    @Context
    private UriInfo uriInfo;

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

    @Override
    protected DocumentoDTO aDTO(Documento entidad) {
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
