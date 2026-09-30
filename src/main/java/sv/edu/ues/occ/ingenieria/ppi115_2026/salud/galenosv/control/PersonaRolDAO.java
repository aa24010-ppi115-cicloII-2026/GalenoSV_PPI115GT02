package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

@Stateless
public class PersonaRolDAO extends DefaultDAO<PersonaRol> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public PersonaRolDAO() {
        super(PersonaRol.class);
    }

    @Override
    public List<PersonaRol> findAll() {
        try {
            return getEntityManager()
                    .createQuery("SELECT p FROM PersonaRol p LEFT JOIN FETCH p.idPersona LEFT JOIN FETCH p.idRol LEFT JOIN FETCH p.idClinica", PersonaRol.class)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden listar persona rol", ex);
        }
    }

    @Override
    public List<PersonaRol> findRange(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Parametros invalidos");
        }

        try {
            TypedQuery<PersonaRol> query = getEntityManager()
                    .createQuery("SELECT p FROM PersonaRol p LEFT JOIN FETCH p.idPersona LEFT JOIN FETCH p.idRol LEFT JOIN FETCH p.idClinica", PersonaRol.class);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden listar persona rol", ex);
        }
    }


    public boolean existeAsignacion(java.util.UUID idPersona, java.util.UUID idRol, java.util.UUID idClinica, java.util.UUID excluirId) {
        if (idPersona == null || idRol == null || idClinica == null) {
            return false;
        }
        String jpql = "SELECT COUNT(p) FROM PersonaRol p WHERE p.idPersona.idPersona = :per AND p.idRol.idRol = :rol AND p.idClinica.idClinica = :cli" + (excluirId == null ? "" : " AND p.idPersonaRol <> :excluir");
        var q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("per", idPersona);
        q.setParameter("rol", idRol);
        q.setParameter("cli", idClinica);
        if (excluirId != null) {
            q.setParameter("excluir", excluirId);
        }
        return q.getSingleResult() > 0;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<PersonaRol> findByPersona(UUID idPersona) {
        return getEntityManager().createQuery(
                "SELECT p FROM PersonaRol p JOIN FETCH p.idPersona JOIN FETCH p.idRol JOIN FETCH p.idClinica WHERE p.idPersona.idPersona = :id ORDER BY p.fechaCreacion DESC",
                PersonaRol.class).setParameter("id", idPersona)
                .setMaxResults(1000).getResultList();
    }

    public List<PersonaRol> findByClinica(UUID id, int first, int max) {       return getEntityManager().createQuery(
                "SELECT p FROM PersonaRol p JOIN FETCH p.idPersona JOIN FETCH p.idRol JOIN FETCH p.idClinica WHERE p.idClinica.idClinica = :id ORDER BY p.idPersona.apellidos, p.idPersonaRol",
                PersonaRol.class).setParameter("id", id)
                .setFirstResult(first).setMaxResults(max).getResultList();
    }

    public Long countByClinica(UUID id) {
        return getEntityManager().createQuery(
                "SELECT COUNT(p) FROM PersonaRol p WHERE p.idClinica.idClinica = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    public List<PersonaRol> findPacientes() {
        return getEntityManager().createQuery(
                "SELECT p FROM PersonaRol p JOIN FETCH p.idPersona JOIN FETCH p.idRol JOIN FETCH p.idClinica WHERE LOWER(TRIM(p.idRol.nombre)) = :rol ORDER BY p.idPersona.apellidos",
                PersonaRol.class).setParameter("rol", "paciente").getResultList();
    }
}
