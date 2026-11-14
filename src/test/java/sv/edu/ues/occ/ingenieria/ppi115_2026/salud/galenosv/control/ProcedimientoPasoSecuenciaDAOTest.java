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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcedimientoPasoSecuenciaDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<ProcedimientoPasoSecuencia> query;

    ProcedimientoPasoSecuenciaDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ProcedimientoPasoSecuenciaDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByPaso method, of class ProcedimientoPasoSecuenciaDAO.
     */
    @Test
    public void testFindByPaso() {
        UUID id = UUID.randomUUID();
        List<ProcedimientoPasoSecuencia> lista = List.of(mock(ProcedimientoPasoSecuencia.class));
        when(em.createQuery(anyString(), eq(ProcedimientoPasoSecuencia.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ProcedimientoPasoSecuencia> result = dao.findByPaso(id);

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
     * Test of findDependientes method, of class ProcedimientoPasoSecuenciaDAO.
     */
    @Test
    public void testFindDependientes() {
        UUID idPasoReferencia = UUID.randomUUID();
        List<ProcedimientoPasoSecuencia> lista = List.of(mock(ProcedimientoPasoSecuencia.class));
        when(em.createQuery(anyString(), eq(ProcedimientoPasoSecuencia.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ProcedimientoPasoSecuencia> result = dao.findDependientes(idPasoReferencia);

        verify(query).setParameter("id", idPasoReferencia);
        verify(query).setMaxResults(1000);
        assertEquals(lista, result);
    }

    @Test
    public void testFindDependientesNulo() {
        assertTrue(dao.findDependientes(null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ProcedimientoPasoSecuenciaDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
