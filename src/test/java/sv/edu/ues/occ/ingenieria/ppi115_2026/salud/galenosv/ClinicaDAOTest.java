package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClinicaDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<Clinica> query;
    @Mock TypedQuery<Long> countQuery;
    ClinicaDAO dao;

    @BeforeEach void setUp() {
        dao = new ClinicaDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void findActivas_ok() {
        when(em.createQuery(contains("c.activo = TRUE"), eq(Clinica.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Clinica()));
        assertEquals(1, dao.findActivas().size());
    }

    @Test void existeNombre_nulo_retornaFalse() {
        assertFalse(dao.existeNombre(null, null));
        assertFalse(dao.existeNombre("  ", null));
        verifyNoInteractions(em);
    }

    @Test void existeNombre_ok() {
        when(em.createQuery(contains("LOWER(e.nombre)"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("nom"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(dao.existeNombre("San Salvador", null));
    }

    @Test void existeNombre_conExcluido() {
        when(em.createQuery(contains("excluir"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        assertFalse(dao.existeNombre("Central", UUID.randomUUID()));
        verify(countQuery).setParameter(eq("excluir"), any());
    }
}
