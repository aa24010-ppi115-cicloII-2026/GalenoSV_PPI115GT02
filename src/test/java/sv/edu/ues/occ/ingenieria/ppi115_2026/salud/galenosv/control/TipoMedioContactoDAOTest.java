package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TipoMedioContactoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<TipoMedioContacto> query;

    @Mock
    TypedQuery<Long> longQuery;

    @Mock
    Query nativeQuery;

    TipoMedioContactoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new TipoMedioContactoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of existeNombre method, of class TipoMedioContactoDAO.
     */
    @Test
    public void testExisteNombre() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeNombre("Telefono", null));
        verify(longQuery).setParameter("nom", "Telefono");
    }

    @Test
    public void testExisteNombreNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Correo", null));
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

        assertFalse(dao.existeNombre("Telefono", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of findActivos method, of class TipoMedioContactoDAO.
     */
    @Test
    public void testFindActivos() {
        List<TipoMedioContacto> lista = List.of(mock(TipoMedioContacto.class));
        when(em.createQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<TipoMedioContacto> result = dao.findActivos();

        verify(em).createQuery(contains("activo"), eq(TipoMedioContacto.class));
        assertEquals(lista, result);
    }

    /**
     * Test of getEntityManager method, of class TipoMedioContactoDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

    /**
     * Test of buscarPorNombreNativo method, of class TipoMedioContactoDAO.
     */
    @Test
    public void testBuscarPorNombreNativo() {
        List<TipoMedioContacto> lista = List.of(mock(TipoMedioContacto.class));
        when(em.createNativeQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(lista);

        List<TipoMedioContacto> result = dao.buscarPorNombreNativo("Telefono");

        verify(nativeQuery).setParameter("nombre", "Telefono");
        assertEquals(lista, result);
    }

    @Test
    public void testBuscarPorNombreNativoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreNativo(null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreNativo("   "));
        verifyNoInteractions(em);
    }

    /**
     * Test of buscarPrimeroPorNombre method, of class TipoMedioContactoDAO.
     */
    @Test
    public void testBuscarPrimeroPorNombre() {
        TipoMedioContacto encontrado = mock(TipoMedioContacto.class);
        when(em.createNativeQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.setMaxResults(anyInt())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of(encontrado));

        TipoMedioContacto result = dao.buscarPrimeroPorNombre("Telefono");

        verify(nativeQuery).setMaxResults(1);
        assertSame(encontrado, result);
    }

    @Test
    public void testBuscarPrimeroPorNombreVacio() {
        when(em.createNativeQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.setMaxResults(anyInt())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());

        assertNull(dao.buscarPrimeroPorNombre("Telefono"));
    }

    @Test
    public void testBuscarPrimeroPorNombreInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPrimeroPorNombre(null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPrimeroPorNombre("   "));
        verifyNoInteractions(em);
    }

}
