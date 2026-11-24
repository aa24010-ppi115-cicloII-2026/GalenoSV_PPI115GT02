package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Los avisos al usuario expresan la causa del error con palabras, nunca el texto tecnico
 * de la base de datos ni de Java.
 */
public class MensajeErrorTest {

    /** Error como llega al modelo: la base lo lanza al confirmar y el contenedor lo envuelve. */
    private static Exception errorDeBase(String sqlState, String detalleTecnico) {
        SQLException sql = new SQLException(detalleTecnico, sqlState);
        return new RuntimeException("jakarta.ejb.EJBTransactionRolledbackException: " + detalleTecnico,
                new IllegalStateException("Error al crear", sql));
    }

    @Test
    public void testValidacionConservaSuMensaje() {
        assertEquals("Ya existe un rol con ese nombre",
                MensajeError.de(new IllegalArgumentException("Ya existe un rol con ese nombre")));
    }

    @Test
    public void testRegistroDuplicado() {
        String mensaje = MensajeError.de(errorDeBase("23505",
                "ERROR: duplicate key value violates unique constraint \"uk_rol_nombre\""));

        assertTrue(mensaje.contains("Ya existe un registro"));
        assertFalse(mensaje.contains("duplicate"));
        assertFalse(mensaje.contains("uk_rol_nombre"));
    }

    @Test
    public void testRegistroRelacionado() {
        String mensaje = MensajeError.de(errorDeBase("23503",
                "ERROR: update or delete on table \"rol\" violates foreign key constraint"));

        assertTrue(mensaje.contains("registros relacionados"));
        assertFalse(mensaje.contains("foreign key"));
    }

    @Test
    public void testDatoObligatorio() {
        assertTrue(MensajeError.de(errorDeBase("23502", "ERROR: null value in column \"nombre\""))
                .contains("dato obligatorio"));
    }

    @Test
    public void testReglaDeLaBase() {
        assertTrue(MensajeError.de(errorDeBase("23514", "ERROR: new row violates check constraint"))
                .contains("no cumple las reglas"));
    }

    @Test
    public void testTextoDemasiadoLargo() {
        assertTrue(MensajeError.de(errorDeBase("22001", "ERROR: value too long for type character varying(50)"))
                .contains("demasiado largo"));
    }

    @Test
    public void testSinConexionConLaBase() {
        assertTrue(MensajeError.de(errorDeBase("08006", "Connection to localhost:5432 refused"))
                .contains("base de datos"));
    }

    @Test
    public void testErrorDeBaseSinClasificarUsaMensajeGeneral() {
        String mensaje = MensajeError.de(errorDeBase("42P01", "ERROR: relation \"x\" does not exist"));

        assertEquals(MensajeError.GENERAL, mensaje);
    }

    @Test
    public void testErrorTecnicoDeJavaUsaMensajeGeneral() {
        String mensaje = MensajeError.de(new NullPointerException(
                "Cannot invoke \"String.trim()\" because \"valor\" is null"));

        assertEquals(MensajeError.GENERAL, mensaje);
    }

    @Test
    public void testValidacionSinMensajeUsaMensajeGeneral() {
        assertEquals(MensajeError.GENERAL, MensajeError.de(new IllegalArgumentException()));
        assertEquals(MensajeError.GENERAL, MensajeError.de(new IllegalArgumentException("  ")));
    }

    @Test
    public void testSinExcepcionUsaMensajeGeneral() {
        assertEquals(MensajeError.GENERAL, MensajeError.de(null));
    }

    @Test
    public void testLaCausaDeLaBasePrevaleceSobreElEnvoltorio() {
        // DefaultDAO.eliminar ya traduce la llave foranea; el aviso dice lo mismo.
        Exception envuelta = new IllegalStateException("No se puede eliminar porque tiene registros relacionados",
                new SQLException("violates foreign key constraint", "23503"));

        assertTrue(MensajeError.de(envuelta).contains("registros relacionados"));
    }

    @Test
    public void testValidacionEnvueltaPorElContenedor() {
        // Lo que lanza un DAO @Stateless llega envuelto; se usa la validacion mas externa.
        Exception envuelta = new RuntimeException("jakarta.ejb.EJBException",
                new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio",
                        new IllegalArgumentException("detalle interno")));

        assertEquals("La fecha de fin no puede ser anterior a la de inicio", MensajeError.de(envuelta));
    }

    @Test
    public void testValidacionDeJavaNoMuestraSuTextoTecnico() {
        // UUID.fromString y JPA tambien lanzan IllegalArgumentException, con texto para programadores.
        IllegalArgumentException deJava = assertThrows(IllegalArgumentException.class, () -> java.util.UUID.fromString("abc"));
        assertEquals(MensajeError.GENERAL, MensajeError.de(deJava));
        assertEquals(MensajeError.GENERAL, MensajeError.de(new RuntimeException("Error al eliminar", deJava)));
    }

    @Test
    public void testCadenaCiclicaNoSeQuedaEnBucle() {
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b", a);
        a.initCause(b);

        assertEquals(MensajeError.GENERAL, MensajeError.de(a));
    }
}
