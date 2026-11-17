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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author david
 */
public class ProcedimientoPasoModelTest {

    private ProcedimientoPasoModel instance;
    private ProcedimientoPasoDAO dao;
    private ProcedimientoDAO procedimientoDAO;
    private RolDAO rolDAO;

    public ProcedimientoPasoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ProcedimientoPasoModel();
        dao = mock(ProcedimientoPasoDAO.class);
        procedimientoDAO = mock(ProcedimientoDAO.class);
        rolDAO = mock(RolDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.procedimientoDAO = procedimientoDAO;
        instance.rolDAO = rolDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of getDao method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<ProcedimientoPaso> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testNuevoRegistro() {
        ProcedimientoPaso resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProcedimientoPaso());
        assertEquals(Boolean.FALSE, resultado.getIndicaFin());
    }

    /**
     * Test of buscarRegistroPorId method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso esperado = new ProcedimientoPaso(id);
        when(dao.find(id)).thenReturn(esperado);

        ProcedimientoPaso resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
        verify(dao, never()).find(any());
    }

    /**
     * Test of getIdAsText method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetIdAsText() {
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdProcedimientoPaso().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso esperado = new ProcedimientoPaso(id);
        when(dao.find(id)).thenReturn(esperado);

        ProcedimientoPaso resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
        assertNull(instance.getIdByText(null));
    }

    /**
     * Test of selectionHandler method, of class ProcedimientoPasoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        UUID idProcedimiento = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());
        r.setIdProcedimiento(new Procedimiento(idProcedimiento));
        r.setIdRol(new Rol(idRol));
        SelectEvent<ProcedimientoPaso> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(r);

        instance.selectionHandler(evento);

        assertSame(r, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertEquals(idProcedimiento.toString(), instance.getIdProcedimientoSeleccionado());
        assertEquals(idRol.toString(), instance.getIdRolSeleccionado());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandlerNulo() {
        SelectEvent<ProcedimientoPaso> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        assertDoesNotThrow(() -> instance.selectionHandler(evento));
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnNuevoHandler method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.setIdProcedimientoSeleccionado("algo");
        instance.setIdRolSeleccionado("algo");

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdProcedimientoSeleccionado());
        assertNull(instance.getIdRolSeleccionado());
    }

    /**
     * Test of btnCancelarHandler method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new ProcedimientoPaso(UUID.randomUUID()));
        instance.setIdProcedimientoSeleccionado("algo");
        instance.setIdRolSeleccionado("algo");

        instance.btnCancelarHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdProcedimientoSeleccionado());
        assertNull(instance.getIdRolSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idProcedimiento = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());
        r.setNombre("Paso uno");
        Procedimiento procedimiento = new Procedimiento(idProcedimiento);
        Rol rol = new Rol(idRol);
        when(procedimientoDAO.find(idProcedimiento)).thenReturn(procedimiento);
        when(rolDAO.find(idRol)).thenReturn(rol);
        instance.setRegistro(r);
        instance.setIdProcedimientoSeleccionado(idProcedimiento.toString());
        instance.setIdRolSeleccionado(idRol.toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertSame(procedimiento, r.getIdProcedimiento());
        assertSame(rol, r.getIdRol());
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdProcedimientoSeleccionado());
        assertNull(instance.getIdRolSeleccionado());
    }

    @Test
    public void testBtnGuardarHandlerSinNombre() {
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());
        r.setNombre("   ");
        instance.setRegistro(r);
        instance.setIdProcedimientoSeleccionado(UUID.randomUUID().toString());
        instance.setIdRolSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerSinProcedimiento() {
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());
        r.setNombre("Paso uno");
        instance.setRegistro(r);
        instance.setIdRolSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    public void testBtnGuardarHandlerSinRol() {
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());
        r.setNombre("Paso uno");
        instance.setRegistro(r);
        instance.setIdProcedimientoSeleccionado(UUID.randomUUID().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(instance.facesContext).addMessage(isNull(), any(FacesMessage.class));
    }

    /**
     * Test of btnModificarHandler method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idProcedimiento = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        ProcedimientoPaso r = new ProcedimientoPaso(UUID.randomUUID());
        r.setNombre("Paso uno");
        when(procedimientoDAO.find(idProcedimiento)).thenReturn(new Procedimiento(idProcedimiento));
        when(rolDAO.find(idRol)).thenReturn(new Rol(idRol));
        instance.setRegistro(r);
        instance.setIdProcedimientoSeleccionado(idProcedimiento.toString());
        instance.setIdRolSeleccionado(idRol.toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of getProcedimientos method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetProcedimientos() {
        List<Procedimiento> esperado = List.of(new Procedimiento(UUID.randomUUID()));
        when(procedimientoDAO.findAll()).thenReturn(esperado);

        List<Procedimiento> resultado = instance.getProcedimientos();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getRoles method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetRoles() {
        List<Rol> esperado = List.of(new Rol(UUID.randomUUID()));
        when(rolDAO.findAll()).thenReturn(esperado);

        List<Rol> resultado = instance.getRoles();

        assertSame(esperado, resultado);
    }

    /**
     * Test of getIdProcedimientoSeleccionado method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetIdProcedimientoSeleccionado() {
        instance.setIdProcedimientoSeleccionado("procedimiento-1");

        assertEquals("procedimiento-1", instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of setIdProcedimientoSeleccionado method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testSetIdProcedimientoSeleccionado() {
        instance.setIdProcedimientoSeleccionado("procedimiento-2");

        assertEquals("procedimiento-2", instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of getIdRolSeleccionado method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testGetIdRolSeleccionado() {
        instance.setIdRolSeleccionado("rol-1");

        assertEquals("rol-1", instance.getIdRolSeleccionado());
    }

    /**
     * Test of setIdRolSeleccionado method, of class ProcedimientoPasoModel.
     */
    @Test
    public void testSetIdRolSeleccionado() {
        instance.setIdRolSeleccionado("rol-2");

        assertEquals("rol-2", instance.getIdRolSeleccionado());
    }

}
