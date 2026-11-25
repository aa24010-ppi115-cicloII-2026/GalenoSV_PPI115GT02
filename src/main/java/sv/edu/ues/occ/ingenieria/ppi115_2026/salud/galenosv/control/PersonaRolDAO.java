package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        return existeConFiltros("p", "idPersonaRol", excluirId,
                new Filtro("idPersona.idPersona", "per", idPersona),
                new Filtro("idRol.idRol", "rol", idRol),
                new Filtro("idClinica.idClinica", "cli", idClinica));
    }


    public Long countByPersona(java.util.UUID idPersona) {
        if (idPersona == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(p) FROM PersonaRol p WHERE p.idPersona.idPersona = :id", Long.class)
                .setParameter("id", idPersona).getSingleResult();
    }

    public Long countByRol(java.util.UUID idRol) {
        if (idRol == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(p) FROM PersonaRol p WHERE p.idRol.idRol = :id", Long.class)
                .setParameter("id", idRol).getSingleResult();
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

    public List<PersonaRol> findActivosByClinica(UUID idClinica) {
        if (idClinica == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT DISTINCT p FROM PersonaRol p "
                        + "JOIN FETCH p.idPersona JOIN FETCH p.idRol r JOIN FETCH p.idClinica c "
                        + "WHERE c.idClinica = :clinica AND c.activo = TRUE AND r.activo = TRUE "
                        + "ORDER BY p.idPersona.apellidos, p.idPersona.nombres, r.nombre", PersonaRol.class)
                .setParameter("clinica", idClinica).getResultList();
    }

    /**
     * Personas con un rol activo en la clinica (activa) que cumplen lo escrito en el buscador:
     * nombre y apellido, numero de documento, nombre del rol o medio de contacto, sin distinguir
     * mayusculas. Los filtros vacios no se aplican. Filtra en la base y trae como maximo {@code max}.
     *
     * @param max cuantas devolver como maximo
     * @return ordenadas por apellido, nombre y rol; vacia si falta la clinica o el cupo
     */
    public List<PersonaRol> buscarAtendibles(UUID idClinica, String nombre, String documento,
            String rol, String medio, int max) {
        Map<String, String> patrones = new LinkedHashMap<>();
        agregarPatron(patrones, "nombre", nombre);
        agregarPatron(patrones, "documento", documento);
        agregarPatron(patrones, "rol", rol);
        agregarPatron(patrones, "medio", medio);
        if (idClinica == null || max <= 0) {
            return List.of();
        }
        StringBuilder jpql = new StringBuilder("SELECT p FROM PersonaRol p JOIN FETCH p.idPersona pe "
                + "JOIN FETCH p.idRol r JOIN FETCH p.idClinica c "
                + "WHERE c.idClinica = :clinica AND c.activo = TRUE AND r.activo = TRUE");
        if (patrones.containsKey("nombre")) {
            jpql.append(" AND LOWER(CONCAT(pe.nombres, ' ', pe.apellidos)) LIKE :nombre ESCAPE '!'");
        }
        if (patrones.containsKey("documento")) {
            jpql.append(" AND EXISTS (SELECT d FROM Documento d WHERE d.idPersona = pe"
                    + " AND LOWER(d.valor) LIKE :documento ESCAPE '!')");
        }
        if (patrones.containsKey("rol")) {
            jpql.append(" AND LOWER(r.nombre) LIKE :rol ESCAPE '!'");
        }
        if (patrones.containsKey("medio")) {
            jpql.append(" AND EXISTS (SELECT m FROM MedioContacto m WHERE m.idPersona = pe"
                    + " AND LOWER(m.valor) LIKE :medio ESCAPE '!')");
        }
        jpql.append(" ORDER BY pe.apellidos, pe.nombres, r.nombre");
        TypedQuery<PersonaRol> query = getEntityManager().createQuery(jpql.toString(), PersonaRol.class)
                .setParameter("clinica", idClinica);
        patrones.forEach(query::setParameter);
        return query.setMaxResults(max).getResultList();
    }

    /** Patron "contiene" en minusculas; los comodines de LIKE que escriba el usuario cuentan como texto. */
    private static void agregarPatron(Map<String, String> patrones, String parametro, String valor) {
        if (valor != null && !valor.isBlank()) {
            String texto = valor.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
            patrones.put(parametro, "%" + texto + "%");
        }
    }

    public List<PersonaRol> findResponsables(UUID idClinica, UUID idRol) {
        if (idClinica == null || idRol == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT p FROM PersonaRol p JOIN FETCH p.idPersona JOIN FETCH p.idRol r JOIN FETCH p.idClinica c "
                        + "WHERE c.idClinica = :clinica AND r.idRol = :rol "
                        + "AND c.activo = TRUE AND r.activo = TRUE ORDER BY p.fechaCreacion, p.idPersonaRol",
                PersonaRol.class)
                .setParameter("clinica", idClinica).setParameter("rol", idRol).getResultList();
    }

    public Long countByClinica(UUID id) {
        return getEntityManager().createQuery(
                "SELECT COUNT(p) FROM PersonaRol p WHERE p.idClinica.idClinica = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

}
