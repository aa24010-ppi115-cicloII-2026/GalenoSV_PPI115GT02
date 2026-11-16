package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.FechaBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ValidacionFormato;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest.ParametroInvalidoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest.RestUtil;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UtilidadesTest {

    @Test void testValidacionFormato_valido(){
        assertDoesNotThrow(()-> ValidacionFormato.validar("12345678-9", "^[0-9]{8}-[0-9]$"));
        assertDoesNotThrow(()-> ValidacionFormato.validar("77778888", "^[0-9]{8}$"));
    }

    @Test void testValidacionFormato_invalido(){
        assertThrows(IllegalArgumentException.class, ()-> ValidacionFormato.validar("ABC", "^[0-9]{8}-[0-9]$"));
        assertThrows(IllegalArgumentException.class, ()-> ValidacionFormato.validar("", "^[0-9]{8}$"));
        assertThrows(IllegalArgumentException.class, ()-> ValidacionFormato.validar(null, "^[0-9]{8}$"));
    }

    @Test void testValidacionFormato_sinRegex_pasa(){
        assertDoesNotThrow(()-> ValidacionFormato.validar("cualquier cosa", ""));
        assertDoesNotThrow(()-> ValidacionFormato.validar("cualquier cosa", null));
    }

    @Test void testValidacionFormato_regexInvalida(){
        assertThrows(IllegalArgumentException.class, ()-> ValidacionFormato.validar("x", "["));
    }

    @Test void testFechaBean(){
        FechaBean b = new FechaBean();
        assertEquals("", b.fecha(null));
        assertEquals("", b.fechaHora(null));
        assertFalse(b.fecha(OffsetDateTime.now()).isBlank());
        assertFalse(b.fechaHora(OffsetDateTime.now()).isBlank());
        assertTrue(b instanceof java.io.Serializable);
    }

    @Test void testIdNuevo_usaElDelClienteParaNoDuplicarAlReintentar(){
        UUID enviado = UUID.randomUUID();
        assertEquals(enviado, RestUtil.idNuevo(enviado.toString(), "idOrdenExamen"));
        assertNotNull(RestUtil.idNuevo(null, "idOrdenExamen"));
        assertNotEquals(RestUtil.idNuevo("", "idOrdenExamen"), RestUtil.idNuevo("", "idOrdenExamen"));
        ParametroInvalidoException error = assertThrows(ParametroInvalidoException.class,
                () -> RestUtil.idNuevo("no-es-uuid", "idOrdenExamen"));
        assertEquals("idOrdenExamen", error.getParametro());
    }
}
