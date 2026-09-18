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

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Consulta> buscarPorConsultasActivas() {
        try {
            String jpql = "SELECT c FROM Consulta c WHERE c.fechaFin IS NULL OR c.fechaFin > :ahora";
            TypedQuery<Consulta> query = getEntityManager().createQuery(jpql, Consulta.class);
            query.setParameter("ahora", OffsetDateTime.now());
            return query.getResultList();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorConsultasActivas", ex);
        }
    }

    public List<Consulta> buscarActivas() {
        try {
            String jpql = "SELECT c FROM Consulta c WHERE c.fechaFin IS NULL OR c.fechaFin > :ahora";
            TypedQuery<Consulta> query = getEntityManager().createQuery(jpql, Consulta.class);
            query.setParameter("ahora", OffsetDateTime.now());
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron buscar las consultas activas", ex);
        }
    }

    public List<Consulta> buscarPorPersonaRol(Object idPersonaRol) {
        if (idPersonaRol == null) {
            throw new IllegalArgumentException("idPersonaRol no puede ser nulo");
        }
        TypedQuery<Consulta> q = getEntityManager().createQuery(
                "SELECT c FROM Consulta c WHERE c.idPersonaRol.idPersonaRol = :id", Consulta.class);
        q.setParameter("id", idPersonaRol);
        return q.getResultList();
    }

    public List<Consulta> buscarActivasPorPersonaRol(Object idPersonaRol) {
        if (idPersonaRol == null) {
            throw new IllegalArgumentException("idPersonaRol requerido");
        }
        TypedQuery<Consulta> q = getEntityManager().createQuery(
                "SELECT c FROM Consulta c WHERE c.idPersonaRol.idPersonaRol = :id AND (c.fechaFin IS NULL OR c.fechaFin > :ahora)", Consulta.class);
        q.setParameter("id", idPersonaRol);
        q.setParameter("ahora", OffsetDateTime.now());
        return q.getResultList();
    }

}
