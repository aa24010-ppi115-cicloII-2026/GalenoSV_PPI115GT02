/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

/**
 *
 * @author duran
 */
@Stateless
public class ConsultaProcedimientoPasoDAO extends DefaultDAO<ConsultaProcedimientoPaso> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ConsultaProcedimientoPasoDAO() {
        super(ConsultaProcedimientoPaso.class);
    }


    public java.util.List<ConsultaProcedimientoPaso> findByProcedimiento(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM ConsultaProcedimientoPaso e WHERE e.idConsultaProcedimiento.idConsultaProcedimiento = :id ORDER BY e.idConsultaProcedimientoPaso", ConsultaProcedimientoPaso.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public Long countByProcedimiento(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ConsultaProcedimientoPaso e WHERE e.idConsultaProcedimiento.idConsultaProcedimiento = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }


    public Long countByPersonaRol(java.util.UUID idPersonaRol) {
        if (idPersonaRol == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ConsultaProcedimientoPaso e WHERE e.idPersonaRol.idPersonaRol = :id", Long.class)
                .setParameter("id", idPersonaRol).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
