/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
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
import static org.mockito.Mockito.*;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author david
 */
public class ProcedimientoPasoExamenModelTest {

    private ProcedimientoPasoExamenModel instance;
    private ProcedimientoPasoExamenDAO dao;
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    private ExamenDAO examenDAO;

    public ProcedimientoPasoExamenModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ProcedimientoPasoExamenModel();
        dao = mock(ProcedimientoPasoExamenDAO.class);
        procedimientoPasoDAO = mock(ProcedimientoPasoDAO.class);
        examenDAO = mock(ExamenDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.procedimientoPasoDAO = procedimientoPasoDAO;
        instance.examenDAO = examenDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ProcedimientoPasoExamen> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testNuevoRegistro() {
        ProcedimientoPasoExamen resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProcedimientoPasoExamen());
        assertNotNull(resultado.getFechaCreacion());
        assertEquals(Boolean.TRUE, resultado.getActivo());
    }

    /**
     * Test of buscarRegistroPorId method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ProcedimientoPasoExamen esperado = new ProcedimientoPasoExamen(id);
        when(dao.find(id)).thenReturn(esperado);

        ProcedimientoPasoExamen resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetIdAsText() {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdProcedimientoPasoExamen().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ProcedimientoPasoExamen esperado = new ProcedimientoPasoExamen(id);
        when(dao.find(id)).thenReturn(esperado);

        ProcedimientoPasoExamen resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of selectionHandler method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        r.setIdProcedimientoPaso(new ProcedimientoPaso(idProcedimientoPaso));
        r.setIdExamen(new Examen(idExamen));
        SelectEvent<ProcedimientoPasoExamen> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idProcedimientoPaso.toString(), instance.getIdProcedimientoPasoSeleccionado());
        assertEquals(idExamen.toString(), instance.getIdExamenSeleccionado());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandlerNulo() {
        SelectEvent<ProcedimientoPasoExamen> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        assertDoesNotThrow(() -> instance.selectionHandler(evento));
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnNuevoHandler method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdProcedimientoPasoSeleccionado("algo");
        instance.setIdExamenSeleccionado("algo");

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdProcedimientoPasoSeleccionado());
        assertNull(instance.getIdExamenSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ProcedimientoPasoExamen(UUID.randomUUID()));
        instance.setIdProcedimientoPasoSeleccionado("algo");
        instance.setIdExamenSeleccionado("algo");

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdProcedimientoPasoSeleccionado());
        assertNull(instance.getIdExamenSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        ProcedimientoPaso paso = new ProcedimientoPaso(idProcedimientoPaso);
        Examen examen = new Examen(idExamen);
        when(procedimientoPasoDAO.find(idProcedimientoPaso)).thenReturn(paso);
        when(examenDAO.find(idExamen)).thenReturn(examen);
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoSeleccionado(idProcedimientoPaso.toString());
        instance.setIdExamenSeleccionado(idExamen.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(paso, r.getIdProcedimientoPaso());
        assertSame(examen, r.getIdExamen());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdProcedimientoPasoSeleccionado());
        assertNull(instance.getIdExamenSeleccionado());
    }

    @Test
    public void testBtnGuardarHandlerSinPaso() {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdExamenSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerSinExamen() {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        when(procedimientoPasoDAO.find(idProcedimientoPaso)).thenReturn(new ProcedimientoPaso(idProcedimientoPaso));
        when(examenDAO.find(idExamen)).thenReturn(new Examen(idExamen));
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoSeleccionado(idProcedimientoPaso.toString());
        instance.setIdExamenSeleccionado(idExamen.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of getPasos method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetPasos() {
        List<ProcedimientoPaso> esperado = List.of(new ProcedimientoPaso(UUID.randomUUID()));
        when(procedimientoPasoDAO.findAll()).thenReturn(esperado);

        List<ProcedimientoPaso> resultado = instance.getPasos();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getExamenes method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetExamenes() {
        List<Examen> esperado = List.of(new Examen(UUID.randomUUID()));
        when(examenDAO.findAll()).thenReturn(esperado);

        List<Examen> resultado = instance.getExamenes();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdProcedimientoPasoSeleccionado method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetIdProcedimientoPasoSeleccionado() {
        instance.setIdProcedimientoPasoSeleccionado("paso-1");

        assertEquals("paso-1", instance.getIdProcedimientoPasoSeleccionado());
    }

    /**
     * Test of setIdProcedimientoPasoSeleccionado method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testSetIdProcedimientoPasoSeleccionado() {
        instance.setIdProcedimientoPasoSeleccionado("paso-2");

        assertEquals("paso-2", instance.getIdProcedimientoPasoSeleccionado());
    }

    /**
     * Test of getIdExamenSeleccionado method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testGetIdExamenSeleccionado() {
        instance.setIdExamenSeleccionado("examen-1");

        assertEquals("examen-1", instance.getIdExamenSeleccionado());
    }

    /**
     * Test of setIdExamenSeleccionado method, of class ProcedimientoPasoExamenModel.
     */
    @Test
    public void testSetIdExamenSeleccionado() {
        instance.setIdExamenSeleccionado("examen-2");

        assertEquals("examen-2", instance.getIdExamenSeleccionado());
    }

}
