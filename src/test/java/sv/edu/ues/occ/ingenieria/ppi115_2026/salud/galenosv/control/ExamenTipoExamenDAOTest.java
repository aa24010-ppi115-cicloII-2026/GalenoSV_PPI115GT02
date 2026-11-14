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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenTipoExamen;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExamenTipoExamenDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<ExamenTipoExamen> query;

    @Mock
    TypedQuery<Long> longQuery;

    ExamenTipoExamenDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ExamenTipoExamenDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findAll method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testFindAll() {
        List<ExamenTipoExamen> lista = List.of(mock(ExamenTipoExamen.class));
        when(em.createQuery(anyString(), eq(ExamenTipoExamen.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ExamenTipoExamen> result = dao.findAll();

        verify(em).createQuery(contains("ExamenTipoExamen"), eq(ExamenTipoExamen.class));
        assertEquals(lista, result);
    }

    @Test
    public void testFindAllError() {
        when(em.createQuery(anyString(), eq(ExamenTipoExamen.class))).thenThrow(new IllegalStateException("Sin conexion"));

        assertThrows(IllegalStateException.class, () -> dao.findAll());
    }

    /**
     * Test of findRange method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testFindRange() {
        List<ExamenTipoExamen> lista = List.of(mock(ExamenTipoExamen.class));
        when(em.createQuery(anyString(), eq(ExamenTipoExamen.class))).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ExamenTipoExamen> result = dao.findRange(0, 10);

        verify(query).setFirstResult(0);
        verify(query).setMaxResults(10);
        assertEquals(lista, result);
    }

    @Test
    public void testFindRangeParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0));
    }

    /**
     * Test of findByIdExamen method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testFindByIdExamen() {
        UUID idExamen = UUID.randomUUID();
        List<ExamenTipoExamen> lista = List.of(mock(ExamenTipoExamen.class));
        when(em.createNamedQuery("ExamenTipoExamen.findByIdExamen", ExamenTipoExamen.class)).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<ExamenTipoExamen> result = dao.findByIdExamen(idExamen, 0, 10);

        verify(query).setParameter("idExamen", idExamen);
        assertEquals(lista, result);
    }

    @Test
    public void testFindByIdExamenParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdExamen(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdExamen(UUID.randomUUID(), -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdExamen(UUID.randomUUID(), 0, 0));
    }

    /**
     * Test of countByIdExamen method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testCountByIdExamen() {
        UUID idExamen = UUID.randomUUID();
        when(em.createNamedQuery("ExamenTipoExamen.countByIdExamen", Long.class)).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(8L);

        int result = dao.countByIdExamen(idExamen);

        verify(longQuery).setParameter("idExamen", idExamen);
        assertEquals(8, result);
    }

    @Test
    public void testCountByIdExamenNulo() {
        assertThrows(IllegalArgumentException.class, () -> dao.countByIdExamen(null));
    }

    /**
     * Test of existeVinculo method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testExisteVinculo() {
        UUID idExamen = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeVinculo(idExamen, idTipo, null));
        verify(longQuery).setParameter("exa", idExamen);
        verify(longQuery).setParameter("tip", idTipo);
    }

    @Test
    public void testExisteVinculoInvalido() {
        assertFalse(dao.existeVinculo(null, UUID.randomUUID(), null));
        assertFalse(dao.existeVinculo(UUID.randomUUID(), null, null));
        verifyNoInteractions(em);
    }

    @Test
    public void testExisteVinculoConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeVinculo(UUID.randomUUID(), UUID.randomUUID(), excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of countByTipo method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testCountByTipo() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(2L);

        assertEquals(2L, dao.countByTipo(id));
        verify(longQuery).setParameter("id", id);
    }

    @Test
    public void testCountByTipoNulo() {
        assertEquals(0L, dao.countByTipo(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ExamenTipoExamenDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
