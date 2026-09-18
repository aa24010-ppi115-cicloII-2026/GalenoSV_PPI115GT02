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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;

/**
 *
 * @author duran
 */
@Stateless
public class PersonaDAO extends DefaultDAO<Persona> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public PersonaDAO() {
        super(Persona.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Persona> buscarPorNombre(String nombres) {
        if (nombres == null || nombres.isBlank()) {
            throw new IllegalArgumentException("Los nombres son requeridos");
        }
        try {
            String jpql = "SELECT p FROM Persona p WHERE p.nombres = :nombres";
            TypedQuery<Persona> q = getEntityManager().createQuery(jpql, Persona.class);
            q.setParameter("nombres", nombres.trim());
            return q.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron buscar personas por nombre", ex);
        }
    }

    public List<Persona> buscarPorApellido(String apellidos) {
        if (apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("Los apellidos son requeridos");
        }
        try {
            String jpql = "SELECT p FROM Persona p WHERE p.apellidos = :apellidos";
            TypedQuery<Persona> q = getEntityManager().createQuery(jpql, Persona.class);
            q.setParameter("apellidos", apellidos.trim());
            return q.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron buscar personas por apellido", ex);
        }
    }

    public List<Persona> buscarPorDocumento(String valorDocumento) {
        if (valorDocumento == null || valorDocumento.isBlank()) {
            throw new IllegalArgumentException("El valor del documento es requerido");
        }
        try {
            String jpql = "SELECT DISTINCT p FROM Persona p JOIN p.documentoList d WHERE d.valor = :valor";
            TypedQuery<Persona> q = getEntityManager().createQuery(jpql, Persona.class);
            q.setParameter("valor", valorDocumento.trim());
            return q.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron buscar personas por documento", ex);
        }
    }

    public List<Persona> buscarPorNombreYApellido(String nombres, String apellidos) {
        if (nombres == null || nombres.isBlank() || apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("Los nombres y apellidos son requeridos");
        }
        try {
            String jpql = "SELECT p FROM Persona p WHERE p.nombres = :nombres AND p.apellidos = :apellidos";
            TypedQuery<Persona> q = getEntityManager().createQuery(jpql, Persona.class);
            q.setParameter("nombres", nombres.trim());
            q.setParameter("apellidos", apellidos.trim());
            return q.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron buscar personas por nombre y apellido", ex);
        }
    }

}
