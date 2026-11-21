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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcedimientoPasoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<ProcedimientoPaso> query;

    @Mock
    TypedQuery<Long> longQuery;

    ProcedimientoPasoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ProcedimientoPasoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByProcedimiento method, of class ProcedimientoPasoDAO.
     */
    @Test
    public void testFindByProcedimiento() {
        UUID id = UUID.randomUUID();
        List<ProcedimientoPaso> lista = List.of(mock(ProcedimientoPaso.class));
        when(em.createQuery(anyString(), eq(ProcedimientoPaso.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ProcedimientoPaso> result = dao.findByProcedimiento(id);

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
     * Test of findInicialesByProcedimiento method, of class ProcedimientoPasoDAO.
     */
    @Test
    public void testFindInicialesByProcedimiento() {
        UUID idProcedimiento = UUID.randomUUID();
        List<ProcedimientoPaso> lista = List.of(mock(ProcedimientoPaso.class));
        when(em.createQuery(anyString(), eq(ProcedimientoPaso.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ProcedimientoPaso> result = dao.findInicialesByProcedimiento(idProcedimiento);

        verify(query).setParameter("procedimiento", idProcedimiento);
        assertEquals(lista, result);
    }

    @Test
    public void testFindInicialesByProcedimientoNulo() {
        assertTrue(dao.findInicialesByProcedimiento(null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of countByProcedimiento method, of class ProcedimientoPasoDAO.
     */
    @Test
    public void testCountByProcedimiento() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(6L);

        assertEquals(6L, dao.countByProcedimiento(id));
        verify(longQuery).setParameter("id", id);
    }

    @Test
    public void testCountByProcedimientoNulo() {
        assertEquals(0L, dao.countByProcedimiento(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of countByRol method, of class ProcedimientoPasoDAO.
     */
    @Test
    public void testCountByRol() {
        UUID idRol = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(4L);

        assertEquals(4L, dao.countByRol(idRol));
        verify(longQuery).setParameter("id", idRol);
    }

    @Test
    public void testCountByRolNulo() {
        assertEquals(0L, dao.countByRol(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ProcedimientoPasoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
