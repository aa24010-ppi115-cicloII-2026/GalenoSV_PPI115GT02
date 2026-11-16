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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenResultado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/** Expone operaciones REST de ExamenResultado siguiendo el contrato de la clase. */
@Path("/examenes-resultado")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamenResultadoResource {

    @Inject
    private ExamenResultadoDAO dao;
    @Inject
    private OrdenExamenDAO ordenExamenDAO;

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
            List<ExamenResultadoDTO> datos = dao.findRange(inicio, limite).stream().map(this::aDTO).toList();
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
            ExamenResultado entidad = dao.find(uuid);
            return entidad == null ? RestUtil.error(404, "id", "No existe el registro")
                    : Response.ok(aDTO(entidad)).build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo consultar el registro");
        }
    }

    /** Crea un registro y devuelve 201 con Location hacia el recurso creado. */
    @POST
    public Response crear(ExamenResultadoDTO datos) {
        try {
            String problema = validar(datos);
            if (problema != null) return RestUtil.error(422, problema, "Revise los datos enviados");

            ExamenResultado entidad = new ExamenResultado(UUID.randomUUID());
            entidad.setFechaCreacion(java.time.OffsetDateTime.now());
            copiar(datos, entidad);
            dao.crear(entidad);
            URI location = uriInfo.getAbsolutePathBuilder().path(entidad.getIdExamenResultado().toString()).build();
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
    public Response modificar(@PathParam("id") String id, ExamenResultadoDTO datos) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            ExamenResultado entidad = dao.find(uuid);
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
            ExamenResultado entidad = dao.find(uuid);
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

    private ExamenResultadoDTO aDTO(ExamenResultado entidad) {
        ExamenResultadoDTO dto = new ExamenResultadoDTO();
        dto.setIdExamenResultado(entidad.getIdExamenResultado() == null ? null : entidad.getIdExamenResultado().toString());
        dto.setFechaCreacion(entidad.getFechaCreacion() == null ? null : entidad.getFechaCreacion().toString());
        dto.setResultado(entidad.getResultado());
        dto.setInterpretacion(entidad.getInterpretacion());
        dto.setRutaAtestado(entidad.getRutaAtestado());
        dto.setIdOrdenExamen(entidad.getIdOrdenExamen() == null ? null : entidad.getIdOrdenExamen().getIdOrdenExamen().toString());
        return dto;
    }

    private void copiar(ExamenResultadoDTO datos, ExamenResultado entidad) {
        if (datos.getFechaCreacion() != null) entidad.setFechaCreacion(RestUtil.fecha(datos.getFechaCreacion(), "fechaCreacion"));
        if (datos.getResultado() != null) entidad.setResultado(datos.getResultado().trim());
        if (datos.getInterpretacion() != null) entidad.setInterpretacion(datos.getInterpretacion().trim());
        if (datos.getRutaAtestado() != null) entidad.setRutaAtestado(datos.getRutaAtestado().trim());
        if (datos.getIdOrdenExamen() != null && !datos.getIdOrdenExamen().isBlank()) entidad.setIdOrdenExamen(ordenExamen(datos.getIdOrdenExamen()));
    }

    private String validar(ExamenResultadoDTO datos) {
        if (datos == null) return "body";
        return null;
    }

    private OrdenExamen ordenExamen(String id) {
        OrdenExamen encontrada = ordenExamenDAO.find(RestUtil.uuid(id, "idOrdenExamen"));
        if (encontrada == null) {
            throw new ParametroInvalidoException("idOrdenExamen", "No existe el idOrdenExamen indicado");
        }
        return encontrada;
    }
}
