package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackendComplementoDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<MedioContacto> medioQuery;
    @Mock TypedQuery<PersonaRol> prQuery;
    @Mock TypedQuery<Long> countQuery;
    @Mock CriteriaBuilder cb;
    @Mock CriteriaQuery<PersonaRol> cq;
    @Mock Root<PersonaRol> root;

    @Test void medio_existePersonaValor_nulo_false() {
        MedioContactoDAO dao = new MedioContactoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        assertFalse(dao.existePersonaValor(null, "x", null));
        assertFalse(dao.existePersonaValor(UUID.randomUUID(), "  ", null));
        verifyNoInteractions(em);
    }

    @Test void medio_existePersonaValor_ok() {
        MedioContactoDAO dao = new MedioContactoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        when(em.createQuery(contains("m.valor = :val"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(dao.existePersonaValor(UUID.randomUUID(), "77778888", null));
    }

    @Test void medio_countByTipo_nulo_cero() {
        MedioContactoDAO dao = new MedioContactoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        assertEquals(0L, dao.countByTipo(null));
    }

    @Test void personaRol_findByPersona_ok() {
        PersonaRolDAO dao = new PersonaRolDAO() { @Override public EntityManager getEntityManager() { return em; } };
        when(em.createQuery(contains("p.idPersona.idPersona"), eq(PersonaRol.class))).thenReturn(prQuery);
        when(prQuery.setParameter(eq("id"), any())).thenReturn(prQuery);
        when(prQuery.setMaxResults(anyInt())).thenReturn(prQuery);
        when(prQuery.getResultList()).thenReturn(List.of(new PersonaRol()));
        assertEquals(1, dao.findByPersona(UUID.randomUUID()).size());
    }

    @Test void personaRol_findActivosByClinica_nulo_vacio() {
        PersonaRolDAO dao = new PersonaRolDAO() { @Override public EntityManager getEntityManager() { return em; } };
        assertTrue(dao.findActivosByClinica(null).isEmpty());
    }

    @Test void personaRol_findResponsables_nulo_vacio() {
        PersonaRolDAO dao = new PersonaRolDAO() { @Override public EntityManager getEntityManager() { return em; } };
        assertTrue(dao.findResponsables(null, UUID.randomUUID()).isEmpty());
        assertTrue(dao.findResponsables(UUID.randomUUID(), null).isEmpty());
    }

    @Test void personaRol_existeAsignacion_nulo_false() {
        PersonaRolDAO dao = new PersonaRolDAO() { @Override public EntityManager getEntityManager() { return em; } };
        assertFalse(dao.existeAsignacion(null, UUID.randomUUID(), UUID.randomUUID(), null));
    }

    @Test void defaultDAO_crud_basico() {
        PersonaRolDAO dao = new PersonaRolDAO() { @Override public EntityManager getEntityManager() { return em; } };
        PersonaRol r = new PersonaRol();
        assertThrows(IllegalArgumentException.class, () -> dao.crear(null));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(null));
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));
        assertThrows(IllegalArgumentException.class, () -> dao.find(null));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        when(em.find(eq(PersonaRol.class), any())).thenReturn(r);
        assertNotNull(dao.find(UUID.randomUUID()));
        doNothing().when(em).persist(any());
        assertDoesNotThrow(() -> dao.crear(r));
        when(em.merge(any())).thenReturn(r);
        assertNotNull(dao.modificar(r));
    }

    @Test void tipoMedio_existeNombre_nulo_false() {
        TipoMedioContactoDAO dao = new TipoMedioContactoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        assertFalse(dao.existeNombre(null, null));
        verifyNoInteractions(em);
    }
}
