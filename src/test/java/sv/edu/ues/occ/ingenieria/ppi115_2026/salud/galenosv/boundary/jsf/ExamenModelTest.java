/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;

/**
 *
 * @author david
 */
public class ExamenModelTest {

    private ExamenModel instance;
    private ExamenDAO dao;
    private ExamenTipoExamenDAO examenTipoExamenDAO;
    private ProcedimientoPasoExamenDAO pasoExamenDAO;
    private ExamenTipoExamenModel examenTipoExamenModel;

    public ExamenModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ExamenModel();
        dao = mock(ExamenDAO.class);
        examenTipoExamenDAO = mock(ExamenTipoExamenDAO.class);
        pasoExamenDAO = mock(ProcedimientoPasoExamenDAO.class);
        examenTipoExamenModel = mock(ExamenTipoExamenModel.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.examenTipoExamenDAO = examenTipoExamenDAO;
        instance.pasoExamenDAO = pasoExamenDAO;
        instance.examenTipoExamenModel = examenTipoExamenModel;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarEliminacion method, of class ExamenModel.
     */
    @Test
    public void testValidarEliminacion() {
        Examen r = new Examen(UUID.randomUUID());
        when(examenTipoExamenDAO.countByIdExamen(r.getIdExamen())).thenReturn(0);
        when(pasoExamenDAO.countByExamen(r.getIdExamen())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConTipos() {
        Examen r = new Examen(UUID.randomUUID());
        when(examenTipoExamenDAO.countByIdExamen(r.getIdExamen())).thenReturn(1);
        when(pasoExamenDAO.countByExamen(r.getIdExamen())).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConPasos() {
        Examen r = new Examen(UUID.randomUUID());
        when(examenTipoExamenDAO.countByIdExamen(r.getIdExamen())).thenReturn(0);
        when(pasoExamenDAO.countByExamen(r.getIdExamen())).thenReturn(2L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        Examen r = new Examen();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(examenTipoExamenDAO, never()).countByIdExamen(any());
        verify(pasoExamenDAO, never()).countByExamen(any());
    }

    /**
     * Test of getFacesContext method, of class ExamenModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class ExamenModel.
     */
    @Test
    public void testValidarUnicidad() {
        Examen r = new Examen();
        r.setNombre("  Examen  ");
        when(dao.existeNombre("Examen", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Examen", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        Examen r = new Examen();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        Examen r = new Examen(UUID.randomUUID());
        r.setNombre("Examen");
        when(dao.existeNombre("Examen", r.getIdExamen())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class ExamenModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Examen> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ExamenModel.
     */
    @Test
    public void testNuevoRegistro() {
        Examen resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdExamen());
        assertEquals(Boolean.TRUE, resultado.getActivo());
    }

    /**
     * Test of selectionHandler method, of class ExamenModel.
     */
    @Test
    public void testSelectionHandler() {
        Examen r = new Examen(UUID.randomUUID());
        SelectEvent<Examen> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        verify(examenTipoExamenModel).setIdExamen(r.getIdExamen());
    }

    @Test
    public void testSelectionHandlerNulo() {
        instance.selectionHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of btnNuevoHandler method, of class ExamenModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertNotNull(instance.getRegistro().getIdExamen());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        verify(examenTipoExamenModel).setIdExamen(instance.getRegistro().getIdExamen());
    }

    /**
     * Test of btnCancelarHandler method, of class ExamenModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new Examen(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(examenTipoExamenModel).setIdExamen(null);
    }

    /**
     * Test of btnGuardarHandler method, of class ExamenModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        Examen r = new Examen(UUID.randomUUID());
        r.setNombre("Examen");
        when(dao.existeNombre("Examen", null)).thenReturn(false);
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnGuardarHandlerNombreVacio() {
        Examen r = new Examen(UUID.randomUUID());
        r.setNombre("  ");
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
    }

    /**
     * Test of btnModificarHandler method, of class ExamenModel.
     */
    @Test
    public void testBtnModificarHandler() {
        Examen r = new Examen(UUID.randomUUID());
        r.setNombre("Examen");
        when(dao.existeNombre("Examen", r.getIdExamen())).thenReturn(false);
        instance.setRegistro(r);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of btnEliminarHandler method, of class ExamenModel.
     */
    @Test
    public void testBtnEliminarHandler() {
        Examen r = new Examen(UUID.randomUUID());
        when(examenTipoExamenDAO.countByIdExamen(r.getIdExamen())).thenReturn(0);
        when(pasoExamenDAO.countByExamen(r.getIdExamen())).thenReturn(0L);
        instance.setRegistro(r);

        instance.btnEliminarHandler(mock(ActionEvent.class));

        verify(dao).eliminar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of buscarRegistroPorId method, of class ExamenModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Examen esperado = new Examen(id);
        instance.inicializarRegistros();
        instance.getModelo().setWrappedData(List.of(esperado));
        when(dao.findAll()).thenReturn(List.of(esperado));

        Examen resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
    }

    /**
     * Test of getIdAsText method, of class ExamenModel.
     */
    @Test
    public void testGetIdAsText() {
        Examen r = new Examen(UUID.randomUUID());

        assertEquals(r.getIdExamen().toString(), instance.getIdAsText(r));
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
        assertNull(instance.getIdAsText(new Examen()));
    }

    /**
     * Test of getIdByText method, of class ExamenModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Examen esperado = new Examen(id);
        instance.inicializarRegistros();
        instance.getModelo().setWrappedData(List.of(esperado));

        assertSame(esperado, instance.getIdByText(id.toString()));
    }

    @Test
    public void testGetIdByTextInvalido() {
        instance.inicializarRegistros();
        instance.getModelo().setWrappedData(List.of(new Examen(UUID.randomUUID())));

        assertNull(instance.getIdByText("no-es-uuid"));
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of getExamenTipoExamenModel method, of class ExamenModel.
     */
    @Test
    public void testGetExamenTipoExamenModel() {
        Examen r = new Examen(UUID.randomUUID());
        instance.setRegistro(r);

        ExamenTipoExamenModel resultado = instance.getExamenTipoExamenModel();

        assertSame(examenTipoExamenModel, resultado);
        verify(examenTipoExamenModel).setIdExamen(r.getIdExamen());
    }

    @Test
    public void testGetExamenTipoExamenModelSinRegistro() {
        instance.setRegistro(null);

        assertSame(examenTipoExamenModel, instance.getExamenTipoExamenModel());
        verify(examenTipoExamenModel).setIdExamen(null);
    }
}
