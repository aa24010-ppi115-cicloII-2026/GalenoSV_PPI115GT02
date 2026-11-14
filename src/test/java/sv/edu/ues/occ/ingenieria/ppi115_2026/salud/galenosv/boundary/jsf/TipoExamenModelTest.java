/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

/**
 *
 * @author david
 */
public class TipoExamenModelTest {

    private TipoExamenModel instance;
    private TipoExamenDAO dao;
    private ExamenTipoExamenDAO examenTipoExamenDAO;

    public TipoExamenModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new TipoExamenModel();
        dao = mock(TipoExamenDAO.class);
        examenTipoExamenDAO = mock(ExamenTipoExamenDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.examenTipoExamenDAO = examenTipoExamenDAO;
        instance.inicializarRegistros();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarEliminacion method, of class TipoExamenModel.
     */
    @Test
    public void testValidarEliminacion() {
        TipoExamen r = new TipoExamen(UUID.randomUUID());
        when(examenTipoExamenDAO.countByTipo(r.getIdTipoExamen())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConExamenes() {
        TipoExamen r = new TipoExamen(UUID.randomUUID());
        when(examenTipoExamenDAO.countByTipo(r.getIdTipoExamen())).thenReturn(2L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        TipoExamen r = new TipoExamen();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(examenTipoExamenDAO, never()).countByTipo(any());
    }

    /**
     * Test of getFacesContext method, of class TipoExamenModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class TipoExamenModel.
     */
    @Test
    public void testValidarUnicidad() {
        TipoExamen r = new TipoExamen();
        r.setNombre("  Examen  ");
        when(dao.existeNombre("Examen", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Examen", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        TipoExamen r = new TipoExamen();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        TipoExamen r = new TipoExamen(UUID.randomUUID());
        r.setNombre("Examen");
        when(dao.existeNombre("Examen", r.getIdTipoExamen())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class TipoExamenModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<TipoExamen> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class TipoExamenModel.
     */
    @Test
    public void testNuevoRegistro() {
        TipoExamen resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoExamen());
        assertEquals(Boolean.TRUE, resultado.getActivo());
    }

    /**
     * Test of buscarRegistroPorId method, of class TipoExamenModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        TipoExamen esperado = new TipoExamen(id);
        instance.getModelo().setWrappedData(List.of(esperado));
        when(dao.findAll()).thenReturn(List.of(esperado));

        TipoExamen resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        TipoExamen resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
    }

    @Test
    public void testBuscarRegistroPorIdModeloVacio() {
        UUID id = UUID.randomUUID();
        instance.getModelo().setWrappedData(List.of());

        TipoExamen resultado = instance.buscarRegistroPorId(id);

        assertNull(resultado);
    }

    /**
     * Test of getIdAsText method, of class TipoExamenModel.
     */
    @Test
    public void testGetIdAsText() {
        TipoExamen r = new TipoExamen(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdTipoExamen().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class TipoExamenModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        TipoExamen esperado = new TipoExamen(id);
        instance.getModelo().setWrappedData(List.of(esperado));

        TipoExamen resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        instance.getModelo().setWrappedData(List.of(new TipoExamen(UUID.randomUUID())));

        assertNull(instance.getIdByText("no-es-uuid"));
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
    }

}
