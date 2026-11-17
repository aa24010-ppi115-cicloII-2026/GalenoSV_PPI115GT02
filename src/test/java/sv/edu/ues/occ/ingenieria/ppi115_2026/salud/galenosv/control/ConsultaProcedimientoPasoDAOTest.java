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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConsultaProcedimientoPasoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<ConsultaProcedimientoPaso> query;

    @Mock
    TypedQuery<Long> longQuery;

    ConsultaProcedimientoPasoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ConsultaProcedimientoPasoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByProcedimiento method, of class ConsultaProcedimientoPasoDAO.
     */
    @Test
    public void testFindByProcedimiento() {
        UUID id = UUID.randomUUID();
        List<ConsultaProcedimientoPaso> lista = List.of(mock(ConsultaProcedimientoPaso.class));
        when(em.createQuery(anyString(), eq(ConsultaProcedimientoPaso.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ConsultaProcedimientoPaso> result = dao.findByProcedimiento(id);

        verify(query).setParameter("id", id);
        verify(query).setMaxResults(1000);
        assertEquals(lista, result);
    }

    @Test
    public void testFindByProcedimientoNulo() {
        assertTrue(dao.findByProcedimiento(null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of countByProcedimiento method, of class ConsultaProcedimientoPasoDAO.
     */
    @Test
    public void testCountByProcedimiento() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(5L);

        assertEquals(5L, dao.countByProcedimiento(id));
        verify(longQuery).setParameter("id", id);
    }

    @Test
    public void testCountByProcedimientoNulo() {
        assertEquals(0L, dao.countByProcedimiento(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of countByPersonaRol method, of class ConsultaProcedimientoPasoDAO.
     */
    @Test
    public void testCountByPersonaRol() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertEquals(1L, dao.countByPersonaRol(id));
        verify(longQuery).setParameter("id", id);
    }

    @Test
    public void testCountByPersonaRolNulo() {
        assertEquals(0L, dao.countByPersonaRol(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ConsultaProcedimientoPasoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
