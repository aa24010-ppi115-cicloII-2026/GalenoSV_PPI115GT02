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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 *
 * @author david
 */
public class MedioContactoModelTest {

    private MedioContactoModel instance;
    private MedioContactoDAO dao;
    private PersonaDAO personaDAO;
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    public MedioContactoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new MedioContactoModel();
        dao = mock(MedioContactoDAO.class);
        personaDAO = mock(PersonaDAO.class);
        tipoMedioContactoDAO = mock(TipoMedioContactoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.personaDAO = personaDAO;
        instance.tipoMedioContactoDAO = tipoMedioContactoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarUnicidad method, of class MedioContactoModel.
     */
    @Test
    public void testValidarUnicidad() {
        Persona persona = new Persona(UUID.randomUUID());
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setIdPersona(persona);
        r.setValor("88888888");
        when(dao.existePersonaValor(persona.getIdPersona(), "88888888", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao).existePersonaValor(persona.getIdPersona(), "88888888", null);
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        Persona persona = new Persona(UUID.randomUUID());
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setIdPersona(persona);
        r.setValor("88888888");
        when(dao.existePersonaValor(persona.getIdPersona(), "88888888", null)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, false));
    }

    @Test
    public void testValidarUnicidadSinPersona() {
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setValor("88888888");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existePersonaValor(any(), any(), any());
    }

    /**
     * Test of getFacesContext method, of class MedioContactoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class MedioContactoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<MedioContacto> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class MedioContactoModel.
     */
    @Test
    public void testNuevoRegistro() {
        MedioContacto resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdMedioContacto());
        assertNotNull(resultado.getFechaCreacion());
        assertNull(resultado.getIdPersona());
    }

    @Test
    public void testNuevoRegistroConPersonaMaestro() {
        UUID idPersona = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        instance.establecerPersonaMaestro(idPersona);
        when(personaDAO.find(idPersona)).thenReturn(persona);

        MedioContacto resultado = instance.nuevoRegistro();

        assertNotNull(resultado.getIdMedioContacto());
        assertNotNull(resultado.getFechaCreacion());
        assertSame(persona, resultado.getIdPersona());
    }

    /**
     * Test of inicializarRegistros method, of class MedioContactoModel.
     */
    @Test
    public void testInicializarRegistros() {
        instance.inicializarRegistros();

        assertNotNull(instance.getModelo());
    }

