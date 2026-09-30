package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;

@Stateless
public class DocumentoDAO extends DefaultDAO<Documento> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public DocumentoDAO() {
        super(Documento.class);
    }

    public List<Documento> buscarPorValor(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                throw new IllegalArgumentException("valor no puede ser nulo");
            }
            String jpql = "SELECT d FROM Documento d WHERE d.valor = :valor";
            TypedQuery<Documento> q = getEntityManager().createQuery(jpql, Documento.class);
            q.setParameter("valor", valor.trim());
            return q.getResultList();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorValor", ex);
        }
    }

    public List<Documento> findByPersona(UUID idPersona, int first, int pageSize) {
        if (idPersona == null) {
            throw new IllegalArgumentException("La persona es requerida");
        }
        TypedQuery<Documento> query = getEntityManager().createQuery(
                "SELECT d FROM Documento d WHERE d.idPersona.idPersona = :idPersona ORDER BY d.valor",
                Documento.class);
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
                "SELECT COUNT(d) FROM Documento d WHERE d.idPersona.idPersona = :idPersona",
                Long.class);
        query.setParameter("idPersona", idPersona);
        return query.getSingleResult();
    }


    public boolean existeValor(String valor, java.util.UUID excluirId) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(d) FROM Documento d WHERE d.valor = :val" + (excluirId == null ? "" : " AND d.idDocumento <> :excluir");
        var q = getEntityManager().createQuery(jpql, Long.class);
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
                "SELECT COUNT(d) FROM Documento d WHERE d.idTipoDocumento.idTipoDocumento = :id", Long.class)
                .setParameter("id", idTipo).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
