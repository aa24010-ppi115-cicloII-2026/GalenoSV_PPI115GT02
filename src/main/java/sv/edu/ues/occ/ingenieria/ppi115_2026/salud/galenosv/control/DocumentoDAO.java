package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collection;
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

    /**
     * Registros de varias personas en una sola consulta y con su tipo, para mostrarlos en una
     * lista sin consultar la base por cada fila.
     *
     * @param idsPersona personas de la lista; vacia o null no consulta
     */
    public List<Documento> findByPersonas(Collection<UUID> idsPersona) {
        if (idsPersona == null || idsPersona.isEmpty()) {
            return List.of();
        }
        return getEntityManager().createQuery(
                "SELECT d FROM Documento d LEFT JOIN FETCH d.idTipoDocumento "
                        + "WHERE d.idPersona.idPersona IN :ids ORDER BY d.valor", Documento.class)
                .setParameter("ids", idsPersona).getResultList();
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
        return existeTextoIgual("d", "valor", "idDocumento", "val", valor, excluirId);
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
