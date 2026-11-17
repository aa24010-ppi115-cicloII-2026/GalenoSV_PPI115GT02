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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcedimientoPasoExamenDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<ProcedimientoPasoExamen> query;

    @Mock
    TypedQuery<Long> longQuery;

    ProcedimientoPasoExamenDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ProcedimientoPasoExamenDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByPaso method, of class ProcedimientoPasoExamenDAO.
     */
    @Test
    public void testFindByPaso() {
        UUID id = UUID.randomUUID();
        List<ProcedimientoPasoExamen> lista = List.of(mock(ProcedimientoPasoExamen.class));
        when(em.createQuery(anyString(), eq(ProcedimientoPasoExamen.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ProcedimientoPasoExamen> result = dao.findByPaso(id);

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
     * Test of countByExamen method, of class ProcedimientoPasoExamenDAO.
     */
    @Test
    public void testCountByExamen() {
        UUID idExamen = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(3L);

        assertEquals(3L, dao.countByExamen(idExamen));
        verify(longQuery).setParameter("id", idExamen);
    }

    @Test
    public void testCountByExamenNulo() {
        assertEquals(0L, dao.countByExamen(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ProcedimientoPasoExamenDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
