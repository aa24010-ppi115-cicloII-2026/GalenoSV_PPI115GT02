package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PersonaDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Persona> query;

    PersonaDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new PersonaDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of getEntityManager method, of class PersonaDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

    /**
     * Test of buscarPorNombre method, of class PersonaDAO.
     */
    @Test
    public void testBuscarPorNombre() {
        List<Persona> lista = List.of(mock(Persona.class));
        when(em.createQuery(anyString(), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Persona> result = dao.buscarPorNombre("Juan");

        verify(query).setParameter("nombres", "Juan");
        assertEquals(lista, result);
    }

    @Test
    public void testBuscarPorNombreInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombre(null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombre("   "));
        verifyNoInteractions(em);
    }

    @Test
    public void testBuscarPorNombreError() {
        when(em.createQuery(anyString(), eq(Persona.class))).thenThrow(new IllegalStateException("Sin conexion"));

        assertThrows(IllegalStateException.class, () -> dao.buscarPorNombre("Juan"));
    }

    /**
     * Test of buscarPorApellido method, of class PersonaDAO.
     */
    @Test
    public void testBuscarPorApellido() {
        List<Persona> lista = List.of(mock(Persona.class));
        when(em.createQuery(anyString(), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Persona> result = dao.buscarPorApellido("Perez");

        verify(query).setParameter("apellidos", "Perez");
        assertEquals(lista, result);
    }

    @Test
    public void testBuscarPorApellidoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorApellido(null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorApellido("   "));
        verifyNoInteractions(em);
    }

    /**
     * Test of buscarPorDocumento method, of class PersonaDAO.
     */
    @Test
    public void testBuscarPorDocumento() {
        List<Persona> lista = List.of(mock(Persona.class));
        when(em.createQuery(anyString(), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Persona> result = dao.buscarPorDocumento("12345678");

        verify(query).setParameter("valor", "12345678");
        assertEquals(lista, result);
    }

    @Test
    public void testBuscarPorDocumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorDocumento(null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorDocumento("   "));
        verifyNoInteractions(em);
    }

    /**
     * Test of buscarPorNombreYApellido method, of class PersonaDAO.
     */
    @Test
    public void testBuscarPorNombreYApellido() {
        List<Persona> lista = List.of(mock(Persona.class));
        when(em.createQuery(anyString(), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Persona> result = dao.buscarPorNombreYApellido("Juan", "Perez");

        verify(query).setParameter("nombres", "Juan");
        verify(query).setParameter("apellidos", "Perez");
        assertEquals(lista, result);
    }

    @Test
    public void testBuscarPorNombreYApellidoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreYApellido(null, "Perez"));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreYApellido("Juan", null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreYApellido("   ", "Perez"));
        verifyNoInteractions(em);
    }

}
