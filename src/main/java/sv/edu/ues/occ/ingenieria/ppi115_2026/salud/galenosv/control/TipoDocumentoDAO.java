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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;

/**
 *
 * @author duran
 */
@Stateless
public class TipoDocumentoDAO extends DefaultDAO<TipoDocumento> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public TipoDocumentoDAO() {
        super(TipoDocumento.class);
    }

    public List<TipoDocumento> buscarPorNombre(String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("EL nombre no puede ser nulo");
            }
            String jpql = "SELECT t FROM TipoDocumento t WHERE t.nombre = :nombre";
            TypedQuery<TipoDocumento> q = getEntityManager().createQuery(jpql, TipoDocumento.class);
            q.setParameter("nombre", nombre.trim());
            return q.getResultList();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorNombre", ex);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
