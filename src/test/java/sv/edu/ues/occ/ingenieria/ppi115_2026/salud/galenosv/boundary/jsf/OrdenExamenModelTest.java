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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/**
 *
 * @author david
 */
public class OrdenExamenModelTest {

    private OrdenExamenModel instance;
    private OrdenExamenDAO dao;
    private ExamenResultadoDAO examenResultadoDAO;
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    public OrdenExamenModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new OrdenExamenModel();
        dao = mock(OrdenExamenDAO.class);
        examenResultadoDAO = mock(ExamenResultadoDAO.class);
        consultaProcedimientoPasoDAO = mock(ConsultaProcedimientoPasoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.examenResultadoDAO = examenResultadoDAO;
        instance.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarEliminacion method, of class OrdenExamenModel.
     */
    @Test
    public void testValidarEliminacion() {
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
        when(examenResultadoDAO.countByOrden(r.getIdOrdenExamen())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConResultados() {
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
        when(examenResultadoDAO.countByOrden(r.getIdOrdenExamen())).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        OrdenExamen r = new OrdenExamen();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(examenResultadoDAO, never()).countByOrden(any());
    }

    /**
     * Test of getFacesContext method, of class OrdenExamenModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class OrdenExamenModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<OrdenExamen> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class OrdenExamenModel.
     */
    @Test
    public void testNuevoRegistro() {
        OrdenExamen resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdOrdenExamen());
        assertNotNull(resultado.getFechaCreacion());
    }

    /**
     * Test of buscarRegistroPorId method, of class OrdenExamenModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        OrdenExamen esperado = new OrdenExamen(id);
        when(dao.find(id)).thenReturn(esperado);

        assertSame(esperado, instance.buscarRegistroPorId(id));
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
    }

    /**
     * Test of getIdAsText method, of class OrdenExamenModel.
     */
    @Test
    public void testGetIdAsText() {
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());

        assertEquals(r.getIdOrdenExamen().toString(), instance.getIdAsText(r));
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
        assertNull(instance.getIdAsText(new OrdenExamen()));
    }

    /**
     * Test of getIdByText method, of class OrdenExamenModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        OrdenExamen esperado = new OrdenExamen(id);
        when(dao.find(id)).thenReturn(esperado);

        assertSame(esperado, instance.getIdByText(id.toString()));
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of selectionHandler method, of class OrdenExamenModel.
     */
    @Test
    public void testSelectionHandler() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
        r.setIdConsultaProcedimientoPaso(paso);
        SelectEvent<OrdenExamen> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idPaso.toString(), instance.getIdConsultaProcedimientoPasoSeleccionado());
    }

    @Test
    public void testSelectionHandlerNulo() {
        instance.selectionHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdConsultaProcedimientoPasoSeleccionado());
    }

    /**
     * Test of btnNuevoHandler method, of class OrdenExamenModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdConsultaProcedimientoPasoSeleccionado(UUID.randomUUID().toString());

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertNotNull(instance.getRegistro().getIdOrdenExamen());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdConsultaProcedimientoPasoSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class OrdenExamenModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new OrdenExamen(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);
        instance.setIdConsultaProcedimientoPasoSeleccionado(UUID.randomUUID().toString());

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdConsultaProcedimientoPasoSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class OrdenExamenModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
        when(consultaProcedimientoPasoDAO.find(idPaso)).thenReturn(paso);
        instance.setRegistro(r);
        instance.setIdConsultaProcedimientoPasoSeleccionado(idPaso.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(paso, r.getIdConsultaProcedimientoPaso());
        assertNull(instance.getRegistro());
        assertNull(instance.getIdConsultaProcedimientoPasoSeleccionado());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnGuardarHandlerSinPaso() {
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
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
     * Test of btnModificarHandler method, of class OrdenExamenModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
        when(consultaProcedimientoPasoDAO.find(idPaso)).thenReturn(paso);
        instance.setRegistro(r);
        instance.setIdConsultaProcedimientoPasoSeleccionado(idPaso.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertSame(paso, r.getIdConsultaProcedimientoPaso());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnModificarHandlerSinPaso() {
        OrdenExamen r = new OrdenExamen(UUID.randomUUID());
        instance.setRegistro(r);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
        assertSame(r, instance.getRegistro());
    }

    /**
     * Test of getConsultaProcedimientoPasos method, of class OrdenExamenModel.
     */
    @Test
    public void testGetConsultaProcedimientoPasos() {
        List<ConsultaProcedimientoPaso> esperados = List.of(new ConsultaProcedimientoPaso(UUID.randomUUID()));
        when(consultaProcedimientoPasoDAO.findAll()).thenReturn(esperados);

        List<ConsultaProcedimientoPaso> resultado = instance.getConsultaProcedimientoPasos();

        assertSame(esperados, resultado);
    }

    /**
     * Test of getIdConsultaProcedimientoPasoSeleccionado method, of class OrdenExamenModel.
     */
    @Test
    public void testGetIdConsultaProcedimientoPasoSeleccionado() {
        assertNull(instance.getIdConsultaProcedimientoPasoSeleccionado());

        instance.setIdConsultaProcedimientoPasoSeleccionado("abc");

        assertEquals("abc", instance.getIdConsultaProcedimientoPasoSeleccionado());
    }

    /**
     * Test of setIdConsultaProcedimientoPasoSeleccionado method, of class OrdenExamenModel.
     */
    @Test
    public void testSetIdConsultaProcedimientoPasoSeleccionado() {
        String id = UUID.randomUUID().toString();

        instance.setIdConsultaProcedimientoPasoSeleccionado(id);

        assertEquals(id, instance.getIdConsultaProcedimientoPasoSeleccionado());
    }
}
