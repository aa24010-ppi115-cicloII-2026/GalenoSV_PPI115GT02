/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 *
 * @author david
 */
public class PersonaRolModelTest {

    private PersonaRolModel instance;
    private PersonaRolDAO dao;
    private PersonaDAO personaDAO;
    private RolDAO rolDAO;
    private ClinicaDAO clinicaDAO;
    private ConsultaDAO consultaDAO;
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    public PersonaRolModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new PersonaRolModel();
        dao = mock(PersonaRolDAO.class);
        personaDAO = mock(PersonaDAO.class);
        rolDAO = mock(RolDAO.class);
        clinicaDAO = mock(ClinicaDAO.class);
        consultaDAO = mock(ConsultaDAO.class);
        consultaProcedimientoPasoDAO = mock(ConsultaProcedimientoPasoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.personaDAO = personaDAO;
        instance.rolDAO = rolDAO;
        instance.clinicaDAO = clinicaDAO;
        instance.consultaDAO = consultaDAO;
        instance.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of establecerClinicaMaestro method, of class PersonaRolModel.
     */
    @Test
    public void testEstablecerClinicaMaestro() {
        UUID id = UUID.randomUUID();

        instance.establecerClinicaMaestro(id);

        assertEquals(id.toString(), instance.getIdClinicaSeleccionada());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNotNull(instance.getModelo());
    }

    @Test
    public void testEstablecerClinicaMaestroNulo() {
        UUID id = UUID.randomUUID();
        instance.establecerClinicaMaestro(id);

        instance.establecerClinicaMaestro(null);

        assertNull(instance.getIdClinicaSeleccionada());
    }

    /**
     * Test of inicializarRegistros method, of class PersonaRolModel.
     */
    @Test
    public void testInicializarRegistros() {
        instance.inicializarRegistros();

        assertNotNull(instance.getModelo());
    }

    /**
     * Test of validarUnicidad method, of class PersonaRolModel.
     */
    @Test
    public void testValidarUnicidad() {
        Persona persona = new Persona(UUID.randomUUID());
        Rol rol = new Rol(UUID.randomUUID());
        Clinica clinica = new Clinica(UUID.randomUUID(), "Clinica");
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        r.setIdPersona(persona);
        r.setIdRol(rol);
        r.setIdClinica(clinica);
        when(dao.existeAsignacion(persona.getIdPersona(), rol.getIdRol(), clinica.getIdClinica(), null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao).existeAsignacion(persona.getIdPersona(), rol.getIdRol(), clinica.getIdClinica(), null);
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        Persona persona = new Persona(UUID.randomUUID());
        Rol rol = new Rol(UUID.randomUUID());
        Clinica clinica = new Clinica(UUID.randomUUID(), "Clinica");
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        r.setIdPersona(persona);
        r.setIdRol(rol);
        r.setIdClinica(clinica);
        when(dao.existeAsignacion(persona.getIdPersona(), rol.getIdRol(), clinica.getIdClinica(), null)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, false));
    }

    @Test
    public void testValidarUnicidadIncompleto() {
        PersonaRol r = new PersonaRol(UUID.randomUUID());

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeAsignacion(any(), any(), any(), any());
    }

    /**
     * Test of validarEliminacion method, of class PersonaRolModel.
     */
    @Test
    public void testValidarEliminacion() {
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        when(consultaDAO.countByPersonaRol(r.getIdPersonaRol())).thenReturn(0L);
        when(consultaProcedimientoPasoDAO.countByPersonaRol(r.getIdPersonaRol())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(consultaDAO).countByPersonaRol(r.getIdPersonaRol());
        verify(consultaProcedimientoPasoDAO).countByPersonaRol(r.getIdPersonaRol());
    }

    @Test
    public void testValidarEliminacionConDependencias() {
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        when(consultaDAO.countByPersonaRol(r.getIdPersonaRol())).thenReturn(2L);
        when(consultaProcedimientoPasoDAO.countByPersonaRol(r.getIdPersonaRol())).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        PersonaRol r = new PersonaRol();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(consultaDAO, never()).countByPersonaRol(any());
        verify(consultaProcedimientoPasoDAO, never()).countByPersonaRol(any());
    }

    /**
     * Test of getFacesContext method, of class PersonaRolModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class PersonaRolModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<PersonaRol> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class PersonaRolModel.
     */
    @Test
    public void testNuevoRegistro() {
        PersonaRol resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdPersonaRol());
        assertNotNull(resultado.getFechaCreacion());
    }

    /**
     * Test of buscarRegistroPorId method, of class PersonaRolModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        PersonaRol esperado = new PersonaRol(id);
        when(dao.find(id)).thenReturn(esperado);

        PersonaRol resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        PersonaRol resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class PersonaRolModel.
     */
    @Test
    public void testGetIdAsText() {
        PersonaRol r = new PersonaRol(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdPersonaRol().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
        assertNull(instance.getIdAsText(new PersonaRol()));
    }

    /**
     * Test of getIdByText method, of class PersonaRolModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        PersonaRol esperado = new PersonaRol(id);
        when(dao.find(id)).thenReturn(esperado);

        PersonaRol resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
        verify(dao, never()).find(any());
    }

    /**
     * Test of selectionHandler method, of class PersonaRolModel.
     */
    @Test
    public void testSelectionHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        r.setIdPersona(new Persona(idPersona));
        r.setIdRol(new Rol(idRol));
        r.setIdClinica(new Clinica(idClinica, "Clinica"));
        SelectEvent<PersonaRol> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idPersona.toString(), instance.getIdPersonaSeleccionada());
        assertEquals(idRol.toString(), instance.getIdRolSeleccionado());
        assertEquals(idClinica.toString(), instance.getIdClinicaSeleccionada());
    }