    /**
     * Test of buscarRegistroPorId method, of class MedioContactoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        MedioContacto esperado = new MedioContacto(id);
        when(dao.find(id)).thenReturn(esperado);

        MedioContacto resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        MedioContacto resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class MedioContactoModel.
     */
    @Test
    public void testGetIdAsText() {
        MedioContacto r = new MedioContacto(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdMedioContacto().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class MedioContactoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        MedioContacto esperado = new MedioContacto(id);
        when(dao.find(id)).thenReturn(esperado);

        MedioContacto resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
        verify(dao, never()).find(any());
    }

    /**
     * Test of selectionHandler method, of class MedioContactoModel.
     */
    @Test
    public void testSelectionHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setIdPersona(new Persona(idPersona));
        r.setIdTipoMedioContacto(new TipoMedioContacto(idTipo));
        SelectEvent<MedioContacto> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idPersona.toString(), instance.getIdPersonaSeleccionada());
        assertEquals(idTipo.toString(), instance.getIdTipoMedioContactoSeleccionado());
    }

    /**
     * Test of btnNuevoHandler method, of class MedioContactoModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdTipoMedioContactoSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class MedioContactoModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new MedioContacto(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdTipoMedioContactoSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class MedioContactoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        TipoMedioContacto tipo = new TipoMedioContacto(idTipo);
        tipo.setActivo(Boolean.TRUE);
        tipo.setExpresionRegular("\\d{8}");
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setValor("88888888");
        instance.setRegistro(r);
        instance.setIdPersonaSeleccionada(idPersona.toString());
        instance.setIdTipoMedioContactoSeleccionado(idTipo.toString());
        when(personaDAO.find(idPersona)).thenReturn(persona);
        when(tipoMedioContactoDAO.find(idTipo)).thenReturn(tipo);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(persona, r.getIdPersona());
        assertSame(tipo, r.getIdTipoMedioContacto());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    public void testBtnGuardarHandlerSinPersona() {
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setValor("88888888");
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class MedioContactoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        TipoMedioContacto tipo = new TipoMedioContacto(idTipo);
        tipo.setActivo(Boolean.TRUE);
        tipo.setExpresionRegular("\\d{8}");
        MedioContacto r = new MedioContacto(UUID.randomUUID());
        r.setValor("88888888");
        instance.setRegistro(r);
        instance.setIdPersonaSeleccionada(idPersona.toString());
        instance.setIdTipoMedioContactoSeleccionado(idTipo.toString());
        when(personaDAO.find(idPersona)).thenReturn(persona);
        when(tipoMedioContactoDAO.find(idTipo)).thenReturn(tipo);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertSame(persona, r.getIdPersona());
        assertSame(tipo, r.getIdTipoMedioContacto());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of establecerPersonaMaestro method, of class MedioContactoModel.
     */
    @Test
    public void testEstablecerPersonaMaestro() {
        UUID idPersona = UUID.randomUUID();

        instance.establecerPersonaMaestro(idPersona);

        assertEquals(idPersona.toString(), instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdTipoMedioContactoSeleccionado());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNotNull(instance.getModelo());
    }

    @Test
    public void testEstablecerPersonaMaestroNulo() {
        UUID idPersona = UUID.randomUUID();
        instance.establecerPersonaMaestro(idPersona);

        instance.establecerPersonaMaestro(null);

        assertNull(instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdTipoMedioContactoSeleccionado());
    }

    /**
     * Test of getPersonas method, of class MedioContactoModel.
     */
    @Test
    public void testGetPersonas() {
        List<Persona> esperado = Arrays.asList(new Persona(UUID.randomUUID()));
        when(personaDAO.findAll()).thenReturn(esperado);

        List<Persona> resultado = instance.getPersonas();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getTiposMedioContacto method, of class MedioContactoModel.
     */
    @Test
    public void testGetTiposMedioContacto() {
        List<TipoMedioContacto> esperado = Arrays.asList(new TipoMedioContacto(UUID.randomUUID()));
        when(tipoMedioContactoDAO.findActivos()).thenReturn(esperado);

        List<TipoMedioContacto> resultado = instance.getTiposMedioContacto();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdPersonaSeleccionada method, of class MedioContactoModel.
     */
    @Test
    public void testGetIdPersonaSeleccionada() {
        instance.setIdPersonaSeleccionada("persona-1");

        String resultado = instance.getIdPersonaSeleccionada();

        assertEquals("persona-1", resultado);
    }

    /**
     * Test of setIdPersonaSeleccionada method, of class MedioContactoModel.
     */
    @Test
    public void testSetIdPersonaSeleccionada() {
        String idPersonaSeleccionada = "persona-2";

        instance.setIdPersonaSeleccionada(idPersonaSeleccionada);

        assertEquals(idPersonaSeleccionada, instance.getIdPersonaSeleccionada());
    }

    /**
     * Test of getIdTipoMedioContactoSeleccionado method, of class MedioContactoModel.
     */
    @Test
    public void testGetIdTipoMedioContactoSeleccionado() {
        instance.setIdTipoMedioContactoSeleccionado("tipo-1");

        String resultado = instance.getIdTipoMedioContactoSeleccionado();

        assertEquals("tipo-1", resultado);
    }

    /**
     * Test of setIdTipoMedioContactoSeleccionado method, of class MedioContactoModel.
     */
    @Test
    public void testSetIdTipoMedioContactoSeleccionado() {
        String idTipoMedioContactoSeleccionado = "tipo-2";

        instance.setIdTipoMedioContactoSeleccionado(idTipoMedioContactoSeleccionado);

        assertEquals(idTipoMedioContactoSeleccionado, instance.getIdTipoMedioContactoSeleccionado());
    }

}
