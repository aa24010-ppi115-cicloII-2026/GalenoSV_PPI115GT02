/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenResultado;

/**
 *
 * @author duran
 */
@Stateless
public class ExamenResultadoDAO extends DefaultDAO<ExamenResultado> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ExamenResultadoDAO() {
        super(ExamenResultado.class);
    }


    public java.util.List<ExamenResultado> findByOrden(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM ExamenResultado e WHERE e.idOrdenExamen.idOrdenExamen = :id ORDER BY e.idExamenResultado", ExamenResultado.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public Long countByOrden(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM ExamenResultado e WHERE e.idOrdenExamen.idOrdenExamen = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
