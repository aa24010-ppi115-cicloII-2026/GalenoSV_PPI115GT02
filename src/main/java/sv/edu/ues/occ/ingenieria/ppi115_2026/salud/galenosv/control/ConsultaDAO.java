/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author duran
 */
@Stateless
public class ConsultaDAO extends DefaultDAO<Consulta> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ConsultaDAO() {
        super(Consulta.class);
    }

    @Override
    public List<Consulta> findAll() {
        try {
            return getEntityManager()
                    .createQuery("SELECT c FROM Consulta c LEFT JOIN FETCH c.idPersonaRol pr LEFT JOIN FETCH pr.idPersona LEFT JOIN FETCH pr.idRol", Consulta.class)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden listar consultas", ex);
        }
    }

    @Override
    public List<Consulta> findRange(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Parametros invalidos");
        }

        try {
            TypedQuery<Consulta> query = getEntityManager()
                    .createQuery("SELECT c FROM Consulta c LEFT JOIN FETCH c.idPersonaRol pr LEFT JOIN FETCH pr.idPersona LEFT JOIN FETCH pr.idRol", Consulta.class);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden listar consultas", ex);
        }
    }


    public java.util.List<Consulta> findByPersonaRol(java.util.UUID id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT e FROM Consulta e WHERE e.idPersonaRol.idPersonaRol = :id ORDER BY e.idConsulta", Consulta.class)
                .setParameter("id", id).setMaxResults(1000).getResultList();
    }

    public Long countByPersonaRol(java.util.UUID id) {
        if (id == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(e) FROM Consulta e WHERE e.idPersonaRol.idPersonaRol = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }





    public List<Consulta> findByClinicaYFechas(UUID idClinica, OffsetDateTime desde,
            OffsetDateTime hasta, int first, int max) {
        if (idClinica == null || desde == null || hasta == null || first < 0 || max <= 0) {
            return java.util.Collections.emptyList();
        }
        return getEntityManager().createQuery(
                "SELECT c FROM Consulta c JOIN FETCH c.idPersonaRol pr "
                        + "JOIN FETCH pr.idPersona JOIN FETCH pr.idRol JOIN FETCH pr.idClinica cli "
                        + "WHERE cli.idClinica = :clinica AND c.fechaInicio >= :desde "
                        + "AND c.fechaInicio < :hasta ORDER BY c.fechaInicio DESC", Consulta.class)
                .setParameter("clinica", idClinica).setParameter("desde", desde)
                .setParameter("hasta", hasta).setFirstResult(first).setMaxResults(max).getResultList();
    }

    public long countByClinicaYFechas(UUID idClinica, OffsetDateTime desde, OffsetDateTime hasta) {
        if (idClinica == null || desde == null || hasta == null) {
            return 0L;
        }
        return getEntityManager().createQuery(
                "SELECT COUNT(c) FROM Consulta c WHERE c.idPersonaRol.idClinica.idClinica = :clinica "
                        + "AND c.fechaInicio >= :desde AND c.fechaInicio < :hasta", Long.class)
                .setParameter("clinica", idClinica).setParameter("desde", desde)
                .setParameter("hasta", hasta).getSingleResult();
    }

}
