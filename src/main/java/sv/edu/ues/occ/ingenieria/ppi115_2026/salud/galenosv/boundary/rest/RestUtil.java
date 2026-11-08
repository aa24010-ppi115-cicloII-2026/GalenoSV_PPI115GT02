package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

/** Conversiones y respuestas compartidas por los recursos REST. */
public final class RestUtil {

    private RestUtil() {
    }

    /** Convierte el id de la ruta; devuelve null cuando no es un UUID valido. */
    public static UUID parsearId(String id) {
        try {
            return id == null ? null : UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Convierte un UUID del cuerpo y nombra el campo cuando el formato es erroneo. */
    public static UUID uuid(String valor, String parametro) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new ParametroInvalidoException(parametro, "El UUID de " + parametro + " no es valido");
        }
    }

    /** Acepta fecha con zona, fecha-hora o fecha simple; informa el campo invalido. */
    public static OffsetDateTime fecha(String valor, String parametro) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String texto = valor.trim();
        try {
            return OffsetDateTime.parse(texto);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(texto).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ex2) {
                try {
                    return LocalDate.parse(texto).atStartOfDay().atOffset(ZoneOffset.UTC);
                } catch (DateTimeParseException ex3) {
                    throw new ParametroInvalidoException(parametro, "La fecha de " + parametro + " no es valida");
                }
            }
        }
    }

    /** Reconoce conflictos de llave foranea aunque la transaccion falle al hacer commit. */
    public static boolean esConflicto(Throwable ex) {
        while (ex != null) {
            String nombre = ex.getClass().getSimpleName();
            String mensaje = String.valueOf(ex.getMessage());
            if (nombre.contains("ConstraintViolation") || nombre.contains("RollbackException")
                    || nombre.contains("IntegrityConstraintViolation")
                    || mensaje.contains("violates foreign key") || mensaje.contains("llave for")
                    || mensaje.contains("foreign key") || mensaje.contains("FK_") || mensaje.contains("fk_")
                    || mensaje.contains("23503") || mensaje.contains("relacionados")) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    /** Construye el error con el mensaje y el header custom que exige el contrato. */
    public static Response error(int estado, String parametro, String mensaje) {
        Response.ResponseBuilder respuesta = Response.status(estado)
                .entity(Map.of("mensaje", mensaje == null ? "Error" : mensaje));
        if (parametro != null && !parametro.isBlank()) {
            respuesta.header("X-Parametro-Error", parametro);
        }
        return respuesta.build();
    }
}
