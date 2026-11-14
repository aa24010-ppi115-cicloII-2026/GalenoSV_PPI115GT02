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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 *
 * @author david
 */
public class DocumentoModelTest {

    private DocumentoModel instance;
    private DocumentoDAO dao;
    private PersonaDAO personaDAO;
    private TipoDocumentoDAO tipoDocumentoDAO;

    public DocumentoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new DocumentoModel();
        dao = mock(DocumentoDAO.class);
        personaDAO = mock(PersonaDAO.class);
        tipoDocumentoDAO = mock(TipoDocumentoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.personaDAO = personaDAO;
        instance.tipoDocumentoDAO = tipoDocumentoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarUnicidad method, of class DocumentoModel.
     */
    @Test
    public void testValidarUnicidad() {
        Documento r = new Documento(UUID.randomUUID());
        r.setValor("12345678");
        when(dao.existeValor("12345678", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao).existeValor("12345678", null);
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        Documento r = new Documento(UUID.randomUUID());
        r.setValor("12345678");
        when(dao.existeValor("12345678", null)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, false));
    }

    @Test
    public void testValidarUnicidadValorVacio() {
        Documento r = new Documento(UUID.randomUUID());
        r.setValor("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeValor(any(), any());
    }

    /**
     * Test of getFacesContext method, of class DocumentoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class DocumentoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Documento> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class DocumentoModel.
     */
    @Test
    public void testNuevoRegistro() {
        Documento resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdDocumento());
        assertNull(resultado.getIdPersona());
    }

    @Test
    public void testNuevoRegistroConPersonaMaestro() {
        UUID idPersona = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        instance.establecerPersonaMaestro(idPersona);
        when(personaDAO.find(idPersona)).thenReturn(persona);

        Documento resultado = instance.nuevoRegistro();

        assertNotNull(resultado.getIdDocumento());
        assertSame(persona, resultado.getIdPersona());
    }

    /**
     * Test of inicializarRegistros method, of class DocumentoModel.
     */
    @Test
    public void testInicializarRegistros() {
        instance.inicializarRegistros();

        assertNotNull(instance.getModelo());
    }

    /**
     * Test of buscarRegistroPorId method, of class DocumentoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Documento esperado = new Documento(id);
        when(dao.find(id)).thenReturn(esperado);

        Documento resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        Documento resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class DocumentoModel.
     */
    @Test
    public void testGetIdAsText() {
        Documento r = new Documento(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdDocumento().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class DocumentoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Documento esperado = new Documento(id);
        when(dao.find(id)).thenReturn(esperado);

        Documento resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
        verify(dao, never()).find(any());
    }

    /**
     * Test of selectionHandler method, of class DocumentoModel.
     */
    @Test
    public void testSelectionHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        Documento r = new Documento(UUID.randomUUID());
        r.setIdPersona(new Persona(idPersona));
        r.setIdTipoDocumento(new TipoDocumento(idTipo));
        SelectEvent<Documento> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idPersona.toString(), instance.getIdPersonaSeleccionada());
        assertEquals(idTipo.toString(), instance.getIdTipoDocumentoSeleccionado());
    }

    /**
     * Test of btnNuevoHandler method, of class DocumentoModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdTipoDocumentoSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class DocumentoModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new Documento(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdTipoDocumentoSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class DocumentoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        TipoDocumento tipo = new TipoDocumento(idTipo);
        tipo.setActivo(Boolean.TRUE);
        tipo.setExpresionRegular("\\d{8}");
        Documento r = new Documento(UUID.randomUUID());
        r.setValor("12345678");
        instance.setRegistro(r);
        instance.setIdPersonaSeleccionada(idPersona.toString());
        instance.setIdTipoDocumentoSeleccionado(idTipo.toString());
        when(personaDAO.find(idPersona)).thenReturn(persona);
        when(tipoDocumentoDAO.find(idTipo)).thenReturn(tipo);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(persona, r.getIdPersona());
        assertSame(tipo, r.getIdTipoDocumento());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    public void testBtnGuardarHandlerSinPersona() {
        Documento r = new Documento(UUID.randomUUID());
        r.setValor("12345678");
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class DocumentoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        Persona persona = new Persona(idPersona);
        TipoDocumento tipo = new TipoDocumento(idTipo);
        tipo.setActivo(Boolean.TRUE);
        tipo.setExpresionRegular("\\d{8}");
        Documento r = new Documento(UUID.randomUUID());
        r.setValor("12345678");
        instance.setRegistro(r);
        instance.setIdPersonaSeleccionada(idPersona.toString());
        instance.setIdTipoDocumentoSeleccionado(idTipo.toString());
        when(personaDAO.find(idPersona)).thenReturn(persona);
        when(tipoDocumentoDAO.find(idTipo)).thenReturn(tipo);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertSame(persona, r.getIdPersona());
        assertSame(tipo, r.getIdTipoDocumento());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of establecerPersonaMaestro method, of class DocumentoModel.
     */
    @Test
    public void testEstablecerPersonaMaestro() {
        UUID idPersona = UUID.randomUUID();

        instance.establecerPersonaMaestro(idPersona);

        assertEquals(idPersona.toString(), instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdTipoDocumentoSeleccionado());
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
        assertNull(instance.getIdTipoDocumentoSeleccionado());
    }

    /**
     * Test of getPersonas method, of class DocumentoModel.
     */
    @Test
    public void testGetPersonas() {
        List<Persona> esperado = Arrays.asList(new Persona(UUID.randomUUID()));
        when(personaDAO.findAll()).thenReturn(esperado);

        List<Persona> resultado = instance.getPersonas();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getTiposDocumento method, of class DocumentoModel.
     */
    @Test
    public void testGetTiposDocumento() {
        List<TipoDocumento> esperado = Arrays.asList(new TipoDocumento(UUID.randomUUID()));
        when(tipoDocumentoDAO.findActivos()).thenReturn(esperado);

        List<TipoDocumento> resultado = instance.getTiposDocumento();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdPersonaSeleccionada method, of class DocumentoModel.
     */
    @Test
    public void testGetIdPersonaSeleccionada() {
        instance.setIdPersonaSeleccionada("persona-1");

        String resultado = instance.getIdPersonaSeleccionada();

        assertEquals("persona-1", resultado);
    }

    /**
     * Test of setIdPersonaSeleccionada method, of class DocumentoModel.
     */
    @Test
    public void testSetIdPersonaSeleccionada() {
        String idPersonaSeleccionada = "persona-2";

        instance.setIdPersonaSeleccionada(idPersonaSeleccionada);

        assertEquals(idPersonaSeleccionada, instance.getIdPersonaSeleccionada());
    }

    /**
     * Test of getIdTipoDocumentoSeleccionado method, of class DocumentoModel.
     */
    @Test
    public void testGetIdTipoDocumentoSeleccionado() {
        instance.setIdTipoDocumentoSeleccionado("tipo-1");

        String resultado = instance.getIdTipoDocumentoSeleccionado();

        assertEquals("tipo-1", resultado);
    }

    /**
     * Test of setIdTipoDocumentoSeleccionado method, of class DocumentoModel.
     */
    @Test
    public void testSetIdTipoDocumentoSeleccionado() {
        String idTipoDocumentoSeleccionado = "tipo-2";

        instance.setIdTipoDocumentoSeleccionado(idTipoDocumentoSeleccionado);

        assertEquals(idTipoDocumentoSeleccionado, instance.getIdTipoDocumentoSeleccionado());
    }

}
