/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import org.primefaces.event.SelectEvent;
import org.mockito.ArgumentCaptor;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.MensajeError;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 *
 * @author david
 */
public class ConsultaModelTest {

    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ConsultaModel instance;
    private ConsultaDAO dao;
    private PersonaRolDAO personaRolDAO;
    private DocumentoDAO documentoDAO;
    private MedioContactoDAO medioContactoDAO;
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;
    private OrdenExamenDAO ordenExamenDAO;
    private ProcedimientoDAO procedimientoDAO;
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    private ClinicaTrabajoBean clinicaTrabajoBean;
    private FacesContext facesContext;

    public ConsultaModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ConsultaModel();
        dao = mock(ConsultaDAO.class);
        personaRolDAO = mock(PersonaRolDAO.class);
        documentoDAO = mock(DocumentoDAO.class);
        medioContactoDAO = mock(MedioContactoDAO.class);
        consultaProcedimientoDAO = mock(ConsultaProcedimientoDAO.class);
        consultaProcedimientoPasoDAO = mock(ConsultaProcedimientoPasoDAO.class);
        ordenExamenDAO = mock(OrdenExamenDAO.class);
        procedimientoDAO = mock(ProcedimientoDAO.class);
        procedimientoPasoDAO = mock(ProcedimientoPasoDAO.class);
        clinicaTrabajoBean = mock(ClinicaTrabajoBean.class);
        facesContext = mock(FacesContext.class);

