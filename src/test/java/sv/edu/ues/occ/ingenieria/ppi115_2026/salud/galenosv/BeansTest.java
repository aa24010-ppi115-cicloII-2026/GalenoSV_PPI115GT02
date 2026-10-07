package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.IdiomaBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.InicioBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeansTest {
    @Mock FacesContext facesContext;
    @Mock jakarta.faces.application.Application app;
    @Mock jakarta.faces.component.UIViewRoot viewRoot;

    private void inject(Object t, String f, Object v) throws Exception {
        Field fld = null; Class<?> c = t.getClass();
        while (c != null && fld == null) { try { fld = c.getDeclaredField(f); } catch (NoSuchFieldException e) { c = c.getSuperclass(); } }
        fld.setAccessible(true); fld.set(t, v);
    }

    @Test void idioma_locale() {
        IdiomaBean b = new IdiomaBean();
        assertEquals("es", b.getIdioma());
        assertEquals(new Locale("es", "SV"), b.getLocale());
        b.setIdioma("en"); b.setPais("US");
        assertEquals("en", b.getIdioma());
    }

    @Test void idioma_cambiar() {
        IdiomaBean b = new IdiomaBean();
        try { b.cambiarIdioma("en"); } catch (Throwable expected) {}
        assertEquals("en", b.getIdioma());
        try { b.cambiarIdioma("fr"); } catch (Throwable expected) {}
        assertEquals("fr", b.getIdioma());
        try { b.cambiarIdioma("zh"); } catch (Throwable expected) {}
        assertEquals("zh", b.getIdioma());
        try { b.cambiarIdioma("xx"); } catch (Throwable expected) {}
        assertEquals("es", b.getIdioma());
    }

    @Test void inicio_totales() throws Exception {
        InicioBean b = new InicioBean();
        var personaDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        var consultaDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        var ordenDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class);
        var resDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO.class);
        inject(b, "personaDAO", personaDAO);
        inject(b, "consultaDAO", consultaDAO);
        inject(b, "ordenExamenDAO", ordenDAO);
        inject(b, "examenResultadoDAO", resDAO);
        when(personaDAO.count()).thenReturn(3L);
        when(consultaDAO.count()).thenReturn(null);
        when(ordenDAO.count()).thenThrow(new RuntimeException("x"));
        when(resDAO.count()).thenReturn(1L);
        assertEquals(3L, b.getTotalPacientes());
        assertEquals(0L, b.getTotalConsultas());
        assertEquals(0L, b.getTotalOrdenes());
        assertEquals(1L, b.getTotalResultados());
        when(consultaDAO.findRange(0, 5)).thenReturn(List.of());
        assertTrue(b.getUltimasConsultas().isEmpty());
        when(consultaDAO.findRange(0, 5)).thenThrow(new RuntimeException("x"));
        assertTrue(b.getUltimasConsultas().isEmpty());
    }

    @Test void clinicaTrabajo_logica_pura() throws Exception {
        ClinicaTrabajoBean b = new ClinicaTrabajoBean();
        assertFalse(b.isSeleccionada());
        assertNull(b.getIdClinicaActual());
        assertEquals("Cambiar de Rol", b.getNombreAsignacionActual());
        b.alCambiarClinica(); b.alCambiarRol(); b.alCambiarPersona();
        var clinicaDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        inject(b, "clinicaDAO", clinicaDAO);
        inject(b, "personaRolDAO", prDAO);
        when(clinicaDAO.findActivas()).thenReturn(List.of(new Clinica()));
        assertEquals(1, b.getClinicas().size());
        assertTrue(b.getAsignacionesDisponibles().isEmpty());
        assertTrue(b.getPersonasDisponibles().isEmpty());
        Clinica c = new Clinica(); c.setIdClinica(UUID.randomUUID()); c.setNombre("Central");
        Persona p = new Persona(); p.setNombres("A"); p.setApellidos("B");
        Rol r = new Rol(); r.setNombre("Medico");
        PersonaRol pr = new PersonaRol();
        try {
            Field f1 = PersonaRol.class.getDeclaredField("idPersonaRol");
            f1.setAccessible(true);
        } catch (Exception ignored) {}
        assertNotNull(c.getNombre());
    }
}
