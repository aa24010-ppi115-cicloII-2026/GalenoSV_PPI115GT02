/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 *
 * @author duran
 */
@Stateless
public class TipoMedioContactoDAO extends DefaultDAO<TipoMedioContacto> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public TipoMedioContactoDAO() {
        super(TipoMedioContacto.class);
    }

    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        return existeTexto("nombre", "idTipoMedioContacto", "nom", nombre, excluirId);
    }

    public List<TipoMedioContacto> findActivos() {
        return getEntityManager().createQuery(
                "SELECT t FROM TipoMedioContacto t WHERE t.activo = TRUE ORDER BY t.nombre",
                TipoMedioContacto.class).getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
