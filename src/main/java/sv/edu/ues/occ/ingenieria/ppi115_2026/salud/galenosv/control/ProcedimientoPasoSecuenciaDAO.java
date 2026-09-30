/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author duran
 */
@Stateless
public class ProcedimientoPasoSecuenciaDAO extends DefaultDAO<ProcedimientoPasoSecuencia> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ProcedimientoPasoSecuenciaDAO() {
        super(ProcedimientoPasoSecuencia.class);
    }


    public java.util.List<ProcedimientoPasoSecuencia> findByPaso(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM ProcedimientoPasoSecuencia e WHERE e.idProcedimientoPaso.idProcedimientoPaso = :id ORDER BY e.idProcedimientoPasoSecuencia", ProcedimientoPasoSecuencia.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public Long countByPaso(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ProcedimientoPasoSecuencia e WHERE e.idProcedimientoPaso.idProcedimientoPaso = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
