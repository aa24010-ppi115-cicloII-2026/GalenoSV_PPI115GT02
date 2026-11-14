package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExamenDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Examen> query;

    @Mock
    TypedQuery<Long> longQuery;

    ExamenDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ExamenDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of existeNombre method, of class ExamenDAO.
     */
    @Test
    public void testExisteNombre() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeNombre("Hemograma", null));
        verify(longQuery).setParameter("nom", "Hemograma");
    }

    @Test
    public void testExisteNombreNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Glucosa", null));
    }

    @Test
    public void testExisteNombreInvalido() {
        assertFalse(dao.existeNombre(null, null));
        assertFalse(dao.existeNombre("   ", null));
        verifyNoInteractions(em);
    }

    @Test
    public void testExisteNombreConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Hemograma", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of findActivos method, of class ExamenDAO.
     */
    @Test
    public void testFindActivos() {
        List<Examen> lista = List.of(mock(Examen.class), mock(Examen.class));
        when(em.createQuery(anyString(), eq(Examen.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Examen> result = dao.findActivos();

        verify(em).createQuery(contains("activo"), eq(Examen.class));
        assertEquals(lista, result);
    }

    /**
     * Test of findActivosByNombreLike method, of class ExamenDAO.
     */
    @Test
    public void testFindActivosByNombreLike() {
        List<Examen> lista = List.of(mock(Examen.class));
        when(em.createQuery(anyString(), eq(Examen.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Examen> result = dao.findActivosByNombreLike("hemo", 0, 10);

        verify(query).setParameter("nombre", "%HEMO%");
        assertEquals(lista, result);
    }

    @Test
    public void testFindActivosByNombreLikeParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("ab", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("hemo", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("hemo", 0, 0));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ExamenDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
