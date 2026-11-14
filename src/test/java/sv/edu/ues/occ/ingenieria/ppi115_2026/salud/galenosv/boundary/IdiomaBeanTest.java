/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;

/**
 *
 * @author david
 */
public class IdiomaBeanTest {

    private IdiomaBean instance;

    public IdiomaBeanTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new IdiomaBean();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getLocale method, of class IdiomaBean.
     */
    @Test
    public void testGetLocale() {
        Locale resultado = instance.getLocale();

        assertEquals(Locale.of("es", "SV"), resultado);
    }

    /**
     * Test of getIdioma method, of class IdiomaBean.
     */
    @Test
    public void testGetIdioma() {
        assertEquals("es", instance.getIdioma());
    }

    /**
     * Test of setIdioma method, of class IdiomaBean.
     */
    @Test
    public void testSetIdioma() {
        instance.setIdioma("en");

        assertEquals("en", instance.getIdioma());
    }

    /**
     * Test of getPais method, of class IdiomaBean.
     */
    @Test
    public void testGetPais() {
        assertEquals("SV", instance.getPais());
    }

    /**
     * Test of setPais method, of class IdiomaBean.
     */
    @Test
    public void testSetPais() {
        instance.setPais("US");

        assertEquals("US", instance.getPais());
    }

    /**
     * Test of cambiarIdioma method, of class IdiomaBean.
     */
    @Test
    public void testCambiarIdiomaIngles() {
        cambiarIdioma("en", "en", "US");
    }

    @Test
    public void testCambiarIdiomaFrances() {
        cambiarIdioma("fr", "fr", "FR");
    }

    @Test
    public void testCambiarIdiomaChino() {
        cambiarIdioma("zh", "zh", "CN");
    }

    @Test
    public void testCambiarIdiomaEspanol() {
        cambiarIdioma("es", "es", "SV");
    }

    @Test
    public void testCambiarIdiomaDesconocidoVuelveAEspanol() {
        cambiarIdioma("de", "es", "SV");
    }

    private void cambiarIdioma(String nuevoIdioma, String idiomaEsperado, String paisEsperado) {
        FacesContext facesContext = mock(FacesContext.class);
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        when(facesContext.getViewRoot()).thenReturn(viewRoot);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            instance.cambiarIdioma(nuevoIdioma);

            assertEquals(idiomaEsperado, instance.getIdioma());
            assertEquals(paisEsperado, instance.getPais());
            assertEquals(Locale.of(idiomaEsperado, paisEsperado), instance.getLocale());
            verify(viewRoot).setLocale(Locale.of(idiomaEsperado, paisEsperado));
        }
    }
}
