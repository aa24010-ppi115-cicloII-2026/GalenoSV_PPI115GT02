/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Collections;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenTipoExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

/**
 *
 * @author david
 */
public class ExamenTipoExamenModelTest {

    private ExamenTipoExamenModel instance;
    private ExamenTipoExamenDAO dao;
    private ExamenDAO examenDAO;
    private TipoExamenDAO tipoExamenDAO;

    public ExamenTipoExamenModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ExamenTipoExamenModel();
        dao = mock(ExamenTipoExamenDAO.class);
        examenDAO = mock(ExamenDAO.class);
        tipoExamenDAO = mock(TipoExamenDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.examenDAO = examenDAO;
        instance.tipoExamenDAO = tipoExamenDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarUnicidad method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testValidarUnicidad() {
        UUID idExamen = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdExamen(new Examen(idExamen));
        r.setIdTipoExamen(new TipoExamen(idTipo));
        when(dao.existeVinculo(idExamen, idTipo, null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        UUID idExamen = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        UUID idVinculo = UUID.randomUUID();
        ExamenTipoExamen r = new ExamenTipoExamen(idVinculo);
        r.setIdExamen(new Examen(idExamen));
        r.setIdTipoExamen(new TipoExamen(idTipo));
        when(dao.existeVinculo(idExamen, idTipo, idVinculo)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    @Test
    public void testValidarUnicidadSinRelaciones() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeVinculo(any(), any(), any());
    }

    /**
     * Test of getFacesContext method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ExamenTipoExamen> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of inicializarRegistros method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testInicializarRegistros() {
        instance.inicializarRegistros();

        assertNotNull(instance.getModelo());
    }

    @Test
    public void testInicializarRegistrosCuentaTodos() {
        when(dao.count()).thenReturn(7L);
        instance.inicializarRegistros();

        assertEquals(7, instance.getModelo().count(Collections.emptyMap()));
    }

    @Test
    public void testInicializarRegistrosCuentaPorExamen() {
        UUID idExamen = UUID.randomUUID();
        when(dao.countByIdExamen(idExamen)).thenReturn(3);
        instance.setIdExamen(idExamen);

        assertEquals(3, instance.getModelo().count(Collections.emptyMap()));
    }

    @Test
    public void testInicializarRegistrosCargaPorExamen() {
        UUID idExamen = UUID.randomUUID();
        List<ExamenTipoExamen> esperados = List.of(new ExamenTipoExamen(UUID.randomUUID()));
        when(dao.findByIdExamen(idExamen, 0, 10)).thenReturn(esperados);
        instance.setIdExamen(idExamen);

        List<ExamenTipoExamen> resultado = instance.getModelo()
                .load(0, 10, Collections.emptyMap(), Collections.emptyMap());

        assertSame(esperados, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testNuevoRegistro() {
        ExamenTipoExamen resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdExamenTipoExamen());
        assertNotNull(resultado.getFechaCreacion());
        assertNull(resultado.getIdExamen());
    }

    @Test
    public void testNuevoRegistroConExamen() {
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        when(examenDAO.find(idExamen)).thenReturn(examen);
        instance.setIdExamen(idExamen);

        ExamenTipoExamen resultado = instance.nuevoRegistro();

        assertSame(examen, resultado.getIdExamen());
    }

    /**
     * Test of buscarRegistroPorId method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ExamenTipoExamen esperado = new ExamenTipoExamen(id);
        when(dao.findAll()).thenReturn(List.of(esperado));

        assertSame(esperado, instance.buscarRegistroPorId(id));
    }

    @Test
    public void testBuscarRegistroPorIdNoEncontrado() {
        when(dao.findAll()).thenReturn(List.of(new ExamenTipoExamen(UUID.randomUUID())));

        assertNull(instance.buscarRegistroPorId(UUID.randomUUID()));
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
    }

    /**
     * Test of getIdAsText method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetIdAsText() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());

        assertEquals(r.getIdExamenTipoExamen().toString(), instance.getIdAsText(r));
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
        assertNull(instance.getIdAsText(new ExamenTipoExamen()));
    }

    /**
     * Test of getIdByText method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ExamenTipoExamen esperado = new ExamenTipoExamen(id);
        when(dao.findAll()).thenReturn(List.of(esperado));

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
     * Test of selectionHandler method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testSelectionHandler() {
        Examen examen = new Examen(UUID.randomUUID());
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setNombre("Sangre");
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdExamen(examen);
        r.setIdTipoExamen(tipo);
        SelectEvent<ExamenTipoExamen> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertSame(tipo, instance.getTipoExamenSeleccionado());
        assertEquals("Sangre", instance.getNombreTipoExamenSeleccionado());
    }

    @Test
    public void testSelectionHandlerNulo() {
        instance.selectionHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getTipoExamenSeleccionado());
    }

    /**
     * Test of btnNuevoHandler method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        when(examenDAO.find(idExamen)).thenReturn(examen);
        instance.setIdExamen(idExamen);

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertSame(examen, instance.getRegistro().getIdExamen());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getTipoExamenSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ExamenTipoExamen(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);
        instance.setTipoExamenSeleccionado(new TipoExamen(UUID.randomUUID()));

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getTipoExamenSeleccionado());
        assertEquals("", instance.getNombreTipoExamenSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setActivo(Boolean.TRUE);
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        when(examenDAO.find(idExamen)).thenReturn(examen);
        when(dao.existeVinculo(idExamen, tipo.getIdTipoExamen(), null)).thenReturn(false);
        instance.setIdExamen(idExamen);
        instance.setTipoExamenSeleccionado(tipo);
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(examen, r.getIdExamen());
        assertSame(tipo, r.getIdTipoExamen());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getTipoExamenSeleccionado());
    }

    @Test
    public void testBtnGuardarHandlerSinExamen() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerSinTipo() {
        UUID idExamen = UUID.randomUUID();
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        instance.setIdExamen(idExamen);
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerTipoInactivo() {
        UUID idExamen = UUID.randomUUID();
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setActivo(Boolean.FALSE);
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        instance.setIdExamen(idExamen);
        instance.setTipoExamenSeleccionado(tipo);
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerDuplicado() {
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setActivo(Boolean.TRUE);
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        when(examenDAO.find(idExamen)).thenReturn(examen);
        when(dao.existeVinculo(idExamen, tipo.getIdTipoExamen(), null)).thenReturn(true);
        instance.setIdExamen(idExamen);
        instance.setTipoExamenSeleccionado(tipo);
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
     * Test of btnModificarHandler method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setActivo(Boolean.TRUE);
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        when(examenDAO.find(idExamen)).thenReturn(examen);
        when(dao.existeVinculo(idExamen, tipo.getIdTipoExamen(), r.getIdExamenTipoExamen())).thenReturn(false);
        instance.setIdExamen(idExamen);
        instance.setTipoExamenSeleccionado(tipo);
        instance.setRegistro(r);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertSame(examen, r.getIdExamen());
        assertSame(tipo, r.getIdTipoExamen());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnModificarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
    }

    /**
     * Test of buscarTipoPorNombre method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testBuscarTipoPorNombre() {
        List<TipoExamen> esperados = List.of(new TipoExamen(UUID.randomUUID()));
        when(tipoExamenDAO.findActivosByNombreLike("abc", 0, 30)).thenReturn(esperados);

        assertSame(esperados, instance.buscarTipoPorNombre("abc"));
    }

    @Test
    public void testBuscarTipoPorNombreError() {
        when(tipoExamenDAO.findActivosByNombreLike("ab", 0, 30))
                .thenThrow(new IllegalArgumentException("filtro muy corto"));

        List<TipoExamen> resultado = instance.buscarTipoPorNombre("ab");

        assertTrue(resultado.isEmpty());
        verify(tipoExamenDAO).findActivosByNombreLike("ab", 0, 30);
    }

    /**
     * Test of getIdExamen method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetIdExamen() {
        assertNull(instance.getIdExamen());

        UUID id = UUID.randomUUID();
        instance.setIdExamen(id);

        assertSame(id, instance.getIdExamen());
    }

    /**
     * Test of setIdExamen method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testSetIdExamen() {
        UUID id = UUID.randomUUID();

        instance.setIdExamen(id);

        assertEquals(id, instance.getIdExamen());
        assertNotNull(instance.getModelo());

        instance.setIdExamen(null);

        assertNull(instance.getIdExamen());
    }

    /**
     * Test of getTipoExamenSeleccionado method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetTipoExamenSeleccionado() {
        assertNull(instance.getTipoExamenSeleccionado());

        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        instance.setTipoExamenSeleccionado(tipo);

        assertSame(tipo, instance.getTipoExamenSeleccionado());
    }

    /**
     * Test of getNombreTipoExamenSeleccionado method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testGetNombreTipoExamenSeleccionado() {
        assertEquals("", instance.getNombreTipoExamenSeleccionado());

        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setNombre("Orina");
        instance.setTipoExamenSeleccionado(tipo);

        assertEquals("Orina", instance.getNombreTipoExamenSeleccionado());
    }

    /**
     * Test of setTipoExamenSeleccionado method, of class ExamenTipoExamenModel.
     */
    @Test
    public void testSetTipoExamenSeleccionado() {
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        tipo.setNombre("Heces");

        instance.setTipoExamenSeleccionado(tipo);

        assertSame(tipo, instance.getTipoExamenSeleccionado());
        assertEquals("Heces", instance.getNombreTipoExamenSeleccionado());

        instance.setTipoExamenSeleccionado(null);

        assertNull(instance.getTipoExamenSeleccionado());
        assertEquals("", instance.getNombreTipoExamenSeleccionado());
    }
}
