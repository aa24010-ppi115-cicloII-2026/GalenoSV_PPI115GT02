/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;

/**
 *
 * @author duran
 */
@Stateless
public class ConsultaProcedimientoDAO extends DefaultDAO<ConsultaProcedimiento> {

    @PersistenceContext(unitName = "clinica_ppi")
    private EntityManager em;

    public ConsultaProcedimientoDAO() {
        super(ConsultaProcedimiento.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
