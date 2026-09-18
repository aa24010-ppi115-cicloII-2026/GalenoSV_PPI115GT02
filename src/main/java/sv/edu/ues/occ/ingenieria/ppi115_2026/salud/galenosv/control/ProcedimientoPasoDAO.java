/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author duran
 */
@Stateless
public class ProcedimientoPasoDAO extends DefaultDAO<ProcedimientoPaso> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ProcedimientoPasoDAO() {
        super(ProcedimientoPaso.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
