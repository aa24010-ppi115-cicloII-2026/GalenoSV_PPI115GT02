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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 *
 * @author david
 */
public class TipoMedioContactoModelTest {

    private TipoMedioContactoModel instance;
    private TipoMedioContactoDAO dao;
    private MedioContactoDAO medioContactoDAO;

    public TipoMedioContactoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new TipoMedioContactoModel();
        dao = mock(TipoMedioContactoDAO.class);
        medioContactoDAO = mock(MedioContactoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.medioContactoDAO = medioContactoDAO;
        instance.inicializarRegistros();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarEliminacion method, of class TipoMedioContactoModel.
     */
    @Test
    public void testValidarEliminacion() {
        TipoMedioContacto r = new TipoMedioContacto(UUID.randomUUID());
        when(medioContactoDAO.countByTipo(r.getIdTipoMedioContacto())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConContactos() {
        TipoMedioContacto r = new TipoMedioContacto(UUID.randomUUID());
        when(medioContactoDAO.countByTipo(r.getIdTipoMedioContacto())).thenReturn(4L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        TipoMedioContacto r = new TipoMedioContacto();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(medioContactoDAO, never()).countByTipo(any());
    }

    /**
     * Test of getFacesContext method, of class TipoMedioContactoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class TipoMedioContactoModel.
     */
    @Test
    public void testValidarUnicidad() {
        TipoMedioContacto r = new TipoMedioContacto();
        r.setNombre("  Telefono  ");
        when(dao.existeNombre("Telefono", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Telefono", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        TipoMedioContacto r = new TipoMedioContacto();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        TipoMedioContacto r = new TipoMedioContacto(UUID.randomUUID());
        r.setNombre("Telefono");
        when(dao.existeNombre("Telefono", r.getIdTipoMedioContacto())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class TipoMedioContactoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<TipoMedioContacto> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class TipoMedioContactoModel.
     */
    @Test
    public void testNuevoRegistro() {
        TipoMedioContacto resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoMedioContacto());
        assertEquals(Boolean.TRUE, resultado.getActivo());
        assertEquals(".*", resultado.getExpresionRegular());
    }

    /**
     * Test of buscarRegistroPorId method, of class TipoMedioContactoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        TipoMedioContacto esperado = new TipoMedioContacto(id);
        instance.getModelo().setWrappedData(List.of(esperado));
        when(dao.findAll()).thenReturn(List.of(esperado));

        TipoMedioContacto resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        TipoMedioContacto resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
    }

    @Test
    public void testBuscarRegistroPorIdModeloVacio() {
        UUID id = UUID.randomUUID();
        instance.getModelo().setWrappedData(List.of());

        TipoMedioContacto resultado = instance.buscarRegistroPorId(id);

        assertNull(resultado);
    }

    /**
     * Test of getIdAsText method, of class TipoMedioContactoModel.
     */
    @Test
    public void testGetIdAsText() {
        TipoMedioContacto r = new TipoMedioContacto(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdTipoMedioContacto().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class TipoMedioContactoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        TipoMedioContacto esperado = new TipoMedioContacto(id);
        instance.getModelo().setWrappedData(List.of(esperado));

        TipoMedioContacto resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        instance.getModelo().setWrappedData(List.of(new TipoMedioContacto(UUID.randomUUID())));

        assertNull(instance.getIdByText("no-es-uuid"));
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
    }

}
