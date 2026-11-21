/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 *
 * @author david
 */
public class PersonaModelTest {

    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");

    private PersonaModel instance;
    private PersonaDAO dao;
    private RolDAO rolDAO;
    private PersonaRolDAO personaRolDAO;
    private TipoMedioContactoDAO tipoMedioContactoDAO;
    private MedioContactoDAO medioContactoDAO;
    private ClinicaDAO clinicaDAO;
    private DocumentoDAO documentoDAO;
    private DocumentoModel documentoModel;

    public PersonaModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new PersonaModel();
        dao = mock(PersonaDAO.class);
        rolDAO = mock(RolDAO.class);
        personaRolDAO = mock(PersonaRolDAO.class);
        tipoMedioContactoDAO = mock(TipoMedioContactoDAO.class);
        medioContactoDAO = mock(MedioContactoDAO.class);
        clinicaDAO = mock(ClinicaDAO.class);
        documentoDAO = mock(DocumentoDAO.class);
        documentoModel = mock(DocumentoModel.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.rolDAO = rolDAO;
        instance.personaRolDAO = personaRolDAO;
        instance.tipoMedioContactoDAO = tipoMedioContactoDAO;
        instance.medioContactoDAO = medioContactoDAO;
        instance.clinicaDAO = clinicaDAO;
        instance.documentoDAO = documentoDAO;
        instance.documentoModel = documentoModel;
    }

    @AfterEach
    public void tearDown() {
    }

    private Persona personaValida() {
        Persona p = new Persona(UUID.randomUUID());
        p.setNombres("Juan");
        p.setApellidos("Perez");
        return p;
    }

    private Rol rolActivo() {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre("Paciente");
        r.setActivo(Boolean.TRUE);
        return r;
    }

    private Clinica clinicaActiva() {
        Clinica c = new Clinica(UUID.randomUUID(), "Clinica Central");
        c.setActivo(Boolean.TRUE);
        return c;
    }

    private TipoMedioContacto tipoActivo() {
        TipoMedioContacto t = new TipoMedioContacto(UUID.randomUUID());
        t.setNombre("Correo");
        t.setActivo(Boolean.TRUE);
        return t;
    }

    /**
     * Test of validarEliminacion method, of class PersonaModel.
     */
    @Test
    public void testValidarEliminacion() {
        Persona r = new Persona(UUID.randomUUID());
        when(documentoDAO.countByPersona(r.getIdPersona())).thenReturn(0L);
        when(medioContactoDAO.countByPersona(r.getIdPersona())).thenReturn(0L);
        when(personaRolDAO.countByPersona(r.getIdPersona())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(documentoDAO).countByPersona(r.getIdPersona());
        verify(medioContactoDAO).countByPersona(r.getIdPersona());
        verify(personaRolDAO).countByPersona(r.getIdPersona());
    }

    @Test
    public void testValidarEliminacionConHijos() {
        Persona r = new Persona(UUID.randomUUID());
        when(documentoDAO.countByPersona(r.getIdPersona())).thenReturn(1L);
        when(medioContactoDAO.countByPersona(r.getIdPersona())).thenReturn(0L);
        when(personaRolDAO.countByPersona(r.getIdPersona())).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        Persona r = new Persona();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(documentoDAO, never()).countByPersona(any());
        verify(medioContactoDAO, never()).countByPersona(any());
        verify(personaRolDAO, never()).countByPersona(any());
    }

    /**
     * Test of getFacesContext method, of class PersonaModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class PersonaModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Persona> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class PersonaModel.
     */
    @Test
    public void testNuevoRegistro() {
        Persona resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdPersona());
        assertNotNull(resultado.getFechaCreacion());
    }

