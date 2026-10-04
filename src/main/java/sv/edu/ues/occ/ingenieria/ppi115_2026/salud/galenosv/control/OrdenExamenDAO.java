/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

/**
 *
 * @author duran
 */
@Stateless
public class OrdenExamenDAO extends DefaultDAO<OrdenExamen> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public OrdenExamenDAO() {
        super(OrdenExamen.class);
    }


    public java.util.List<OrdenExamen> findByPaso(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM OrdenExamen e WHERE e.idConsultaProcedimientoPaso.idConsultaProcedimientoPaso = :id ORDER BY e.idOrdenExamen", OrdenExamen.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }


    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
