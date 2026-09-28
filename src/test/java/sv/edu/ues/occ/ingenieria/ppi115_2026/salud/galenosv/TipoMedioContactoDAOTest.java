package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoMedioContactoDAOTest {

    @Mock
    EntityManager em;

    @Mock
    Query query;

    TipoMedioContactoDAO dao;

    @BeforeEach
    void setUp() {
        dao = new TipoMedioContactoDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
    }

    @Test
    void testFindByNombreNative_ok() {
        TipoMedioContacto t = new TipoMedioContacto();
        t.setIdTipoMedioContacto(UUID.randomUUID());
        t.setNombre("Telefono");

        // Mock del TypedQuery / Query para prueba unitaria (stub)
        when(em.createNativeQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(t));

        List<TipoMedioContacto> result = dao.buscarPorNombreNativo("Telefono");

        assertEquals(1, result.size());
        assertEquals("Telefono", result.get(0).getNombre());
        verify(em).createNativeQuery(contains("tipo_medio_contacto"), eq(TipoMedioContacto.class));
        verify(query).setParameter("nombre", "Telefono");
    }

    @Test
    void testFindByNombreNative_parametroInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreNativo(null));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreNativo(""));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombreNativo("   "));
    }

    @Test
    void testFindFirstByNombre_conMockitoStub() {
        TipoMedioContacto t = new TipoMedioContacto();
        t.setIdTipoMedioContacto(UUID.randomUUID());
        t.setNombre("Email");

        when(em.createNativeQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(t));

        TipoMedioContacto result = dao.buscarPrimeroPorNombre("Email");
        assertNotNull(result);
        assertEquals("Email", result.getNombre());
        // Verifica cobertura del bucle y try
        verify(query).getResultList();
    }

    @Test
    void testFindFirstByNombre_noEncontrado_retornaNull() {
        when(em.createNativeQuery(anyString(), eq(TipoMedioContacto.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertNull(dao.buscarPrimeroPorNombre("NoExiste"));
    }

    @Test
    void testEntityImplementsSerializable() {
        TipoMedioContacto e = new TipoMedioContacto();
        assertTrue(e instanceof java.io.Serializable);
    }
}
