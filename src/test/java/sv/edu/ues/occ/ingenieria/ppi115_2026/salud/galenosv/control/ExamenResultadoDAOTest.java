package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExamenResultadoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Long> longQuery;

    ExamenResultadoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ExamenResultadoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of countByOrden method, of class ExamenResultadoDAO.
     */
    @Test
    public void testCountByOrden() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(6L);

        assertEquals(6L, dao.countByOrden(id));
        verify(longQuery).setParameter("id", id);
    }

    @Test
    public void testCountByOrdenNulo() {
        assertEquals(0L, dao.countByOrden(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ExamenResultadoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
