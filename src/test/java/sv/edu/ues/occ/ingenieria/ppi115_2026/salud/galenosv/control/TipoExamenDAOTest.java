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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TipoExamenDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<TipoExamen> query;

    @Mock
    TypedQuery<Long> longQuery;

    TipoExamenDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new TipoExamenDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findActivosByNombreLike method, of class TipoExamenDAO.
     */
    @Test
    public void testFindActivosByNombreLike() {
        List<TipoExamen> lista = List.of(mock(TipoExamen.class));
        when(em.createQuery(anyString(), eq(TipoExamen.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<TipoExamen> result = dao.findActivosByNombreLike("sang", 0, 10);

        verify(query).setParameter("nombre", "%SANG%");
        assertEquals(lista, result);
    }

    @Test
    public void testFindActivosByNombreLikeParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("ab", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("sang", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("sang", 0, 0));
        verifyNoInteractions(em);
    }

    /**
     * Test of findActivos method, of class TipoExamenDAO.
     */
    @Test
    public void testFindActivos() {
        List<TipoExamen> lista = List.of(mock(TipoExamen.class));
        when(em.createQuery(anyString(), eq(TipoExamen.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<TipoExamen> result = dao.findActivos();

        verify(em).createQuery(contains("activo"), eq(TipoExamen.class));
        assertEquals(lista, result);
    }

    /**
     * Test of existeNombre method, of class TipoExamenDAO.
     */
    @Test
    public void testExisteNombre() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeNombre("Sangre", null));
        verify(longQuery).setParameter("nom", "Sangre");
    }

    @Test
    public void testExisteNombreNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Orina", null));
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

        assertFalse(dao.existeNombre("Sangre", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of getEntityManager method, of class TipoExamenDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
