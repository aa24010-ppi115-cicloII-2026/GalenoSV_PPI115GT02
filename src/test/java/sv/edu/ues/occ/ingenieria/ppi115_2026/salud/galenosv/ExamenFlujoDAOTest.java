package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenTipoExamen;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamenFlujoDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<ExamenTipoExamen> query;
    @Mock TypedQuery<Long> countQuery;
    ExamenResultadoDAO resDAO;
    ExamenTipoExamenDAO vDAO;

    @BeforeEach void setUp() {
        resDAO = new ExamenResultadoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        vDAO = new ExamenTipoExamenDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void countByOrden_nulo_cero() {
        assertEquals(0L, resDAO.countByOrden(null));
        verifyNoInteractions(em);
    }

    @Test void countByOrden_ok() {
        when(em.createQuery(contains("COUNT(e) FROM ExamenResultado"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("id"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertEquals(1L, resDAO.countByOrden(UUID.randomUUID()));
    }

    @Test void findByIdExamen_nulo_excepcion() {
        assertThrows(IllegalArgumentException.class, () -> vDAO.findByIdExamen(null, 0, 10));
    }

    @Test void findByIdExamen_ok() {
        when(em.createNamedQuery(eq("ExamenTipoExamen.findByIdExamen"), eq(ExamenTipoExamen.class))).thenReturn(query);
        when(query.setParameter(eq("idExamen"), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new ExamenTipoExamen()));
        assertEquals(1, vDAO.findByIdExamen(UUID.randomUUID(), 0, 10).size());
    }

    @Test void existeVinculo_nulo_false() {
        assertFalse(vDAO.existeVinculo(null, UUID.randomUUID(), null));
        assertFalse(vDAO.existeVinculo(UUID.randomUUID(), null, null));
    }

    @Test void existeVinculo_ok() {
        when(em.createQuery(contains("ExamenTipoExamen e WHERE"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(vDAO.existeVinculo(UUID.randomUUID(), UUID.randomUUID(), null));
    }

    @Test void countByTipo_nulo_cero() {
        assertEquals(0L, vDAO.countByTipo(null));
    }
}
