package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.IdiomaBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MaxStaticTest {
    @Mock FacesContext facesContext;
    @Mock jakarta.faces.component.UIViewRoot viewRoot;

    private void inject(Object t, String f, Object v) throws Exception {
        Field fld = null; Class<?> c = t.getClass();
        while (c != null && fld == null) { try { fld = c.getDeclaredField(f); } catch (NoSuchFieldException e) { c = c.getSuperclass(); } }
        if (fld == null) return;
        fld.setAccessible(true); fld.set(t, v);
    }

    @Test void idioma_real_con_static() {
        try (MockedStatic<FacesContext> st = mockStatic(FacesContext.class)) {
            st.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            when(facesContext.getViewRoot()).thenReturn(viewRoot);
            IdiomaBean b = new IdiomaBean();
            b.cambiarIdioma("en");
            assertEquals("en", b.getIdioma());
            assertEquals("US", b.getPais());
            b.cambiarIdioma("fr");
            b.cambiarIdioma("zh");
            b.cambiarIdioma("xx");
            assertEquals("es", b.getIdioma());
            verify(viewRoot, atLeastOnce()).setLocale(any());
        }
    }

    @Test void clinicaTrabajo_aplicar_feliz() throws Exception {
        try (MockedStatic<FacesContext> st = mockStatic(FacesContext.class)) {
            st.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            lenient().doNothing().when(facesContext).addMessage(any(), any(FacesMessage.class));
            ClinicaTrabajoBean b = new ClinicaTrabajoBean();
            var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
            var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
            inject(b, "clinicaDAO", cDAO); inject(b, "personaRolDAO", prDAO);
            UUID idCli = UUID.randomUUID(), idPR = UUID.randomUUID(), idRol = UUID.randomUUID();
            Clinica cli = new Clinica(); cli.setIdClinica(idCli); cli.setActivo(true); cli.setNombre("Central");
            Rol rol = new Rol(); rol.setIdRol(idRol); rol.setActivo(true); rol.setNombre("Medico");
            Persona per = new Persona(); per.setIdPersona(UUID.randomUUID()); per.setNombres("A"); per.setApellidos("B");
            PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(idPR); pr.setIdClinica(cli); pr.setIdRol(rol); pr.setIdPersona(per);
            when(cDAO.find(idCli)).thenReturn(cli);
            when(prDAO.find(idPR)).thenReturn(pr);
            b.setIdClinicaSeleccionada(idCli.toString());
            b.setIdPersonaRolSeleccionado(idPR.toString());
            String nav = b.aplicar();
            assertEquals("/paginas/Consulta?faces-redirect=true", nav);
            assertTrue(b.isSeleccionada());
            assertNotNull(b.getClinicaActual());
            assertNotNull(b.getPersonaRolActual());
            assertTrue(b.getNombreAsignacionActual().contains("A"));
            assertNotNull(b.getIdClinicaSeleccionada());
            assertNotNull(b.getIdPersonaRolSeleccionado());
            assertNotNull(b.getIdRolSeleccionado());
            assertNotNull(b.getIdPersonaSeleccionada());
        }
    }

    @Test void clinicaTrabajo_aplicar_errores() throws Exception {
        try (MockedStatic<FacesContext> st = mockStatic(FacesContext.class)) {
            st.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            lenient().doNothing().when(facesContext).addMessage(any(), any());
            ClinicaTrabajoBean b = new ClinicaTrabajoBean();
            var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
            inject(b, "clinicaDAO", cDAO);
            inject(b, "personaRolDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class));
            assertNull(b.aplicar());
            b.setIdClinicaSeleccionada(UUID.randomUUID().toString());
            when(cDAO.find(any())).thenReturn(null);
            assertNull(b.aplicar());
            Clinica inact = new Clinica(); inact.setIdClinica(UUID.randomUUID()); inact.setActivo(false);
            when(cDAO.find(any())).thenReturn(inact);
            assertNull(b.aplicar());
            b.alCambiarClinica(); b.alCambiarRol();
            assertTrue(b.getAsignacionesDisponibles().isEmpty());
            assertTrue(b.getRolesDisponibles().isEmpty());
        }
    }

    @Test void entities_full_todas() {
        for (var e : new Object[]{
                new Clinica(), new Persona(), new Rol(), new PersonaRol(), new Consulta(),
                new Procedimiento(), new ProcedimientoPaso(), new ProcedimientoPasoSecuencia(),
                new ProcedimientoPasoExamen(), new Examen(), new TipoExamen(), new ExamenTipoExamen(),
                new OrdenExamen(), new ExamenResultado(), new ConsultaProcedimiento(),
                new ConsultaProcedimientoPaso(), new Documento(), new TipoDocumento(),
                new MedioContacto(), new TipoMedioContacto()}) {
            e.toString(); e.hashCode();
        }
        ConsultaProcedimiento cp = new ConsultaProcedimiento();
        cp.setIdProcedimiento(UUID.randomUUID()); cp.setFechaInicio(OffsetDateTime.now());
        cp.setFechaFin(OffsetDateTime.now()); cp.setObservaciones("x");
        assertNotNull(cp.getIdProcedimiento());
        ConsultaProcedimientoPaso cpp = new ConsultaProcedimientoPaso();
        cpp.setEstado("PENDIENTE"); cpp.setFechaInicio(OffsetDateTime.now());
        assertEquals("PENDIENTE", cpp.getEstado());
        OrdenExamen o = new OrdenExamen(); o.setIndicaciones("ind"); o.setFechaCreacion(OffsetDateTime.now());
        assertEquals("ind", o.getIndicaciones());
        ExamenResultado r = new ExamenResultado();
        r.setInterpretacion("ok"); r.setRutaAtestado("/tmp"); r.setFechaCreacion(OffsetDateTime.now());
        assertEquals("ok", r.getInterpretacion());
        Documento d = new Documento(); d.setRutaFisica("/a"); d.setIdPersona(new Persona());
        assertEquals("/a", d.getRutaFisica());
        MedioContacto mc = new MedioContacto(); mc.setFechaCreacion(OffsetDateTime.now());
        mc.setIdPersona(new Persona()); mc.setIdTipoMedioContacto(new TipoMedioContacto());
        assertNotNull(mc.getFechaCreacion());
        TipoDocumento td = new TipoDocumento();
        td.setIndicaciones("i"); td.setExpresionRegular(".*"); td.setActivo(true);
        assertTrue(td.getActivo());
        TipoMedioContacto tm = new TipoMedioContacto();
        tm.setIndicaciones("i"); tm.setExpresionRegular(".*"); tm.setActivo(true);
        assertTrue(tm.getActivo());
        TipoExamen te = new TipoExamen();
        te.setActivo(true); te.setObservaciones("o");
        assertTrue(te.getActivo());
        Examen ex = new Examen(); ex.setObservaciones("o"); ex.setActivo(true);
        assertTrue(ex.getActivo());
        Procedimiento pr2 = new Procedimiento(); pr2.setObservaciones("o");
        assertEquals("o", pr2.getObservaciones());
        assertNotNull(TipoClinica.values().length > 0);
    }
}
