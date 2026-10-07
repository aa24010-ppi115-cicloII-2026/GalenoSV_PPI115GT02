package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcedimientoFlujoDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<ProcedimientoPaso> pasoQuery;
    @Mock TypedQuery<ProcedimientoPasoExamen> exQuery;
    @Mock TypedQuery<ProcedimientoPasoSecuencia> secQuery;
    @Mock TypedQuery<Long> countQuery;

    ProcedimientoPasoDAO pasoDAO;
    ProcedimientoPasoExamenDAO exDAO;
    ProcedimientoPasoSecuenciaDAO secDAO;

    @BeforeEach void setUp() {
        pasoDAO = new ProcedimientoPasoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        exDAO = new ProcedimientoPasoExamenDAO() { @Override public EntityManager getEntityManager() { return em; } };
        secDAO = new ProcedimientoPasoSecuenciaDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void findByProcedimiento_nulo_vacio() {
        assertTrue(pasoDAO.findByProcedimiento(null).isEmpty());
    }

    @Test void findByProcedimiento_ok() {
        when(em.createQuery(contains("idProcedimiento.idProcedimiento"), eq(ProcedimientoPaso.class))).thenReturn(pasoQuery);
        when(pasoQuery.setParameter(eq("id"), any())).thenReturn(pasoQuery);
        when(pasoQuery.setMaxResults(anyInt())).thenReturn(pasoQuery);
        when(pasoQuery.getResultList()).thenReturn(List.of(new ProcedimientoPaso()));
        assertEquals(1, pasoDAO.findByProcedimiento(UUID.randomUUID()).size());
    }

    @Test void countByProcedimiento_nulo_cero() {
        assertEquals(0L, pasoDAO.countByProcedimiento(null));
    }

    @Test void countByRol_nulo_cero() {
        assertEquals(0L, pasoDAO.countByRol(null));
    }

    @Test void findIniciales_nulo_vacio() {
        assertTrue(pasoDAO.findInicialesByProcedimiento(null).isEmpty());
    }

    @Test void ex_findByPaso_nulo_vacio() {
        assertTrue(exDAO.findByPaso(null).isEmpty());
    }

    @Test void ex_countByExamen_nulo_cero() {
        assertEquals(0L, exDAO.countByExamen(null));
    }

    @Test void ex_countByExamen_ok() {
        when(em.createQuery(contains("COUNT(e) FROM ProcedimientoPasoExamen"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("id"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        assertEquals(2L, exDAO.countByExamen(UUID.randomUUID()));
    }

    @Test void sec_findByPaso_nulo_vacio() {
        assertTrue(secDAO.findByPaso(null).isEmpty());
    }

    @Test void sec_findDependientes_nulo_vacio() {
        assertTrue(secDAO.findDependientes(null).isEmpty());
    }

    @Test void sec_findDependientes_ok() {
        when(em.createQuery(contains("ProcedimientoPasoReferencia"), eq(ProcedimientoPasoSecuencia.class))).thenReturn(secQuery);
        when(secQuery.setParameter(eq("id"), any())).thenReturn(secQuery);
        when(secQuery.setMaxResults(anyInt())).thenReturn(secQuery);
        when(secQuery.getResultList()).thenReturn(List.of(new ProcedimientoPasoSecuencia()));
        assertEquals(1, secDAO.findDependientes(UUID.randomUUID()).size());
    }
}