    /**
     * Test of buscarRegistroPorId method, of class PersonaModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Persona esperado = new Persona(id);
        when(dao.find(id)).thenReturn(esperado);

        Persona resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    @Test
    public void testBuscarRegistroPorIdNulo() {
        assertNull(instance.buscarRegistroPorId(null));
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class PersonaModel.
     */
    @Test
    public void testGetIdAsText() {
        Persona r = new Persona(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdPersona().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    @Test
    public void testGetIdAsTextSinId() {
        assertNull(instance.getIdAsText(new Persona()));
    }

    /**
     * Test of getIdByText method, of class PersonaModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Persona esperado = new Persona(id);
        when(dao.find(id)).thenReturn(esperado);

        Persona resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
        verify(dao, never()).find(any());
    }

    /**
     * Test of selectionHandler method, of class PersonaModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        Persona seleccion = new Persona(UUID.randomUUID());
        List<MedioContacto> medios = new ArrayList<>();
        List<PersonaRol> roles = new ArrayList<>();
        when(medioContactoDAO.findByPersona(seleccion.getIdPersona(), 0, 1000)).thenReturn(medios);
        when(personaRolDAO.findByPersona(seleccion.getIdPersona())).thenReturn(roles);
        SelectEvent<Persona> r = mock(SelectEvent.class);
        when(r.getObject()).thenReturn(seleccion);

        instance.selectionHandler(r);

        assertSame(seleccion, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertSame(medios, seleccion.getMedioContactoList());
        assertSame(roles, seleccion.getPersonaRolList());
        assertFalse(instance.isEditandoRol());
        assertFalse(instance.isEditandoMedio());
    }

    @Test
    public void testSelectionHandlerNulo() {
        assertDoesNotThrow(() -> instance.selectionHandler(null));
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnGuardarHandler method, of class PersonaModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        Persona registro = personaValida();
        instance.setRegistro(registro);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(registro);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(instance.facesContext, atLeastOnce()).addMessage(any(), any());
    }

    @Test
    public void testBtnGuardarHandlerRecortandoEspacios() {
        Persona registro = new Persona(UUID.randomUUID());
        registro.setNombres("  Juan  ");
        registro.setApellidos("  Perez  ");
        instance.setRegistro(registro);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        assertEquals("Juan", registro.getNombres());
        assertEquals("Perez", registro.getApellidos());
        verify(dao).crear(registro);
    }

    @Test
    public void testBtnGuardarHandlerNombresVacios() {
        Persona registro = new Persona(UUID.randomUUID());
        registro.setNombres("   ");
        registro.setApellidos("Perez");
        instance.setRegistro(registro);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        assertSame(registro, instance.getRegistro());
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testBtnGuardarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext, never()).addMessage(any(), any());
    }

    /**
     * Test of btnModificarHandler method, of class PersonaModel.
     */
    @Test
    public void testBtnModificarHandler() {
        Persona registro = personaValida();
        instance.setRegistro(registro);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(registro);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(instance.facesContext, atLeastOnce()).addMessage(any(), any());
    }

    @Test
    public void testBtnModificarHandlerApellidosVacios() {
        Persona registro = new Persona(UUID.randomUUID());
        registro.setNombres("Juan");
        registro.setApellidos(" ");
        instance.setRegistro(registro);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
        assertSame(registro, instance.getRegistro());
    }

    @Test
    public void testBtnModificarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
        verify(instance.facesContext, never()).addMessage(any(), any());
    }

    /**
     * Test of prepararNuevoRol method, of class PersonaModel.
     */
    @Test
    public void testPrepararNuevoRol() {
        instance.prepararNuevoRol();

        assertNotNull(instance.getNuevoRol());
        assertNotNull(instance.getNuevoRol().getIdPersonaRol());
        assertNull(instance.getIdRolSeleccionado());
        assertNull(instance.getIdClinicaSeleccionadaRol());
        assertFalse(instance.isCapturandoRol());
    }

    /**
     * Test of iniciarCapturaRol method, of class PersonaModel.
     */
    @Test
    public void testIniciarCapturaRol() {
        instance.iniciarCapturaRol();

        assertTrue(instance.isCapturandoRol());
        assertNotNull(instance.getNuevoRol());
        assertNotNull(instance.getNuevoRol().getIdPersonaRol());
    }

    /**
     * Test of onRolSelect method, of class PersonaModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnRolSelect() {
        Persona persona = new Persona(UUID.randomUUID());
        Rol rol = new Rol(UUID.randomUUID());
        Clinica clinica = new Clinica(UUID.randomUUID());
        PersonaRol seleccion = new PersonaRol(UUID.randomUUID());
        seleccion.setIdPersona(persona);
        seleccion.setIdRol(rol);
        seleccion.setIdClinica(clinica);
        SelectEvent<PersonaRol> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(seleccion);

        instance.onRolSelect(event);

        assertSame(seleccion, instance.getRolSeleccionado());
        assertEquals(seleccion.getIdPersonaRol(), instance.getNuevoRol().getIdPersonaRol());
        assertEquals(rol.getIdRol().toString(), instance.getIdRolSeleccionado());
        assertEquals(clinica.getIdClinica().toString(), instance.getIdClinicaSeleccionadaRol());
        assertTrue(instance.isEditandoRol());
        assertTrue(instance.isCapturandoRol());
    }

    @Test
    public void testOnRolSelectSinRolNiClinica() {
        PersonaRol seleccion = new PersonaRol(UUID.randomUUID());

        instance.onRolSelect(selectEventReturns(seleccion));

        assertNull(instance.getIdRolSeleccionado());
        assertNull(instance.getIdClinicaSeleccionadaRol());
        assertTrue(instance.isEditandoRol());
    }

    @SuppressWarnings("unchecked")
    private <T> SelectEvent<T> selectEventReturns(T valor) {
        SelectEvent<T> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(valor);
        return event;
    }

    /**
     * Test of cancelarEdicionRol method, of class PersonaModel.
     */
    @Test
    public void testCancelarEdicionRol() {
        instance.setRolSeleccionado(new PersonaRol(UUID.randomUUID()));
        instance.setEditandoRol(true);
        instance.setCapturandoRol(true);

        instance.cancelarEdicionRol();

        assertNull(instance.getRolSeleccionado());
        assertFalse(instance.isEditandoRol());
        assertFalse(instance.isCapturandoRol());
        assertNotNull(instance.getNuevoRol());
    }

    /**
     * Test of modificarRol method, of class PersonaModel.
     */
    @Test
    public void testModificarRol() {
        Persona persona = personaValida();
        Rol rol = rolActivo();
        Clinica clinica = clinicaActiva();
        PersonaRol anterior = new PersonaRol(UUID.randomUUID());
        PersonaRol nuevo = new PersonaRol(UUID.randomUUID());
        persona.setPersonaRolList(new ArrayList<>(List.of(anterior)));
        instance.setRegistro(persona);
        instance.setNuevoRol(nuevo);
        instance.setRolSeleccionado(anterior);
        instance.setIdRolSeleccionado(rol.getIdRol().toString());
        instance.setIdClinicaSeleccionadaRol(clinica.getIdClinica().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);
        when(clinicaDAO.find(clinica.getIdClinica())).thenReturn(clinica);

        instance.modificarRol();

        verify(personaRolDAO).modificar(nuevo);
        assertEquals(rol, nuevo.getIdRol());
        assertEquals(clinica, nuevo.getIdClinica());
        assertSame(persona, nuevo.getIdPersona());
        assertTrue(persona.getPersonaRolList().contains(nuevo));
        assertFalse(persona.getPersonaRolList().contains(anterior));
        assertFalse(instance.isEditandoRol());
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testModificarRolSinSeleccion() {
        instance.setRegistro(personaValida());
        instance.setNuevoRol(new PersonaRol(UUID.randomUUID()));
        instance.setIdRolSeleccionado("  ");

        instance.modificarRol();

        verify(personaRolDAO, never()).modificar(any());
        assertFalse(instance.isEditandoRol());
    }

    @Test
    public void testModificarRolInactivo() {
        Rol rol = rolActivo();
        rol.setActivo(Boolean.FALSE);
        Clinica clinica = clinicaActiva();
        instance.setRegistro(personaValida());
        instance.setNuevoRol(new PersonaRol(UUID.randomUUID()));
        instance.setIdRolSeleccionado(rol.getIdRol().toString());
        instance.setIdClinicaSeleccionadaRol(clinica.getIdClinica().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);
        when(clinicaDAO.find(clinica.getIdClinica())).thenReturn(clinica);

        instance.modificarRol();

        verify(personaRolDAO, never()).modificar(any());
    }

    @Test
    public void testModificarRolRegistroNulo() {
        instance.setRegistro(null);

        instance.modificarRol();

        verify(personaRolDAO, never()).modificar(any());
    }

    /**
     * Test of agregarRol method, of class PersonaModel.
     */
    @Test
    public void testAgregarRol() {
        Persona persona = personaValida();
        Rol rol = rolActivo();
        Clinica clinica = clinicaActiva();
        PersonaRol nuevo = new PersonaRol(UUID.randomUUID());
        instance.setRegistro(persona);
        instance.setNuevoRol(nuevo);
        instance.setIdRolSeleccionado(rol.getIdRol().toString());
        instance.setIdClinicaSeleccionadaRol(clinica.getIdClinica().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);
        when(clinicaDAO.find(clinica.getIdClinica())).thenReturn(clinica);
        when(personaRolDAO.existeAsignacion(persona.getIdPersona(), rol.getIdRol(), clinica.getIdClinica(), null))
                .thenReturn(false);

        instance.agregarRol();

        verify(personaRolDAO).crear(nuevo);
        assertNotNull(nuevo.getFechaCreacion());
        assertEquals(rol, nuevo.getIdRol());
        assertEquals(clinica, nuevo.getIdClinica());
        assertSame(persona, nuevo.getIdPersona());
        assertTrue(persona.getPersonaRolList().contains(nuevo));
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testAgregarRolDuplicado() {
        Persona persona = personaValida();
        Rol rol = rolActivo();
        Clinica clinica = clinicaActiva();
        instance.setRegistro(persona);
        instance.setNuevoRol(new PersonaRol(UUID.randomUUID()));
        instance.setIdRolSeleccionado(rol.getIdRol().toString());
        instance.setIdClinicaSeleccionadaRol(clinica.getIdClinica().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);
        when(clinicaDAO.find(clinica.getIdClinica())).thenReturn(clinica);
        when(personaRolDAO.existeAsignacion(persona.getIdPersona(), rol.getIdRol(), clinica.getIdClinica(), null))
                .thenReturn(true);

        instance.agregarRol();

        verify(personaRolDAO, never()).crear(any());
    }

    @Test
    public void testAgregarRolSinClinica() {
        instance.setRegistro(personaValida());
        instance.setNuevoRol(new PersonaRol(UUID.randomUUID()));
        instance.setIdRolSeleccionado(UUID.randomUUID().toString());

        instance.agregarRol();

        verify(personaRolDAO, never()).crear(any());
    }

    @Test
    public void testAgregarRolRegistroNulo() {
        instance.setRegistro(null);

        instance.agregarRol();

        verify(personaRolDAO, never()).crear(any());
    }

    /**
     * Test of eliminarRol method, of class PersonaModel.
     */
    @Test
    public void testEliminarRol() {
        Persona persona = personaValida();
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        persona.setPersonaRolList(new ArrayList<>(List.of(pr)));
        instance.setRegistro(persona);

        instance.eliminarRol(pr);

        verify(personaRolDAO).eliminar(pr);
        assertFalse(persona.getPersonaRolList().contains(pr));
        assertFalse(instance.isEditandoRol());
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testEliminarRolNulo() {
        assertDoesNotThrow(() -> instance.eliminarRol(null));
        verify(personaRolDAO, never()).eliminar(any());
    }

    @Test
    public void testEliminarRolConError() {
        Persona persona = personaValida();
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        persona.setPersonaRolList(new ArrayList<>(List.of(pr)));
        instance.setRegistro(persona);
        doThrow(new RuntimeException("fallo")).when(personaRolDAO).eliminar(pr);

        assertDoesNotThrow(() -> instance.eliminarRol(pr));
        assertTrue(persona.getPersonaRolList().contains(pr));
        verify(instance.facesContext).addMessage(any(), any());
    }

    /**
     * Test of getRolesDePersona method, of class PersonaModel.
     */
    @Test
    public void testGetRolesDePersona() {
        Persona persona = personaValida();
        List<PersonaRol> roles = new ArrayList<>(List.of(new PersonaRol(UUID.randomUUID())));
        persona.setPersonaRolList(roles);
        instance.setRegistro(persona);

        List<PersonaRol> resultado = instance.getRolesDePersona();

        assertSame(roles, resultado);
    }

    @Test
    public void testGetRolesDePersonaSinRegistro() {
        List<PersonaRol> resultado = instance.getRolesDePersona();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetRolesDePersonaListaNula() {
        Persona persona = personaValida();
        persona.setPersonaRolList(null);
        instance.setRegistro(persona);

        List<PersonaRol> resultado = instance.getRolesDePersona();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of prepararNuevoMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testPrepararNuevoMedioContacto() {
        instance.prepararNuevoMedioContacto();

        assertNotNull(instance.getNuevoMedioContacto());
        assertNotNull(instance.getNuevoMedioContacto().getIdMedioContacto());
        assertNotNull(instance.getNuevoMedioContacto().getFechaCreacion());
        assertNull(instance.getIdTipoMedioContactoSeleccionado());
        assertFalse(instance.isCapturandoMedio());
    }

    /**
     * Test of iniciarCapturaMedio method, of class PersonaModel.
     */
    @Test
    public void testIniciarCapturaMedio() {
        instance.iniciarCapturaMedio();

        assertTrue(instance.isCapturandoMedio());
        assertNotNull(instance.getNuevoMedioContacto());
        assertNotNull(instance.getNuevoMedioContacto().getIdMedioContacto());
    }

    /**
     * Test of onMedioSelect method, of class PersonaModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnMedioSelect() {
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        MedioContacto seleccion = new MedioContacto(UUID.randomUUID());
        seleccion.setValor("correo@dominio.com");
        seleccion.setIdPersona(persona);
        seleccion.setIdTipoMedioContacto(tipo);
        SelectEvent<MedioContacto> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(seleccion);

        instance.onMedioSelect(event);

        assertSame(seleccion, instance.getMedioSeleccionado());
        assertEquals(seleccion.getIdMedioContacto(), instance.getNuevoMedioContacto().getIdMedioContacto());
        assertEquals("correo@dominio.com", instance.getNuevoMedioContacto().getValor());
        assertEquals(tipo.getIdTipoMedioContacto().toString(), instance.getIdTipoMedioContactoSeleccionado());
        assertTrue(instance.isEditandoMedio());
        assertTrue(instance.isCapturandoMedio());
    }

    @Test
    public void testOnMedioSelectSinTipo() {
        MedioContacto seleccion = new MedioContacto(UUID.randomUUID());

        instance.onMedioSelect(selectEventReturns(seleccion));

        assertNull(instance.getIdTipoMedioContactoSeleccionado());
        assertNull(instance.getNuevoMedioContacto().getIdTipoMedioContacto());
        assertTrue(instance.isEditandoMedio());
    }

    /**
     * Test of cancelarEdicionMedio method, of class PersonaModel.
     */
    @Test
    public void testCancelarEdicionMedio() {
        instance.setMedioSeleccionado(new MedioContacto(UUID.randomUUID()));
        instance.setEditandoMedio(true);
        instance.setCapturandoMedio(true);

        instance.cancelarEdicionMedio();

        assertNull(instance.getMedioSeleccionado());
        assertFalse(instance.isEditandoMedio());
        assertFalse(instance.isCapturandoMedio());
        assertNotNull(instance.getNuevoMedioContacto());
    }

    /**
     * Test of modificarMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testModificarMedioContacto() {
        Persona persona = personaValida();
        TipoMedioContacto tipo = tipoActivo();
        MedioContacto anterior = new MedioContacto(UUID.randomUUID());
        MedioContacto nuevo = new MedioContacto(UUID.randomUUID());
        nuevo.setValor("correo@dominio.com");
        persona.setMedioContactoList(new ArrayList<>(List.of(anterior)));
        instance.setRegistro(persona);
        instance.setNuevoMedioContacto(nuevo);
        instance.setMedioSeleccionado(anterior);
        instance.setIdTipoMedioContactoSeleccionado(tipo.getIdTipoMedioContacto().toString());
        when(tipoMedioContactoDAO.find(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        when(medioContactoDAO.existePersonaValor(persona.getIdPersona(), "correo@dominio.com",
                nuevo.getIdMedioContacto())).thenReturn(false);

        instance.modificarMedioContacto();

        verify(medioContactoDAO).modificar(nuevo);
        assertEquals(tipo, nuevo.getIdTipoMedioContacto());
        assertSame(persona, nuevo.getIdPersona());
        assertTrue(persona.getMedioContactoList().contains(nuevo));
        assertFalse(persona.getMedioContactoList().contains(anterior));
        assertFalse(instance.isEditandoMedio());
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testModificarMedioContactoValorVacio() {
        Persona persona = personaValida();
        MedioContacto nuevo = new MedioContacto(UUID.randomUUID());
        nuevo.setValor("   ");
        instance.setRegistro(persona);
        instance.setNuevoMedioContacto(nuevo);
        instance.setIdTipoMedioContactoSeleccionado(UUID.randomUUID().toString());

        instance.modificarMedioContacto();

        verify(medioContactoDAO, never()).modificar(any());
    }

    @Test
    public void testModificarMedioContactoTipoInactivo() {
        Persona persona = personaValida();
        TipoMedioContacto tipo = tipoActivo();
        tipo.setActivo(Boolean.FALSE);
        MedioContacto nuevo = new MedioContacto(UUID.randomUUID());
        nuevo.setValor("correo@dominio.com");
        instance.setRegistro(persona);
        instance.setNuevoMedioContacto(nuevo);
        instance.setIdTipoMedioContactoSeleccionado(tipo.getIdTipoMedioContacto().toString());
        when(tipoMedioContactoDAO.find(tipo.getIdTipoMedioContacto())).thenReturn(tipo);

        instance.modificarMedioContacto();

        verify(medioContactoDAO, never()).modificar(any());
    }

    @Test
    public void testModificarMedioContactoRegistroNulo() {
        instance.setRegistro(null);

        instance.modificarMedioContacto();

        verify(medioContactoDAO, never()).modificar(any());
    }

    /**
     * Test of agregarMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testAgregarMedioContacto() {
        Persona persona = personaValida();
        TipoMedioContacto tipo = tipoActivo();
        MedioContacto nuevo = new MedioContacto(UUID.randomUUID());
        nuevo.setValor("correo@dominio.com");
        instance.setRegistro(persona);
        instance.setNuevoMedioContacto(nuevo);
        instance.setIdTipoMedioContactoSeleccionado(tipo.getIdTipoMedioContacto().toString());
        when(tipoMedioContactoDAO.find(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        when(medioContactoDAO.existePersonaValor(persona.getIdPersona(), "correo@dominio.com", null))
                .thenReturn(false);

        instance.agregarMedioContacto();

        verify(medioContactoDAO).crear(nuevo);
        assertEquals(tipo, nuevo.getIdTipoMedioContacto());
        assertSame(persona, nuevo.getIdPersona());
        assertNotNull(nuevo.getFechaCreacion());
        assertTrue(persona.getMedioContactoList().contains(nuevo));
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testAgregarMedioContactoDuplicado() {
        Persona persona = personaValida();
        TipoMedioContacto tipo = tipoActivo();
        MedioContacto nuevo = new MedioContacto(UUID.randomUUID());
        nuevo.setValor("correo@dominio.com");
        instance.setRegistro(persona);
        instance.setNuevoMedioContacto(nuevo);
        instance.setIdTipoMedioContactoSeleccionado(tipo.getIdTipoMedioContacto().toString());
        when(tipoMedioContactoDAO.find(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        when(medioContactoDAO.existePersonaValor(persona.getIdPersona(), "correo@dominio.com", null))
                .thenReturn(true);

        instance.agregarMedioContacto();

        verify(medioContactoDAO, never()).crear(any());
    }

    @Test
    public void testAgregarMedioContactoSinTipo() {
        instance.setRegistro(personaValida());
        MedioContacto nuevo = new MedioContacto(UUID.randomUUID());
        nuevo.setValor("correo@dominio.com");
        instance.setNuevoMedioContacto(nuevo);

        instance.agregarMedioContacto();

        verify(medioContactoDAO, never()).crear(any());
    }

    @Test
    public void testAgregarMedioContactoRegistroNulo() {
        instance.setRegistro(null);

        instance.agregarMedioContacto();

        verify(medioContactoDAO, never()).crear(any());
    }

    /**
     * Test of eliminarMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testEliminarMedioContacto() {
        Persona persona = personaValida();
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        persona.setMedioContactoList(new ArrayList<>(List.of(mc)));
        instance.setRegistro(persona);

        instance.eliminarMedioContacto(mc);

        verify(medioContactoDAO).eliminar(mc);
        assertFalse(persona.getMedioContactoList().contains(mc));
        assertFalse(instance.isEditandoMedio());
        verify(instance.facesContext).addMessage(any(), any());
    }

    @Test
    public void testEliminarMedioContactoNulo() {
        assertDoesNotThrow(() -> instance.eliminarMedioContacto(null));
        verify(medioContactoDAO, never()).eliminar(any());
    }

    @Test
    public void testEliminarMedioContactoConError() {
        Persona persona = personaValida();
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        persona.setMedioContactoList(new ArrayList<>(List.of(mc)));
        instance.setRegistro(persona);
        doThrow(new RuntimeException("fallo")).when(medioContactoDAO).eliminar(mc);

        assertDoesNotThrow(() -> instance.eliminarMedioContacto(mc));
        assertTrue(persona.getMedioContactoList().contains(mc));
        verify(instance.facesContext).addMessage(any(), any());
    }

    /**
     * Test of getMediosDeContacto method, of class PersonaModel.
     */
    @Test
    public void testGetMediosDeContacto() {
        Persona persona = personaValida();
        List<MedioContacto> medios = new ArrayList<>(List.of(new MedioContacto(UUID.randomUUID())));
        persona.setMedioContactoList(medios);
        instance.setRegistro(persona);

        List<MedioContacto> resultado = instance.getMediosDeContacto();

        assertSame(medios, resultado);
    }

    @Test
    public void testGetMediosDeContactoSinRegistro() {
        List<MedioContacto> resultado = instance.getMediosDeContacto();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetMediosDeContactoListaNula() {
        Persona persona = personaValida();
        persona.setMedioContactoList(null);
        instance.setRegistro(persona);

        List<MedioContacto> resultado = instance.getMediosDeContacto();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of getRolesDisponibles method, of class PersonaModel.
     */
    @Test
    public void testGetRolesDisponibles() {
        List<Rol> roles = new ArrayList<>(List.of(rolActivo()));
        when(rolDAO.findActivos()).thenReturn(roles);

        List<Rol> resultado = instance.getRolesDisponibles();

        assertSame(roles, resultado);
        verify(rolDAO).findActivos();
    }

    @Test
    public void testGetRolesDisponiblesVacio() {
        when(rolDAO.findActivos()).thenReturn(new ArrayList<>());

        List<Rol> resultado = instance.getRolesDisponibles();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of getTiposMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testGetTiposMedioContacto() {
        List<TipoMedioContacto> tipos = new ArrayList<>(List.of(tipoActivo()));
        when(tipoMedioContactoDAO.findActivos()).thenReturn(tipos);

        List<TipoMedioContacto> resultado = instance.getTiposMedioContacto();

        assertSame(tipos, resultado);
        verify(tipoMedioContactoDAO).findActivos();
    }

    @Test
    public void testGetTiposMedioContactoVacio() {
        when(tipoMedioContactoDAO.findActivos()).thenReturn(new ArrayList<>());

        List<TipoMedioContacto> resultado = instance.getTiposMedioContacto();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of getNuevoRol method, of class PersonaModel.
     */
    @Test
    public void testGetNuevoRol() {
        assertNull(instance.getNuevoRol());
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        instance.setNuevoRol(pr);

        assertSame(pr, instance.getNuevoRol());
    }

    /**
     * Test of setNuevoRol method, of class PersonaModel.
     */
    @Test
    public void testSetNuevoRol() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());

        instance.setNuevoRol(pr);

        assertSame(pr, instance.getNuevoRol());
    }

    /**
     * Test of getIdRolSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testGetIdRolSeleccionado() {
        assertNull(instance.getIdRolSeleccionado());
        instance.setIdRolSeleccionado("rol-1");

        assertEquals("rol-1", instance.getIdRolSeleccionado());
    }

    /**
     * Test of setIdRolSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testSetIdRolSeleccionado() {
        instance.setIdRolSeleccionado("rol-2");

        assertEquals("rol-2", instance.getIdRolSeleccionado());
    }

    /**
     * Test of getIdClinicaSeleccionadaRol method, of class PersonaModel.
     */
    @Test
    public void testGetIdClinicaSeleccionadaRol() {
        assertNull(instance.getIdClinicaSeleccionadaRol());
        instance.setIdClinicaSeleccionadaRol("clinica-1");

        assertEquals("clinica-1", instance.getIdClinicaSeleccionadaRol());
    }

    /**
     * Test of setIdClinicaSeleccionadaRol method, of class PersonaModel.
     */
    @Test
    public void testSetIdClinicaSeleccionadaRol() {
        instance.setIdClinicaSeleccionadaRol("clinica-2");

        assertEquals("clinica-2", instance.getIdClinicaSeleccionadaRol());
    }

    /**
     * Test of getClinicas method, of class PersonaModel.
     */
    @Test
    public void testGetClinicas() {
        List<Clinica> clinicas = new ArrayList<>(List.of(clinicaActiva()));
        when(clinicaDAO.findActivas()).thenReturn(clinicas);

        List<Clinica> resultado = instance.getClinicas();

        assertSame(clinicas, resultado);
        verify(clinicaDAO).findActivas();
    }

    @Test
    public void testGetClinicasVacio() {
        when(clinicaDAO.findActivas()).thenReturn(new ArrayList<>());

        List<Clinica> resultado = instance.getClinicas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of getFechaNacimiento method, of class PersonaModel.
     */
    @Test
    public void testGetFechaNacimiento() {
        Persona persona = personaValida();
        LocalDate fecha = LocalDate.of(1990, 5, 15);
        persona.setFechaNacimiento(fecha.atStartOfDay(ZONA_LOCAL).toOffsetDateTime());
        instance.setRegistro(persona);

        assertEquals(fecha, instance.getFechaNacimiento());
    }

    @Test
    public void testGetFechaNacimientoSinRegistro() {
        assertNull(instance.getFechaNacimiento());
    }

    @Test
    public void testGetFechaNacimientoNula() {
        Persona persona = personaValida();
        persona.setFechaNacimiento(null);
        instance.setRegistro(persona);

        assertNull(instance.getFechaNacimiento());
    }

    /**
     * Test of setFechaNacimiento method, of class PersonaModel.
     */
    @Test
    public void testSetFechaNacimiento() {
        Persona persona = personaValida();
        instance.setRegistro(persona);
        LocalDate fecha = LocalDate.of(2000, 1, 20);

        instance.setFechaNacimiento(fecha);

        OffsetDateTime esperado = fecha.atStartOfDay(ZONA_LOCAL).toOffsetDateTime();
        assertEquals(esperado, persona.getFechaNacimiento());
        assertEquals(fecha, instance.getFechaNacimiento());
    }

    @Test
    public void testSetFechaNacimientoNula() {
        Persona persona = personaValida();
        persona.setFechaNacimiento(LocalDate.of(1990, 1, 1).atStartOfDay(ZONA_LOCAL).toOffsetDateTime());
        instance.setRegistro(persona);

        instance.setFechaNacimiento(null);

        assertNull(persona.getFechaNacimiento());
    }

    @Test
    public void testSetFechaNacimientoSinRegistro() {
        assertDoesNotThrow(() -> instance.setFechaNacimiento(LocalDate.of(2000, 1, 1)));
    }

    /**
     * Test of getDocumentoModel method, of class PersonaModel.
     */
    @Test
    public void testGetDocumentoModel() {
        Persona persona = personaValida();
        instance.setEstado(ESTADO_CRUD.MODIFICAR);
        instance.setRegistro(persona);

        DocumentoModel resultado = instance.getDocumentoModel();

        assertSame(documentoModel, resultado);
        verify(documentoModel).establecerPersonaMaestro(persona.getIdPersona());
    }

    @Test
    public void testGetDocumentoModelSinModificar() {
        instance.setEstado(ESTADO_CRUD.CREAR);
        instance.setRegistro(personaValida());

        DocumentoModel resultado = instance.getDocumentoModel();

        assertSame(documentoModel, resultado);
        verify(documentoModel).establecerPersonaMaestro(null);
    }

    /**
     * Test of getRolSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testGetRolSeleccionado() {
        assertNull(instance.getRolSeleccionado());
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        instance.setRolSeleccionado(pr);

        assertSame(pr, instance.getRolSeleccionado());
    }

    /**
     * Test of setRolSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testSetRolSeleccionado() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());

        instance.setRolSeleccionado(pr);

        assertSame(pr, instance.getRolSeleccionado());
    }

    /**
     * Test of isEditandoRol method, of class PersonaModel.
     */
    @Test
    public void testIsEditandoRol() {
        assertFalse(instance.isEditandoRol());
        instance.setEditandoRol(true);

        assertTrue(instance.isEditandoRol());
    }

    /**
     * Test of setEditandoRol method, of class PersonaModel.
     */
    @Test
    public void testSetEditandoRol() {
        instance.setEditandoRol(true);
        assertTrue(instance.isEditandoRol());

        instance.setEditandoRol(false);
        assertFalse(instance.isEditandoRol());
    }

    /**
     * Test of isCapturandoRol method, of class PersonaModel.
     */
    @Test
    public void testIsCapturandoRol() {
        assertFalse(instance.isCapturandoRol());
        instance.setCapturandoRol(true);

        assertTrue(instance.isCapturandoRol());
    }

    /**
     * Test of setCapturandoRol method, of class PersonaModel.
     */
    @Test
    public void testSetCapturandoRol() {
        instance.setCapturandoRol(true);
        assertTrue(instance.isCapturandoRol());

        instance.setCapturandoRol(false);
        assertFalse(instance.isCapturandoRol());
    }

    /**
     * Test of getNuevoMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testGetNuevoMedioContacto() {
        assertNull(instance.getNuevoMedioContacto());
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        instance.setNuevoMedioContacto(mc);

        assertSame(mc, instance.getNuevoMedioContacto());
    }

    /**
     * Test of setNuevoMedioContacto method, of class PersonaModel.
     */
    @Test
    public void testSetNuevoMedioContacto() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());

        instance.setNuevoMedioContacto(mc);

        assertSame(mc, instance.getNuevoMedioContacto());
    }

    /**
     * Test of getIdTipoMedioContactoSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testGetIdTipoMedioContactoSeleccionado() {
        assertNull(instance.getIdTipoMedioContactoSeleccionado());
        instance.setIdTipoMedioContactoSeleccionado("tipo-1");

        assertEquals("tipo-1", instance.getIdTipoMedioContactoSeleccionado());
    }

    /**
     * Test of setIdTipoMedioContactoSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testSetIdTipoMedioContactoSeleccionado() {
        instance.setIdTipoMedioContactoSeleccionado("tipo-2");

        assertEquals("tipo-2", instance.getIdTipoMedioContactoSeleccionado());
    }

    /**
     * Test of getMedioSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testGetMedioSeleccionado() {
        assertNull(instance.getMedioSeleccionado());
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        instance.setMedioSeleccionado(mc);

        assertSame(mc, instance.getMedioSeleccionado());
    }

    /**
     * Test of setMedioSeleccionado method, of class PersonaModel.
     */
    @Test
    public void testSetMedioSeleccionado() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());

        instance.setMedioSeleccionado(mc);

        assertSame(mc, instance.getMedioSeleccionado());
    }

    /**
     * Test of isEditandoMedio method, of class PersonaModel.
     */
    @Test
    public void testIsEditandoMedio() {
        assertFalse(instance.isEditandoMedio());
        instance.setEditandoMedio(true);

        assertTrue(instance.isEditandoMedio());
    }

    /**
     * Test of setEditandoMedio method, of class PersonaModel.
     */
    @Test
    public void testSetEditandoMedio() {
        instance.setEditandoMedio(true);
        assertTrue(instance.isEditandoMedio());

        instance.setEditandoMedio(false);
        assertFalse(instance.isEditandoMedio());
    }

    /**
     * Test of isCapturandoMedio method, of class PersonaModel.
     */
    @Test
    public void testIsCapturandoMedio() {
        assertFalse(instance.isCapturandoMedio());
        instance.setCapturandoMedio(true);

        assertTrue(instance.isCapturandoMedio());
    }

    /**
     * Test of setCapturandoMedio method, of class PersonaModel.
     */
    @Test
    public void testSetCapturandoMedio() {
        instance.setCapturandoMedio(true);
        assertTrue(instance.isCapturandoMedio());

        instance.setCapturandoMedio(false);
        assertFalse(instance.isCapturandoMedio());
    }

}
