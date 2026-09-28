package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.FechaBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ValidacionFormato;

import java.time.OffsetDateTime;

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
}
