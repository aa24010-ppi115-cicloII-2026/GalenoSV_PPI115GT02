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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author david
 */
public class ProcedimientoPasoSecuenciaModelTest {

    private ProcedimientoPasoSecuenciaModel instance;
    private ProcedimientoPasoSecuenciaDAO dao;
    private ProcedimientoPasoDAO procedimientoPasoDAO;

    public ProcedimientoPasoSecuenciaModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ProcedimientoPasoSecuenciaModel();
        dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        procedimientoPasoDAO = mock(ProcedimientoPasoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.procedimientoPasoDAO = procedimientoPasoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ProcedimientoPasoSecuencia> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testNuevoRegistro() {
        ProcedimientoPasoSecuencia resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProcedimientoPasoSecuencia());
    }

    /**
     * Test of buscarRegistroPorId method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ProcedimientoPasoSecuencia esperado = new ProcedimientoPasoSecuencia(id);
        when(dao.find(id)).thenReturn(esperado);

        ProcedimientoPasoSecuencia resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetIdAsText() {
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdProcedimientoPasoSecuencia().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ProcedimientoPasoSecuencia esperado = new ProcedimientoPasoSecuencia(id);
        when(dao.find(id)).thenReturn(esperado);

        ProcedimientoPasoSecuencia resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of selectionHandler method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        UUID idReferencia = UUID.randomUUID();
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        r.setIdProcedimientoPaso(new ProcedimientoPaso(idProcedimientoPaso));
        r.setIdProcedimientoPasoReferencia(idReferencia);
        SelectEvent<ProcedimientoPasoSecuencia> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idProcedimientoPaso.toString(), instance.getIdProcedimientoPasoSeleccionado());
        assertEquals(idReferencia.toString(), instance.getIdProcedimientoPasoReferenciaTexto());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandlerNulo() {
        SelectEvent<ProcedimientoPasoSecuencia> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        assertDoesNotThrow(() -> instance.selectionHandler(evento));
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnNuevoHandler method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdProcedimientoPasoSeleccionado("algo");
        instance.setIdProcedimientoPasoReferenciaTexto("algo");

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdProcedimientoPasoSeleccionado());
        assertNull(instance.getIdProcedimientoPasoReferenciaTexto());
    }

    /**
     * Test of btnCancelarHandler method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ProcedimientoPasoSecuencia(UUID.randomUUID()));
        instance.setIdProcedimientoPasoSeleccionado("algo");
        instance.setIdProcedimientoPasoReferenciaTexto("algo");

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdProcedimientoPasoSeleccionado());
        assertNull(instance.getIdProcedimientoPasoReferenciaTexto());
    }

    /**
     * Test of btnGuardarHandler method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        UUID idReferencia = UUID.randomUUID();
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        ProcedimientoPaso paso = new ProcedimientoPaso(idProcedimientoPaso);
        when(procedimientoPasoDAO.find(idProcedimientoPaso)).thenReturn(paso);
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoSeleccionado(idProcedimientoPaso.toString());
        instance.setIdProcedimientoPasoReferenciaTexto(idReferencia.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(paso, r.getIdProcedimientoPaso());
        assertEquals(idReferencia, r.getIdProcedimientoPasoReferencia());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdProcedimientoPasoSeleccionado());
        assertNull(instance.getIdProcedimientoPasoReferenciaTexto());
    }

    @Test
    public void testBtnGuardarHandlerSinReferencia() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        when(procedimientoPasoDAO.find(idProcedimientoPaso)).thenReturn(new ProcedimientoPaso(idProcedimientoPaso));
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoSeleccionado(idProcedimientoPaso.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertNull(r.getIdProcedimientoPasoReferencia());
    }

    @Test
    public void testBtnGuardarHandlerSinPaso() {
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoReferenciaTexto(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
        assertSame(r, instance.getRegistro());
    }

    /**
     * Test of btnModificarHandler method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idProcedimientoPaso = UUID.randomUUID();
        UUID idReferencia = UUID.randomUUID();
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        when(procedimientoPasoDAO.find(idProcedimientoPaso)).thenReturn(new ProcedimientoPaso(idProcedimientoPaso));
        instance.setRegistro(r);
        instance.setIdProcedimientoPasoSeleccionado(idProcedimientoPaso.toString());
        instance.setIdProcedimientoPasoReferenciaTexto(idReferencia.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of getPasos method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetPasos() {
        List<ProcedimientoPaso> esperado = List.of(new ProcedimientoPaso(UUID.randomUUID()));
        when(procedimientoPasoDAO.findAll()).thenReturn(esperado);

        List<ProcedimientoPaso> resultado = instance.getPasos();

        assertSame(esperado, resultado);
    }

    /**
     * Test of nombrePasoReferencia method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testNombrePasoReferencia() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(id);
        paso.setNombre("Paso referenciado");
        when(procedimientoPasoDAO.find(id)).thenReturn(paso);

        String resultado = instance.nombrePasoReferencia(id);

        assertEquals("Paso referenciado", resultado);
    }

    @Test
    public void testNombrePasoReferenciaNulo() {
        assertEquals("", instance.nombrePasoReferencia(null));
        verify(procedimientoPasoDAO, never()).find(any());
    }

    @Test
    public void testNombrePasoReferenciaSinNombre() {
        UUID id = UUID.randomUUID();
        when(procedimientoPasoDAO.find(id)).thenReturn(new ProcedimientoPaso(id));

        String resultado = instance.nombrePasoReferencia(id);

        assertEquals(id.toString(), resultado);
    }

    @Test
    public void testNombrePasoReferenciaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(procedimientoPasoDAO.find(id)).thenReturn(null);

        String resultado = instance.nombrePasoReferencia(id);

        assertEquals(id.toString(), resultado);
    }

    @Test
    public void testNombrePasoReferenciaError() {
        UUID id = UUID.randomUUID();
        when(procedimientoPasoDAO.find(id)).thenThrow(new IllegalStateException("error"));

        String resultado = instance.nombrePasoReferencia(id);

        assertEquals(id.toString(), resultado);
    }

    /**
     * Test of getIdProcedimientoPasoSeleccionado method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetIdProcedimientoPasoSeleccionado() {
        instance.setIdProcedimientoPasoSeleccionado("paso-1");

        assertEquals("paso-1", instance.getIdProcedimientoPasoSeleccionado());
    }

    /**
     * Test of setIdProcedimientoPasoSeleccionado method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testSetIdProcedimientoPasoSeleccionado() {
        instance.setIdProcedimientoPasoSeleccionado("paso-2");

        assertEquals("paso-2", instance.getIdProcedimientoPasoSeleccionado());
    }

    /**
     * Test of getIdProcedimientoPasoReferenciaTexto method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testGetIdProcedimientoPasoReferenciaTexto() {
        instance.setIdProcedimientoPasoReferenciaTexto("referencia-1");

        assertEquals("referencia-1", instance.getIdProcedimientoPasoReferenciaTexto());
    }

    /**
     * Test of setIdProcedimientoPasoReferenciaTexto method, of class ProcedimientoPasoSecuenciaModel.
     */
    @Test
    public void testSetIdProcedimientoPasoReferenciaTexto() {
        instance.setIdProcedimientoPasoReferenciaTexto("referencia-2");

        assertEquals("referencia-2", instance.getIdProcedimientoPasoReferenciaTexto());
    }

}
