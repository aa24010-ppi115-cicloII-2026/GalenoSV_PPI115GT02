/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenResultado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/**
 *
 * @author david
 */
public class ExamenResultadoModelTest {

    private ExamenResultadoModel instance;
    private ExamenResultadoDAO dao;
    private OrdenExamenDAO ordenExamenDAO;

    public ExamenResultadoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ExamenResultadoModel();
        dao = mock(ExamenResultadoDAO.class);
        ordenExamenDAO = mock(OrdenExamenDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.ordenExamenDAO = ordenExamenDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class ExamenResultadoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ExamenResultadoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ExamenResultado> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ExamenResultadoModel.
     */
    @Test
    public void testNuevoRegistro() {
        ExamenResultado resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdExamenResultado());
        assertNotNull(resultado.getFechaCreacion());
    }

    /**
     * Test of buscarRegistroPorId method, of class ExamenResultadoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ExamenResultado esperado = new ExamenResultado(id);
        when(dao.find(id)).thenReturn(esperado);

        assertSame(esperado, instance.buscarRegistroPorId(id));
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
    }

    /**
     * Test of getIdAsText method, of class ExamenResultadoModel.
     */
    @Test
    public void testGetIdAsText() {
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());

        assertEquals(r.getIdExamenResultado().toString(), instance.getIdAsText(r));
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
        assertNull(instance.getIdAsText(new ExamenResultado()));
    }

    /**
     * Test of getIdByText method, of class ExamenResultadoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ExamenResultado esperado = new ExamenResultado(id);
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
     * Test of selectionHandler method, of class ExamenResultadoModel.
     */
    @Test
    public void testSelectionHandler() {
        UUID idOrden = UUID.randomUUID();
        OrdenExamen orden = new OrdenExamen(idOrden);
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());
        r.setIdOrdenExamen(orden);
        SelectEvent<ExamenResultado> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idOrden.toString(), instance.getIdOrdenExamenSeleccionada());
    }

    @Test
    public void testSelectionHandlerNulo() {
        instance.selectionHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdOrdenExamenSeleccionada());
    }

    /**
     * Test of btnNuevoHandler method, of class ExamenResultadoModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdOrdenExamenSeleccionada(UUID.randomUUID().toString());

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertNotNull(instance.getRegistro().getIdExamenResultado());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdOrdenExamenSeleccionada());
    }

    /**
     * Test of btnCancelarHandler method, of class ExamenResultadoModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ExamenResultado(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);
        instance.setIdOrdenExamenSeleccionada(UUID.randomUUID().toString());

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdOrdenExamenSeleccionada());
    }

    /**
     * Test of btnGuardarHandler method, of class ExamenResultadoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idOrden = UUID.randomUUID();
        OrdenExamen orden = new OrdenExamen(idOrden);
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());
        when(ordenExamenDAO.find(idOrden)).thenReturn(orden);
        instance.setRegistro(r);
        instance.setIdOrdenExamenSeleccionada(idOrden.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(orden, r.getIdOrdenExamen());
        assertNull(instance.getRegistro());
        assertNull(instance.getIdOrdenExamenSeleccionada());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnGuardarHandlerSinOrden() {
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());
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
     * Test of btnModificarHandler method, of class ExamenResultadoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idOrden = UUID.randomUUID();
        OrdenExamen orden = new OrdenExamen(idOrden);
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());
        when(ordenExamenDAO.find(idOrden)).thenReturn(orden);
        instance.setRegistro(r);
        instance.setIdOrdenExamenSeleccionada(idOrden.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertSame(orden, r.getIdOrdenExamen());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnModificarHandlerSinOrden() {
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());
        instance.setRegistro(r);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
        assertSame(r, instance.getRegistro());
    }

    /**
     * Test of descripcionOrden method, of class ExamenResultadoModel.
     */
    @Test
    public void testDescripcionOrden() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        orden.setFechaCreacion(OffsetDateTime.of(2024, 1, 2, 10, 30, 0, 0, ZoneOffset.UTC));
        orden.setIndicaciones("Reposo");

        String resultado = instance.descripcionOrden(orden);

        assertEquals(" | 02/01/2024 04:30 | Reposo", resultado);
    }

    @Test
    public void testDescripcionOrdenNula() {
        assertEquals("N/A", instance.descripcionOrden(null));
    }

    /**
     * Test of formatearFecha method, of class ExamenResultadoModel.
     */
    @Test
    public void testFormatearFecha() {
        OffsetDateTime fecha = OffsetDateTime.of(2024, 1, 2, 10, 30, 0, 0, ZoneOffset.UTC);

        assertEquals("02/01/2024 04:30", instance.formatearFecha(fecha));
    }

    @Test
    public void testFormatearFechaNula() {
        assertEquals("", instance.formatearFecha(null));
    }

    /**
     * Test of getOrdenesExamen method, of class ExamenResultadoModel.
     */
    @Test
    public void testGetOrdenesExamen() {
        List<OrdenExamen> esperados = List.of(new OrdenExamen(UUID.randomUUID()));
        when(ordenExamenDAO.findAll()).thenReturn(esperados);

        assertSame(esperados, instance.getOrdenesExamen());
    }

    /**
     * Test of getIdOrdenExamenSeleccionada method, of class ExamenResultadoModel.
     */
    @Test
    public void testGetIdOrdenExamenSeleccionada() {
        assertNull(instance.getIdOrdenExamenSeleccionada());

        instance.setIdOrdenExamenSeleccionada("abc");

        assertEquals("abc", instance.getIdOrdenExamenSeleccionada());
    }

    /**
     * Test of setIdOrdenExamenSeleccionada method, of class ExamenResultadoModel.
     */
    @Test
    public void testSetIdOrdenExamenSeleccionada() {
        String id = UUID.randomUUID().toString();

        instance.setIdOrdenExamenSeleccionada(id);

        assertEquals(id, instance.getIdOrdenExamenSeleccionada());
    }
}
