package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentoDAOTest {

    @Mock EntityManager em;
    @Mock TypedQuery<Documento> query;
    @Mock TypedQuery<Long> countQuery;

    DocumentoDAO dao;

    @BeforeEach void setUp(){
        dao = new DocumentoDAO(){ @Override public EntityManager getEntityManager(){ return em; } };
    }

    @Test void testFindByPersona_ok(){
        Documento d = new Documento(); d.setIdDocumento(UUID.randomUUID());
        when(em.createQuery(contains("d.idPersona"), eq(Documento.class))).thenReturn(query);
        when(query.setParameter(eq("idPersona"), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(d));
        UUID id = UUID.randomUUID();
        List<Documento> r = dao.findByPersona(id, 0, 10);
        assertEquals(1, r.size());
        verify(query).setParameter("idPersona", id);
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(10);
    }

    @Test void testFindByPersona_nulo(){
        assertThrows(IllegalArgumentException.class, ()-> dao.findByPersona(null, 0, 10));
    }

    @Test void testCountByPersona_ok(){
        when(em.createQuery(contains("COUNT(d)"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("idPersona"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);
        assertEquals(3L, dao.countByPersona(UUID.randomUUID()));
    }

    @Test void testCountByPersona_nulo_retornaCero(){
        assertEquals(0L, dao.countByPersona(null));
        verifyNoInteractions(em);
    }

    @Test void testBuscarPorValor_ok(){
        Documento d = new Documento();
        when(em.createQuery(contains("d.valor"), eq(Documento.class))).thenReturn(query);
        when(query.setParameter(eq("valor"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(d));
        assertEquals(1, dao.buscarPorValor("12345678-9").size());
    }

    @Test void testBuscarPorValor_invalido(){
        assertThrows(IllegalArgumentException.class, ()-> dao.buscarPorValor(null));
        assertThrows(IllegalArgumentException.class, ()-> dao.buscarPorValor("   "));
    }
}
