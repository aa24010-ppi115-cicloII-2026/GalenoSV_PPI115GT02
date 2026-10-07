/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;

/**
 *
 * @author duran
 */
@Stateless
public class ClinicaDAO extends DefaultDAO<Clinica> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ClinicaDAO() {
        super(Clinica.class);
    }

    public List<Clinica> findActivas() {
        return getEntityManager().createQuery(
                "SELECT c FROM Clinica c WHERE c.activo = TRUE ORDER BY c.nombre", Clinica.class)
                .getResultList();
    }


    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(e) FROM Clinica e WHERE LOWER(e.nombre) = LOWER(:nom)" + (excluirId == null ? "" : " AND e.idClinica <> :excluir");
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
