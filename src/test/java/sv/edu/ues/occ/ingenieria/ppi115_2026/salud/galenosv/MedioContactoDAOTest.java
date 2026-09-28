package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedioContactoDAOTest {

    @Mock EntityManager em;
    @Mock TypedQuery<MedioContacto> query;
    @Mock TypedQuery<Long> countQuery;

    MedioContactoDAO dao;

    @BeforeEach void setUp(){
        dao = new MedioContactoDAO(){ @Override public EntityManager getEntityManager(){ return em; } };
    }

    @Test void testFindByPersona_ok(){
        MedioContacto m = new MedioContacto();
        when(em.createQuery(contains("m.idPersona"), eq(MedioContacto.class))).thenReturn(query);
        when(query.setParameter(eq("idPersona"), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(m));
        UUID id = UUID.randomUUID();
        assertEquals(1, dao.findByPersona(id, 0, 20).size());
        verify(query).setParameter("idPersona", id);
    }

    @Test void testFindByPersona_nulo(){
        assertThrows(IllegalArgumentException.class, ()-> dao.findByPersona(null, 0, 10));
    }

    @Test void testCountByPersona_ok(){
        when(em.createQuery(contains("COUNT(m)"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("idPersona"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        assertEquals(2L, dao.countByPersona(UUID.randomUUID()));
    }

    @Test void testCountByPersona_nulo_retornaCero(){
        assertEquals(0L, dao.countByPersona(null));
        verifyNoInteractions(em);
    }
}
