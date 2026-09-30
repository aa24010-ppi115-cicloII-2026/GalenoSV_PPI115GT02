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
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenTipoExamen;

/**
 *
 * @author duran
 */
@Stateless
public class ExamenTipoExamenDAO extends DefaultDAO<ExamenTipoExamen> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ExamenTipoExamenDAO() {
        super(ExamenTipoExamen.class);
    }
    @Override
    public List<ExamenTipoExamen> findAll() {
        try {
            return getEntityManager()
                    .createQuery("SELECT e FROM ExamenTipoExamen e LEFT JOIN FETCH e.idExamen LEFT JOIN FETCH e.idTipoExamen", ExamenTipoExamen.class)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden listar registros de examen tipo examen", ex);
        }
    }

    @Override
    public List<ExamenTipoExamen> findRange(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Parametros invalidos");
        }

        try {
            TypedQuery<ExamenTipoExamen> query = getEntityManager()
                    .createQuery("SELECT e FROM ExamenTipoExamen e LEFT JOIN FETCH e.idExamen LEFT JOIN FETCH e.idTipoExamen", ExamenTipoExamen.class);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden listar registros de examen tipo examen", ex);
        }
    }

    public List<ExamenTipoExamen> findByIdExamen(UUID idExamen, int first, int max) {
        if (idExamen == null || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parametros invalidos para buscar por examen");
        }

        try {
            TypedQuery<ExamenTipoExamen> query = getEntityManager()
                    .createNamedQuery("ExamenTipoExamen.findByIdExamen", ExamenTipoExamen.class);
            query.setParameter("idExamen", idExamen);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden buscar registros por examen", ex);
        }
    }

    public int countByIdExamen(UUID idExamen) {
        if (idExamen == null) {
            throw new IllegalArgumentException("idExamen no puede ser nulo");
        }

        try {
            TypedQuery<Long> query = getEntityManager()
                    .createNamedQuery("ExamenTipoExamen.countByIdExamen", Long.class);
            query.setParameter("idExamen", idExamen);

            return query.getSingleResult().intValue();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden contar registros por examen", ex);
        }
    }

    public boolean existeVinculo(java.util.UUID idExamen, java.util.UUID idTipo, java.util.UUID excluirId) {
        if (idExamen == null || idTipo == null) {
            return false;
        }
        String jpql = "SELECT COUNT(e) FROM ExamenTipoExamen e WHERE e.idExamen.idExamen = :exa AND e.idTipoExamen.idTipoExamen = :tip" + (excluirId == null ? "" : " AND e.idExamenTipoExamen <> :excluir");
        var q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("exa", idExamen);
        q.setParameter("tip", idTipo);
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
