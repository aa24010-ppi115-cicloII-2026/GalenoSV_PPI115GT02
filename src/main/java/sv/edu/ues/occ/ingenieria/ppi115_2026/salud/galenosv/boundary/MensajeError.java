package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Texto de los avisos de error para el usuario: expresa la causa con palabras y nunca muestra
 * el texto tecnico de la base de datos ni de Java (ese queda en el registro del servidor).
 *
 * <p>Las validaciones del sistema lanzan IllegalArgumentException con un mensaje ya pensado
 * para el usuario. Java y JPA lanzan la misma excepcion con texto para programadores
 * (UUID.fromString, EntityManager), asi que solo se muestra la que lanzo el codigo del sistema. Los errores de PostgreSQL se reconocen por su SQLSTATE
 * (https://www.postgresql.org/docs/current/errcodes-appendix.html). Se busca en toda la cadena
 * de causas porque el contenedor envuelve lo que lanzan los DAO {@code @Stateless}.
 */
public final class MensajeError {

    private static final Logger LOG = Logger.getLogger(MensajeError.class.getName());

    /** Paquete raiz del sistema: el de boundary sin su ultimo nivel. */
    private static final String PAQUETE_SISTEMA = MensajeError.class.getPackageName()
            .substring(0, MensajeError.class.getPackageName().lastIndexOf('.') + 1);

    /** Aviso cuando la causa no se puede explicar mejor. */
    public static final String GENERAL =
            "No se pudo completar la operacion. Intente de nuevo; si el problema continua, avise al administrador.";

    private MensajeError() {
    }

    /**
     * Mensaje para mostrar al usuario a partir de un error.
     *
     * @param error lo que se capturo; puede ser null
     * @return la causa en palabras, o {@link #GENERAL}
     */
    public static String de(Throwable error) {
        String porBase = segunBaseDeDatos(error);
        if (porBase != null) {
            registrar(error, porBase);
            return porBase;
        }
        String validacion = mensajeDeValidacion(error);
        if (validacion != null) {
            return validacion;
        }
        registrar(error, GENERAL);
        return GENERAL;
    }

    private static String segunBaseDeDatos(Throwable error) {
        for (Throwable causa : cadena(error)) {
            if (causa instanceof SQLException sql && sql.getSQLState() != null) {
                String mensaje = segunSqlState(sql.getSQLState());
                if (mensaje != null) {
                    return mensaje;
                }
            }
        }
        return null;
    }

    private static String segunSqlState(String estado) {
        return switch (estado) {
            case "23505" -> "Ya existe un registro con esos datos.";
            case "23503" -> "La operacion no se puede completar porque hay registros relacionados.";
            case "23502" -> "Falta un dato obligatorio.";
            case "23514" -> "Algun dato no cumple las reglas del sistema.";
            case "22001" -> "Uno de los textos es demasiado largo.";
            default -> estado.startsWith("08")
                    ? "No hay conexion con la base de datos. Intente de nuevo en unos minutos."
                    : null;
        };
    }

    private static String mensajeDeValidacion(Throwable error) {
        for (Throwable causa : cadena(error)) {
            if (causa instanceof IllegalArgumentException && lanzadaPorElSistema(causa)
                    && causa.getMessage() != null && !causa.getMessage().isBlank()) {
                return causa.getMessage();
            }
        }
        return null;
    }

    /** La primera linea de la pila dice quien creo la excepcion; sin pila no se confia en ella. */
    private static boolean lanzadaPorElSistema(Throwable error) {
        StackTraceElement[] pila = error.getStackTrace();
        return pila.length > 0 && pila[0].getClassName().startsWith(PAQUETE_SISTEMA);
    }

    /** El error y sus causas, de afuera hacia adentro y sin repetir (una cadena puede ser ciclica). */
    private static List<Throwable> cadena(Throwable error) {
        List<Throwable> causas = new ArrayList<>();
        Set<Throwable> vistas = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable actual = error; actual != null && vistas.add(actual); actual = actual.getCause()) {
            causas.add(actual);
        }
        return causas;
    }

    private static void registrar(Throwable error, String aviso) {
        if (error != null) {
            LOG.log(Level.WARNING, "Error mostrado al usuario como: " + aviso, error);
        }
    }
}
