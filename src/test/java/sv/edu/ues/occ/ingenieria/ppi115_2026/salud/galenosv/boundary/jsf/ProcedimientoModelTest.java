/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
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
import org.mockito.ArgumentCaptor;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author david
 */
public class ProcedimientoModelTest {

    private ProcedimientoModel instance;
    private ProcedimientoDAO dao;
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    private ProcedimientoPasoDAO pasoDAO;
    private ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    private ProcedimientoPasoExamenDAO pasoExamenDAO;
    private RolDAO rolDAO;
    private ExamenDAO examenDAO;
    private FacesContext facesContext;

    public ProcedimientoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ProcedimientoModel();
        dao = mock(ProcedimientoDAO.class);
        consultaProcedimientoDAO = mock(ConsultaProcedimientoDAO.class);
        pasoDAO = mock(ProcedimientoPasoDAO.class);
        secuenciaDAO = mock(ProcedimientoPasoSecuenciaDAO.class);
        pasoExamenDAO = mock(ProcedimientoPasoExamenDAO.class);
        rolDAO = mock(RolDAO.class);
        examenDAO = mock(ExamenDAO.class);
        facesContext = mock(FacesContext.class);

        instance.facesContext = facesContext;
        instance.dao = dao;
        instance.consultaProcedimientoDAO = consultaProcedimientoDAO;
        instance.pasoDAO = pasoDAO;
        instance.secuenciaDAO = secuenciaDAO;
        instance.pasoExamenDAO = pasoExamenDAO;
        instance.rolDAO = rolDAO;
        instance.examenDAO = examenDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    private Rol rolActivo() {
        Rol rol = new Rol(UUID.randomUUID());
        rol.setNombre("Enfermero");
        rol.setActivo(true);
        return rol;
    }

    private Examen examenActivo() {
        Examen examen = new Examen(UUID.randomUUID());
        examen.setNombre("Hemograma");
        examen.setActivo(true);
        return examen;
    }

    /**
     * Test of validarEliminacion method, of class ProcedimientoModel.
     */
    @Test
    public void testValidarEliminacion() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        when(pasoDAO.countByProcedimiento(r.getIdProcedimiento())).thenReturn(0L);
        when(consultaProcedimientoDAO.countByProcedimiento(r.getIdProcedimiento())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(pasoDAO).countByProcedimiento(r.getIdProcedimiento());
        verify(consultaProcedimientoDAO).countByProcedimiento(r.getIdProcedimiento());
    }

