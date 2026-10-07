package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DemolicionTest {
    @Mock FacesContext facesContext;

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

    @Test void arbol_tres_niveles_y_seleccion() throws Exception {
        ProcedimientoModel m = new ProcedimientoModel();
        base(m); inject(m, "dao", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class));
        Procedimiento proc = new Procedimiento(); proc.setIdProcedimiento(UUID.randomUUID());
        proc.setProcedimientoPasoList(new ArrayList<>());
        ProcedimientoPaso raiz = new ProcedimientoPaso(); raiz.setIdProcedimientoPaso(UUID.randomUUID());
        raiz.setNombre("Raiz"); raiz.setProcedimientoPasoSecuenciaList(new ArrayList<>());
        raiz.setProcedimientoPasoExamenList(new ArrayList<>());
        ProcedimientoPaso hijo = new ProcedimientoPaso(); hijo.setIdProcedimientoPaso(UUID.randomUUID());
        hijo.setNombre("Hijo");
        ProcedimientoPasoSecuencia s1 = new ProcedimientoPasoSecuencia();
        s1.setIdProcedimientoPasoSecuencia(UUID.randomUUID()); s1.setIdProcedimientoPaso(hijo);
        s1.setIdProcedimientoPasoReferencia(raiz.getIdProcedimientoPaso());
        hijo.setProcedimientoPasoSecuenciaList(List.of(s1));
        hijo.setProcedimientoPasoExamenList(new ArrayList<>());
        ProcedimientoPaso nieto = new ProcedimientoPaso(); nieto.setIdProcedimientoPaso(UUID.randomUUID());
        nieto.setNombre("Nieto");
        ProcedimientoPasoSecuencia s2 = new ProcedimientoPasoSecuencia();
        s2.setIdProcedimientoPasoSecuencia(UUID.randomUUID()); s2.setIdProcedimientoPaso(nieto);
        s2.setIdProcedimientoPasoReferencia(hijo.getIdProcedimientoPaso());
        nieto.setProcedimientoPasoSecuenciaList(List.of(s2));
        nieto.setProcedimientoPasoExamenList(new ArrayList<>());
        ProcedimientoPaso huerfano = new ProcedimientoPaso(); huerfano.setIdProcedimientoPaso(UUID.randomUUID());
        huerfano.setNombre("Huerfano");
        huerfano.setProcedimientoPasoSecuenciaList(new ArrayList<>());
        huerfano.setProcedimientoPasoExamenList(new ArrayList<>());
        proc.getProcedimientoPasoList().addAll(List.of(raiz, hijo, nieto, huerfano));
        m.setRegistro(proc);
        assertNotNull(m.getArbolPasos());
        assertEquals(4, m.getPasosDelProcedimiento().size());
        assertFalse(m.getPadresDisponibles().isEmpty());
        m.prepararEdicionPasoCompleto(hijo);
        m.guardarPasoCompleto();
        m.prepararPasoDependiente(raiz);
        assertTrue(m.isCapturandoPaso());
        m.cancelarCapturaPaso();
    }

    @Test void refrescar_consulta_con_hijos() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        var cpDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO.class);
        var cppDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class);
        var ordDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var bean = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean.class);
        base(m); inject(m, "dao", dao); inject(m, "consultaProcedimientoDAO", cpDAO);
        inject(m, "consultaProcedimientoPasoDAO", cppDAO); inject(m, "ordenExamenDAO", ordDAO);
        inject(m, "personaRolDAO", prDAO); inject(m, "clinicaTrabajoBean", bean);
        inject(m, "procedimientoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class));
        inject(m, "procedimientoPasoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class));
        inject(m, "documentoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class));
        inject(m, "medioContactoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class));
        Consulta c = new Consulta(); c.setIdConsulta(UUID.randomUUID());
        PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(UUID.randomUUID());
        Persona per = new Persona(); per.setNombres("A"); per.setApellidos("B"); pr.setIdPersona(per);
        c.setIdPersonaRol(pr);
        ConsultaProcedimiento cp = new ConsultaProcedimiento(); cp.setIdConsultaProcedimiento(UUID.randomUUID());
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(); paso.setIdConsultaProcedimientoPaso(UUID.randomUUID());
        paso.setEstado("PENDIENTE");
        OrdenExamen o = new OrdenExamen(); o.setIdOrdenExamen(UUID.randomUUID());
        lenient().when(cpDAO.findByConsulta(c.getIdConsulta())).thenReturn(List.of(cp));
        lenient().when(cppDAO.findByProcedimiento(cp.getIdConsultaProcedimiento())).thenReturn(List.of(paso));
        lenient().when(ordDAO.findByPaso(paso.getIdConsultaProcedimientoPaso())).thenReturn(List.of(o));
        m.setRegistro(c);
        m.selectionHandler(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class), mock(jakarta.faces.component.behavior.Behavior.class), c));
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
        assertFalse(m.getConsultaProcedimientosDeConsulta().isEmpty());
        m.btnCancelarHandler(null);
        m.setRegistro(c); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(cpDAO.countByConsulta(any())).thenReturn(2L);
        m.btnEliminarHandler(null);
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
        m.setFiltroPaciente("A"); m.setFiltroDocumento(""); m.setFiltroRol(""); m.setFiltroMedio("");
        lenient().when(prDAO.findPersonasAtendiblesByClinica(any())).thenReturn(List.of(pr));
        assertFalse(m.getPacientesFiltrados().isEmpty());
    }

    @Test void persona_validaciones_duplicado_y_errores() throws Exception {
        PersonaModel m = new PersonaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        base(m); inject(m, "dao", dao);
        inject(m, "documentoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class));
        inject(m, "medioContactoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class));
        inject(m, "personaRolDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class));
        inject(m, "consultaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class));
        inject(m, "clinicaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class));
        inject(m, "rolDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class));
        inject(m, "tipoMedioContactoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class));
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID()); p.setNombres("Juan"); p.setApellidos("P");
        p.setFechaCreacion(OffsetDateTime.now());
        m.setRegistro(p); m.setEstado(ESTADO_CRUD.CREAR);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.setRegistro(p); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(dao.modificar(any())).thenReturn(p);
        m.btnModificarHandler(null);
        m.setRegistro(null);
        m.btnGuardarHandler(null); m.btnModificarHandler(null); m.btnEliminarHandler(null);
        m.selectionHandler(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class), mock(jakarta.faces.component.behavior.Behavior.class), p));
        m.selectionHandler(null);
        m.setRegistro(p);
        m.prepararNuevoRol(); m.prepararNuevoMedioContacto();
        assertNotNull(m.getNuevoRol()); assertNotNull(m.getNuevoMedioContacto());
    }
}
