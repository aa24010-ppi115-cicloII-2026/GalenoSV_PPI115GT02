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
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoClinica;

/**
 *
 * @author david
 */
public class ClinicaModelTest {

    private ClinicaModel instance;
    private ClinicaDAO dao;
    private PersonaRolDAO personaRolDAO;
    private PersonaRolModel personaRolModel;

    public ClinicaModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ClinicaModel();
        dao = mock(ClinicaDAO.class);
        personaRolDAO = mock(PersonaRolDAO.class);
        personaRolModel = mock(PersonaRolModel.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.personaRolDAO = personaRolDAO;
        instance.personaRolModel = personaRolModel;
        instance.inicializarRegistros();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getPersonaRolModel method, of class ClinicaModel.
     */
    @Test
    public void testGetPersonaRolModel() {
        PersonaRolModel resultado = instance.getPersonaRolModel();

        assertSame(personaRolModel, resultado);
        verify(personaRolModel).establecerClinicaMaestro(null);
    }

    @Test
    public void testGetPersonaRolModelModificando() {
        Clinica c = new Clinica(UUID.randomUUID());
        instance.setRegistro(c);
        instance.setEstado(ESTADO_CRUD.MODIFICAR);

        PersonaRolModel resultado = instance.getPersonaRolModel();

        assertSame(personaRolModel, resultado);
        verify(personaRolModel).establecerClinicaMaestro(c.getIdClinica());
    }

    /**
     * Test of getTiposClinica method, of class ClinicaModel.
     */
    @Test
    public void testGetTiposClinica() {
        TipoClinica[] resultado = instance.getTiposClinica();

        assertArrayEquals(TipoClinica.values(), resultado);
    }

    /**
     * Test of validarEliminacion method, of class ClinicaModel.
     */
    @Test
    public void testValidarEliminacion() {
        Clinica r = new Clinica(UUID.randomUUID());
        when(personaRolDAO.countByClinica(r.getIdClinica())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConAsignaciones() {
        Clinica r = new Clinica(UUID.randomUUID());
        when(personaRolDAO.countByClinica(r.getIdClinica())).thenReturn(5L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        Clinica r = new Clinica();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(personaRolDAO, never()).countByClinica(any());
    }

    /**
     * Test of getFacesContext method, of class ClinicaModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class ClinicaModel.
     */
    @Test
    public void testValidarUnicidad() {
        Clinica r = new Clinica();
        r.setNombre("  Central  ");
        when(dao.existeNombre("Central", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Central", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        Clinica r = new Clinica();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        Clinica r = new Clinica(UUID.randomUUID());
        r.setNombre("Central");
        when(dao.existeNombre("Central", r.getIdClinica())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class ClinicaModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<Clinica> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class ClinicaModel.
     */
    @Test
    public void testNuevoRegistro() {
        Clinica resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdClinica());
        assertEquals(Boolean.TRUE, resultado.getActivo());
    }

    /**
     * Test of buscarRegistroPorId method, of class ClinicaModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        Clinica esperado = new Clinica(id);
        when(dao.find(id)).thenReturn(esperado);

        Clinica resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        Clinica resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
    }

    /**
     * Test of getIdAsText method, of class ClinicaModel.
     */
    @Test
    public void testGetIdAsText() {
        Clinica r = new Clinica(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdClinica().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class ClinicaModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        Clinica esperado = new Clinica(id);
        when(dao.find(id)).thenReturn(esperado);

        Clinica resultado = instance.getIdByText(id.toString());

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
