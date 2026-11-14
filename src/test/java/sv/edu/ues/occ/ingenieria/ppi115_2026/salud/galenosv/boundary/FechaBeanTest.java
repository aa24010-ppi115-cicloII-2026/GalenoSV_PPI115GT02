/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author david
 */
public class FechaBeanTest {

    private FechaBean instance;

    public FechaBeanTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new FechaBean();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of fecha method, of class FechaBean.
     */
    @Test
    public void testFecha() {
        OffsetDateTime valor = OffsetDateTime.of(2026, 11, 13, 14, 30, 0, 0, ZoneOffset.UTC);

        String resultado = instance.fecha(valor);

        assertEquals("13/11/2026", resultado);
    }

    @Test
    public void testFechaNula() {
        String resultado = instance.fecha(null);

        assertEquals("", resultado);
    }

    /**
     * Test of fechaHora method, of class FechaBean.
     */
    @Test
    public void testFechaHora() {
        OffsetDateTime valor = OffsetDateTime.of(2026, 11, 13, 14, 30, 0, 0, ZoneOffset.UTC);

        String resultado = instance.fechaHora(valor);

        assertEquals("13/11/2026 14:30", resultado);
    }

    @Test
    public void testFechaHoraNula() {
        String resultado = instance.fechaHora(null);

        assertEquals("", resultado);
    }

    @Test
    public void testEsSerializable() {
        assertTrue(instance instanceof Serializable);
    }
}
