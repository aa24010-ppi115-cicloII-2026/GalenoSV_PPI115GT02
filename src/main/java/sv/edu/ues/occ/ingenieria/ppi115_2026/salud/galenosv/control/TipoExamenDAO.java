/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

/**
 *
 * @author duran
 */
@Stateless
public class TipoExamenDAO extends DefaultDAO<TipoExamen> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public TipoExamenDAO() {
        super(TipoExamen.class);
    }

    public List<TipoExamen> findActivosByNombreLike(String filtro, int first, int max) {
        if (filtro == null || filtro.trim().length() < 3 || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parametros invalidos para buscar tipo de examen");
        }
        return getEntityManager().createQuery(
                "SELECT t FROM TipoExamen t WHERE t.activo = TRUE "
                        + "AND UPPER(t.nombre) LIKE :nombre ORDER BY t.nombre", TipoExamen.class)
                .setParameter("nombre", "%" + filtro.trim().toUpperCase() + "%")
                .setFirstResult(first).setMaxResults(max).getResultList();
    }

    public List<TipoExamen> findActivos() {
        return getEntityManager().createQuery(
                "SELECT t FROM TipoExamen t WHERE t.activo = TRUE ORDER BY t.nombre",
                TipoExamen.class).getResultList();
    }


    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(e) FROM TipoExamen e WHERE LOWER(e.nombre) = LOWER(:nom)" + (excluirId == null ? "" : " AND e.idTipoExamen <> :excluir");
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
