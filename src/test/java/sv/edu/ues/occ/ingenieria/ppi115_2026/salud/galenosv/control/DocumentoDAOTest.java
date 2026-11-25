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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DocumentoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Documento> query;

    @Mock
    TypedQuery<Long> longQuery;

    DocumentoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new DocumentoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of findByPersona method, of class DocumentoDAO.
     */
    @Test
    public void testFindByPersona() {
        UUID idPersona = UUID.randomUUID();
        List<Documento> lista = List.of(mock(Documento.class), mock(Documento.class));
        when(em.createQuery(anyString(), eq(Documento.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Documento> result = dao.findByPersona(idPersona, 0, 10);

        verify(query).setParameter("idPersona", idPersona);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByPersonaNulo() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByPersona(null, 0, 10));
        verifyNoInteractions(em);
    }

    /**
     * Test of countByPersona method, of class DocumentoDAO.
     */
    @Test
    public void testCountByPersona() {
        UUID idPersona = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(3L);

        assertEquals(3L, dao.countByPersona(idPersona));
        verify(longQuery).setParameter("idPersona", idPersona);
    }

    @Test
    public void testCountByPersonaNulo() {
        assertEquals(0L, dao.countByPersona(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of existeValor method, of class DocumentoDAO.
     */
    @Test
    public void testExisteValor() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeValor("12345678", null));
        verify(longQuery).setParameter("val", "12345678");
    }

    @Test
    public void testExisteValorNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeValor("00000000", null));
    }

    @Test
    public void testExisteValorInvalido() {
        assertFalse(dao.existeValor(null, null));
        assertFalse(dao.existeValor("   ", null));
        verifyNoInteractions(em);
    }

    @Test
    public void testExisteValorConExcluir() {
        UUID excluir = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeValor("12345678", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of countByTipo method, of class DocumentoDAO.
     */
    @Test
    public void testCountByTipo() {
        UUID idTipo = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(9L);

        assertEquals(9L, dao.countByTipo(idTipo));
        verify(longQuery).setParameter("id", idTipo);
    }

    @Test
    public void testCountByTipoNulo() {
        assertEquals(0L, dao.countByTipo(null));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class DocumentoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }


    @Test
    public void testFindByPersonasSinPersonasNoConsulta() {
        assertTrue(dao.findByPersonas(List.of()).isEmpty());
        assertTrue(dao.findByPersonas(null).isEmpty());
        verify(em, never()).createQuery(anyString(), eq(Documento.class));
    }

    @Test
    public void testFindByPersonasTraeLosDeTodasEnUnaConsultaConSuTipo() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<Documento> esperado = List.of(new Documento(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(Documento.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findByPersonas(ids));
        verify(em).createQuery(contains("d.idPersona.idPersona IN :ids"), eq(Documento.class));
        verify(em).createQuery(contains("LEFT JOIN FETCH d.idTipoDocumento"), eq(Documento.class));
        verify(query).setParameter("ids", ids);
    }
}
