package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcedimientoDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<Procedimiento> query;
    @Mock TypedQuery<Long> countQuery;
    ProcedimientoDAO dao;

    @BeforeEach void setUp() {
        dao = new ProcedimientoDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void existeNombre_nulo_false() {
        assertFalse(dao.existeNombre(null, null));
        verifyNoInteractions(em);
    }

    @Test void existeNombre_ok() {
        when(em.createQuery(contains("Procedimiento e WHERE"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(dao.existeNombre("Limpieza", null));
    }

    @Test void findActivos_ok() {
        when(em.createQuery(contains("p.activo = TRUE"), eq(Procedimiento.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Procedimiento()));
        assertEquals(1, dao.findActivos().size());
    }
}
