/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import java.util.List;

/**
 *
 * @author duran
 */
public interface DAOInterface<T> {

    void crear(T registro);

    List<T> findRange(int first, int max);

    Long count();

    void eliminar(T registro);

}
