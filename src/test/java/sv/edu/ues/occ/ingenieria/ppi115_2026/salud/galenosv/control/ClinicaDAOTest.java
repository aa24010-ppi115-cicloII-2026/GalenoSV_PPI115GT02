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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ClinicaDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Clinica> query;

    @Mock
    TypedQuery<Long> longQuery;

    ClinicaDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new ClinicaDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findActivas method, of class ClinicaDAO.
     */
    @Test
    public void testFindActivas() {
        List<Clinica> lista = List.of(mock(Clinica.class), mock(Clinica.class));
        when(em.createQuery(anyString(), eq(Clinica.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Clinica> result = dao.findActivas();

        verify(em).createQuery(contains("Clinica"), eq(Clinica.class));
        assertEquals(lista, result);
        assertEquals(2, result.size());
    }

    /**
     * Test of existeNombre method, of class ClinicaDAO.
     */
    @Test
    public void testExisteNombre() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        boolean result = dao.existeNombre("Clinica Uno", null);

        verify(longQuery).setParameter("nom", "Clinica Uno");
        assertTrue(result);
    }

    @Test
    public void testExisteNombreNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Clinica Dos", null));
    }

    @Test
    public void testExisteNombreConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Clinica", excluir));

        verify(longQuery).setParameter("excluir", excluir);
    }

    @Test
    public void testExisteNombreInvalido() {
        assertFalse(dao.existeNombre(null, null));
        assertFalse(dao.existeNombre("   ", null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class ClinicaDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