    @Test
    public void testSelectionHandlerSinObjeto() {
        SelectEvent<PersonaRol> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        instance.selectionHandler(evento);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of btnNuevoHandler method, of class PersonaRolModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdRolSeleccionado());
        assertNull(instance.getIdClinicaSeleccionada());
    }

    /**
     * Test of btnCancelarHandler method, of class PersonaRolModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new PersonaRol(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdRolSeleccionado());
        assertNull(instance.getIdClinicaSeleccionada());
    }

    /**
     * Test of btnGuardarHandler method, of class PersonaRolModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        Rol rol = new Rol(idRol);
        rol.setActivo(Boolean.TRUE);
        Clinica clinica = new Clinica(idClinica, "Clinica");
        clinica.setActivo(Boolean.TRUE);
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdPersonaSeleccionada(idPersona.toString());
        instance.setIdRolSeleccionado(idRol.toString());
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(personaDAO.find(idPersona)).thenReturn(persona);
        when(rolDAO.find(idRol)).thenReturn(rol);
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(persona, r.getIdPersona());
        assertSame(rol, r.getIdRol());
        assertSame(clinica, r.getIdClinica());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    public void testBtnGuardarHandlerSinPersona() {
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class PersonaRolModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        Rol rol = new Rol(idRol);
        rol.setActivo(Boolean.TRUE);
        Clinica clinica = new Clinica(idClinica, "Clinica");
        clinica.setActivo(Boolean.TRUE);
        PersonaRol r = new PersonaRol(UUID.randomUUID());
        instance.setRegistro(r);
        instance.setIdPersonaSeleccionada(idPersona.toString());
        instance.setIdRolSeleccionado(idRol.toString());
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(personaDAO.find(idPersona)).thenReturn(persona);
        when(rolDAO.find(idRol)).thenReturn(rol);
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertSame(persona, r.getIdPersona());
        assertSame(rol, r.getIdRol());
        assertSame(clinica, r.getIdClinica());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of getPersonas method, of class PersonaRolModel.
     */
    @Test
    public void testGetPersonas() {
        List<Persona> esperado = Arrays.asList(new Persona(UUID.randomUUID()));
        when(personaDAO.findAll()).thenReturn(esperado);

        List<Persona> resultado = instance.getPersonas();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getRoles method, of class PersonaRolModel.
     */
    @Test
    public void testGetRoles() {
        List<Rol> esperado = Arrays.asList(new Rol(UUID.randomUUID()));
        when(rolDAO.findActivos()).thenReturn(esperado);

        List<Rol> resultado = instance.getRoles();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getClinicas method, of class PersonaRolModel.
     */
    @Test
    public void testGetClinicas() {
        List<Clinica> esperado = Arrays.asList(new Clinica(UUID.randomUUID(), "Clinica"));
        when(clinicaDAO.findActivas()).thenReturn(esperado);

        List<Clinica> resultado = instance.getClinicas();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdPersonaSeleccionada method, of class PersonaRolModel.
     */
    @Test
    public void testGetIdPersonaSeleccionada() {
        instance.setIdPersonaSeleccionada("persona-1");

        String resultado = instance.getIdPersonaSeleccionada();

        assertEquals("persona-1", resultado);
    }

    /**
     * Test of setIdPersonaSeleccionada method, of class PersonaRolModel.
     */
    @Test
    public void testSetIdPersonaSeleccionada() {
        String idPersonaSeleccionada = "persona-2";

        instance.setIdPersonaSeleccionada(idPersonaSeleccionada);

        assertEquals(idPersonaSeleccionada, instance.getIdPersonaSeleccionada());
    }

    /**
     * Test of getIdRolSeleccionado method, of class PersonaRolModel.
     */
    @Test
    public void testGetIdRolSeleccionado() {
        instance.setIdRolSeleccionado("rol-1");

        String resultado = instance.getIdRolSeleccionado();

        assertEquals("rol-1", resultado);
    }

    /**
     * Test of setIdRolSeleccionado method, of class PersonaRolModel.
     */
    @Test
    public void testSetIdRolSeleccionado() {
        String idRolSeleccionado = "rol-2";

        instance.setIdRolSeleccionado(idRolSeleccionado);

        assertEquals(idRolSeleccionado, instance.getIdRolSeleccionado());
    }

    /**
     * Test of getIdClinicaSeleccionada method, of class PersonaRolModel.
     */
    @Test
    public void testGetIdClinicaSeleccionada() {
        instance.setIdClinicaSeleccionada("clinica-1");

        String resultado = instance.getIdClinicaSeleccionada();

        assertEquals("clinica-1", resultado);
    }

    /**
     * Test of setIdClinicaSeleccionada method, of class PersonaRolModel.
     */
    @Test
    public void testSetIdClinicaSeleccionada() {
        String idClinicaSeleccionada = "clinica-2";

        instance.setIdClinicaSeleccionada(idClinicaSeleccionada);

        assertEquals(idClinicaSeleccionada, instance.getIdClinicaSeleccionada());
    }

}
