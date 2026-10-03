/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author duran
 */
@Stateless
public class ProcedimientoPasoDAO extends DefaultDAO<ProcedimientoPaso> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ProcedimientoPasoDAO() {
        super(ProcedimientoPaso.class);
    }


    public java.util.List<ProcedimientoPaso> findByProcedimiento(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM ProcedimientoPaso e WHERE e.idProcedimiento.idProcedimiento = :id ORDER BY e.idProcedimientoPaso", ProcedimientoPaso.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public java.util.List<ProcedimientoPaso> findInicialesByProcedimiento(java.util.UUID idProcedimiento) {
        if (idProcedimiento == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT p FROM ProcedimientoPaso p LEFT JOIN FETCH p.idRol r "
                        + "WHERE p.idProcedimiento.idProcedimiento = :procedimiento "
                        + "AND NOT EXISTS (SELECT s.idProcedimientoPasoSecuencia "
                        + "FROM ProcedimientoPasoSecuencia s WHERE s.idProcedimientoPaso = p) "
                        + "ORDER BY p.nombre", ProcedimientoPaso.class)
                .setParameter("procedimiento", idProcedimiento).getResultList();
    }

    public Long countByProcedimiento(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ProcedimientoPaso e WHERE e.idProcedimiento.idProcedimiento = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }


    public Long countByRol(java.util.UUID idRol) {
        if (idRol == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ProcedimientoPaso e WHERE e.idRol.idRol = :id", Long.class)
                .setParameter("id", idRol).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
