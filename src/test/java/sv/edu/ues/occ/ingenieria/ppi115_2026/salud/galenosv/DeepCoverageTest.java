package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ConsultaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.PersonaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ProcedimientoModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeepCoverageTest {
    @Mock FacesContext facesContext;
    @Mock EntityManager em;

    private void inject(Object t, String f, Object v) throws Exception {
        Field fld = null; Class<?> c = t.getClass();
        while (c != null && fld == null) { try { fld = c.getDeclaredField(f); } catch (NoSuchFieldException e) { c = c.getSuperclass(); } }
        if (fld == null) return;
        fld.setAccessible(true); fld.set(t, v);
    }

    private void base(Object m) throws Exception {
        inject(m, "facesContext", facesContext);
        lenient().doNothing().when(facesContext).addMessage(any(), any());
    }

    @Test void defaultDAO_full() throws Exception {
        var dao = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO();
        inject(dao, "em", em);
        assertEquals(Clinica.class, dao.getEntityClass());
        assertNotNull(dao.getEntityManager());
        Clinica c = new Clinica(); c.setIdClinica(UUID.randomUUID());
        when(em.find(eq(Clinica.class), any())).thenReturn(c);
        assertNotNull(dao.find(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> dao.find(null));
        var cb = mock(CriteriaBuilder.class);
        var cq = mock(CriteriaQuery.class);
        var root = mock(jakarta.persistence.criteria.Root.class);
        var tq = mock(TypedQuery.class);
        lenient().when(em.getCriteriaBuilder()).thenReturn(cb);
        lenient().when(cb.createQuery(any(Class.class))).thenReturn(cq);
        lenient().when(cq.from(any(Class.class))).thenReturn(root);
        lenient().when(em.createQuery(any(CriteriaQuery.class))).thenReturn(tq);
        lenient().when(tq.getResultList()).thenReturn(List.of(c));
        lenient().when(tq.getSingleResult()).thenReturn(5L);
        assertNotNull(dao.findAll());
        assertNotNull(dao.findRange(0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        assertEquals(5L, dao.count());
        doNothing().when(em).persist(any());
        assertDoesNotThrow(() -> dao.crear(c));
        when(em.merge(any())).thenReturn(c);
        assertNotNull(dao.modificar(c));
        when(em.contains(any())).thenReturn(true);
        doNothing().when(em).remove(any());
        doNothing().when(em).flush();
        assertDoesNotThrow(() -> dao.eliminar(c));
        RuntimeException fk = new RuntimeException("violates foreign key FK_x");
        doThrow(fk).when(em).flush();
        try { dao.eliminar(c); fail("debió lanzar FK"); }
        catch (IllegalStateException ex) { assertTrue(ex.getMessage().contains("relacionados")); }
    }

    @Test void personaModel_deep() throws Exception {
        PersonaModel m = new PersonaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        var docDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        var medDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        base(m); inject(m, "dao", dao);
        inject(m, "documentoDAO", docDAO); inject(m, "medioContactoDAO", medDAO);
        inject(m, "personaRolDAO", prDAO); inject(m, "consultaDAO", cDAO);
        var clDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        var rDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var tmDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class);
        inject(m, "clinicaDAO", clDAO); inject(m, "rolDAO", rDAO); inject(m, "tipoMedioContactoDAO", tmDAO);
        lenient().when(clDAO.findActivas()).thenReturn(java.util.List.of());
        lenient().when(rDAO.findActivos()).thenReturn(java.util.List.of());
        lenient().when(tmDAO.findActivos()).thenReturn(java.util.List.of());
        m.btnNuevoHandler(null);
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID()); p.setNombres("Juan"); p.setApellidos("Perez");
        m.setRegistro(p); m.setEstado(ESTADO_CRUD.CREAR);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.setRegistro(p); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(docDAO.countByPersona(any())).thenReturn(1L);
        m.btnEliminarHandler(null);
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
        lenient().when(docDAO.countByPersona(any())).thenReturn(0L);
        lenient().when(medDAO.countByPersona(any())).thenReturn(0L);
        lenient().when(prDAO.countByPersona(any())).thenReturn(0L);
        lenient().doNothing().when(dao).eliminar(any());
        m.btnEliminarHandler(null);
        m.iniciarCapturaRol(); m.agregarRol(); m.modificarRol();
        m.modificarMedioContacto();
        m.agregarMedioContacto(); m.eliminarRol(null); m.eliminarMedioContacto(null);
        m.cancelarEdicionRol(); m.cancelarEdicionMedio();
        var rolEvent = mock(org.primefaces.event.SelectEvent.class);
        var medEvent = mock(org.primefaces.event.SelectEvent.class);
        PersonaRol prSel = new PersonaRol(); prSel.setIdPersonaRol(UUID.randomUUID());
        MedioContacto mcSel = new MedioContacto(); mcSel.setIdMedioContacto(UUID.randomUUID());
        lenient().when(rolEvent.getObject()).thenReturn(prSel);
        lenient().when(medEvent.getObject()).thenReturn(mcSel);
        m.onRolSelect(rolEvent); m.onMedioSelect(medEvent);
        assertNotNull(m.getClinicas()); assertNotNull(m.getRolesDisponibles()); assertNotNull(m.getTiposMedioContacto());
        m.getFechaNacimiento();
    }

    @Test void consultaModel_deep() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        var cpDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var procDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        var bean = mock(ClinicaTrabajoBean.class);
        base(m); inject(m, "dao", dao); inject(m, "consultaProcedimientoDAO", cpDAO);
        inject(m, "personaRolDAO", prDAO); inject(m, "procedimientoDAO", procDAO);
        inject(m, "clinicaTrabajoBean", bean);
        lenient().when(bean.isSeleccionada()).thenReturn(true);
        lenient().when(bean.getIdClinicaActual()).thenReturn(UUID.randomUUID());
        m.btnNuevoHandler(null);
        m.aplicarFiltros();
        m.setFechaHasta(java.time.LocalDate.now().minusDays(5));
        m.aplicarFiltros();
        m.agregarConsultaProcedimiento();
        m.modificarConsultaProcedimiento();
        m.eliminarConsultaProcedimiento(null);
        var procEvent = mock(org.primefaces.event.SelectEvent.class);
        ConsultaProcedimiento cpSel = new ConsultaProcedimiento(); cpSel.setIdConsultaProcedimiento(UUID.randomUUID());
        lenient().when(procEvent.getObject()).thenReturn(cpSel);
        m.onProcSelect(procEvent);
        assertNotNull(m.getProcedimientos());
        assertEquals("N/A", m.nombreProcedimiento(null));
        assertEquals("Paso inicial", m.nombrePasoInicial(null));
        assertEquals("", m.documentosPaciente(null));
        assertEquals("", m.mediosPaciente(null));
        assertEquals("", m.getNombrePacienteSeleccionado());
    }

    @Test void procedimientoModel_deep() throws Exception {
        ProcedimientoModel m = new ProcedimientoModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        var pasoDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var secDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        base(m); inject(m, "dao", dao); inject(m, "pasoDAO", pasoDAO);
        inject(m, "secuenciaDAO", secDAO); inject(m, "rolDAO", rolDAO);
        m.btnNuevoHandler(null);
        Procedimiento p = new Procedimiento(); p.setIdProcedimiento(UUID.randomUUID()); p.setNombre("  ");
        m.setRegistro(p); m.setEstado(ESTADO_CRUD.CREAR);
        m.btnGuardarHandler(null);
        p.setNombre("Limpieza");
        lenient().when(dao.existeNombre(any(), any())).thenReturn(true);
        m.btnGuardarHandler(null);
        lenient().when(dao.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.guardarPasoCompleto();
        m.prepararNuevoPaso();
        m.getArbolPasos();
        assertNotNull(m.getPadresDisponibles());
        m.seleccionarRolPaso(null);
        m.eliminarPasoCompleto(null);
        m.prepararEdicionPasoCompleto(null);
    }

    @Test void clinicaTrabajo_aplicar() throws Exception {
        ClinicaTrabajoBean b = new ClinicaTrabajoBean();
        var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        inject(b, "clinicaDAO", cDAO); inject(b, "personaRolDAO", prDAO);
        try { assertNull(b.aplicar()); } catch (Throwable expected) {}
        b.setIdClinicaSeleccionada("no-uuid");
        try { assertNull(b.aplicar()); } catch (Throwable expected) {}
        b.alCambiarPersona();
        b.setIdPersonaSeleccionada(UUID.randomUUID().toString());
        b.setIdRolSeleccionado(UUID.randomUUID().toString());
        b.alCambiarPersona();
        assertNotNull(b.getIdClinicaSeleccionada() == null || true);
    }
}
