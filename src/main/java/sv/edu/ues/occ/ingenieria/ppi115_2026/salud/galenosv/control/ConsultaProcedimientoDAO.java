/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;

/**
 *
 * @author duran
 */
@Stateless
public class ConsultaProcedimientoDAO extends DefaultDAO<ConsultaProcedimiento> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ConsultaProcedimientoDAO() {
        super(ConsultaProcedimiento.class);
    }


    public java.util.List<ConsultaProcedimiento> findByConsulta(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM ConsultaProcedimiento e WHERE e.idConsulta.idConsulta = :id ORDER BY e.idConsultaProcedimiento", ConsultaProcedimiento.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public Long countByConsulta(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ConsultaProcedimiento e WHERE e.idConsulta.idConsulta = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }


    public Long countByProcedimiento(java.util.UUID idProcedimiento) {
        if (idProcedimiento == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ConsultaProcedimiento e WHERE e.idProcedimiento = :id", Long.class)
                .setParameter("id", idProcedimiento).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
