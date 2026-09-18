/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;

/**
 *
 * @author duran
 */
@Stateless
public class ExamenDAO extends DefaultDAO<Examen> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ExamenDAO() {
        super(Examen.class);
    }

    public List<Examen> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("nombre no puede ser nulo");
        }

        try {
            String jpql = "SELECT e FROM Examen e WHERE e.nombre = :nombre";
            TypedQuery<Examen> q = getEntityManager().createQuery(jpql, Examen.class);
            q.setParameter("nombre", nombre.trim());
            return q.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorNombre", ex);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
