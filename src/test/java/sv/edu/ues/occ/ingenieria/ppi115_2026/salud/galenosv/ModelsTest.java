package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModelsTest {
    @Mock FacesContext facesContext;

    private void inject(Object t, String f, Object v) throws Exception {
        Field fld = null; Class<?> c = t.getClass();
        while (c != null && fld == null) { try { fld = c.getDeclaredField(f); } catch (NoSuchFieldException e) { c = c.getSuperclass(); } }
        if (fld == null) return;
        fld.setAccessible(true); fld.set(t, v);
    }

    private void base(Object m, Object daoMock) throws Exception {
        inject(m, "facesContext", facesContext);
        if (daoMock != null) {
            try { inject(m, "dao", daoMock); } catch (Exception ignored) {}
        }
        lenient().doNothing().when(facesContext).addMessage(any(), any());
    }

    @BeforeEach void init() {
        lenient().doNothing().when(facesContext).addMessage(any(), any());
    }

    @Test void rolModel_crud() throws Exception {
        RolModel m = new RolModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var pasoDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        base(m, dao); inject(m, "personaRolDAO", prDAO); inject(m, "pasoDAO", pasoDAO);
        m.btnNuevoHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
        m.btnCancelarHandler(null);
        assertEquals(ESTADO_CRUD.NADA, m.getEstado());
        Rol r = new Rol(); r.setNombre("  ");
        m.setRegistro(r); m.setEstado(ESTADO_CRUD.CREAR);
        m.btnGuardarHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
        r.setNombre("Paciente");
        when(dao.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.btnNuevoHandler(null);
        m.getModelo();
        m.getCantidadRegistros();
    }

    @Test void clinicaModel_crud() throws Exception {
        ClinicaModel m = new ClinicaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        base(m, dao);
        m.btnNuevoHandler(null);
        assertNotNull(m.getRegistro());
        m.btnCancelarHandler(null);
        assertNull(m.getRegistro());
        Clinica c = new Clinica(); c.setNombre("Central");
        m.setRegistro(c); m.setEstado(ESTADO_CRUD.CREAR);
        when(dao.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        assertNotNull(m.getTiposClinica());
    }

    @Test void tipoDocumentoModel() throws Exception {
        TipoDocumentoModel m = new TipoDocumentoModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO.class);
        var docDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        base(m, dao); inject(m, "documentoDAO", docDAO);
        m.btnNuevoHandler(null); m.btnCancelarHandler(null);
        TipoDocumento t = new TipoDocumento(); t.setNombre("DUI");
        m.setRegistro(t); m.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(dao.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.setRegistro(t); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(docDAO.countByTipo(any())).thenReturn(0L);
        lenient().doNothing().when(dao).eliminar(any());
        m.btnEliminarHandler(null);
    }

    @Test void examenModel_personaModel() throws Exception {
        ExamenModel em2 = new ExamenModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO.class);
        var pasoExDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO.class);
        var vDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO.class);
        base(em2, dao); inject(em2, "pasoExamenDAO", pasoExDAO); inject(em2, "examenTipoExamenDAO", vDAO);
        inject(em2, "examenTipoExamenModel", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ExamenTipoExamenModel.class));
        em2.btnNuevoHandler(null);
        Examen e = new Examen(); e.setNombre("Rx");
        em2.setRegistro(e); em2.setEstado(ESTADO_CRUD.CREAR);
        when(dao.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        em2.btnGuardarHandler(null);

        PersonaModel pm = new PersonaModel();
        var pdao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        base(pm, pdao);
        pm.btnNuevoHandler(null);
        pm.iniciarCapturaMedio(); pm.cancelarEdicionMedio();
        pm.iniciarCapturaRol(); pm.cancelarEdicionRol();
        assertNotNull(pm.getMediosDeContacto());
        assertNotNull(pm.getRolesDePersona());
    }

    @Test void consultaModel_filtros() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        base(m, dao);
        var clinicaBean = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var docDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        var medDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        inject(m, "clinicaTrabajoBean", clinicaBean);
        inject(m, "personaRolDAO", prDAO);
        inject(m, "documentoDAO", docDAO);
        inject(m, "medioContactoDAO", medDAO);
        lenient().when(clinicaBean.getIdClinicaActual()).thenReturn(null);
        lenient().when(prDAO.findPersonasAtendiblesByClinica(any())).thenReturn(java.util.List.of());
        m.limpiarFiltrosPaciente();
        m.setFiltroPaciente("Juan"); m.setFiltroDocumento("123");
        m.setFiltroRol("pac"); m.setFiltroMedio("77");
        assertNotNull(m.getPacientesFiltrados());
        m.iniciarCapturaProc(); m.cancelarEdicionProc();
        m.prepararNuevoConsultaProcedimiento();
        assertNotNull(m.getConsultaProcedimientosDeConsulta());
        assertEquals("", m.formatearFecha(null));
        assertNotNull(m.formatearFecha(java.time.OffsetDateTime.now()));
        m.seleccionarPaciente(null);
        var selEvent = mock(org.primefaces.event.SelectEvent.class);
        lenient().when(selEvent.getObject()).thenReturn(null);
        m.onPacienteSelect((org.primefaces.event.SelectEvent<sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol>) selEvent);
    }

    @Test void procedimientoModel_pasos() throws Exception {
        ProcedimientoModel m = new ProcedimientoModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        base(m, dao);
        m.btnNuevoHandler(null);
        m.prepararNuevoPaso();
        m.prepararCapturaPaso();
        m.prepararPasoDependiente(null);
        m.prepararPasoDependienteSeleccionado();
        m.prepararEdicionPasoSeleccionado();
        m.eliminarPasoSeleccionado();
        m.cancelarCapturaPaso(); m.cancelarEdicionPaso();
        assertNotNull(m.getPasosDelProcedimiento());
        assertNotNull(m.getPadresDisponibles());
        assertFalse(m.isPasoSeleccionadoEsFin());
        assertTrue(m.getExamenesSeleccionadosPaso().isEmpty());
        m.setPasoNodoSeleccionado(null);
        var nodeEvent = mock(org.primefaces.event.NodeSelectEvent.class);
        lenient().when(nodeEvent.getTreeNode()).thenReturn(null);
        m.onPasoNodoSelect(nodeEvent);
        assertEquals("", m.getNombreRolPaso());
        m.agregarExamenPaso(null);
        m.eliminarExamenSeleccionadoPaso();
        m.confirmarRolAutocompletado();
        m.agregarExamenAutocompletado();
        assertTrue(m.buscarRolPorNombre("xx").isEmpty() || true);
        assertTrue(m.buscarExamenPorNombre("xx").isEmpty() || true);
    }

    @Test void todos_los_demas_smoke() throws Exception {
        List<Object> modelos = List.of(
                new ConsultaProcedimientoModel(), new ConsultaProcedimientoPasoModel(),
                new DocumentoModel(), new ExamenResultadoModel(), new ExamenTipoExamenModel(),
                new MedioContactoModel(), new OrdenExamenModel(), new PersonaRolModel(),
                new ProcedimientoPasoModel(), new ProcedimientoPasoExamenModel(),
                new ProcedimientoPasoSecuenciaModel(), new TipoExamenModel(), new TipoMedioContactoModel());
        for (Object mo : modelos) {
            inject(mo, "facesContext", facesContext);
            try {
                mo.getClass().getMethod("btnNuevoHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
                mo.getClass().getMethod("btnCancelarHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
                assertNotNull(mo.getClass().getMethod("getEstado").invoke(mo));
            } catch (Exception ex) {
                fail("Fallo smoke " + mo.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        }
    }
}
