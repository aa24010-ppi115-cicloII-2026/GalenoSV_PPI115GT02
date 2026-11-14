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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

/**
 *
 * @author david
 */
public class ConsultaProcedimientoPasoModelTest {

    private ConsultaProcedimientoPasoModel instance;
    private ConsultaProcedimientoPasoDAO dao;
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    private PersonaRolDAO personaRolDAO;

    public ConsultaProcedimientoPasoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ConsultaProcedimientoPasoModel();
        dao = mock(ConsultaProcedimientoPasoDAO.class);
        consultaProcedimientoDAO = mock(ConsultaProcedimientoDAO.class);
        personaRolDAO = mock(PersonaRolDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.consultaProcedimientoDAO = consultaProcedimientoDAO;
        instance.personaRolDAO = personaRolDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ConsultaProcedimientoPaso> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testNuevoRegistro() {
        ConsultaProcedimientoPaso resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdConsultaProcedimientoPaso());
        assertNotNull(resultado.getFechaInicio());
        assertEquals("PENDIENTE", resultado.getEstado());
    }

    /**
     * Test of buscarRegistroPorId method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimientoPaso esperado = new ConsultaProcedimientoPaso(id);
        when(dao.find(id)).thenReturn(esperado);

        ConsultaProcedimientoPaso resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetIdAsText() {
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdConsultaProcedimientoPaso().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimientoPaso esperado = new ConsultaProcedimientoPaso(id);
        when(dao.find(id)).thenReturn(esperado);

        ConsultaProcedimientoPaso resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of selectionHandler method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        UUID idConsultaProcedimiento = UUID.randomUUID();
        UUID idPersonaRol = UUID.randomUUID();
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso(UUID.randomUUID());
        r.setIdConsultaProcedimiento(new ConsultaProcedimiento(idConsultaProcedimiento));
        r.setIdPersonaRol(new PersonaRol(idPersonaRol));
        SelectEvent<ConsultaProcedimientoPaso> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idConsultaProcedimiento.toString(), instance.getIdConsultaProcedimientoSeleccionado());
        assertEquals(idPersonaRol.toString(), instance.getIdPersonaRolSeleccionado());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandlerNulo() {
        SelectEvent<ConsultaProcedimientoPaso> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        assertDoesNotThrow(() -> instance.selectionHandler(evento));
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnNuevoHandler method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdConsultaProcedimientoSeleccionado("algo");
        instance.setIdPersonaRolSeleccionado("algo");

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdConsultaProcedimientoSeleccionado());
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ConsultaProcedimientoPaso(UUID.randomUUID()));
        instance.setIdConsultaProcedimientoSeleccionado("algo");
        instance.setIdPersonaRolSeleccionado("algo");

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdConsultaProcedimientoSeleccionado());
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idConsultaProcedimiento = UUID.randomUUID();
        UUID idPersonaRol = UUID.randomUUID();
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso(UUID.randomUUID());
        ConsultaProcedimiento consultaProcedimiento = new ConsultaProcedimiento(idConsultaProcedimiento);
        PersonaRol personaRol = new PersonaRol(idPersonaRol);
        when(consultaProcedimientoDAO.find(idConsultaProcedimiento)).thenReturn(consultaProcedimiento);
        when(personaRolDAO.find(idPersonaRol)).thenReturn(personaRol);
        instance.setRegistro(r);
        instance.setIdConsultaProcedimientoSeleccionado(idConsultaProcedimiento.toString());
        instance.setIdPersonaRolSeleccionado(idPersonaRol.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(consultaProcedimiento, r.getIdConsultaProcedimiento());
        assertSame(personaRol, r.getIdPersonaRol());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdConsultaProcedimientoSeleccionado());
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testBtnGuardarHandlerSinConsultaProcedimiento() {
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdPersonaRolSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerSinPersonaRol() {
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdConsultaProcedimientoSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idConsultaProcedimiento = UUID.randomUUID();
        UUID idPersonaRol = UUID.randomUUID();
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(consultaProcedimientoDAO.find(idConsultaProcedimiento)).thenReturn(new ConsultaProcedimiento(idConsultaProcedimiento));
        when(personaRolDAO.find(idPersonaRol)).thenReturn(new PersonaRol(idPersonaRol));
        instance.setRegistro(r);
        instance.setIdConsultaProcedimientoSeleccionado(idConsultaProcedimiento.toString());
        instance.setIdPersonaRolSeleccionado(idPersonaRol.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of getConsultaProcedimientos method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetConsultaProcedimientos() {
        List<ConsultaProcedimiento> esperado = List.of(new ConsultaProcedimiento(UUID.randomUUID()));
        when(consultaProcedimientoDAO.findAll()).thenReturn(esperado);

        List<ConsultaProcedimiento> resultado = instance.getConsultaProcedimientos();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getPersonasRol method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetPersonasRol() {
        List<PersonaRol> esperado = List.of(new PersonaRol(UUID.randomUUID()));
        when(personaRolDAO.findAll()).thenReturn(esperado);

        List<PersonaRol> resultado = instance.getPersonasRol();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdConsultaProcedimientoSeleccionado method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetIdConsultaProcedimientoSeleccionado() {
        instance.setIdConsultaProcedimientoSeleccionado("consulta-procedimiento-1");

        assertEquals("consulta-procedimiento-1", instance.getIdConsultaProcedimientoSeleccionado());
    }

    /**
     * Test of setIdConsultaProcedimientoSeleccionado method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testSetIdConsultaProcedimientoSeleccionado() {
        instance.setIdConsultaProcedimientoSeleccionado("consulta-procedimiento-2");

        assertEquals("consulta-procedimiento-2", instance.getIdConsultaProcedimientoSeleccionado());
    }

    /**
     * Test of getIdPersonaRolSeleccionado method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testGetIdPersonaRolSeleccionado() {
        instance.setIdPersonaRolSeleccionado("persona-rol-1");

        assertEquals("persona-rol-1", instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of setIdPersonaRolSeleccionado method, of class ConsultaProcedimientoPasoModel.
     */
    @Test
    public void testSetIdPersonaRolSeleccionado() {
        instance.setIdPersonaRolSeleccionado("persona-rol-2");

        assertEquals("persona-rol-2", instance.getIdPersonaRolSeleccionado());
    }

}
