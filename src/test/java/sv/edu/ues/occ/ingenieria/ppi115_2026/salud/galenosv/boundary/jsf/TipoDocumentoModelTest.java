/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;

/**
 *
 * @author david
 */
public class TipoDocumentoModelTest {

    private TipoDocumentoModel instance;
    private TipoDocumentoDAO dao;
    private DocumentoDAO documentoDAO;

    public TipoDocumentoModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new TipoDocumentoModel();
        dao = mock(TipoDocumentoDAO.class);
        documentoDAO = mock(DocumentoDAO.class);
        instance.facesContext = mock(FacesContext.class);
        instance.dao = dao;
        instance.documentoDAO = documentoDAO;
        instance.inicializarRegistros();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validarEliminacion method, of class TipoDocumentoModel.
     */
    @Test
    public void testValidarEliminacion() {
        TipoDocumento r = new TipoDocumento(UUID.randomUUID());
        when(documentoDAO.countByTipo(r.getIdTipoDocumento())).thenReturn(0L);

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionConDocumentos() {
        TipoDocumento r = new TipoDocumento(UUID.randomUUID());
        when(documentoDAO.countByTipo(r.getIdTipoDocumento())).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> instance.validarEliminacion(r));
    }

    @Test
    public void testValidarEliminacionSinId() {
        TipoDocumento r = new TipoDocumento();

        assertDoesNotThrow(() -> instance.validarEliminacion(r));
        verify(documentoDAO, never()).countByTipo(any());
    }

    /**
     * Test of getFacesContext method, of class TipoDocumentoModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(instance.facesContext, resultado);
    }

    /**
     * Test of validarUnicidad method, of class TipoDocumentoModel.
     */
    @Test
    public void testValidarUnicidad() {
        TipoDocumento r = new TipoDocumento();
        r.setNombre("  Documento  ");
        when(dao.existeNombre("Documento", null)).thenReturn(false);

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        assertEquals("Documento", r.getNombre());
    }

    @Test
    public void testValidarUnicidadNombreVacio() {
        TipoDocumento r = new TipoDocumento();
        r.setNombre("   ");

        assertDoesNotThrow(() -> instance.validarUnicidad(r, false));
        verify(dao, never()).existeNombre(any(), any());
    }

    @Test
    public void testValidarUnicidadDuplicado() {
        TipoDocumento r = new TipoDocumento(UUID.randomUUID());
        r.setNombre("Documento");
        when(dao.existeNombre("Documento", r.getIdTipoDocumento())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> instance.validarUnicidad(r, true));
    }

    /**
     * Test of getDao method, of class TipoDocumentoModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<TipoDocumento> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class TipoDocumentoModel.
     */
    @Test
    public void testNuevoRegistro() {
        TipoDocumento resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoDocumento());
        assertEquals(Boolean.TRUE, resultado.getActivo());
        assertEquals(".*", resultado.getExpresionRegular());
    }

    /**
     * Test of buscarRegistroPorId method, of class TipoDocumentoModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        UUID id = UUID.randomUUID();
        TipoDocumento esperado = new TipoDocumento(id);
        instance.getModelo().setWrappedData(List.of(esperado));
        when(dao.findAll()).thenReturn(List.of(esperado));

        TipoDocumento resultado = instance.buscarRegistroPorId(id);

        assertSame(esperado, resultado);
    }

    @Test
    public void testBuscarRegistroPorIdNoUuid() {
        TipoDocumento resultado = instance.buscarRegistroPorId("no-es-uuid");

        assertNull(resultado);
    }

    @Test
    public void testBuscarRegistroPorIdModeloVacio() {
        UUID id = UUID.randomUUID();
        instance.getModelo().setWrappedData(List.of());

        TipoDocumento resultado = instance.buscarRegistroPorId(id);

        assertNull(resultado);
    }

    /**
     * Test of getIdAsText method, of class TipoDocumentoModel.
     */
    @Test
    public void testGetIdAsText() {
        TipoDocumento r = new TipoDocumento(UUID.randomUUID());

        String resultado = instance.getIdAsText(r);

        assertEquals(r.getIdTipoDocumento().toString(), resultado);
    }

    @Test
    public void testGetIdAsTextNulo() {
        assertNull(instance.getIdAsText(null));
    }

    /**
     * Test of getIdByText method, of class TipoDocumentoModel.
     */
    @Test
    public void testGetIdByText() {
        UUID id = UUID.randomUUID();
        TipoDocumento esperado = new TipoDocumento(id);
        instance.getModelo().setWrappedData(List.of(esperado));

        TipoDocumento resultado = instance.getIdByText(id.toString());

        assertSame(esperado, resultado);
    }

    @Test
    public void testGetIdByTextInvalido() {
        instance.getModelo().setWrappedData(List.of(new TipoDocumento(UUID.randomUUID())));

        assertNull(instance.getIdByText("no-es-uuid"));
    }

    @Test
    public void testGetIdByTextNulo() {
        assertNull(instance.getIdByText(null));
    }

}
