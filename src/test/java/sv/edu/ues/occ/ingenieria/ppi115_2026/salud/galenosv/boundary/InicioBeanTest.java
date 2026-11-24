package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;

/**
 * Pantalla de inicio: totales y ultimas consultas. Los DAO son mocks; cada prueba prepara
 * lo que devuelven, ejecuta cargar() como lo hace el contenedor y revisa lo que ve la vista.
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

    @Test
    public void testCargarTotales() {
        when(personaDAO.count()).thenReturn(7L);
        when(consultaDAO.count()).thenReturn(3L);
        when(ordenExamenDAO.count()).thenReturn(5L);
        when(examenResultadoDAO.count()).thenReturn(2L);

        instance.cargar();

        assertEquals(7L, instance.getTotalPacientes());
        assertEquals(3L, instance.getTotalConsultas());
        assertEquals(5L, instance.getTotalOrdenes());
        assertEquals(2L, instance.getTotalResultados());
    }

    @Test
    public void testTotalNuloEsCero() {
        when(personaDAO.count()).thenReturn(null);
        when(consultaDAO.count()).thenReturn(null);
        when(ordenExamenDAO.count()).thenReturn(null);
        when(examenResultadoDAO.count()).thenReturn(null);

        instance.cargar();

        assertEquals(0L, instance.getTotalPacientes());
        assertEquals(0L, instance.getTotalConsultas());
        assertEquals(0L, instance.getTotalOrdenes());
        assertEquals(0L, instance.getTotalResultados());
    }

    @Test
    public void testUnTotalQueFallaNoImpideLosDemas() {
        when(personaDAO.count()).thenThrow(new RuntimeException("Error de prueba"));
        when(consultaDAO.count()).thenReturn(3L);
        when(ordenExamenDAO.count()).thenThrow(new RuntimeException("Error de prueba"));
        when(examenResultadoDAO.count()).thenReturn(2L);

        instance.cargar();

        assertEquals(0L, instance.getTotalPacientes());
        assertEquals(3L, instance.getTotalConsultas());
        assertEquals(0L, instance.getTotalOrdenes());
        assertEquals(2L, instance.getTotalResultados());
    }

    @Test
    public void testUltimasConsultasSonLasMasRecientes() {
        List<Consulta> consultas = List.of(new Consulta(), new Consulta());
        when(consultaDAO.findRecientes(InicioBean.CONSULTAS_RECIENTES)).thenReturn(consultas);

        instance.cargar();

        assertEquals(consultas, instance.getUltimasConsultas());
        verify(consultaDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    public void testUltimasConsultasNuloEsListaVacia() {
        when(consultaDAO.findRecientes(InicioBean.CONSULTAS_RECIENTES)).thenReturn(null);

        instance.cargar();

        assertTrue(instance.getUltimasConsultas().isEmpty());
    }

    @Test
    public void testUltimasConsultasConErrorEsListaVacia() {
        when(consultaDAO.findRecientes(InicioBean.CONSULTAS_RECIENTES)).thenThrow(new RuntimeException("Error de prueba"));

        instance.cargar();

        assertTrue(instance.getUltimasConsultas().isEmpty());
    }

    @Test
    public void testLaVistaLeeVariasVecesPeroLaBaseSeConsultaUnaVez() {
        when(consultaDAO.findRecientes(InicioBean.CONSULTAS_RECIENTES)).thenReturn(List.of(new Consulta()));
        when(personaDAO.count()).thenReturn(7L);

        instance.cargar();
        // La tabla de JSF evalua su value varias veces durante un mismo render.
        for (int i = 0; i < 3; i++) {
            instance.getUltimasConsultas();
            instance.getTotalPacientes();
        }

        verify(consultaDAO, times(1)).findRecientes(InicioBean.CONSULTAS_RECIENTES);
        verify(personaDAO, times(1)).count();
    }

    @Test
    public void testSinCargarNoHayDatos() {
        assertEquals(0L, instance.getTotalPacientes());
        assertTrue(instance.getUltimasConsultas().isEmpty());
        verifyNoInteractions(personaDAO, consultaDAO, ordenExamenDAO, examenResultadoDAO);
    }
}
