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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrdenExamenDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<OrdenExamen> query;

    OrdenExamenDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new OrdenExamenDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByPaso method, of class OrdenExamenDAO.
     */
    @Test
    public void testFindByPaso() {
        UUID id = UUID.randomUUID();
        List<OrdenExamen> lista = List.of(mock(OrdenExamen.class));
        when(em.createQuery(anyString(), eq(OrdenExamen.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<OrdenExamen> result = dao.findByPaso(id);

        verify(query).setParameter("id", id);
        verify(query).setMaxResults(1000);
        assertEquals(lista, result);
    }

    @Test
    public void testFindByPasoNulo() {
        assertTrue(dao.findByPaso(null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class OrdenExamenDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
