/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ClinicaTrabajoBeanTest {

    @Mock
    private ClinicaDAO clinicaDAO;
    @Mock
    private PersonaRolDAO personaRolDAO;

    @InjectMocks
    private ClinicaTrabajoBean instance;

    private UUID idClinica;
    private Clinica clinica;

    public ClinicaTrabajoBeanTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        idClinica = UUID.randomUUID();
        clinica = new Clinica(idClinica, "Central");
        clinica.setActivo(true);
    }

    @AfterEach
    public void tearDown() {
    }

    private Persona persona(String nombres, String apellidos) {
        Persona p = new Persona(UUID.randomUUID());
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        return p;
    }

    private Rol rol(String nombre, boolean activo) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombre);
        r.setActivo(activo);
        return r;
    }

    private PersonaRol asignacion(Clinica c, Persona p, Rol r) {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdClinica(c);
        pr.setIdPersona(p);
        pr.setIdRol(r);
        return pr;
    }

    /**
     * Test of getClinicas method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testGetClinicas() {
        when(clinicaDAO.findActivas()).thenReturn(List.of(clinica));

        List<Clinica> resultado = instance.getClinicas();

        assertEquals(1, resultado.size());
        assertSame(clinica, resultado.get(0));
        verify(clinicaDAO).findActivas();
    }

    /**
     * Test of getAsignacionesDisponibles method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testGetAsignacionesDisponiblesSinClinica() {
        List<PersonaRol> resultado = instance.getAsignacionesDisponibles();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(personaRolDAO, never()).findActivosByClinica(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void testGetAsignacionesDisponiblesConClinicaSeleccionada() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        Persona p = persona("Ana", "Lopez");
        Rol r = rol("Medico", true);
        PersonaRol pr = asignacion(clinica, p, r);
        when(personaRolDAO.findActivosByClinica(idClinica)).thenReturn(List.of(pr));

        List<PersonaRol> resultado = instance.getAsignacionesDisponibles();

        assertEquals(1, resultado.size());
        assertSame(pr, resultado.get(0));
        verify(personaRolDAO).findActivosByClinica(idClinica);
    }

    /**
     * Test of getRolesDisponibles method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testGetRolesDisponibles() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        Persona p1 = persona("Ana", "Lopez");
        Persona p2 = persona("Beto", "Diaz");
        Rol r1 = rol("Medico", true);
        Rol r2 = rol("Enfermero", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        PersonaRol pr2 = asignacion(clinica, p2, r1);
        PersonaRol pr3 = asignacion(clinica, p1, r2);
        PersonaRol prSinRol = new PersonaRol(UUID.randomUUID());
        prSinRol.setIdClinica(clinica);
        prSinRol.setIdPersona(p1);
        when(personaRolDAO.findActivosByClinica(idClinica))
                .thenReturn(List.of(pr1, pr2, pr3, prSinRol));

        List<Rol> resultado = instance.getRolesDisponibles();

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(r1));
        assertTrue(resultado.contains(r2));
    }

    @Test
    public void testGetRolesDisponiblesSinAsignaciones() {
        List<Rol> resultado = instance.getRolesDisponibles();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test of getPersonasDisponibles method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testGetPersonasDisponiblesSinRolSeleccionado() {
        List<Persona> resultado = instance.getPersonasDisponibles();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetPersonasDisponibles() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        Persona p1 = persona("Ana", "Lopez");
        Persona p2 = persona("Beto", "Diaz");
        Rol r1 = rol("Medico", true);
        Rol r2 = rol("Enfermero", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        PersonaRol pr2 = asignacion(clinica, p2, r1);
        PersonaRol pr3 = asignacion(clinica, p1, r2);
        when(personaRolDAO.findActivosByClinica(idClinica)).thenReturn(List.of(pr1, pr2, pr3));

        instance.setIdRolSeleccionado(r1.getIdRol().toString());
        List<Persona> resultado = instance.getPersonasDisponibles();

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(p1));
        assertTrue(resultado.contains(p2));
    }

    @Test
    public void testGetPersonasDisponiblesOtroRol() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        Persona p1 = persona("Ana", "Lopez");
        Persona p2 = persona("Beto", "Diaz");
        Rol r1 = rol("Medico", true);
        Rol r2 = rol("Enfermero", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        PersonaRol pr2 = asignacion(clinica, p2, r1);
        PersonaRol pr3 = asignacion(clinica, p1, r2);
        when(personaRolDAO.findActivosByClinica(idClinica)).thenReturn(List.of(pr1, pr2, pr3));

        instance.setIdRolSeleccionado(r2.getIdRol().toString());
        List<Persona> resultado = instance.getPersonasDisponibles();

        assertEquals(1, resultado.size());
        assertSame(p1, resultado.get(0));
    }

    /**
     * Test of alCambiarClinica method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testAlCambiarClinica() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        instance.setIdPersonaRolSeleccionado(UUID.randomUUID().toString());
        instance.setIdRolSeleccionado(UUID.randomUUID().toString());
        instance.setIdPersonaSeleccionada(UUID.randomUUID().toString());

        instance.alCambiarClinica();

        assertEquals(idClinica.toString(), instance.getIdClinicaSeleccionada());
        assertNull(instance.getIdPersonaRolSeleccionado());
        assertNull(instance.getIdRolSeleccionado());
        assertNull(instance.getIdPersonaSeleccionada());
    }

    /**
     * Test of alCambiarRol method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testAlCambiarRol() {
        String idRol = UUID.randomUUID().toString();
        instance.setIdRolSeleccionado(idRol);
        instance.setIdPersonaSeleccionada(UUID.randomUUID().toString());
        instance.setIdPersonaRolSeleccionado(UUID.randomUUID().toString());

        instance.alCambiarRol();

        assertEquals(idRol, instance.getIdRolSeleccionado());
        assertNull(instance.getIdPersonaSeleccionada());
        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of alCambiarPersona method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testAlCambiarPersonaDatosIncompletos() {
        instance.setIdClinicaSeleccionada(idClinica.toString());

        instance.alCambiarPersona();

        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testAlCambiarPersonaEncuentraAsignacion() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        Persona p1 = persona("Ana", "Lopez");
        Rol r1 = rol("Medico", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        when(personaRolDAO.findActivosByClinica(idClinica)).thenReturn(List.of(pr1));

        instance.setIdPersonaSeleccionada(p1.getIdPersona().toString());
        instance.setIdRolSeleccionado(r1.getIdRol().toString());

        instance.alCambiarPersona();

        assertEquals(pr1.getIdPersonaRol().toString(), instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testAlCambiarPersonaSinCoincidencia() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        Persona p1 = persona("Ana", "Lopez");
        Rol r1 = rol("Medico", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        when(personaRolDAO.findActivosByClinica(idClinica)).thenReturn(List.of(pr1));

        instance.setIdPersonaSeleccionada(UUID.randomUUID().toString());
        instance.setIdRolSeleccionado(r1.getIdRol().toString());

        instance.alCambiarPersona();

        assertNull(instance.getIdPersonaRolSeleccionado());
    }

    /**
     * Test of aplicar method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testAplicarSinClinica() {
        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertNull(resultado);
            assertFalse(instance.isSeleccionada());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR));
        }
    }

    @Test
    public void testAplicarClinicaInexistente() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(null);
        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertNull(resultado);
            assertFalse(instance.isSeleccionada());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR));
        }
    }

    @Test
    public void testAplicarClinicaInactiva() {
        clinica.setActivo(false);
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);
        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertNull(resultado);
            assertFalse(instance.isSeleccionada());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR));
        }
    }

    @Test
    public void testAplicarExitosoSinAsignacion() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);
        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertEquals("/paginas/Consulta?faces-redirect=true", resultado);
            assertTrue(instance.isSeleccionada());
            assertEquals(idClinica, instance.getIdClinicaActual());
            assertSame(clinica, instance.getClinicaActual());
            assertNull(instance.getPersonaRolActual());
            assertEquals("Central", instance.getNombreAsignacionActual());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_INFO));
        }
    }

    @Test
    public void testAplicarExitosoConAsignacion() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);

        Persona p1 = persona("Ana", "Lopez");
        Rol r1 = rol("Medico", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        instance.setIdPersonaRolSeleccionado(pr1.getIdPersonaRol().toString());
        when(personaRolDAO.find(pr1.getIdPersonaRol())).thenReturn(pr1);

        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertEquals("/paginas/Consulta?faces-redirect=true", resultado);
            assertTrue(instance.isSeleccionada());
            assertSame(pr1, instance.getPersonaRolActual());
            assertEquals("Ana Lopez - Medico", instance.getNombreAsignacionActual());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_INFO));
        }
    }

    @Test
    public void testAplicarAsignacionNula() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);
        UUID idAsignacion = UUID.randomUUID();
        instance.setIdPersonaRolSeleccionado(idAsignacion.toString());
        when(personaRolDAO.find(idAsignacion)).thenReturn(null);
        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertNull(resultado);
            assertFalse(instance.isSeleccionada());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR));
        }
    }

    @Test
    public void testAplicarAsignacionOtraClinica() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);

        Clinica otra = new Clinica(UUID.randomUUID(), "Otra");
        otra.setActivo(true);
        Persona p1 = persona("Ana", "Lopez");
        Rol r1 = rol("Medico", true);
        PersonaRol pr1 = asignacion(otra, p1, r1);
        instance.setIdPersonaRolSeleccionado(pr1.getIdPersonaRol().toString());
        when(personaRolDAO.find(pr1.getIdPersonaRol())).thenReturn(pr1);

        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertNull(resultado);
            assertFalse(instance.isSeleccionada());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR));
        }
    }

    @Test
    public void testAplicarAsignacionRolInactivo() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);

        Persona p1 = persona("Ana", "Lopez");
        Rol r1 = rol("Medico", false);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        instance.setIdPersonaRolSeleccionado(pr1.getIdPersonaRol().toString());
        when(personaRolDAO.find(pr1.getIdPersonaRol())).thenReturn(pr1);

        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String resultado = instance.aplicar();

            assertNull(resultado);
            assertFalse(instance.isSeleccionada());
            verify(facesContext).addMessage(isNull(),
                    argThat(mensaje -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR));
        }
    }

    /**
     * Test of isSeleccionada method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testIsSeleccionadaInicial() {
        assertFalse(instance.isSeleccionada());
    }

    /**
     * Test of getIdClinicaActual method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testGetIdClinicaActualInicial() {
        assertNull(instance.getIdClinicaActual());
    }

    @Test
    public void testGetIdClinicaSeleccionadaDesdeClinicaActual() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);
        FacesContext facesContext = mock(FacesContext.class);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            instance.aplicar();
        }

        instance.setIdClinicaSeleccionada(null);

        assertEquals(idClinica.toString(), instance.getIdClinicaSeleccionada());
    }

    @Test
    public void testGettersDesdeAsignacionActual() {
        instance.setIdClinicaSeleccionada(idClinica.toString());
        when(clinicaDAO.find(idClinica)).thenReturn(clinica);

        Persona p1 = persona("Ana", "Lopez");
        Rol r1 = rol("Medico", true);
        PersonaRol pr1 = asignacion(clinica, p1, r1);
        instance.setIdPersonaRolSeleccionado(pr1.getIdPersonaRol().toString());
        when(personaRolDAO.find(pr1.getIdPersonaRol())).thenReturn(pr1);

        FacesContext facesContext = mock(FacesContext.class);
        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            instance.aplicar();
        }

        instance.setIdPersonaRolSeleccionado(null);
        instance.setIdRolSeleccionado(null);
        instance.setIdPersonaSeleccionada(null);

        assertEquals(pr1.getIdPersonaRol().toString(), instance.getIdPersonaRolSeleccionado());
        assertEquals(r1.getIdRol().toString(), instance.getIdRolSeleccionado());
        assertEquals(p1.getIdPersona().toString(), instance.getIdPersonaSeleccionada());
    }

    /**
     * Test of getNombreAsignacionActual method, of class ClinicaTrabajoBean.
     */
    @Test
    public void testGetNombreAsignacionActualInicial() {
        assertEquals("Cambiar de Rol", instance.getNombreAsignacionActual());
    }

    /**
     * Test of getters and setters.
     */
    @Test
    public void testGetSetIdPersonaRolSeleccionado() {
        instance.setIdPersonaRolSeleccionado("abc");

        assertEquals("abc", instance.getIdPersonaRolSeleccionado());
    }

    @Test
    public void testGetSetIdRolSeleccionado() {
        instance.setIdRolSeleccionado("abc");

        assertEquals("abc", instance.getIdRolSeleccionado());
    }

    @Test
    public void testGetSetIdPersonaSeleccionada() {
        instance.setIdPersonaSeleccionada("abc");

        assertEquals("abc", instance.getIdPersonaSeleccionada());
    }

    @Test
    public void testGetSetIdClinicaSeleccionada() {
        instance.setIdClinicaSeleccionada("abc");

        assertEquals("abc", instance.getIdClinicaSeleccionada());
    }
}
