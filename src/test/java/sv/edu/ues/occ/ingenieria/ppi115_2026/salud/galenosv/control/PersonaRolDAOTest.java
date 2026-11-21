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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PersonaRolDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<PersonaRol> query;

    @Mock
    TypedQuery<Long> longQuery;

    PersonaRolDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new PersonaRolDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findAll method, of class PersonaRolDAO.
     */
    @Test
    public void testFindAll() {
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findAll();

        verify(em).createQuery(contains("PersonaRol"), eq(PersonaRol.class));
        assertEquals(lista, result);
    }

    @Test
    public void testFindAllError() {
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenThrow(new IllegalStateException("Sin conexion"));

        assertThrows(IllegalStateException.class, () -> dao.findAll());
    }

    /**
     * Test of findRange method, of class PersonaRolDAO.
     */
    @Test
    public void testFindRange() {
        List<PersonaRol> lista = List.of(mock(PersonaRol.class), mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findRange(0, 10);

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
     * Test of existeAsignacion method, of class PersonaRolDAO.
     */
    @Test
    public void testExisteAsignacion() {
        UUID idPersona = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeAsignacion(idPersona, idRol, idClinica, null));

        verify(longQuery).setParameter("per", idPersona);
        verify(longQuery).setParameter("rol", idRol);
        verify(longQuery).setParameter("cli", idClinica);
    }

    @Test
    public void testExisteAsignacionInvalido() {
        assertFalse(dao.existeAsignacion(null, UUID.randomUUID(), UUID.randomUUID(), null));
        assertFalse(dao.existeAsignacion(UUID.randomUUID(), null, UUID.randomUUID(), null));
        assertFalse(dao.existeAsignacion(UUID.randomUUID(), UUID.randomUUID(), null, null));
        verifyNoInteractions(em);
    }

    @Test
    public void testExisteAsignacionConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeAsignacion(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of countByPersona method, of class PersonaRolDAO.
     */
    @Test
    public void testCountByPersona() {
        UUID idPersona = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(3L);

        assertEquals(3L, dao.countByPersona(idPersona));
        verify(longQuery).setParameter("id", idPersona);
    }

    @Test
    public void testCountByPersonaNulo() {
        assertEquals(0L, dao.countByPersona(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of countByRol method, of class PersonaRolDAO.
     */
    @Test
    public void testCountByRol() {
        UUID idRol = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(2L);

        assertEquals(2L, dao.countByRol(idRol));
        verify(longQuery).setParameter("id", idRol);
    }

    @Test
    public void testCountByRolNulo() {
        assertEquals(0L, dao.countByRol(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class PersonaRolDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

    /**
     * Test of findByPersona method, of class PersonaRolDAO.
     */
    @Test
    public void testFindByPersona() {
        UUID idPersona = UUID.randomUUID();
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findByPersona(idPersona);

        verify(query).setParameter("id", idPersona);
        verify(query).setMaxResults(1000);
        assertEquals(lista, result);
    }

    /**
     * Test of findByClinica method, of class PersonaRolDAO.
     */
    @Test
    public void testFindByClinica() {
        UUID id = UUID.randomUUID();
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findByClinica(id, 0, 10);

        verify(query).setParameter("id", id);
        assertEquals(lista, result);
    }

    /**
     * Test of findActivosByClinica method, of class PersonaRolDAO.
     */
    @Test
    public void testFindActivosByClinica() {
        UUID idClinica = UUID.randomUUID();
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findActivosByClinica(idClinica);

        verify(query).setParameter("clinica", idClinica);
        assertEquals(lista, result);
    }

    @Test
    public void testFindActivosByClinicaNulo() {
        assertTrue(dao.findActivosByClinica(null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of findPersonasAtendiblesByClinica method, of class PersonaRolDAO.
     */
    @Test
    public void testFindPersonasAtendiblesByClinica() {
        UUID idClinica = UUID.randomUUID();
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findPersonasAtendiblesByClinica(idClinica);

        assertEquals(lista, result);
    }

    @Test
    public void testFindPersonasAtendiblesByClinicaNulo() {
        assertTrue(dao.findPersonasAtendiblesByClinica(null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of findResponsables method, of class PersonaRolDAO.
     */
    @Test
    public void testFindResponsables() {
        UUID idClinica = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findResponsables(idClinica, idRol);

        verify(query).setParameter("clinica", idClinica);
        verify(query).setParameter("rol", idRol);
        assertEquals(lista, result);
    }

    @Test
    public void testFindResponsablesInvalido() {
        assertTrue(dao.findResponsables(null, UUID.randomUUID()).isEmpty());
        assertTrue(dao.findResponsables(UUID.randomUUID(), null).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of countByClinica method, of class PersonaRolDAO.
     */
    @Test
    public void testCountByClinica() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(5L);

        assertEquals(5L, dao.countByClinica(id));
        verify(longQuery).setParameter("id", id);
    }

    /**
     * Test of findPacientes method, of class PersonaRolDAO.
     */
    @Test
    public void testFindPacientes() {
        List<PersonaRol> lista = List.of(mock(PersonaRol.class));
        when(em.createQuery(anyString(), eq(PersonaRol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<PersonaRol> result = dao.findPacientes();

        verify(query).setParameter("rol", "%paciente%");
        verify(query).setParameter("cli", "%cliente%");
        assertEquals(lista, result);
    }

}
