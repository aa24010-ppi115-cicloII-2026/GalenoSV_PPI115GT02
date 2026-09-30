package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;

@Stateless
public class MedioContactoDAO extends DefaultDAO<MedioContacto> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public MedioContactoDAO() {
        super(MedioContacto.class);
    }

    public List<MedioContacto> findByPersona(UUID idPersona, int first, int pageSize) {
        if (idPersona == null) {
            throw new IllegalArgumentException("La persona es requerida");
        }
        TypedQuery<MedioContacto> query = getEntityManager().createQuery(
                "SELECT m FROM MedioContacto m WHERE m.idPersona.idPersona = :idPersona ORDER BY m.fechaCreacion DESC",
                MedioContacto.class);
        query.setParameter("idPersona", idPersona);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    public Long countByPersona(UUID idPersona) {
        if (idPersona == null) {
            return 0L;
        }
        TypedQuery<Long> query = getEntityManager().createQuery(
                "SELECT COUNT(m) FROM MedioContacto m WHERE m.idPersona.idPersona = :idPersona",
                Long.class);
        query.setParameter("idPersona", idPersona);
        return query.getSingleResult();
    }


    public boolean existePersonaValor(java.util.UUID idPersona, String valor, java.util.UUID excluirId) {
        if (idPersona == null || valor == null || valor.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(m) FROM MedioContacto m WHERE m.idPersona.idPersona = :per AND m.valor = :val" + (excluirId == null ? "" : " AND m.idMedioContacto <> :excluir");
        var q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("per", idPersona);
        q.setParameter("val", valor.trim());
        if (excluirId != null) {
            q.setParameter("excluir", excluirId);
        }
        return q.getSingleResult() > 0;
    }


    public Long countByTipo(java.util.UUID idTipo) {
        if (idTipo == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(m) FROM MedioContacto m WHERE m.idTipoMedioContacto.idTipoMedioContacto = :id", Long.class)
                .setParameter("id", idTipo).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