        instance.facesContext = facesContext;
        instance.dao = dao;
        instance.personaRolDAO = personaRolDAO;
        instance.documentoDAO = documentoDAO;
        instance.medioContactoDAO = medioContactoDAO;
        instance.consultaProcedimientoDAO = consultaProcedimientoDAO;
        instance.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
        instance.ordenExamenDAO = ordenExamenDAO;
        instance.procedimientoDAO = procedimientoDAO;
        instance.procedimientoPasoDAO = procedimientoPasoDAO;
        instance.clinicaTrabajoBean = clinicaTrabajoBean;
    }

    @AfterEach
    public void tearDown() {
    }

    private PersonaRol crearPersonaRol(String nombres, String apellidos, String rol) {
        Persona persona = new Persona(UUID.randomUUID());
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(persona);
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(rol);
        r.setActivo(Boolean.TRUE);
        pr.setIdRol(r);
        return pr;
    }

    private PersonaRol crearPersonaRolParaClinica(UUID idClinica, boolean rolActivo) {
        Clinica clinica = new Clinica(idClinica);
        Rol rol = new Rol(UUID.randomUUID());
        rol.setActivo(rolActivo);
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdClinica(clinica);
        pr.setIdRol(rol);
        return pr;
    }

    /**
     * Test of validarEliminacion method, of class ConsultaModel.
     */
    @Test
    public void testValidarEliminacion() {
        Consulta r = new Consulta(UUID.randomUUID());
        when(consultaProcedimientoDAO.countByConsulta(r.getIdConsulta())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConProcedimientos() {
        Consulta r = new Consulta(UUID.randomUUID());
        when(consultaProcedimientoDAO.countByConsulta(r.getIdConsulta())).thenReturn(3L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instance.validarEliminacion(r));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    public void testValidarEliminacionSinId() {
        Consulta r = new Consulta();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(consultaProcedimientoDAO, never()).countByConsulta(any());
    }

    /**
     * Test of inicializarRegistros method, of class ConsultaModel.
     */
    @Test
    public void testInicializarRegistros() {
        instance.inicializarRegistros();

        assertNotNull(instance.getModelo());
    }

    @Test
    public void testInicializarRegistrosCargaDatos() {
        UUID idClinica = UUID.randomUUID();
        Consulta c = new Consulta(UUID.randomUUID());
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(dao.countByClinicaYFechas(any(), any(), any())).thenReturn(4L);
        when(dao.findByClinicaYFechas(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(c));

        instance.inicializarRegistros();

        assertEquals(4, instance.getModelo().count(Collections.emptyMap()));
        assertEquals(1, instance.getModelo().load(0, 10, Collections.emptyMap(), Collections.emptyMap()).size());
    }

    /**
     * Test of aplicarFiltros method, of class ConsultaModel.
     */
    @Test
    public void testAplicarFiltros() {
        instance.setModelo(null);
        LocalDate hoy = LocalDate.now(ZONA_LOCAL);
        instance.setFechaDesde(hoy);
        instance.setFechaHasta(hoy);

        instance.aplicarFiltros();

        assertNotNull(instance.getModelo());
    }

    @Test
    public void testAplicarFiltrosRangoInvalido() {
        LocalDate hoy = LocalDate.now(ZONA_LOCAL);
        instance.setFechaDesde(hoy.plusDays(1));
        instance.setFechaHasta(hoy);
        instance.setModelo(null);

        instance.aplicarFiltros();

        assertNull(instance.getModelo());
        assertEquals("La fecha final debe ser igual o posterior a la inicial", avisoDelFiltro().getDetail());
    }

    @Test
    public void testAplicarFiltrosFechaNulaDiceQueFaltaLaFecha() {
        instance.setFechaDesde(null);
        instance.setFechaHasta(LocalDate.now(ZONA_LOCAL));
        instance.setModelo(null);

        instance.aplicarFiltros();

        assertNull(instance.getModelo());
        FacesMessage aviso = avisoDelFiltro();
        assertEquals(FacesMessage.SEVERITY_WARN, aviso.getSeverity());
        assertEquals("Seleccione la fecha inicial y la final para filtrar", aviso.getDetail());
    }

    private FacesMessage avisoDelFiltro() {
        ArgumentCaptor<FacesMessage> aviso = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), aviso.capture());
        return aviso.getValue();
    }

    /**
     * Test of getFacesContext method, of class ConsultaModel.
     */
    @Test
    public void testGetFacesContext() {
        assertSame(facesContext, instance.getFacesContext());
    }

    /**
     * Test of getDao method, of class ConsultaModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Consulta> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ConsultaModel.
     */
    @Test
    public void testNuevoRegistro() {
        Consulta resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdConsulta());
        assertNotNull(resultado.getFechaInicio());
    }

    /**
     * Test of buscarRegistroPorId method, of class ConsultaModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Consulta esperado = new Consulta(id);
        when(dao.find(id)).thenReturn(esperado);

        Consulta resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        assertNull(instance.buscarRegistroPorId("no-es-uuid"));
    }

    @Test
    public void testBuscarRegistroPorIdNulo() {
        assertNull(instance.buscarRegistroPorId(null));
    }

    /**
     * Test of getIdAsText method, of class ConsultaModel.
     */
    @Test
    public void testGetIdAsText() {
        Consulta r = new Consulta(UUID.randomUUID());

        assertEquals(r.getIdConsulta().toString(), instance.getIdAsText(r));
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
        assertNull(instance.getIdAsText(new Consulta()));
    }

    /**
     * Test of getIdByText method, of class ConsultaModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Consulta esperado = new Consulta(id);
        when(dao.find(id)).thenReturn(esperado);

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
     * Test of selectionHandler method, of class ConsultaModel.
     */
    @Test
    public void testSelectionHandler() {
        Consulta esperado = new Consulta(UUID.randomUUID());
        when(consultaProcedimientoDAO.findByConsulta(esperado.getIdConsulta()))
                .thenReturn(new ArrayList<>());
        SelectEvent<Consulta> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(esperado);

        instance.selectionHandler(evento);

        assertSame(esperado, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertFalse(instance.isEditandoProc());
    }

    @Test
    public void testSelectionHandlerEventoNulo() {
        instance.selectionHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of btnNuevoHandler method, of class ConsultaModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        when(clinicaTrabajoBean.isSeleccionada()).thenReturn(true);

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testBtnNuevoHandlerSinClinica() {
        when(clinicaTrabajoBean.isSeleccionada()).thenReturn(false);

        instance.btnNuevoHandler(mock(ActionEvent.class));

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        verify(facesContext).addMessage(any(), any());
    }

    /**
     * Test of btnCancelarHandler method, of class ConsultaModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        instance.setRegistro(new Consulta(UUID.randomUUID()));
        instance.setEstado(ESTADO_CRUD.CREAR);
        instance.setIdPersonaRolSeleccionado(UUID.randomUUID().toString());

        instance.btnCancelarHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of btnGuardarHandler method, of class ConsultaModel.
     */
    @Test
    public void testBtnGuardarHandler() {
        UUID idClinica = UUID.randomUUID();
        PersonaRol pr = crearPersonaRolParaClinica(idClinica, true);
        Consulta cons = new Consulta(UUID.randomUUID());
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(personaRolDAO.find(pr.getIdPersonaRol())).thenReturn(pr);
        when(consultaProcedimientoDAO.findByConsulta(cons.getIdConsulta())).thenReturn(new ArrayList<>());
        instance.setRegistro(cons);
        instance.setIdPersonaRolSeleccionado(pr.getIdPersonaRol().toString());

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao).crear(cons);
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
        assertSame(pr, cons.getIdPersonaRol());
    }

    @Test
    public void testBtnGuardarHandlerSinPaciente() {
        Consulta cons = new Consulta(UUID.randomUUID());
        instance.setRegistro(cons);

        instance.btnGuardarHandler(mock(ActionEvent.class));

        verify(dao, never()).crear(any());
        verify(facesContext).addMessage(any(), any());
    }

    /**
     * Test of btnModificarHandler method, of class ConsultaModel.
     */
    @Test
    public void testBtnModificarHandler() {
        UUID idClinica = UUID.randomUUID();
        PersonaRol pr = crearPersonaRolParaClinica(idClinica, true);
        Consulta cons = new Consulta(UUID.randomUUID());
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(personaRolDAO.find(pr.getIdPersonaRol())).thenReturn(pr);
        instance.setRegistro(cons);
        instance.setIdPersonaRolSeleccionado(pr.getIdPersonaRol().toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao).modificar(cons);
        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testBtnModificarHandlerError() {
        UUID idClinica = UUID.randomUUID();
        PersonaRol pr = crearPersonaRolParaClinica(idClinica, false);
        Consulta cons = new Consulta(UUID.randomUUID());
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(personaRolDAO.find(pr.getIdPersonaRol())).thenReturn(pr);
        instance.setRegistro(cons);
        instance.setIdPersonaRolSeleccionado(pr.getIdPersonaRol().toString());

        instance.btnModificarHandler(mock(ActionEvent.class));

        verify(dao, never()).modificar(any());
        verify(facesContext).addMessage(any(), any());
    }

    /**
     * Test of prepararNuevoConsultaProcedimiento method, of class ConsultaModel.
     */
    @Test
    public void testPrepararNuevoConsultaProcedimiento() {
        instance.prepararNuevoConsultaProcedimiento();

        ConsultaProcedimiento nuevo = instance.getNuevoConsultaProcedimiento();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdConsultaProcedimiento());
        assertNotNull(nuevo.getFechaInicio());
        assertNull(instance.getIdProcedimientoSeleccionado());
        assertFalse(instance.isCapturandoProc());
    }

    /**
     * Test of iniciarCapturaProc method, of class ConsultaModel.
     */
    @Test
    public void testIniciarCapturaProc() {
        instance.iniciarCapturaProc();

        assertNotNull(instance.getNuevoConsultaProcedimiento());
        assertTrue(instance.isCapturandoProc());
        assertFalse(instance.isEditandoProc());
    }

    /**
     * Test of onProcSelect method, of class ConsultaModel.
     */
    @Test
    public void testOnProcSelect() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setIdProcedimiento(UUID.randomUUID());
        cp.setFechaInicio(OffsetDateTime.now());
        cp.setObservaciones("observacion");
        cp.setIdConsulta(new Consulta(UUID.randomUUID()));
        SelectEvent<ConsultaProcedimiento> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(cp);

        instance.onProcSelect(evento);

        assertSame(cp, instance.getProcSeleccionado());
        assertTrue(instance.isEditandoProc());
        assertTrue(instance.isCapturandoProc());
        assertEquals(cp.getIdProcedimiento().toString(), instance.getIdProcedimientoSeleccionado());
        assertEquals(cp.getIdConsultaProcedimiento(),
                instance.getNuevoConsultaProcedimiento().getIdConsultaProcedimiento());
        assertEquals("observacion", instance.getNuevoConsultaProcedimiento().getObservaciones());
    }

    /**
     * Test of cancelarEdicionProc method, of class ConsultaModel.
     */
    @Test
    public void testCancelarEdicionProc() {
        instance.setProcSeleccionado(new ConsultaProcedimiento(UUID.randomUUID()));
        instance.setEditandoProc(true);
        instance.setCapturandoProc(true);

        instance.cancelarEdicionProc();

        assertNull(instance.getProcSeleccionado());
        assertFalse(instance.isEditandoProc());
        assertFalse(instance.isCapturandoProc());
        assertNotNull(instance.getNuevoConsultaProcedimiento());
    }

    /**
     * Test of modificarConsultaProcedimiento method, of class ConsultaModel.
     */
    @Test
    public void testModificarConsultaProcedimiento() {
        Consulta cons = new Consulta(UUID.randomUUID());
        ConsultaProcedimiento anterior = new ConsultaProcedimiento(UUID.randomUUID());
        List<ConsultaProcedimiento> lista = new ArrayList<>();
        lista.add(anterior);
        cons.setConsultaProcedimientoList(lista);
        instance.setRegistro(cons);
        instance.prepararNuevoConsultaProcedimiento();
        ConsultaProcedimiento nuevo = instance.getNuevoConsultaProcedimiento();
        instance.setIdProcedimientoSeleccionado(UUID.randomUUID().toString());
        instance.setProcSeleccionado(anterior);

        instance.modificarConsultaProcedimiento();

        verify(consultaProcedimientoDAO).modificar(nuevo);
        assertFalse(cons.getConsultaProcedimientoList().contains(anterior));
        assertTrue(cons.getConsultaProcedimientoList().contains(nuevo));
        assertFalse(instance.isEditandoProc());
    }

    @Test
    public void testModificarConsultaProcedimientoSinProcedimiento() {
        Consulta cons = new Consulta(UUID.randomUUID());
        cons.setConsultaProcedimientoList(new ArrayList<>());
        instance.setRegistro(cons);
        instance.prepararNuevoConsultaProcedimiento();
        instance.setIdProcedimientoSeleccionado(null);

        instance.modificarConsultaProcedimiento();

        verify(consultaProcedimientoDAO, never()).modificar(any());
        verify(facesContext).addMessage(any(), any());
    }

    @Test
    public void testModificarConsultaProcedimientoSinRegistro() {
        instance.setRegistro(null);

        instance.modificarConsultaProcedimiento();

        verify(consultaProcedimientoDAO, never()).modificar(any());
    }

    /**
     * Test of agregarConsultaProcedimiento method, of class ConsultaModel.
     */
    @Test
    public void testAgregarConsultaProcedimiento() {
        UUID idClinica = UUID.randomUUID();
        UUID idProcedimiento = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();

        Procedimiento proc = new Procedimiento(idProcedimiento);
        proc.setActivo(Boolean.TRUE);
        proc.setNombre("Procedimiento de prueba");
        Rol rol = new Rol(idRol);
        rol.setActivo(Boolean.TRUE);
        rol.setNombre("Medico");
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setNombre("Paso inicial");
        paso.setIdRol(rol);
        Persona persona = new Persona(UUID.randomUUID());
        persona.setNombres("Juan");
        PersonaRol responsable = new PersonaRol(UUID.randomUUID());
        responsable.setIdPersona(persona);

        Consulta cons = new Consulta(UUID.randomUUID());
        cons.setConsultaProcedimientoList(new ArrayList<>());

        when(procedimientoDAO.find(idProcedimiento)).thenReturn(proc);
        when(procedimientoPasoDAO.findInicialesByProcedimiento(idProcedimiento))
                .thenReturn(Collections.singletonList(paso));
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(personaRolDAO.findResponsables(idClinica, idRol))
                .thenReturn(Collections.singletonList(responsable));

        instance.setRegistro(cons);
        instance.prepararNuevoConsultaProcedimiento();
        instance.setIdProcedimientoSeleccionado(idProcedimiento.toString());

        instance.agregarConsultaProcedimiento();

        verify(consultaProcedimientoDAO).crear(any(ConsultaProcedimiento.class));
        verify(consultaProcedimientoPasoDAO).crear(any(ConsultaProcedimientoPaso.class));
        assertEquals(1, cons.getConsultaProcedimientoList().size());
        assertNull(instance.getIdProcedimientoSeleccionado());
    }

    @Test
    public void testAgregarConsultaProcedimientoSinProcedimientoSeleccionado() {
        Consulta cons = new Consulta(UUID.randomUUID());
        cons.setConsultaProcedimientoList(new ArrayList<>());
        instance.setRegistro(cons);
        instance.prepararNuevoConsultaProcedimiento();
        instance.setIdProcedimientoSeleccionado(" ");

        instance.agregarConsultaProcedimiento();

        verify(consultaProcedimientoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any());
    }

    @Test
    public void testAgregarConsultaProcedimientoInactivo() {
        UUID idProcedimiento = UUID.randomUUID();
        Procedimiento proc = new Procedimiento(idProcedimiento);
        proc.setActivo(Boolean.FALSE);
        when(procedimientoDAO.find(idProcedimiento)).thenReturn(proc);
        Consulta cons = new Consulta(UUID.randomUUID());
        cons.setConsultaProcedimientoList(new ArrayList<>());
        instance.setRegistro(cons);
        instance.prepararNuevoConsultaProcedimiento();
        instance.setIdProcedimientoSeleccionado(idProcedimiento.toString());

        instance.agregarConsultaProcedimiento();

        verify(consultaProcedimientoDAO, never()).crear(any());
        verify(facesContext).addMessage(any(), any());
    }

    /**
     * Test of getConsultaProcedimientosDeConsulta method, of class ConsultaModel.
     */
    @Test
    public void testGetConsultaProcedimientosDeConsulta() {
        Consulta cons = new Consulta(UUID.randomUUID());
        List<ConsultaProcedimiento> lista = new ArrayList<>();
        lista.add(new ConsultaProcedimiento(UUID.randomUUID()));
        cons.setConsultaProcedimientoList(lista);
        instance.setRegistro(cons);

        List<ConsultaProcedimiento> resultado = instance.getConsultaProcedimientosDeConsulta();

        assertSame(lista, resultado);
    }

    @Test
    public void testGetConsultaProcedimientosDeConsultaSinRegistro() {
        List<ConsultaProcedimiento> resultado = instance.getConsultaProcedimientosDeConsulta();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of formatearFecha method, of class ConsultaModel.
     */
    @Test
    public void testFormatearFecha() {
        OffsetDateTime fecha = OffsetDateTime.of(2026, 3, 15, 14, 30, 0, 0, ZoneOffset.UTC);
        String esperado = fecha.atZoneSameInstant(ZONA_LOCAL).format(FORMATO_FECHA);

        assertEquals(esperado, instance.formatearFecha(fecha));
    }

    @Test
    public void testFormatearFechaNula() {
        assertEquals("", instance.formatearFecha(null));
    }

    // Dialogo "Buscar paciente": la base filtra (una consulta por busqueda, no una por fila
    // ni por cada lectura de la vista) y trae como maximo MAX_PACIENTES_BUSQUEDA.

    @Test
    public void testAlAbrirElDialogoListaLosPacientesDeLaClinicaConUnaConsulta() {
        UUID idClinica = UUID.randomUUID();
        PersonaRol ana = crearPersonaRol("Ana", "Perez", "Paciente");
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(personaRolDAO.buscarAtendibles(idClinica, null, null, null, null, ConsultaModel.MAX_PACIENTES_BUSQUEDA))
                .thenReturn(List.of(ana));

        // La vista lee la tabla y el aviso varias veces en un mismo render.
        instance.getPacientesFiltrados();
        instance.getAvisoLimitePacientes();
        List<PersonaRol> resultado = instance.getPacientesFiltrados();

        assertEquals(List.of(ana), resultado);
        verify(personaRolDAO, times(1)).buscarAtendibles(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    public void testBuscarPacientesFiltraEnLaBaseUnaSolaVez() {
        UUID idClinica = UUID.randomUUID();
        PersonaRol ana = crearPersonaRol("Ana", "Perez", "Paciente");
        when(clinicaTrabajoBean.getIdClinicaActual()).thenReturn(idClinica);
        when(personaRolDAO.buscarAtendibles(idClinica, "ana", null, null, "7777", ConsultaModel.MAX_PACIENTES_BUSQUEDA))
                .thenReturn(List.of(ana));
        instance.setFiltroPaciente("ana");
        instance.setFiltroMedio("7777");

        instance.buscarPacientes();
        // La vista lee la tabla varias veces en un mismo render.
        instance.getPacientesFiltrados();
        List<PersonaRol> resultado = instance.getPacientesFiltrados();

        assertEquals(List.of(ana), resultado);
        verify(personaRolDAO, times(1)).buscarAtendibles(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    public void testDocumentosYContactosSeCarganJuntosParaTodosLosResultados() {
        PersonaRol ana = crearPersonaRol("Ana", "Perez", "Paciente");
        PersonaRol carlos = crearPersonaRol("Carlos", "Lopez", "Paciente");
        List<UUID> ids = List.of(ana.getIdPersona().getIdPersona(), carlos.getIdPersona().getIdPersona());
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        dui.setNombre("DUI");
        Documento documento = new Documento(UUID.randomUUID());
        documento.setIdPersona(ana.getIdPersona());
        documento.setIdTipoDocumento(dui);
        documento.setValor("12345678-9");
        TipoMedioContacto telefono = new TipoMedioContacto(UUID.randomUUID());
        telefono.setNombre("Telefono");
        MedioContacto medio = new MedioContacto(UUID.randomUUID());
        medio.setIdPersona(carlos.getIdPersona());
        medio.setIdTipoMedioContacto(telefono);
        medio.setValor("7777-1234");
        when(personaRolDAO.buscarAtendibles(any(), any(), any(), any(), any(), anyInt())).thenReturn(List.of(ana, carlos));
        when(documentoDAO.findByPersonas(ids)).thenReturn(List.of(documento));
        when(medioContactoDAO.findByPersonas(ids)).thenReturn(List.of(medio));
        instance.setFiltroRol("paciente");

        instance.buscarPacientes();

        assertEquals("DUI: 12345678-9", instance.documentosPaciente(ana));
        assertEquals("", instance.documentosPaciente(carlos));
        assertEquals("Telefono: 7777-1234", instance.mediosPaciente(carlos));
        assertEquals("", instance.mediosPaciente(ana));
        assertEquals("", instance.documentosPaciente(null));
        assertEquals("", instance.mediosPaciente(null));
        verify(documentoDAO, times(1)).findByPersonas(any());
        verify(medioContactoDAO, times(1)).findByPersonas(any());
        verify(documentoDAO, never()).findByPersona(any(), anyInt(), anyInt());
        verify(medioContactoDAO, never()).findByPersona(any(), anyInt(), anyInt());
    }

    @Test
    public void testBuscarPacientesSinResultadosDejaLaTablaVaciaSinAviso() {
        when(personaRolDAO.buscarAtendibles(any(), any(), any(), any(), any(), anyInt())).thenReturn(List.of());
        instance.setFiltroPaciente("zzz");

        instance.buscarPacientes();

        assertTrue(instance.getPacientesFiltrados().isEmpty());
        assertEquals("", instance.getAvisoLimitePacientes());
    }

    @Test
    public void testAvisaCuandoPuedeHaberMasPacientesDeLosQueSeMuestran() {
        List<PersonaRol> muchos = new ArrayList<>();
        for (int i = 0; i < ConsultaModel.MAX_PACIENTES_BUSQUEDA; i++) {
            muchos.add(crearPersonaRol("Paciente" + i, "Apellido", "Paciente"));
        }
        when(personaRolDAO.buscarAtendibles(any(), any(), any(), any(), any(), anyInt())).thenReturn(muchos);
        instance.setFiltroPaciente("paciente");

        instance.buscarPacientes();

        assertTrue(instance.getAvisoLimitePacientes().contains(String.valueOf(ConsultaModel.MAX_PACIENTES_BUSQUEDA)));
    }

    @Test
    public void testBuscarPacientesFallaYExplicaLaCausaSinTextoTecnico() {
        when(personaRolDAO.buscarAtendibles(any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new IllegalStateException("Connection refused: localhost:5432"));
        instance.setFiltroPaciente("ana");

        instance.buscarPacientes();

        // La tabla queda vacia y leerla no vuelve a intentar la consulta fallida.
        assertTrue(instance.getPacientesFiltrados().isEmpty());
        verify(personaRolDAO, times(1)).buscarAtendibles(any(), any(), any(), any(), any(), anyInt());
        ArgumentCaptor<FacesMessage> aviso = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), aviso.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, aviso.getValue().getSeverity());
        assertEquals(MensajeError.GENERAL, aviso.getValue().getDetail());
    }

    /**
     * Test of limpiarFiltrosPaciente method, of class ConsultaModel.
     */
    @Test
    public void testLimpiarFiltrosPaciente() {
        instance.setFiltroPaciente("a");
        instance.setFiltroDocumento("b");
        instance.setFiltroRol("c");
        instance.setFiltroMedio("d");

        instance.limpiarFiltrosPaciente();

        assertNull(instance.getFiltroPaciente());
        assertNull(instance.getFiltroDocumento());
        assertNull(instance.getFiltroRol());
        assertNull(instance.getFiltroMedio());
    }

    @Test
    public void testLimpiarFiltrosPacienteVuelveAListarATodos() {
        PersonaRol ana = crearPersonaRol("Ana", "Perez", "Paciente");
        PersonaRol carlos = crearPersonaRol("Carlos", "Lopez", "Paciente");
        when(personaRolDAO.buscarAtendibles(any(), eq("ana"), any(), any(), any(), anyInt())).thenReturn(List.of(ana));
        when(personaRolDAO.buscarAtendibles(any(), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(ana, carlos));
        instance.setFiltroPaciente("ana");
        instance.buscarPacientes();

        instance.limpiarFiltrosPaciente();

        assertEquals(List.of(ana, carlos), instance.getPacientesFiltrados());
    }

    /**
     * Test of seleccionarPaciente method, of class ConsultaModel.
     */
    @Test
    public void testSeleccionarPaciente() {
        PersonaRol personaRol = new PersonaRol(UUID.randomUUID());

        instance.seleccionarPaciente(personaRol);

        assertEquals(personaRol.getIdPersonaRol().toString(), instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testSeleccionarPacienteNulo() {
        instance.seleccionarPaciente(null);

        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of onPacienteSelect method, of class ConsultaModel.
     */
    @Test
    public void testOnPacienteSelect() {
        PersonaRol personaRol = new PersonaRol(UUID.randomUUID());
        SelectEvent<PersonaRol> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(personaRol);

        instance.onPacienteSelect(evento);

        assertEquals(personaRol.getIdPersonaRol().toString(), instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of getNombrePacienteSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testGetNombrePacienteSeleccionado() {
        PersonaRol personaRol = crearPersonaRol("Ana", "Perez", "Paciente");
        instance.setIdPersonaRolSeleccionado(personaRol.getIdPersonaRol().toString());
        when(personaRolDAO.find(personaRol.getIdPersonaRol())).thenReturn(personaRol);

        assertEquals("Ana Perez", instance.getNombrePacienteSeleccionado());
    }

    @Test
    public void testGetNombrePacienteSeleccionadoVacio() {
        assertEquals("", instance.getNombrePacienteSeleccionado());
    }

    /**
     * Test of getProcedimientos method, of class ConsultaModel.
     */
    @Test
    public void testGetProcedimientos() {
        List<Procedimiento> esperado = new ArrayList<>();
        esperado.add(new Procedimiento(UUID.randomUUID()));
        when(procedimientoDAO.findActivos()).thenReturn(esperado);

        assertSame(esperado, instance.getProcedimientos());
    }

    /**
     * Test of nombreProcedimiento method, of class ConsultaModel.
     */
    @Test
    public void testNombreProcedimiento() {
        UUID id = UUID.randomUUID();
        Procedimiento proc = new Procedimiento(id);
        proc.setNombre("Limpieza dental");
        when(procedimientoDAO.find(id)).thenReturn(proc);

        assertEquals("Limpieza dental", instance.nombreProcedimiento(id));
    }

    @Test
    public void testNombreProcedimientoNulo() {
        assertEquals("N/A", instance.nombreProcedimiento(null));
    }

    @Test
    public void testNombreProcedimientoDesconocido() {
        UUID id = UUID.randomUUID();
        when(procedimientoDAO.find(id)).thenReturn(null);

        assertEquals("Desconocido", instance.nombreProcedimiento(id));
    }

    /**
     * Test of nombrePasoInicial method, of class ConsultaModel.
     */
    @Test
    public void testNombrePasoInicial() {
        UUID idProcedimiento = UUID.randomUUID();
        Procedimiento proc = new Procedimiento(idProcedimiento);
        Rol rol = new Rol(UUID.randomUUID());
        rol.setActivo(Boolean.TRUE);
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        paso.setNombre("Paso inicial");
        paso.setIdRol(rol);
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setIdProcedimiento(idProcedimiento);
        when(procedimientoDAO.find(idProcedimiento)).thenReturn(proc);
        when(procedimientoPasoDAO.findInicialesByProcedimiento(idProcedimiento))
                .thenReturn(Collections.singletonList(paso));

        assertEquals("Paso inicial", instance.nombrePasoInicial(cp));
    }

    @Test
    public void testNombrePasoInicialError() {
        assertEquals("Paso inicial", instance.nombrePasoInicial(null));
    }

    /**
     * Test of getIdPersonaRolSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testGetIdPersonaRolSeleccionado() {
        String id = UUID.randomUUID().toString();
        instance.setIdPersonaRolSeleccionado(id);

        assertEquals(id, instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testGetIdPersonaRolSeleccionadoInicial() {
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of setIdPersonaRolSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testSetIdPersonaRolSeleccionado() {
        String id = UUID.randomUUID().toString();

        instance.setIdPersonaRolSeleccionado(id);

        assertEquals(id, instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of getPacienteSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testGetPacienteSeleccionado() {
        PersonaRol personaRol = new PersonaRol(UUID.randomUUID());
        instance.setPacienteSeleccionado(personaRol);

        assertSame(personaRol, instance.getPacienteSeleccionado());
    }

    @Test
    public void testGetPacienteSeleccionadoInicial() {
        assertNull(instance.getPacienteSeleccionado());
    }

    /**
     * Test of setPacienteSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testSetPacienteSeleccionado() {
        PersonaRol personaRol = new PersonaRol(UUID.randomUUID());

        instance.setPacienteSeleccionado(personaRol);

        assertSame(personaRol, instance.getPacienteSeleccionado());
    }

    /**
     * Test of getFiltroPaciente method, of class ConsultaModel.
     */
    @Test
    public void testGetFiltroPaciente() {
        instance.setFiltroPaciente("Ana");

        assertEquals("Ana", instance.getFiltroPaciente());
    }

    @Test
    public void testGetFiltroPacienteInicial() {
        assertNull(instance.getFiltroPaciente());
    }

    /**
     * Test of setFiltroPaciente method, of class ConsultaModel.
     */
    @Test
    public void testSetFiltroPaciente() {
        instance.setFiltroPaciente("Ana");

        assertEquals("Ana", instance.getFiltroPaciente());
    }

    /**
     * Test of getFiltroDocumento method, of class ConsultaModel.
     */
    @Test
    public void testGetFiltroDocumento() {
        instance.setFiltroDocumento("1234");

        assertEquals("1234", instance.getFiltroDocumento());
    }

    @Test
    public void testGetFiltroDocumentoInicial() {
        assertNull(instance.getFiltroDocumento());
    }

    /**
     * Test of setFiltroDocumento method, of class ConsultaModel.
     */
    @Test
    public void testSetFiltroDocumento() {
        instance.setFiltroDocumento("1234");

        assertEquals("1234", instance.getFiltroDocumento());
    }

    /**
     * Test of getFiltroRol method, of class ConsultaModel.
     */
    @Test
    public void testGetFiltroRol() {
        instance.setFiltroRol("Paciente");

        assertEquals("Paciente", instance.getFiltroRol());
    }

    @Test
    public void testGetFiltroRolInicial() {
        assertNull(instance.getFiltroRol());
    }

    /**
     * Test of setFiltroRol method, of class ConsultaModel.
     */
    @Test
    public void testSetFiltroRol() {
        instance.setFiltroRol("Paciente");

        assertEquals("Paciente", instance.getFiltroRol());
    }

    /**
     * Test of getFiltroMedio method, of class ConsultaModel.
     */
    @Test
    public void testGetFiltroMedio() {
        instance.setFiltroMedio("Telefono");

        assertEquals("Telefono", instance.getFiltroMedio());
    }

    @Test
    public void testGetFiltroMedioInicial() {
        assertNull(instance.getFiltroMedio());
    }

    /**
     * Test of setFiltroMedio method, of class ConsultaModel.
     */
    @Test
    public void testSetFiltroMedio() {
        instance.setFiltroMedio("Telefono");

        assertEquals("Telefono", instance.getFiltroMedio());
    }

    /**
     * Test of getNuevoConsultaProcedimiento method, of class ConsultaModel.
     */
    @Test
    public void testGetNuevoConsultaProcedimiento() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        instance.setNuevoConsultaProcedimiento(cp);

        assertSame(cp, instance.getNuevoConsultaProcedimiento());
    }

    @Test
    public void testGetNuevoConsultaProcedimientoInicial() {
        assertNull(instance.getNuevoConsultaProcedimiento());
    }

    /**
     * Test of setNuevoConsultaProcedimiento method, of class ConsultaModel.
     */
    @Test
    public void testSetNuevoConsultaProcedimiento() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());

        instance.setNuevoConsultaProcedimiento(cp);

        assertSame(cp, instance.getNuevoConsultaProcedimiento());
    }

    /**
     * Test of getProcSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testGetProcSeleccionado() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        instance.setProcSeleccionado(cp);

        assertSame(cp, instance.getProcSeleccionado());
    }

    @Test
    public void testGetProcSeleccionadoInicial() {
        assertNull(instance.getProcSeleccionado());
    }

    /**
     * Test of setProcSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testSetProcSeleccionado() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());

        instance.setProcSeleccionado(cp);

        assertSame(cp, instance.getProcSeleccionado());
    }

    /**
     * Test of getIdProcedimientoSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testGetIdProcedimientoSeleccionado() {
        String id = UUID.randomUUID().toString();
        instance.setIdProcedimientoSeleccionado(id);

        assertEquals(id, instance.getIdProcedimientoSeleccionado());
    }

    @Test
    public void testGetIdProcedimientoSeleccionadoInicial() {
        assertNull(instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of setIdProcedimientoSeleccionado method, of class ConsultaModel.
     */
    @Test
    public void testSetIdProcedimientoSeleccionado() {
        String id = UUID.randomUUID().toString();

        instance.setIdProcedimientoSeleccionado(id);

        assertEquals(id, instance.getIdProcedimientoSeleccionado());
    }

    /**
     * Test of isEditandoProc method, of class ConsultaModel.
     */
    @Test
    public void testIsEditandoProc() {
        instance.setEditandoProc(true);

        assertTrue(instance.isEditandoProc());
    }

    @Test
    public void testIsEditandoProcInicial() {
        assertFalse(instance.isEditandoProc());
    }

    /**
     * Test of setEditandoProc method, of class ConsultaModel.
     */
    @Test
    public void testSetEditandoProc() {
        instance.setEditandoProc(true);

        assertTrue(instance.isEditandoProc());
    }

    /**
     * Test of isCapturandoProc method, of class ConsultaModel.
     */
    @Test
    public void testIsCapturandoProc() {
        instance.setCapturandoProc(true);

        assertTrue(instance.isCapturandoProc());
    }

    @Test
    public void testIsCapturandoProcInicial() {
        assertFalse(instance.isCapturandoProc());
    }

    /**
     * Test of setCapturandoProc method, of class ConsultaModel.
     */
    @Test
    public void testSetCapturandoProc() {
        instance.setCapturandoProc(true);

        assertTrue(instance.isCapturandoProc());
    }

    /**
     * Test of getFechaDesde method, of class ConsultaModel.
     */
    @Test
    public void testGetFechaDesde() {
        LocalDate fecha = LocalDate.of(2026, 1, 10);
        instance.setFechaDesde(fecha);

        assertEquals(fecha, instance.getFechaDesde());
    }

    @Test
    public void testGetFechaDesdeInicial() {
        assertNotNull(instance.getFechaDesde());
    }

    /**
     * Test of setFechaDesde method, of class ConsultaModel.
     */
    @Test
    public void testSetFechaDesde() {
        LocalDate fecha = LocalDate.of(2026, 2, 20);

        instance.setFechaDesde(fecha);

        assertEquals(fecha, instance.getFechaDesde());
    }

    /**
     * Test of getFechaHasta method, of class ConsultaModel.
     */
    @Test
    public void testGetFechaHasta() {
        LocalDate fecha = LocalDate.of(2026, 3, 10);
        instance.setFechaHasta(fecha);

        assertEquals(fecha, instance.getFechaHasta());
    }

    @Test
    public void testGetFechaHastaInicial() {
        assertNotNull(instance.getFechaHasta());
    }

    /**
     * Test of setFechaHasta method, of class ConsultaModel.
     */
    @Test
    public void testSetFechaHasta() {
        LocalDate fecha = LocalDate.of(2026, 4, 20);

        instance.setFechaHasta(fecha);

        assertEquals(fecha, instance.getFechaHasta());
    }

}
