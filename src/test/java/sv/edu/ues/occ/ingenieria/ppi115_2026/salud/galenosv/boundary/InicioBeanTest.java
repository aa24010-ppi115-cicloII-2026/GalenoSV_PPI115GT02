/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InicioBeanTest {

    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private ConsultaDAO consultaDAO;
    @Mock
    private OrdenExamenDAO ordenExamenDAO;
    @Mock
    private ExamenResultadoDAO examenResultadoDAO;

    @InjectMocks
    private InicioBean instance;

    public InicioBeanTest() {
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
     * Test of getTotalPacientes method, of class InicioBean.
     */
    @Test
    public void testGetTotalPacientes() {
        when(personaDAO.count()).thenReturn(7L);

        assertEquals(7L, instance.getTotalPacientes());
    }

    @Test
    public void testGetTotalPacientesNulo() {
        when(personaDAO.count()).thenReturn(null);

        assertEquals(0L, instance.getTotalPacientes());
    }

    @Test
    public void testGetTotalPacientesExcepcion() {
        when(personaDAO.count()).thenThrow(new RuntimeException("Error de prueba"));

        assertEquals(0L, instance.getTotalPacientes());
    }

    /**
     * Test of getTotalConsultas method, of class InicioBean.
     */
    @Test
    public void testGetTotalConsultas() {
        when(consultaDAO.count()).thenReturn(3L);

        assertEquals(3L, instance.getTotalConsultas());
    }

    @Test
    public void testGetTotalConsultasNulo() {
        when(consultaDAO.count()).thenReturn(null);

        assertEquals(0L, instance.getTotalConsultas());
    }

    @Test
    public void testGetTotalConsultasExcepcion() {
        when(consultaDAO.count()).thenThrow(new RuntimeException("Error de prueba"));

        assertEquals(0L, instance.getTotalConsultas());
    }

    /**
     * Test of getTotalOrdenes method, of class InicioBean.
     */
    @Test
    public void testGetTotalOrdenes() {
        when(ordenExamenDAO.count()).thenReturn(5L);

        assertEquals(5L, instance.getTotalOrdenes());
    }

    @Test
    public void testGetTotalOrdenesNulo() {
        when(ordenExamenDAO.count()).thenReturn(null);

        assertEquals(0L, instance.getTotalOrdenes());
    }

    @Test
    public void testGetTotalOrdenesExcepcion() {
        when(ordenExamenDAO.count()).thenThrow(new RuntimeException("Error de prueba"));

        assertEquals(0L, instance.getTotalOrdenes());
    }

    /**
     * Test of getTotalResultados method, of class InicioBean.
     */
    @Test
    public void testGetTotalResultados() {
        when(examenResultadoDAO.count()).thenReturn(2L);

        assertEquals(2L, instance.getTotalResultados());
    }

    @Test
    public void testGetTotalResultadosNulo() {
        when(examenResultadoDAO.count()).thenReturn(null);

        assertEquals(0L, instance.getTotalResultados());
    }

    @Test
    public void testGetTotalResultadosExcepcion() {
        when(examenResultadoDAO.count()).thenThrow(new RuntimeException("Error de prueba"));

        assertEquals(0L, instance.getTotalResultados());
    }

    /**
     * Test of getUltimasConsultas method, of class InicioBean.
     */
    @Test
    public void testGetUltimasConsultas() {
        List<Consulta> consultas = List.of(new Consulta(), new Consulta());
        when(consultaDAO.findRange(0, 5)).thenReturn(consultas);

        List<Consulta> resultado = instance.getUltimasConsultas();

        assertEquals(consultas, resultado);
    }

    @Test
    public void testGetUltimasConsultasNulo() {
        when(consultaDAO.findRange(0, 5)).thenReturn(null);

        List<Consulta> resultado = instance.getUltimasConsultas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetUltimasConsultasExcepcion() {
        when(consultaDAO.findRange(0, 5)).thenThrow(new RuntimeException("Error de prueba"));

        List<Consulta> resultado = instance.getUltimasConsultas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
