package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsultaFlujoDAOTest {
    @Mock EntityManager em;
    @Mock TypedQuery<Consulta> consultaQuery;
    @Mock TypedQuery<ConsultaProcedimiento> cpQuery;
    @Mock TypedQuery<ConsultaProcedimientoPaso> pasoQuery;
    @Mock TypedQuery<OrdenExamen> ordenQuery;
    @Mock TypedQuery<Long> countQuery;

    ConsultaDAO consultaDAO;
    ConsultaProcedimientoDAO cpDAO;
    ConsultaProcedimientoPasoDAO pasoDAO;
    OrdenExamenDAO ordenDAO;

    @BeforeEach void setUp() {
        consultaDAO = new ConsultaDAO() { @Override public EntityManager getEntityManager() { return em; } };
        cpDAO = new ConsultaProcedimientoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        pasoDAO = new ConsultaProcedimientoPasoDAO() { @Override public EntityManager getEntityManager() { return em; } };
        ordenDAO = new OrdenExamenDAO() { @Override public EntityManager getEntityManager() { return em; } };
    }

    @Test void consulta_countByPersonaRol_nulo_cero() {
        assertEquals(0L, consultaDAO.countByPersonaRol(null));
        verifyNoInteractions(em);
    }

    @Test void consulta_countByPersonaRol_ok() {
        when(em.createQuery(contains("COUNT(e) FROM Consulta"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("id"), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        assertEquals(2L, consultaDAO.countByPersonaRol(UUID.randomUUID()));
    }

    @Test void consulta_findByClinicaYFechas_nulo_vacio() {
        assertTrue(consultaDAO.findByClinicaYFechas(null, null, null, 0, 10).isEmpty());
    }

    @Test void consulta_findByClinicaYFechas_ok() {
        var desde = LocalDate.now().atStartOfDay(ZoneId.of("America/El_Salvador")).toOffsetDateTime();
        var hasta = desde.plusDays(1);
        when(em.createQuery(contains("JOIN FETCH c.idPersonaRol"), eq(Consulta.class))).thenReturn(consultaQuery);
        when(consultaQuery.setParameter(anyString(), any())).thenReturn(consultaQuery);
        when(consultaQuery.setFirstResult(anyInt())).thenReturn(consultaQuery);
        when(consultaQuery.setMaxResults(anyInt())).thenReturn(consultaQuery);
        when(consultaQuery.getResultList()).thenReturn(List.of(new Consulta()));
        assertEquals(1, consultaDAO.findByClinicaYFechas(UUID.randomUUID(), desde, hasta, 0, 10).size());
    }

    @Test void cp_findByConsulta_nulo_vacio() {
        assertTrue(cpDAO.findByConsulta(null).isEmpty());
    }

    @Test void cp_findByConsulta_ok() {
        when(em.createQuery(contains("idConsulta.idConsulta"), eq(ConsultaProcedimiento.class))).thenReturn(cpQuery);
        when(cpQuery.setParameter(eq("id"), any())).thenReturn(cpQuery);
        when(cpQuery.setMaxResults(anyInt())).thenReturn(cpQuery);
        when(cpQuery.getResultList()).thenReturn(List.of(new ConsultaProcedimiento()));
        assertEquals(1, cpDAO.findByConsulta(UUID.randomUUID()).size());
    }

    @Test void cp_countByConsulta_nulo_cero() {
        assertEquals(0L, cpDAO.countByConsulta(null));
    }

    @Test void paso_findByProcedimiento_nulo_vacio() {
        assertTrue(pasoDAO.findByProcedimiento(null).isEmpty());
    }

    @Test void paso_countByPersonaRol_nulo_cero() {
        assertEquals(0L, pasoDAO.countByPersonaRol(null));
    }

    @Test void orden_findByPaso_nulo_vacio() {
        assertTrue(ordenDAO.findByPaso(null).isEmpty());
    }

    @Test void orden_findByPaso_ok() {
        when(em.createQuery(contains("idConsultaProcedimientoPaso"), eq(OrdenExamen.class))).thenReturn(ordenQuery);
        when(ordenQuery.setParameter(eq("id"), any())).thenReturn(ordenQuery);
        when(ordenQuery.setMaxResults(anyInt())).thenReturn(ordenQuery);
        when(ordenQuery.getResultList()).thenReturn(List.of(new OrdenExamen()));
        assertEquals(1, ordenDAO.findByPaso(UUID.randomUUID()).size());
    }
}
