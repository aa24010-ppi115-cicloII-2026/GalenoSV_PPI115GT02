/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author duran
 */
@Stateless
public class RolDAO extends DefaultDAO<Rol> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public RolDAO() {
        super(Rol.class);
    }

    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        return existeTexto("nombre", "idRol", "nom", nombre, excluirId);
    }

    public List<Rol> findActivos() {
        return getEntityManager().createQuery(
                "SELECT r FROM Rol r WHERE r.activo = TRUE ORDER BY r.nombre", Rol.class)
                .getResultList();
    }

    public List<Rol> findActivosByNombreLike(String filtro, int first, int max) {
        if (filtro == null || filtro.trim().length() < 3 || first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parametros invalidos para buscar rol");
        }
        return getEntityManager().createQuery(
                "SELECT r FROM Rol r WHERE r.activo = TRUE "
                        + "AND UPPER(r.nombre) LIKE :nombre ORDER BY r.nombre", Rol.class)
                .setParameter("nombre", "%" + filtro.trim().toUpperCase() + "%")
                .setFirstResult(first).setMaxResults(max).getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