    @Test
    public void testValidarEliminacionConPasos() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        when(pasoDAO.countByProcedimiento(r.getIdProcedimiento())).thenReturn(2L);
        when(consultaProcedimientoDAO.countByProcedimiento(r.getIdProcedimiento())).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConConsultas() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        when(pasoDAO.countByProcedimiento(r.getIdProcedimiento())).thenReturn(0L);
        when(consultaProcedimientoDAO.countByProcedimiento(r.getIdProcedimiento())).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        Procedimiento r = new Procedimiento();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(pasoDAO, never()).countByProcedimiento(any());
        verify(consultaProcedimientoDAO, never()).countByProcedimiento(any());
    }

    /**
     * Test of getFacesContext method, of class ProcedimientoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class ProcedimientoModel.
     */
    @Test
    public void testValidarUnicidad() {
        Procedimiento r = new Procedimiento();
        r.setNombre("  Consulta general  ");
        when(dao.existeNombre("Consulta general", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Consulta general", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        Procedimiento r = new Procedimiento();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadNombreNulo() {
        Procedimiento r = new Procedimiento();

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicadoEnModificacion() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setNombre("Consulta general");
        when(dao.existeNombre("Consulta general", r.getIdProcedimiento())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class ProcedimientoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Procedimiento> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ProcedimientoModel.
     */
    @Test
    public void testNuevoRegistro() {
        Procedimiento resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProcedimiento());
        assertEquals(Boolean.TRUE, resultado.getActivo());
    }

    /**
     * Test of buscarRegistroPorId method, of class ProcedimientoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Procedimiento esperado = new Procedimiento(id);
        when(dao.find(id)).thenReturn(esperado);

        Procedimiento resultado = instance.buscarRegistroPorId(id);

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
     * Test of getIdAsText method, of class ProcedimientoModel.
     */
    @Test
    public void testGetIdAsText() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdProcedimiento().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    @Test
    public void testGetIdAsTextSinId() {
        assertNull(instance.getIdAsText(new Procedimiento()));
    }

    /**
     * Test of getIdByText method, of class ProcedimientoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Procedimiento esperado = new Procedimiento(id);
        when(dao.find(id)).thenReturn(esperado);

        Procedimiento resultado = instance.getIdByText(id.toString());

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
     * Test of selectionHandler method, of class ProcedimientoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSelectionHandler() {
        Procedimiento esperado = new Procedimiento(UUID.randomUUID());
        SelectEvent<Procedimiento> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(esperado);

        instance.selectionHandler(event);

        assertSame(esperado, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
    }

    @Test
    public void testSelectionHandlerNulo() {
        assertDoesNotThrow(() -> instance.selectionHandler(null));
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getRegistro());
    }

    /**
     * Test of btnGuardarHandler method, of class ProcedimientoModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setNombre("Nuevo procedimiento");
        instance.setRegistro(r);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(r);
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
    }

    @Test
    public void testBtnGuardarHandlerDuplicado() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setNombre("Duplicado");
        instance.setRegistro(r);
        when(dao.existeNombre("Duplicado", null)).thenReturn(true);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnGuardarHandlerSinRegistro() {
        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(facesContext, never()).addMessage(any(), any());
    }

    /**
     * Test of btnModificarHandler method, of class ProcedimientoModel.
     */
    @Test
    public void testBtnModificarHandler() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setNombre("Procedimiento editado");
        instance.setRegistro(r);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(r);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnModificarHandlerDuplicado() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setNombre("Duplicado");
        instance.setRegistro(r);
        when(dao.existeNombre("Duplicado", r.getIdProcedimiento())).thenReturn(true);

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
        assertSame(r, instance.getRegistro());
    }

    @Test
    public void testBtnModificarHandlerSinRegistro() {
        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
    }

    /**
     * Test of prepararNuevoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testPrepararNuevoPaso() {
        instance.prepararNuevoPaso();

        ProcedimientoPaso resultado = instance.getNuevoPaso();
        assertNotNull(resultado);
        assertNotNull(resultado.getIdProcedimientoPaso());
        assertEquals(Boolean.FALSE, resultado.getIndicaFin());
        assertNull(instance.getIdRolSeleccionadoPaso());
        assertNull(instance.getIdPasoAnteriorSeleccionadoSecuencia());
        assertTrue(instance.getIdsExamenesPaso().isEmpty());
        assertTrue(instance.getExamenesSeleccionadosPaso().isEmpty());
        assertNull(instance.getExamenPasoSeleccionadoTemporal());
        assertNull(instance.getRolAutocompleteSeleccionado());
        assertNull(instance.getExamenAutocompleteSeleccionado());
        assertFalse(instance.isDependenciaBloqueada());
    }

    /**
     * Test of prepararCapturaPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testPrepararCapturaPaso() {
        instance.prepararCapturaPaso();

        assertTrue(instance.isCapturandoPaso());
        assertFalse(instance.isEditandoPaso());
    }

    @Test
    public void testPrepararCapturaPasoConPasosExistentes() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setProcedimientoPasoList(new ArrayList<>(List.of(new ProcedimientoPaso(UUID.randomUUID()))));
        instance.setRegistro(r);

        instance.prepararCapturaPaso();

        assertFalse(instance.isCapturandoPaso());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    /**
     * Test of isPuedeCrearPasoInicial method, of class ProcedimientoModel.
     */
    @Test
    public void testIsPuedeCrearPasoInicial() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setProcedimientoPasoList(new ArrayList<>());
        instance.setRegistro(r);

        assertTrue(instance.isPuedeCrearPasoInicial());
    }

    @Test
    public void testIsPuedeCrearPasoInicialSinRegistro() {
        assertFalse(instance.isPuedeCrearPasoInicial());
    }

    @Test
    public void testIsPuedeCrearPasoInicialConPasos() {
        Procedimiento r = new Procedimiento(UUID.randomUUID());
        r.setProcedimientoPasoList(new ArrayList<>(List.of(new ProcedimientoPaso(UUID.randomUUID()))));
        instance.setRegistro(r);

        assertFalse(instance.isPuedeCrearPasoInicial());
    }

    /**
     * Test of prepararPasoDependiente method, of class ProcedimientoModel.
     */
    @Test
    public void testPrepararPasoDependiente() {
        ProcedimientoPaso padre = new ProcedimientoPaso(UUID.randomUUID());
        padre.setIndicaFin(false);

        instance.prepararPasoDependiente(padre);

        assertEquals(padre.getIdProcedimientoPaso().toString(),
                instance.getIdPasoAnteriorSeleccionadoSecuencia());
        assertTrue(instance.isDependenciaBloqueada());
        assertTrue(instance.isCapturandoPaso());
    }

    @Test
    public void testPrepararPasoDependienteNulo() {
        instance.prepararPasoDependiente(null);

        assertFalse(instance.isCapturandoPaso());
        assertNull(instance.getIdPasoAnteriorSeleccionadoSecuencia());
    }

    @Test
    public void testPrepararPasoDependienteSinId() {
        instance.prepararPasoDependiente(new ProcedimientoPaso());

        assertFalse(instance.isCapturandoPaso());
        assertNull(instance.getIdPasoAnteriorSeleccionadoSecuencia());
    }

    @Test
    public void testPrepararPasoDependienteDePasoFin() {
        ProcedimientoPaso padre = new ProcedimientoPaso(UUID.randomUUID());
        padre.setIndicaFin(true);

        instance.prepararPasoDependiente(padre);

        assertFalse(instance.isCapturandoPaso());
        assertFalse(instance.isDependenciaBloqueada());
        assertNull(instance.getIdPasoAnteriorSeleccionadoSecuencia());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    /**
     * Test of isPasoSeleccionadoEsFin method, of class ProcedimientoModel.
     */
    @Test
    public void testIsPasoSeleccionadoEsFin() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setIndicaFin(true);
        @SuppressWarnings("unchecked")
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        when(nodo.getData()).thenReturn(paso);
        instance.setPasoNodoSeleccionado(nodo);

        assertTrue(instance.isPasoSeleccionadoEsFin());
    }

    @Test
    public void testIsPasoSeleccionadoEsFinSinNodo() {
        assertFalse(instance.isPasoSeleccionadoEsFin());
    }

    @Test
    public void testIsPasoSeleccionadoEsFinNodoSinDatos() {
        @SuppressWarnings("unchecked")
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        when(nodo.getData()).thenReturn(null);
        instance.setPasoNodoSeleccionado(nodo);

        assertFalse(instance.isPasoSeleccionadoEsFin());
    }

    @Test
    public void testIsPasoSeleccionadoNoEsFin() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setIndicaFin(false);
        @SuppressWarnings("unchecked")
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        when(nodo.getData()).thenReturn(paso);
        instance.setPasoNodoSeleccionado(nodo);

        assertFalse(instance.isPasoSeleccionadoEsFin());
    }

    /**
     * Test of prepararPasoDependienteSeleccionado method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testPrepararPasoDependienteSeleccionado() {
        ProcedimientoPaso padre = new ProcedimientoPaso(UUID.randomUUID());
        padre.setIndicaFin(false);
        @SuppressWarnings("unchecked")
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        when(nodo.getData()).thenReturn(padre);
        instance.setPasoNodoSeleccionado(nodo);

        instance.prepararPasoDependienteSeleccionado();

        assertEquals(padre.getIdProcedimientoPaso().toString(),
                instance.getIdPasoAnteriorSeleccionadoSecuencia());
        assertTrue(instance.isCapturandoPaso());
        assertTrue(instance.isDependenciaBloqueada());
    }

    @Test
    public void testPrepararPasoDependienteSeleccionadoSinNodo() {
        instance.prepararPasoDependienteSeleccionado();

        assertFalse(instance.isCapturandoPaso());
        assertNull(instance.getIdPasoAnteriorSeleccionadoSecuencia());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    /**
     * Test of prepararEdicionPasoSeleccionado method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testPrepararEdicionPasoSeleccionado() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setNombre("Paso a editar");
        @SuppressWarnings("unchecked")
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        when(nodo.getData()).thenReturn(paso);
        instance.setPasoNodoSeleccionado(nodo);

        instance.prepararEdicionPasoSeleccionado();

        assertSame(paso, instance.getPasoSeleccionado());
        assertTrue(instance.isEditandoPaso());
        assertTrue(instance.isCapturandoPaso());
    }

    @Test
    public void testPrepararEdicionPasoSeleccionadoSinNodo() {
        instance.prepararEdicionPasoSeleccionado();

        assertNull(instance.getPasoSeleccionado());
        assertFalse(instance.isEditandoPaso());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    /**
     * Test of eliminarPasoSeleccionado method, of class ProcedimientoModel.
     */
    @Test
    public void testEliminarPasoSeleccionado() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        Procedimiento r = new Procedimiento();
        r.setProcedimientoPasoList(new ArrayList<>(List.of(paso)));
        instance.setRegistro(r);
        @SuppressWarnings("unchecked")
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        when(nodo.getData()).thenReturn(paso);
        instance.setPasoNodoSeleccionado(nodo);
        when(secuenciaDAO.findDependientes(paso.getIdProcedimientoPaso())).thenReturn(new ArrayList<>());
        when(pasoExamenDAO.findByPaso(paso.getIdProcedimientoPaso())).thenReturn(new ArrayList<>());
        when(secuenciaDAO.findByPaso(paso.getIdProcedimientoPaso())).thenReturn(new ArrayList<>());

        instance.eliminarPasoSeleccionado();

        verify(pasoDAO).eliminar(paso);
    }

    @Test
    public void testEliminarPasoSeleccionadoSinNodo() {
        instance.eliminarPasoSeleccionado();

        verify(pasoDAO, never()).eliminar(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    /**
     * Test of onPasoNodoSelect method, of class ProcedimientoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnPasoNodoSelect() {
        TreeNode<ProcedimientoPaso> nodo = mock(TreeNode.class);
        NodeSelectEvent event = mock(NodeSelectEvent.class);
        when(event.getTreeNode()).thenReturn(nodo);

        instance.onPasoNodoSelect(event);

        assertSame(nodo, instance.getPasoNodoSeleccionado());
    }

    /**
     * Test of cancelarCapturaPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testCancelarCapturaPaso() {
        instance.setCapturandoPaso(true);
        instance.setEditandoPaso(true);
        instance.setPasoSeleccionado(new ProcedimientoPaso(UUID.randomUUID()));

        instance.cancelarCapturaPaso();

        assertFalse(instance.isCapturandoPaso());
        assertFalse(instance.isEditandoPaso());
        assertNull(instance.getPasoSeleccionado());
        assertNotNull(instance.getNuevoPaso());
    }

    /**
     * Test of seleccionarRolPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testSeleccionarRolPaso() {
        Rol rol = new Rol(UUID.randomUUID());

        instance.seleccionarRolPaso(rol);

        assertEquals(rol.getIdRol().toString(), instance.getIdRolSeleccionadoPaso());
    }

    @Test
    public void testSeleccionarRolPasoNulo() {
        instance.setIdRolSeleccionadoPaso("anterior");

        instance.seleccionarRolPaso(null);

        assertNull(instance.getIdRolSeleccionadoPaso());
    }

    @Test
    public void testSeleccionarRolPasoSinId() {
        instance.seleccionarRolPaso(new Rol());

        assertNull(instance.getIdRolSeleccionadoPaso());
    }

    /**
     * Test of buscarRolPorNombre method, of class ProcedimientoModel.
     */
    @Test
    public void testBuscarRolPorNombre() {
        Rol rol = rolActivo();
        when(rolDAO.findActivosByNombreLike("Enf", 0, 30)).thenReturn(List.of(rol));

        List<Rol> resultado = instance.buscarRolPorNombre("Enf");

        assertEquals(1, resultado.size());
        assertSame(rol, resultado.get(0));
    }

    @Test
    public void testBuscarRolPorNombreError() {
        when(rolDAO.findActivosByNombreLike(anyString(), anyInt(), anyInt()))
                .thenThrow(new IllegalStateException("fallo"));

        List<Rol> resultado = instance.buscarRolPorNombre("Enf");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of confirmarRolAutocompletado method, of class ProcedimientoModel.
     */
    @Test
    public void testConfirmarRolAutocompletado() {
        Rol rol = rolActivo();
        instance.setRolAutocompleteSeleccionado(rol);

        instance.confirmarRolAutocompletado();

        assertEquals(rol.getIdRol().toString(), instance.getIdRolSeleccionadoPaso());
        assertNull(instance.getRolAutocompleteSeleccionado());
    }

    @Test
    public void testConfirmarRolAutocompletadoSinSeleccion() {
        instance.confirmarRolAutocompletado();

        assertNull(instance.getIdRolSeleccionadoPaso());
        assertNull(instance.getRolAutocompleteSeleccionado());
    }

    @Test
    public void testConfirmarRolAutocompletadoSinId() {
        Rol rol = new Rol();
        instance.setRolAutocompleteSeleccionado(rol);

        instance.confirmarRolAutocompletado();

        assertNull(instance.getIdRolSeleccionadoPaso());
        assertSame(rol, instance.getRolAutocompleteSeleccionado());
    }

    /**
     * Test of buscarExamenPorNombre method, of class ProcedimientoModel.
     */
    @Test
    public void testBuscarExamenPorNombre() {
        Examen examen = examenActivo();
        when(examenDAO.findActivosByNombreLike("Hemo", 0, 30)).thenReturn(List.of(examen));

        List<Examen> resultado = instance.buscarExamenPorNombre("Hemo");

        assertEquals(1, resultado.size());
        assertSame(examen, resultado.get(0));
    }

    @Test
    public void testBuscarExamenPorNombreError() {
        when(examenDAO.findActivosByNombreLike(anyString(), anyInt(), anyInt()))
                .thenThrow(new IllegalStateException("fallo"));

        List<Examen> resultado = instance.buscarExamenPorNombre("Hemo");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of agregarExamenAutocompletado method, of class ProcedimientoModel.
     */
    @Test
    public void testAgregarExamenAutocompletado() {
        Examen examen = examenActivo();
        instance.setExamenAutocompleteSeleccionado(examen);

        instance.agregarExamenAutocompletado();

        assertEquals(1, instance.getExamenesSeleccionadosPaso().size());
        assertSame(examen, instance.getExamenesSeleccionadosPaso().get(0));
        assertNull(instance.getExamenAutocompleteSeleccionado());
    }

    @Test
    public void testAgregarExamenAutocompletadoSinSeleccion() {
        instance.agregarExamenAutocompletado();

        assertTrue(instance.getExamenesSeleccionadosPaso().isEmpty());
    }

    /**
     * Test of agregarExamenPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testAgregarExamenPaso() {
        Examen examen = examenActivo();

        instance.agregarExamenPaso(examen);
        instance.agregarExamenPaso(examen);

        assertEquals(1, instance.getExamenesSeleccionadosPaso().size());
        assertEquals(1, instance.getIdsExamenesPaso().size());
        assertEquals(examen.getIdExamen().toString(), instance.getIdsExamenesPaso().get(0));
    }

    @Test
    public void testAgregarExamenPasoNulo() {
        instance.agregarExamenPaso(null);

        assertTrue(instance.getExamenesSeleccionadosPaso().isEmpty());
    }

    @Test
    public void testAgregarExamenPasoSinId() {
        instance.agregarExamenPaso(new Examen());

        assertTrue(instance.getExamenesSeleccionadosPaso().isEmpty());
    }

    @Test
    public void testAgregarExamenPasoInactivo() {
        Examen examen = new Examen(UUID.randomUUID());
        examen.setActivo(false);

        instance.agregarExamenPaso(examen);

        assertTrue(instance.getExamenesSeleccionadosPaso().isEmpty());
        assertTrue(instance.getIdsExamenesPaso().isEmpty());
    }

    /**
     * Test of eliminarExamenSeleccionadoPaso method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testEliminarExamenSeleccionadoPaso() {
        Examen examen = examenActivo();
        instance.agregarExamenPaso(examen);
        instance.setExamenPasoSeleccionadoTemporal(examen);

        instance.eliminarExamenSeleccionadoPaso();

        assertTrue(instance.getExamenesSeleccionadosPaso().isEmpty());
        assertTrue(instance.getIdsExamenesPaso().isEmpty());
        assertNull(instance.getExamenPasoSeleccionadoTemporal());
    }

    @Test
    public void testEliminarExamenSeleccionadoPasoSinSeleccion() {
        Examen examen = examenActivo();
        instance.agregarExamenPaso(examen);

        instance.eliminarExamenSeleccionadoPaso();

        assertEquals(1, instance.getExamenesSeleccionadosPaso().size());
        assertNull(instance.getExamenPasoSeleccionadoTemporal());
    }

    /**
     * Test of getExamenesSeleccionadosPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testGetExamenesSeleccionadosPaso() {
        List<Examen> resultado = instance.getExamenesSeleccionadosPaso();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetExamenesSeleccionadosPasoConExamenes() {
        Examen examen = examenActivo();
        instance.agregarExamenPaso(examen);

        List<Examen> resultado = instance.getExamenesSeleccionadosPaso();

        assertEquals(1, resultado.size());
        assertSame(examen, resultado.get(0));
    }

    /**
     * Test of prepararEdicionPasoCompleto method, of class ProcedimientoModel.
     */
    @Test
    public void testPrepararEdicionPasoCompleto() {
        UUID idPaso = UUID.randomUUID();
        UUID idPadre = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        paso.setNombre("Paso original");
        paso.setIndicaFin(false);

        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        secuencia.setIdProcedimientoPasoReferencia(idPadre);
        paso.setProcedimientoPasoSecuenciaList(new ArrayList<>(List.of(secuencia)));

        Examen examen = examenActivo();
        ProcedimientoPasoExamen relacion = new ProcedimientoPasoExamen(UUID.randomUUID());
        relacion.setIdExamen(examen);
        relacion.setActivo(true);
        paso.setProcedimientoPasoExamenList(new ArrayList<>(List.of(relacion)));

        instance.prepararEdicionPasoCompleto(paso);

        assertSame(paso, instance.getPasoSeleccionado());
        assertEquals("Paso original", instance.getNuevoPaso().getNombre());
        assertEquals(idPaso, instance.getNuevoPaso().getIdProcedimientoPaso());
        assertEquals(idPadre.toString(), instance.getIdPasoAnteriorSeleccionadoSecuencia());
        assertEquals(1, instance.getNuevoPaso().getProcedimientoPasoSecuenciaList().size());
        assertEquals(1, instance.getIdsExamenesPaso().size());
        assertEquals(examen.getIdExamen().toString(), instance.getIdsExamenesPaso().get(0));
        assertTrue(instance.isEditandoPaso());
        assertTrue(instance.isCapturandoPaso());
        assertTrue(instance.isDependenciaBloqueada());
    }

    @Test
    public void testPrepararEdicionPasoCompletoNulo() {
        instance.prepararEdicionPasoCompleto(null);

        assertNull(instance.getPasoSeleccionado());
        assertFalse(instance.isEditandoPaso());
    }

    @Test
    public void testPrepararEdicionPasoCompletoConsultaDAOs() {
        UUID idPaso = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        paso.setNombre("Paso sin relaciones");
        when(secuenciaDAO.findByPaso(idPaso)).thenReturn(new ArrayList<>());
        when(pasoExamenDAO.findByPaso(idPaso)).thenReturn(new ArrayList<>());

        instance.prepararEdicionPasoCompleto(paso);

        verify(secuenciaDAO).findByPaso(idPaso);
        verify(pasoExamenDAO).findByPaso(idPaso);
        assertTrue(instance.getIdsExamenesPaso().isEmpty());
        assertNull(instance.getIdPasoAnteriorSeleccionadoSecuencia());
    }

    /**
     * Test of getNombreRolPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testGetNombreRolPaso() {
        Rol rol = rolActivo();
        instance.setIdRolSeleccionadoPaso(rol.getIdRol().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);

        assertEquals("Enfermero", instance.getNombreRolPaso());
    }

    @Test
    public void testGetNombreRolPasoVacio() {
        assertEquals("", instance.getNombreRolPaso());
        instance.setIdRolSeleccionadoPaso("   ");
        assertEquals("", instance.getNombreRolPaso());
        verify(rolDAO, never()).find(any());
    }

    @Test
    public void testGetNombreRolPasoNoEncontrado() {
        UUID id = UUID.randomUUID();
        instance.setIdRolSeleccionadoPaso(id.toString());
        when(rolDAO.find(id)).thenReturn(null);

        assertEquals("", instance.getNombreRolPaso());
    }

    /**
     * Test of guardarPasoCompleto method, of class ProcedimientoModel.
     */
    @Test
    public void testGuardarPasoCompleto() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>());
        instance.setRegistro(registro);

        Rol rol = rolActivo();
        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("  Paso inicial  ");
        instance.setIdRolSeleccionadoPaso(rol.getIdRol().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);

        instance.guardarPasoCompleto();

        ArgumentCaptor<ProcedimientoPaso> captor = ArgumentCaptor.forClass(ProcedimientoPaso.class);
        verify(pasoDAO).crear(captor.capture());
        assertEquals("Paso inicial", captor.getValue().getNombre());
        assertSame(rol, captor.getValue().getIdRol());
        assertSame(registro, captor.getValue().getIdProcedimiento());
        assertFalse(instance.isCapturandoPaso());
        assertFalse(instance.isEditandoPaso());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoSinRegistro() {
        instance.prepararNuevoPaso();

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).crear(any());
    }

    @Test
    public void testGuardarPasoCompletoSinNombre() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>());
        instance.setRegistro(registro);
        instance.prepararNuevoPaso();

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoDependenciaRequerida() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        ProcedimientoPaso existente = new ProcedimientoPaso(UUID.randomUUID());
        existente.setNombre("Inicio");
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(existente)));
        instance.setRegistro(registro);

        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("Paso 2");

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoConDependencia() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        ProcedimientoPaso padre = new ProcedimientoPaso(UUID.randomUUID());
        padre.setNombre("Inicio");
        padre.setIndicaFin(false);
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(padre)));
        instance.setRegistro(registro);

        Rol rol = rolActivo();
        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("Paso 2");
        instance.setIdPasoAnteriorSeleccionadoSecuencia(padre.getIdProcedimientoPaso().toString());
        instance.setIdRolSeleccionadoPaso(rol.getIdRol().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);

        instance.guardarPasoCompleto();

        verify(pasoDAO).crear(any(ProcedimientoPaso.class));
        verify(secuenciaDAO).crear(any(ProcedimientoPasoSecuencia.class));
    }

    @Test
    public void testGuardarPasoCompletoNombreDuplicado() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        ProcedimientoPaso existente = new ProcedimientoPaso(UUID.randomUUID());
        existente.setNombre("Inicio");
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(existente)));
        instance.setRegistro(registro);

        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("inicio");
        instance.setIdPasoAnteriorSeleccionadoSecuencia(existente.getIdProcedimientoPaso().toString());

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoModificacion() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setNombre("Paso anterior");
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(paso)));
        instance.setRegistro(registro);
        instance.prepararEdicionPasoCompleto(paso);

        Rol rol = rolActivo();
        instance.getNuevoPaso().setNombre("Paso actualizado");
        instance.setIdRolSeleccionadoPaso(rol.getIdRol().toString());
        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);

        instance.guardarPasoCompleto();

        verify(pasoDAO).modificar(any(ProcedimientoPaso.class));
        verify(pasoDAO, never()).crear(any());
        assertFalse(instance.isEditandoPaso());
        assertFalse(instance.isCapturandoPaso());
    }

    @Test
    public void testGuardarPasoCompletoSinRolActivo() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>());
        instance.setRegistro(registro);
        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("Paso sin rol");

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoDependenciaInexistente() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());

        ProcedimientoPaso existente = new ProcedimientoPaso(UUID.randomUUID());
        existente.setNombre("Inicio");

        registro.setProcedimientoPasoList(new ArrayList<>(List.of(existente)));
        instance.setRegistro(registro);

        Rol rol = rolActivo();

        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("Paso 2");
        instance.setIdPasoAnteriorSeleccionadoSecuencia(UUID.randomUUID().toString());
        instance.setIdRolSeleccionadoPaso(rol.getIdRol().toString());

        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).crear(any());
        verify(secuenciaDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoModificacionSinDependencia() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());

        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setNombre("Paso anterior");

        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
        secuencia.setIdProcedimientoPasoReferencia(UUID.randomUUID());
        secuencia.setTipoSecuencia("DESPUES_DE");

        paso.setProcedimientoPasoSecuenciaList(
                new ArrayList<>(List.of(secuencia))
        );

        registro.setProcedimientoPasoList(new ArrayList<>(List.of(paso)));
        instance.setRegistro(registro);

        instance.prepararEdicionPasoCompleto(paso);
        instance.getNuevoPaso().setNombre("Paso actualizado");

        instance.setIdPasoAnteriorSeleccionadoSecuencia(null);

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).modificar(any());
        verify(pasoDAO, never()).crear(any());

        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoModificacionNombreDuplicado() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());

        ProcedimientoPaso paso1 = new ProcedimientoPaso(UUID.randomUUID());
        paso1.setNombre("Inicio");

        ProcedimientoPaso paso2 = new ProcedimientoPaso(UUID.randomUUID());
        paso2.setNombre("Final");

        registro.setProcedimientoPasoList(
                new ArrayList<>(List.of(paso1, paso2))
        );

        instance.setRegistro(registro);
        instance.prepararEdicionPasoCompleto(paso2);

        instance.getNuevoPaso().setNombre("INICIO");

        instance.guardarPasoCompleto();

        verify(pasoDAO, never()).modificar(any());
        verify(pasoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testGuardarPasoCompletoErrorDAO() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>());

        instance.setRegistro(registro);

        Rol rol = rolActivo();

        instance.prepararNuevoPaso();
        instance.getNuevoPaso().setNombre("Paso inicial");
        instance.setIdRolSeleccionadoPaso(rol.getIdRol().toString());

        when(rolDAO.find(rol.getIdRol())).thenReturn(rol);

        doThrow(new RuntimeException("Error de persistencia"))
                .when(pasoDAO).crear(any(ProcedimientoPaso.class));

        instance.guardarPasoCompleto();

        verify(pasoDAO).crear(any(ProcedimientoPaso.class));
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    /**
     * Test of eliminarPasoCompleto method, of class ProcedimientoModel.
     */
    @Test
    public void testEliminarPasoCompleto() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        Procedimiento registro = new Procedimiento();
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(paso)));
        instance.setRegistro(registro);
        when(secuenciaDAO.findDependientes(paso.getIdProcedimientoPaso())).thenReturn(new ArrayList<>());
        when(pasoExamenDAO.findByPaso(paso.getIdProcedimientoPaso())).thenReturn(new ArrayList<>());
        when(secuenciaDAO.findByPaso(paso.getIdProcedimientoPaso())).thenReturn(new ArrayList<>());

        instance.eliminarPasoCompleto(paso);

        verify(pasoDAO).eliminar(paso);
        assertFalse(registro.getProcedimientoPasoList().contains(paso));
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testEliminarPasoCompletoConDependientes() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        Procedimiento registro = new Procedimiento();
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(paso)));
        instance.setRegistro(registro);
        when(secuenciaDAO.findDependientes(paso.getIdProcedimientoPaso()))
                .thenReturn(List.of(new ProcedimientoPasoSecuencia(UUID.randomUUID())));

        instance.eliminarPasoCompleto(paso);

        verify(pasoDAO, never()).eliminar(any());
        assertTrue(registro.getProcedimientoPasoList().contains(paso));
        verify(facesContext).addMessage(any(), any(FacesMessage.class));
    }

    @Test
    public void testEliminarPasoCompletoNulo() {
        instance.eliminarPasoCompleto(null);

        verify(pasoDAO, never()).eliminar(any());
    }

    /**
     * Test of cancelarEdicionPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testCancelarEdicionPaso() {
        instance.setPasoSeleccionado(new ProcedimientoPaso(UUID.randomUUID()));
        instance.setEditandoPaso(true);

        instance.cancelarEdicionPaso();

        assertNull(instance.getPasoSeleccionado());
        assertFalse(instance.isEditandoPaso());
        assertNotNull(instance.getNuevoPaso());
        assertNotNull(instance.getNuevoPaso().getIdProcedimientoPaso());
    }

    /**
     * Test of getPasosDelProcedimiento method, of class ProcedimientoModel.
     */
    @Test
    public void testGetPasosDelProcedimiento() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(paso)));
        instance.setRegistro(registro);

        List<ProcedimientoPaso> resultado = instance.getPasosDelProcedimiento();

        assertEquals(1, resultado.size());
        assertSame(paso, resultado.get(0));
    }

    @Test
    public void testGetPasosDelProcedimientoSinRegistro() {
        List<ProcedimientoPaso> resultado = instance.getPasosDelProcedimiento();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetPasosDelProcedimientoListaNula() {
        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(null);
        instance.setRegistro(registro);

        List<ProcedimientoPaso> resultado = instance.getPasosDelProcedimiento();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of getPadresDisponibles method, of class ProcedimientoModel.
     */
    @Test
    public void testGetPadresDisponibles() {
        UUID selfId = UUID.randomUUID();
        ProcedimientoPaso disponible = new ProcedimientoPaso(UUID.randomUUID());
        disponible.setIndicaFin(false);
        ProcedimientoPaso fin = new ProcedimientoPaso(UUID.randomUUID());
        fin.setIndicaFin(true);
        ProcedimientoPaso propio = new ProcedimientoPaso(selfId);
        propio.setIndicaFin(false);

        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(disponible, fin, propio)));
        instance.setRegistro(registro);
        instance.setNuevoPaso(new ProcedimientoPaso(selfId));

        List<ProcedimientoPaso> resultado = instance.getPadresDisponibles();

        assertEquals(1, resultado.size());
        assertSame(disponible, resultado.get(0));
    }

    @Test
    public void testGetPadresDisponiblesSinRegistro() {
        assertTrue(instance.getPadresDisponibles().isEmpty());
    }

    /**
     * Test of getArbolPasos method, of class ProcedimientoModel.
     */
    @Test
    public void testGetArbolPasos() {
        UUID idPadre = UUID.randomUUID();
        ProcedimientoPaso padre = new ProcedimientoPaso(idPadre);
        padre.setNombre("Inicio");

        UUID idHijo = UUID.randomUUID();
        ProcedimientoPaso hijo = new ProcedimientoPaso(idHijo);
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        secuencia.setIdProcedimientoPasoReferencia(idPadre);
        hijo.setProcedimientoPasoSecuenciaList(new ArrayList<>(List.of(secuencia)));

        Procedimiento registro = new Procedimiento(UUID.randomUUID());
        registro.setProcedimientoPasoList(new ArrayList<>(List.of(padre, hijo)));
        instance.setRegistro(registro);

        TreeNode<ProcedimientoPaso> raiz = instance.getArbolPasos();

        assertNotNull(raiz);
        assertEquals(1, raiz.getChildren().size());
        TreeNode<ProcedimientoPaso> nodoPadre = raiz.getChildren().get(0);
        assertSame(padre, nodoPadre.getData());
        assertEquals(1, nodoPadre.getChildren().size());
        assertSame(hijo, nodoPadre.getChildren().get(0).getData());
    }

    @Test
    public void testGetArbolPasosVacio() {
        TreeNode<ProcedimientoPaso> raiz = instance.getArbolPasos();

        assertNotNull(raiz);
        assertTrue(raiz.getChildren().isEmpty());
    }

    @Test
    public void testGetArbolPasosCacheado() {
        TreeNode<ProcedimientoPaso> primera = instance.getArbolPasos();
        TreeNode<ProcedimientoPaso> segunda = instance.getArbolPasos();

        assertSame(primera, segunda);
    }

    /**
     * Test of getNuevoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testGetNuevoPaso() {
        ProcedimientoPaso esperado = new ProcedimientoPaso(UUID.randomUUID());
        instance.setNuevoPaso(esperado);

        assertSame(esperado, instance.getNuevoPaso());
    }

    /**
     * Test of setNuevoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testSetNuevoPaso() {
        ProcedimientoPaso nuevoPaso = new ProcedimientoPaso(UUID.randomUUID());

        instance.setNuevoPaso(nuevoPaso);

        assertSame(nuevoPaso, instance.getNuevoPaso());
    }

    /**
     * Test of getPasoSeleccionado method, of class ProcedimientoModel.
     */
    @Test
    public void testGetPasoSeleccionado() {
        ProcedimientoPaso esperado = new ProcedimientoPaso(UUID.randomUUID());
        instance.setPasoSeleccionado(esperado);

        assertSame(esperado, instance.getPasoSeleccionado());
    }

    /**
     * Test of setPasoSeleccionado method, of class ProcedimientoModel.
     */
    @Test
    public void testSetPasoSeleccionado() {
        ProcedimientoPaso pasoSeleccionado = new ProcedimientoPaso(UUID.randomUUID());

        instance.setPasoSeleccionado(pasoSeleccionado);

        assertSame(pasoSeleccionado, instance.getPasoSeleccionado());
    }

    /**
     * Test of getIdRolSeleccionadoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testGetIdRolSeleccionadoPaso() {
        UUID id = UUID.randomUUID();
        instance.setIdRolSeleccionadoPaso(id.toString());

        assertEquals(id.toString(), instance.getIdRolSeleccionadoPaso());
    }

    /**
     * Test of setIdRolSeleccionadoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testSetIdRolSeleccionadoPaso() {
        String idRolSeleccionadoPaso = UUID.randomUUID().toString();

        instance.setIdRolSeleccionadoPaso(idRolSeleccionadoPaso);

        assertEquals(idRolSeleccionadoPaso, instance.getIdRolSeleccionadoPaso());
    }

    /**
     * Test of isEditandoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testIsEditandoPaso() {
        assertFalse(instance.isEditandoPaso());
        instance.setEditandoPaso(true);
        assertTrue(instance.isEditandoPaso());
    }

    /**
     * Test of setEditandoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testSetEditandoPaso() {
        instance.setEditandoPaso(true);

        assertTrue(instance.isEditandoPaso());
    }

    /**
     * Test of isCapturandoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testIsCapturandoPaso() {
        assertFalse(instance.isCapturandoPaso());
        instance.setCapturandoPaso(true);
        assertTrue(instance.isCapturandoPaso());
    }

    /**
     * Test of setCapturandoPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testSetCapturandoPaso() {
        instance.setCapturandoPaso(true);

        assertTrue(instance.isCapturandoPaso());
    }

    /**
     * Test of isDependenciaBloqueada method, of class ProcedimientoModel.
     */
    @Test
    public void testIsDependenciaBloqueada() {
        assertFalse(instance.isDependenciaBloqueada());
        instance.setDependenciaBloqueada(true);
        assertTrue(instance.isDependenciaBloqueada());
    }

    /**
     * Test of setDependenciaBloqueada method, of class ProcedimientoModel.
     */
    @Test
    public void testSetDependenciaBloqueada() {
        instance.setDependenciaBloqueada(true);

        assertTrue(instance.isDependenciaBloqueada());
    }

    /**
     * Test of getIdsExamenesPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testGetIdsExamenesPaso() {
        List<String> esperado = new ArrayList<>(List.of(UUID.randomUUID().toString()));
        instance.setIdsExamenesPaso(esperado);

        assertSame(esperado, instance.getIdsExamenesPaso());
    }

    /**
     * Test of setIdsExamenesPaso method, of class ProcedimientoModel.
     */
    @Test
    public void testSetIdsExamenesPaso() {
        List<String> idsExamenesPaso = new ArrayList<>(List.of(UUID.randomUUID().toString()));

        instance.setIdsExamenesPaso(idsExamenesPaso);

        assertSame(idsExamenesPaso, instance.getIdsExamenesPaso());
    }

    /**
     * Test of getExamenPasoSeleccionadoTemporal method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testGetExamenPasoSeleccionadoTemporal() {
        Examen esperado = examenActivo();
        instance.setExamenPasoSeleccionadoTemporal(esperado);

        assertSame(esperado, instance.getExamenPasoSeleccionadoTemporal());
    }

    /**
     * Test of setExamenPasoSeleccionadoTemporal method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testSetExamenPasoSeleccionadoTemporal() {
        Examen examenPasoSeleccionadoTemporal = examenActivo();

        instance.setExamenPasoSeleccionadoTemporal(examenPasoSeleccionadoTemporal);

        assertSame(examenPasoSeleccionadoTemporal, instance.getExamenPasoSeleccionadoTemporal());
    }

    /**
     * Test of getPasoNodoSeleccionado method, of class ProcedimientoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPasoNodoSeleccionado() {
        TreeNode<ProcedimientoPaso> esperado = mock(TreeNode.class);
        instance.setPasoNodoSeleccionado(esperado);

        assertSame(esperado, instance.getPasoNodoSeleccionado());
    }

    /**
     * Test of setPasoNodoSeleccionado method, of class ProcedimientoModel.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSetPasoNodoSeleccionado() {
        TreeNode<ProcedimientoPaso> pasoNodoSeleccionado = mock(TreeNode.class);

        instance.setPasoNodoSeleccionado(pasoNodoSeleccionado);

        assertSame(pasoNodoSeleccionado, instance.getPasoNodoSeleccionado());
    }

    /**
     * Test of getIdPasoAnteriorSeleccionadoSecuencia method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testGetIdPasoAnteriorSeleccionadoSecuencia() {
        String esperado = UUID.randomUUID().toString();
        instance.setIdPasoAnteriorSeleccionadoSecuencia(esperado);

        assertEquals(esperado, instance.getIdPasoAnteriorSeleccionadoSecuencia());
    }

    /**
     * Test of setIdPasoAnteriorSeleccionadoSecuencia method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testSetIdPasoAnteriorSeleccionadoSecuencia() {
        String idPasoAnteriorSeleccionadoSecuencia = UUID.randomUUID().toString();

        instance.setIdPasoAnteriorSeleccionadoSecuencia(idPasoAnteriorSeleccionadoSecuencia);

        assertEquals(idPasoAnteriorSeleccionadoSecuencia,
                instance.getIdPasoAnteriorSeleccionadoSecuencia());
    }

    /**
     * Test of getRolAutocompleteSeleccionado method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testGetRolAutocompleteSeleccionado() {
        Rol esperado = rolActivo();
        instance.setRolAutocompleteSeleccionado(esperado);

        assertSame(esperado, instance.getRolAutocompleteSeleccionado());
    }

    /**
     * Test of setRolAutocompleteSeleccionado method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testSetRolAutocompleteSeleccionado() {
        Rol rolAutocompleteSeleccionado = rolActivo();

        instance.setRolAutocompleteSeleccionado(rolAutocompleteSeleccionado);

        assertSame(rolAutocompleteSeleccionado, instance.getRolAutocompleteSeleccionado());
    }

    /**
     * Test of getExamenAutocompleteSeleccionado method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testGetExamenAutocompleteSeleccionado() {
        Examen esperado = examenActivo();
        instance.setExamenAutocompleteSeleccionado(esperado);

        assertSame(esperado, instance.getExamenAutocompleteSeleccionado());
    }

    /**
     * Test of setExamenAutocompleteSeleccionado method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testSetExamenAutocompleteSeleccionado() {
        Examen examenAutocompleteSeleccionado = examenActivo();

        instance.setExamenAutocompleteSeleccionado(examenAutocompleteSeleccionado);

        assertSame(examenAutocompleteSeleccionado, instance.getExamenAutocompleteSeleccionado());
    }

    /**
     * Test of getIdPasoSeleccionadoSecuencia method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testGetIdPasoSeleccionadoSecuencia() {
        String esperado = UUID.randomUUID().toString();
        instance.setIdPasoSeleccionadoSecuencia(esperado);

        assertEquals(esperado, instance.getIdPasoSeleccionadoSecuencia());
    }

    /**
     * Test of setIdPasoSeleccionadoSecuencia method, of class
     * ProcedimientoModel.
     */
    @Test
    public void testSetIdPasoSeleccionadoSecuencia() {
        String idPasoSeleccionadoSecuencia = UUID.randomUUID().toString();

        instance.setIdPasoSeleccionadoSecuencia(idPasoSeleccionadoSecuencia);

        assertEquals(idPasoSeleccionadoSecuencia, instance.getIdPasoSeleccionadoSecuencia());
    }

}
