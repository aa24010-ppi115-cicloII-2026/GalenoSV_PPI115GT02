/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        return existeTexto("nombre", "idTipoDocumento", "nom", nombre, excluirId);
    }

    public List<TipoDocumento> findActivos() {
        return getEntityManager().createQuery(
                "SELECT t FROM TipoDocumento t WHERE t.activo = TRUE ORDER BY t.nombre",
                TipoDocumento.class).getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
