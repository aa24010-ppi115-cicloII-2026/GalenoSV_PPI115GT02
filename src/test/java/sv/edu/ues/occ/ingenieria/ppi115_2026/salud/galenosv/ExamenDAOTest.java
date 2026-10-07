package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamenDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<Examen> query;
    @Mock TypedQuery<Long> countQuery;
    ExamenDAO dao;

    @BeforeEach void setUp() {
        dao = new ExamenDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void existeNombre_nulo_false() {
        assertFalse(dao.existeNombre(null, null));
        assertFalse(dao.existeNombre(" ", null));
        verifyNoInteractions(em);
    }

    @Test void existeNombre_ok() {
        when(em.createQuery(contains("LOWER(e.nombre)"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        assertTrue(dao.existeNombre("Rx", null));
    }

    @Test void findActivos_ok() {
        when(em.createQuery(contains("e.activo = TRUE"), eq(Examen.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Examen()));
        assertEquals(1, dao.findActivos().size());
    }

    @Test void findActivosByNombreLike_invalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("ab", 0, 10));
    }

    @Test void findActivosByNombreLike_ok() {
        when(em.createQuery(contains("UPPER(e.nombre)"), eq(Examen.class))).thenReturn(query);
        when(query.setParameter(eq("nombre"), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Examen()));
        assertEquals(1, dao.findActivosByNombreLike("panoramica", 0, 10).size());
    }
}
