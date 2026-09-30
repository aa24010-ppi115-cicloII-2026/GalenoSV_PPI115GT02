/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author duran
 */
@Stateless
public class ProcedimientoDAO extends DefaultDAO<Procedimiento> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ProcedimientoDAO() {
        super(Procedimiento.class);
    }


    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(e) FROM Procedimiento e WHERE LOWER(e.nombre) = LOWER(:nom)" + (excluirId == null ? "" : " AND e.idProcedimiento <> :excluir");
        var q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("nom", nombre.trim());
        if (excluirId != null) {
            q.setParameter("excluir", excluirId);
        }
        return q.getSingleResult() > 0;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
