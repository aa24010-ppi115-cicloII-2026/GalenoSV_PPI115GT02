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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TipoDocumentoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<TipoDocumento> query;

    @Mock
    TypedQuery<Long> longQuery;

    TipoDocumentoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new TipoDocumentoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of existeNombre method, of class TipoDocumentoDAO.
     */
    @Test
    public void testExisteNombre() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeNombre("DUI", null));
        verify(longQuery).setParameter("nom", "DUI");
    }

    @Test
    public void testExisteNombreNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Pasaporte", null));
    }

    @Test
    public void testExisteNombreInvalido() {
        assertFalse(dao.existeNombre(null, null));
        assertFalse(dao.existeNombre("   ", null));
        verifyNoInteractions(em);
    }

    @Test
    public void testExisteNombreConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("DUI", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of findActivos method, of class TipoDocumentoDAO.
     */
    @Test
    public void testFindActivos() {
        List<TipoDocumento> lista = List.of(mock(TipoDocumento.class));
        when(em.createQuery(anyString(), eq(TipoDocumento.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<TipoDocumento> result = dao.findActivos();

        verify(em).createQuery(contains("activo"), eq(TipoDocumento.class));
        assertEquals(lista, result);
    }

    /**
     * Test of getEntityManager method, of class TipoDocumentoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
