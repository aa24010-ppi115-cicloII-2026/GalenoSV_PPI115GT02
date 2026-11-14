/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

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
public class ValidacionFormatoTest {

    public ValidacionFormatoTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validar method, of class ValidacionFormato.
     */
    @Test
    public void testValidar_String_String() {
        assertDoesNotThrow(() -> ValidacionFormato.validar("12345678-9", "^[0-9]{8}-[0-9]$"));
        assertDoesNotThrow(() -> ValidacionFormato.validar("77778888", "^[0-9]{8}$"));
    }

    @Test
    public void testValidarValorNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar(null, "^[0-9]{8}$"));
    }

    @Test
    public void testValidarValorVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("", "^[0-9]{8}$"));
    }

    @Test
    public void testValidarValorSoloEspacios() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("     ", "^[0-9]{8}$"));
    }

    @Test
    public void testValidarCortoSinExpresion() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("abc", null));

        assertTrue(excepcion.getMessage().contains("al menos 8"));
    }

    @Test
    public void testValidarLargoSinExpresion() {
        assertDoesNotThrow(() -> ValidacionFormato.validar("cualquier cosa", null));
        assertDoesNotThrow(() -> ValidacionFormato.validar("cualquier cosa", "   "));
    }

    @Test
    public void testValidarCortoConExpresionValida() {
        assertDoesNotThrow(() -> ValidacionFormato.validar("123", "\\d{3}"));
    }

    @Test
    public void testValidarFormatoIncorrecto() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("ABC", "^[0-9]+$"));

        assertTrue(excepcion.getMessage().contains("no cumple el formato"));
    }

    @Test
    public void testValidarRecortaEspacios() {
        assertDoesNotThrow(() -> ValidacionFormato.validar("  12345678  ", "^[0-9]{8}$"));
    }

    /**
     * Test of validar method, of class ValidacionFormato.
     */
    @Test
    public void testValidar_3args() {
        assertDoesNotThrow(() -> ValidacionFormato.validar("12345678-9", "^[0-9]{8}-[0-9]$", "Formato de DUI"));

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("ABC", "^[0-9]+$", "Solo numeros"));

        assertTrue(excepcion.getMessage().contains("Solo numeros"));
    }

    @Test
    public void testValidar_3argsIndicacionesVacias() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("ABC", "^[0-9]+$", "   "));

        assertTrue(excepcion.getMessage().contains("^[0-9]+$"));
    }

    @Test
    public void testValidarExpresionInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormato.validar("x", "["));
    }
}
