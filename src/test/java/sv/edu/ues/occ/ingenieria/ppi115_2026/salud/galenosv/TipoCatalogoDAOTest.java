package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoCatalogoDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<TipoDocumento> docQuery;
    @Mock TypedQuery<TipoExamen> exQuery;
    @Mock TypedQuery<Long> countQuery;
    TipoDocumentoDAO docDao;
    TipoExamenDAO exDao;

    @BeforeEach void setUp() {
        docDao = new TipoDocumentoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        exDao = new TipoExamenDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void tipoDocumento_existeNombre_ok() {
        when(em.createQuery(contains("TipoDocumento e WHERE"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(docDao.existeNombre("DUI", null));
    }

    @Test void tipoDocumento_existeNombre_nulo() {
        assertFalse(docDao.existeNombre(null, null));
    }

    @Test void tipoDocumento_findActivos_ok() {
        when(em.createQuery(contains("t.activo = TRUE"), eq(TipoDocumento.class))).thenReturn(docQuery);
        when(docQuery.getResultList()).thenReturn(List.of(new TipoDocumento()));
        assertEquals(1, docDao.findActivos().size());
    }

    @Test void tipoExamen_existeNombre_ok() {
        when(em.createQuery(contains("TipoExamen e WHERE"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        assertFalse(exDao.existeNombre("Rx", null));
    }

    @Test void tipoExamen_findActivos_ok() {
        when(em.createQuery(contains("t.activo = TRUE"), eq(TipoExamen.class))).thenReturn(exQuery);
        when(exQuery.getResultList()).thenReturn(List.of(new TipoExamen()));
        assertEquals(1, exDao.findActivos().size());
    }

    @Test void tipoExamen_findActivosByNombreLike_ok() {
        when(em.createQuery(contains("UPPER(t.nombre)"), eq(TipoExamen.class))).thenReturn(exQuery);
        when(exQuery.setParameter(anyString(), any())).thenReturn(exQuery);
        when(exQuery.setFirstResult(anyInt())).thenReturn(exQuery);
        when(exQuery.setMaxResults(anyInt())).thenReturn(exQuery);
        when(exQuery.getResultList()).thenReturn(List.of(new TipoExamen()));
        assertEquals(1, exDao.findActivosByNombreLike("sangre", 0, 5).size());
    }

    @Test void tipoExamen_findActivosByNombreLike_invalido() {
        assertThrows(IllegalArgumentException.class, () -> exDao.findActivosByNombreLike("ab", 0, 5));
    }
}
