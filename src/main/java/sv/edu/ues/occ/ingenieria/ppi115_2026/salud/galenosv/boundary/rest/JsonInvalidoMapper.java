package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Convierte un cuerpo JSON mal formado en 400 en lugar de un 500 generico. */
@Provider
public class JsonInvalidoMapper implements ExceptionMapper<JsonbException> {

    @Override
    public Response toResponse(JsonbException ex) {
        return RestUtil.error(400, "body", "El JSON enviado no es valido");
    }
}
