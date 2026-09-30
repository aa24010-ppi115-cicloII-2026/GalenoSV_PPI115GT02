package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonaRolDAOTest {

    @Mock EntityManager em;
    @Mock TypedQuery<PersonaRol> query;
    @Mock TypedQuery<Long> countQuery;

    PersonaRolDAO dao;

    @BeforeEach void setUp(){
        dao = new PersonaRolDAO(){ @Override public EntityManager getEntityManager(){ return em; } };
    }

    @Test void testFindByClinica_ok(){
        PersonaRol pr = new PersonaRol();
        when(em.createQuery(contains("p.idClinica"), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(eq("id"), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(pr));
        UUID id = UUID.randomUUID();
        assertEquals(1, dao.findByClinica(id, 0, 20).size());
        verify(query).setParameter("id", id);
    }

    @Test void testCountByClinica_ok(){
        when(em.createQuery(contains("COUNT(p)"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("id"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(5L);
        assertEquals(5L, dao.countByClinica(UUID.randomUUID()));
    }

    @Test void testFindPacientes_ok(){
        PersonaRol pr = new PersonaRol();
        when(em.createQuery(contains("LOWER(p.idRol.nombre)"), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(eq("rol"), eq("%paciente%"))).thenReturn(query);
        when(query.setParameter(eq("cli"), eq("%cliente%"))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(pr));
        assertEquals(1, dao.findPacientes().size());
        verify(query).setParameter("rol", "%paciente%");
        verify(query).setParameter("cli", "%cliente%");
    }
}
