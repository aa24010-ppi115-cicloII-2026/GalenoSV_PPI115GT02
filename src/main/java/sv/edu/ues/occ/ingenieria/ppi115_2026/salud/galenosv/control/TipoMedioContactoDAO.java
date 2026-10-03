/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
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
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(e) FROM TipoMedioContacto e WHERE LOWER(e.nombre) = LOWER(:nom)" + (excluirId == null ? "" : " AND e.idTipoMedioContacto <> :excluir");
        var q = getEntityManager().createQuery(jpql, Long.class);
        q.setParameter("nom", nombre.trim());
        if (excluirId != null) {
            q.setParameter("excluir", excluirId);
        }
        return q.getSingleResult() > 0;
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

    public List<TipoMedioContacto> buscarPorNombreNativo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El parametro nombre no puede ser nulo o vacio");
        }
        try {
            Query q = getEntityManager()
                    .createNativeQuery("SELECT * FROM tipo_medio_contacto WHERE nombre = :nombre", TipoMedioContacto.class);
            q.setParameter("nombre", nombre.trim());
            return convertirResultado(q.getResultList());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPorNombreNativo", ex);
        }
    }

    public TipoMedioContacto buscarPrimeroPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Parametro nombre invalido");
        }
        try {
            Query q = getEntityManager()
                    .createNativeQuery("SELECT * FROM tipo_medio_contacto WHERE nombre = :nombre", TipoMedioContacto.class);
            q.setParameter("nombre", nombre.trim());
            q.setMaxResults(1);
            List<TipoMedioContacto> list = convertirResultado(q.getResultList());
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        } catch (Exception ex) {
            throw new IllegalStateException("Error en buscarPrimeroPorNombre", ex);
        }
    }

    private List<TipoMedioContacto> convertirResultado(List<?> datos) {
        List<TipoMedioContacto> resultado = new ArrayList<>();
        for (Object dato : datos) {
            resultado.add(TipoMedioContacto.class.cast(dato));
        }
        return resultado;
    }
}
