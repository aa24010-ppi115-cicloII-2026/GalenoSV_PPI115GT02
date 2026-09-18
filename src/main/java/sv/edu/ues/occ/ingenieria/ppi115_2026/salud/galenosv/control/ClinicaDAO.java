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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;

/**
 *
 * @author duran
 */
@Stateless
public class ClinicaDAO extends DefaultDAO<Clinica> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ClinicaDAO() {
        super(Clinica.class);
    }

    public List<Clinica> buscarPorNombre(String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("nombre no puede ser nulo");
            }
            String jpql = "SELECT c FROM Clinica c WHERE c.nombre = :nombre";
            TypedQuery<Clinica> q = getEntityManager().createQuery(jpql, Clinica.class);
            q.setParameter("nombre", nombre.trim());
            return q.getResultList();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorNombre", ex);
        }
    }

    public List<Clinica> buscarPorTipo(String tipo) {
        try {
            if (tipo == null || tipo.trim().isEmpty()) {
                throw new IllegalArgumentException("tipo no puede ser nulo");
            }
            String jpql = "SELECT c FROM Clinica c WHERE c.tipo = :tipo";
            TypedQuery<Clinica> q = getEntityManager().createQuery(jpql, Clinica.class);
            q.setParameter("tipo", tipo.trim());
            return q.getResultList();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorTipo", ex);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
