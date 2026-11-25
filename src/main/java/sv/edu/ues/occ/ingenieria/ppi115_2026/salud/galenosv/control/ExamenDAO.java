/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        return existeTexto("nombre", "idExamen", "nom", nombre, excluirId);
    }

    public List<Examen> findActivos() {
        return getEntityManager().createQuery(
                "SELECT e FROM Examen e WHERE e.activo = TRUE ORDER BY e.nombre", Examen.class)
                .getResultList();
    }

    public List<Examen> findActivosByNombreLike(String filtro, int first, int max) {
        if (filtro == null || filtro.trim().length() < 3 || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parametros invalidos para buscar examen");
        }
        return getEntityManager().createQuery(
                "SELECT e FROM Examen e WHERE e.activo = TRUE "
                        + "AND UPPER(e.nombre) LIKE :nombre ORDER BY e.nombre", Examen.class)
                .setParameter("nombre", "%" + filtro.trim().toUpperCase() + "%")
                .setFirstResult(first).setMaxResults(max).getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
