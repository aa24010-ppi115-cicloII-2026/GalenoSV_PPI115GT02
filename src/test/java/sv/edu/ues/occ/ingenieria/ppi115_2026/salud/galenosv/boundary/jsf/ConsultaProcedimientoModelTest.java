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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author david
 */
public class ConsultaProcedimientoModelTest {

    private ConsultaProcedimientoModel instance;
    private ConsultaProcedimientoDAO dao;
    private ConsultaDAO consultaDAO;
    private ProcedimientoDAO procedimientoDAO;

    public ConsultaProcedimientoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ConsultaProcedimientoModel();
        dao = mock(ConsultaProcedimientoDAO.class);
        consultaDAO = mock(ConsultaDAO.class);
        procedimientoDAO = mock(ProcedimientoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.consultaDAO = consultaDAO;
        instance.procedimientoDAO = procedimientoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ConsultaProcedimiento> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testNuevoRegistro() {
        ConsultaProcedimiento resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdConsultaProcedimiento());
        assertNotNull(resultado.getFechaInicio());
    }

    /**
     * Test of buscarRegistroPorId method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimiento esperado = new ConsultaProcedimiento(id);
        when(dao.find(id)).thenReturn(esperado);

        ConsultaProcedimiento resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetIdAsText() {
        ConsultaProcedimiento r = new ConsultaProcedimiento(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdConsultaProcedimiento().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimiento esperado = new ConsultaProcedimiento(id);
        when(dao.find(id)).thenReturn(esperado);

        ConsultaProcedimiento resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of selectionHandler method, of class ConsultaProcedimientoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        UUID idConsulta = UUID.randomUUID();
        UUID idProcedimiento = UUID.randomUUID();
        ConsultaProcedimiento r = new ConsultaProcedimiento(UUID.randomUUID());
        r.setIdConsulta(new Consulta(idConsulta));
        r.setIdProcedimiento(idProcedimiento);
        SelectEvent<ConsultaProcedimiento> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idConsulta.toString(), instance.getIdConsultaSeleccionada());
        assertEquals(idProcedimiento.toString(), instance.getIdProcedimientoSeleccionado());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandlerNulo() {
        SelectEvent<ConsultaProcedimiento> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        assertDoesNotThrow(() -> instance.selectionHandler(evento));
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnNuevoHandler method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdConsultaSeleccionada("algo");
        instance.setIdProcedimientoSeleccionado("algo");

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdConsultaSeleccionada());
        assertNull(instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ConsultaProcedimiento(UUID.randomUUID()));
        instance.setIdConsultaSeleccionada("algo");
        instance.setIdProcedimientoSeleccionado("algo");

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdConsultaSeleccionada());
        assertNull(instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idConsulta = UUID.randomUUID();
        UUID idProcedimiento = UUID.randomUUID();
        ConsultaProcedimiento r = new ConsultaProcedimiento(UUID.randomUUID());
        Consulta consulta = new Consulta(idConsulta);
        when(consultaDAO.find(idConsulta)).thenReturn(consulta);
        instance.setRegistro(r);
        instance.setIdConsultaSeleccionada(idConsulta.toString());
        instance.setIdProcedimientoSeleccionado(idProcedimiento.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(consulta, r.getIdConsulta());
        assertEquals(idProcedimiento, r.getIdProcedimiento());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdConsultaSeleccionada());
        assertNull(instance.getIdProcedimientoSeleccionado());
    }

    @Test
    public void testBtnGuardarHandlerSinConsulta() {
        ConsultaProcedimiento r = new ConsultaProcedimiento(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdProcedimientoSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerSinProcedimiento() {
        ConsultaProcedimiento r = new ConsultaProcedimiento(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdConsultaSeleccionada(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idConsulta = UUID.randomUUID();
        UUID idProcedimiento = UUID.randomUUID();
        ConsultaProcedimiento r = new ConsultaProcedimiento(UUID.randomUUID());
        when(consultaDAO.find(idConsulta)).thenReturn(new Consulta(idConsulta));
        instance.setRegistro(r);
        instance.setIdConsultaSeleccionada(idConsulta.toString());
        instance.setIdProcedimientoSeleccionado(idProcedimiento.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of getConsultas method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetConsultas() {
        List<Consulta> esperado = List.of(new Consulta(UUID.randomUUID()));
        when(consultaDAO.findAll()).thenReturn(esperado);

        List<Consulta> resultado = instance.getConsultas();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getProcedimientos method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetProcedimientos() {
        List<Procedimiento> esperado = List.of(new Procedimiento(UUID.randomUUID()));
        when(procedimientoDAO.findAll()).thenReturn(esperado);

        List<Procedimiento> resultado = instance.getProcedimientos();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdConsultaSeleccionada method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetIdConsultaSeleccionada() {
        instance.setIdConsultaSeleccionada("consulta-1");

        assertEquals("consulta-1", instance.getIdConsultaSeleccionada());
    }

    /**
     * Test of setIdConsultaSeleccionada method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testSetIdConsultaSeleccionada() {
        instance.setIdConsultaSeleccionada("consulta-2");

        assertEquals("consulta-2", instance.getIdConsultaSeleccionada());
    }

    /**
     * Test of getIdProcedimientoSeleccionado method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testGetIdProcedimientoSeleccionado() {
        instance.setIdProcedimientoSeleccionado("procedimiento-1");

        assertEquals("procedimiento-1", instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of setIdProcedimientoSeleccionado method, of class ConsultaProcedimientoModel.
     */
    @Test
    public void testSetIdProcedimientoSeleccionado() {
        instance.setIdProcedimientoSeleccionado("procedimiento-2");

        assertEquals("procedimiento-2", instance.getIdProcedimientoSeleccionado());
    }

}
