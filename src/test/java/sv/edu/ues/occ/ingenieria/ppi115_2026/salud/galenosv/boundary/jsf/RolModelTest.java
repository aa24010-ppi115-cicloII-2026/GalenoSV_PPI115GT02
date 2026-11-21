/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author david
 */
public class RolModelTest {

    private RolModel instance;
    private RolDAO dao;
    private PersonaRolDAO personaRolDAO;
    private ProcedimientoPasoDAO pasoDAO;

    public RolModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new RolModel();
        dao = mock(RolDAO.class);
        personaRolDAO = mock(PersonaRolDAO.class);
        pasoDAO = mock(ProcedimientoPasoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.personaRolDAO = personaRolDAO;
        instance.pasoDAO = pasoDAO;
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarEliminacion method, of class RolModel.
     */
    @Test
    public void testValidarEliminacion() {
        Rol r = new Rol(UUID.randomUUID());
        when(personaRolDAO.countByRol(r.getIdRol())).thenReturn(0L);
        when(pasoDAO.countByRol(r.getIdRol())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionAsignado() {
        Rol r = new Rol(UUID.randomUUID());
        when(personaRolDAO.countByRol(r.getIdRol())).thenReturn(2L);
        when(pasoDAO.countByRol(r.getIdRol())).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        Rol r = new Rol();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(personaRolDAO, never()).countByRol(any());
        verify(pasoDAO, never()).countByRol(any());
    }

    /**
     * Test of getFacesContext method, of class RolModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class RolModel.
     */
    @Test
    public void testValidarUnicidad() {
        Rol r = new Rol();
        r.setNombre("  Paciente  ");
        when(dao.existeNombre("Paciente", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Paciente", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        Rol r = new Rol();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre("Paciente");
        when(dao.existeNombre("Paciente", r.getIdRol())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class RolModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Rol> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class RolModel.
     */
    @Test
    public void testNuevoRegistro() {
        Rol resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdRol());
        assertEquals(Boolean.TRUE, resultado.getActivo());
    }

    /**
     * Test of buscarRegistroPorId method, of class RolModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Rol esperado = new Rol(id);
        when(dao.find(id)).thenReturn(esperado);

        Rol resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        Rol resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
    }

    /**
     * Test of getIdAsText method, of class RolModel.
     */
    @Test
    public void testGetIdAsText() {
        Rol r = new Rol(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdRol().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class RolModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Rol esperado = new Rol(id);
        when(dao.find(id)).thenReturn(esperado);

        Rol resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        assertNull(instance.getIdByText("no-es-uuid"));
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
    }
}
