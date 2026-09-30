/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author duran
 */
@Stateless
public class ProcedimientoPasoExamenDAO extends DefaultDAO<ProcedimientoPasoExamen> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ProcedimientoPasoExamenDAO() {
        super(ProcedimientoPasoExamen.class);
    }


    public java.util.List<ProcedimientoPasoExamen> findByPaso(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM ProcedimientoPasoExamen e WHERE e.idProcedimientoPaso.idProcedimientoPaso = :id ORDER BY e.idProcedimientoPasoExamen", ProcedimientoPasoExamen.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public Long countByPaso(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ProcedimientoPasoExamen e WHERE e.idProcedimientoPaso.idProcedimientoPaso = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }


    public Long countByExamen(java.util.UUID idExamen) {
        if (idExamen == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ProcedimientoPasoExamen e WHERE e.idExamen.idExamen = :id", Long.class)
                .setParameter("id", idExamen).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
