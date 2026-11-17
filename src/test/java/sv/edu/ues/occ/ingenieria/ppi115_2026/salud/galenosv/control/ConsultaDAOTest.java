package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConsultaDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Consulta> query;

    @Mock
    TypedQuery<Long> longQuery;

    ConsultaDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ConsultaDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findAll method, of class ConsultaDAO.
     */
    @Test
    public void testFindAll() {
        List<Consulta> lista = List.of(mock(Consulta.class));
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Consulta> result = dao.findAll();

        verify(em).createQuery(contains("Consulta"), eq(Consulta.class));
        assertEquals(lista, result);
    }

    @Test
    public void testFindAllError() {
        when(em.createQuery(anyString(), eq(Consulta.class))).thenThrow(new IllegalStateException("Sin conexion"));

        assertThrows(IllegalStateException.class, () -> dao.findAll());
    }

    /**
     * Test of findRange method, of class ConsultaDAO.
     */
    @Test
    public void testFindRange() {
        List<Consulta> lista = List.of(mock(Consulta.class), mock(Consulta.class));
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Consulta> result = dao.findRange(5, 10);

        verify(query).setFirstResult(5);
        verify(query).setMaxResults(10);
        assertEquals(lista, result);
    }

    @Test
    public void testFindRangeParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0));
    }

    /**
     * Test of countByPersonaRol method, of class ConsultaDAO.
     */
    @Test
    public void testCountByPersonaRol() {
        UUID id = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(7L);

        Long result = dao.countByPersonaRol(id);

        verify(longQuery).setParameter("id", id);
        assertEquals(7L, result);
    }

    @Test
    public void testCountByPersonaRolNulo() {
        assertEquals(0L, dao.countByPersonaRol(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of findByClinicaYFechas method, of class ConsultaDAO.
     */
    @Test
    public void testFindByClinicaYFechas() {
        UUID idClinica = UUID.randomUUID();
        OffsetDateTime desde = OffsetDateTime.now().minusDays(1);
        OffsetDateTime hasta = OffsetDateTime.now();
        List<Consulta> lista = List.of(mock(Consulta.class));

        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Consulta> result = dao.findByClinicaYFechas(idClinica, desde, hasta, 0, 10);

        verify(query).setParameter("clinica", idClinica);
        verify(query).setParameter("desde", desde);
        verify(query).setParameter("hasta", hasta);
        assertEquals(lista, result);
    }

    @Test
    public void testFindByClinicaYFechasParametrosInvalidos() {
        OffsetDateTime desde = OffsetDateTime.now().minusDays(1);
        OffsetDateTime hasta = OffsetDateTime.now();

        assertTrue(dao.findByClinicaYFechas(null, desde, hasta, 0, 10).isEmpty());
        assertTrue(dao.findByClinicaYFechas(UUID.randomUUID(), null, hasta, 0, 10).isEmpty());
        assertTrue(dao.findByClinicaYFechas(UUID.randomUUID(), desde, null, 0, 10).isEmpty());
        assertTrue(dao.findByClinicaYFechas(UUID.randomUUID(), desde, hasta, -1, 10).isEmpty());
        assertTrue(dao.findByClinicaYFechas(UUID.randomUUID(), desde, hasta, 0, 0).isEmpty());
        verifyNoInteractions(em);
    }

    /**
     * Test of countByClinicaYFechas method, of class ConsultaDAO.
     */
    @Test
    public void testCountByClinicaYFechas() {
        UUID idClinica = UUID.randomUUID();
        OffsetDateTime desde = OffsetDateTime.now().minusDays(1);
        OffsetDateTime hasta = OffsetDateTime.now();

        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(3L);

        long result = dao.countByClinicaYFechas(idClinica, desde, hasta);

        assertEquals(3L, result);
    }

    @Test
    public void testCountByClinicaYFechasNulo() {
        assertEquals(0L, dao.countByClinicaYFechas(null, OffsetDateTime.now(), OffsetDateTime.now()));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ConsultaDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
