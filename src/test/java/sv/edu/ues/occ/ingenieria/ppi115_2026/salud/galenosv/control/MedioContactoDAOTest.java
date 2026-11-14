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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MedioContactoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<MedioContacto> query;

    @Mock
    TypedQuery<Long> longQuery;

    MedioContactoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new MedioContactoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByPersona method, of class MedioContactoDAO.
     */
    @Test
    public void testFindByPersona() {
        UUID idPersona = UUID.randomUUID();
        List<MedioContacto> lista = List.of(mock(MedioContacto.class), mock(MedioContacto.class));
        when(em.createQuery(anyString(), eq(MedioContacto.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<MedioContacto> result = dao.findByPersona(idPersona, 0, 10);

        verify(query).setParameter("idPersona", idPersona);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByPersonaNulo() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByPersona(null, 0, 10));
        verifyNoInteractions(em);
    }

    /**
     * Test of countByPersona method, of class MedioContactoDAO.
     */
    @Test
    public void testCountByPersona() {
        UUID idPersona = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(4L);

        assertEquals(4L, dao.countByPersona(idPersona));
        verify(longQuery).setParameter("idPersona", idPersona);
    }

    @Test
    public void testCountByPersonaNulo() {
        assertEquals(0L, dao.countByPersona(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of existePersonaValor method, of class MedioContactoDAO.
     */
    @Test
    public void testExistePersonaValor() {
        UUID idPersona = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existePersonaValor(idPersona, "correo@dominio.com", null));
        verify(longQuery).setParameter("per", idPersona);
        verify(longQuery).setParameter("val", "correo@dominio.com");
    }

    @Test
    public void testExistePersonaValorNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existePersonaValor(UUID.randomUUID(), "otro@dominio.com", null));
    }

    @Test
    public void testExistePersonaValorInvalido() {
        assertFalse(dao.existePersonaValor(null, "correo@dominio.com", null));
        assertFalse(dao.existePersonaValor(UUID.randomUUID(), null, null));
        assertFalse(dao.existePersonaValor(UUID.randomUUID(), "   ", null));
        verifyNoInteractions(em);
    }

    @Test
    public void testExistePersonaValorConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existePersonaValor(UUID.randomUUID(), "correo@dominio.com", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of countByTipo method, of class MedioContactoDAO.
     */
    @Test
    public void testCountByTipo() {
        UUID idTipo = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(11L);

        assertEquals(11L, dao.countByTipo(idTipo));
        verify(longQuery).setParameter("id", idTipo);
    }

    @Test
    public void testCountByTipoNulo() {
        assertEquals(0L, dao.countByTipo(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class MedioContactoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
