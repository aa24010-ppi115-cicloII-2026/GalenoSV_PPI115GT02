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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author david
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RolDAOTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<Rol> query;

    @Mock
    TypedQuery<Long> longQuery;

    RolDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new RolDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    /**
     * Test of existeNombre method, of class RolDAO.
     */
    @Test
    public void testExisteNombre() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeNombre("Administrador", null));
        verify(longQuery).setParameter("nom", "Administrador");
    }

    @Test
    public void testExisteNombreNoExiste() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeNombre("Paciente", null));
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

        assertFalse(dao.existeNombre("Administrador", excluir));
        verify(longQuery).setParameter("excluir", excluir);
    }

    /**
     * Test of findActivos method, of class RolDAO.
     */
    @Test
    public void testFindActivos() {
        List<Rol> lista = List.of(mock(Rol.class));
        when(em.createQuery(anyString(), eq(Rol.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Rol> result = dao.findActivos();

        verify(em).createQuery(contains("activo"), eq(Rol.class));
        assertEquals(lista, result);
    }

    /**
     * Test of findActivosByNombreLike method, of class RolDAO.
     */
    @Test
    public void testFindActivosByNombreLike() {
        List<Rol> lista = List.of(mock(Rol.class));
        when(em.createQuery(anyString(), eq(Rol.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<Rol> result = dao.findActivosByNombreLike("admin", 0, 10);

        verify(query).setParameter("nombre", "%ADMIN%");
        assertEquals(lista, result);
    }

    @Test
    public void testFindActivosByNombreLikeParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("ab", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("admin", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findActivosByNombreLike("admin", 0, 0));
        verifyNoInteractions(em);
    }

    /**
     * Test of getEntityManager method, of class RolDAO.
     */
    @Test
    public void testGetEntityManager() {
        assertSame(em, dao.getEntityManager());
    }

}
