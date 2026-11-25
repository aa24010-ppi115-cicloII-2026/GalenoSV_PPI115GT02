/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    public List<Clinica> findActivas() {
        return getEntityManager().createQuery(
                "SELECT c FROM Clinica c WHERE c.activo = TRUE ORDER BY c.nombre", Clinica.class)
                .getResultList();
    }


    public boolean existeNombre(String nombre, java.util.UUID excluirId) {
        return existeTexto("nombre", "idClinica", "nom", nombre, excluirId);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
