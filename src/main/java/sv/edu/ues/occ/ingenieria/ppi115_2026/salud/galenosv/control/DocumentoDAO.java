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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;

/**
 *
 * @author duran
 */
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

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
