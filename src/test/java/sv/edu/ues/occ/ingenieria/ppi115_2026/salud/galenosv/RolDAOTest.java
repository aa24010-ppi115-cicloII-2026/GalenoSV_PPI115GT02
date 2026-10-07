package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RolDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<Rol> query;
    @Mock TypedQuery<Long> countQuery;
    RolDAO dao;

    @BeforeEach void setUp() {
        dao = new RolDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void existeNombre_nulo_false() {
        assertFalse(dao.existeNombre(null, null));
        verifyNoInteractions(em);
    }

    @Test void existeNombre_ok() {
        when(em.createQuery(contains("LOWER(e.nombre)"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(dao.existeNombre("Paciente", null));
    }

    @Test void findActivos_ok() {
        when(em.createQuery(contains("r.activo = TRUE"), eq(Rol.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Rol()));
        assertEquals(1, dao.findActivos().size());
    }

    @Test void findActivosByNombreLike_invalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("ab", 0, 10));
    }

    @Test void findActivosByNombreLike_ok() {
        when(em.createQuery(contains("UPPER(r.nombre)"), eq(Rol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Rol()));
        assertEquals(1, dao.findActivosByNombreLike("medico", 0, 10).size());
    }
}
