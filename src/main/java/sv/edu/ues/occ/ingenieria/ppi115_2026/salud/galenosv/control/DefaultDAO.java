/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author duran
 */
public abstract class DefaultDAO<T> implements DAOInterface<T>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final Class<T> entityClass;

    public DefaultDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public abstract EntityManager getEntityManager();

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public T find(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            return em.find(entityClass, id);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al buscar por ID", ex);
        }
    }

    public List<T> findAll() {
        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);

            return em.createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al findAll", ex);
        }
    }

    public List<T> findRange(int first, int pageSize) {
        if (first < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Parametros invalidos");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);

            TypedQuery<T> q = em.createQuery(cq);
            q.setFirstResult(first);
            q.setMaxResults(pageSize);
            return q.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al findRange", e);
        }
    }

    public Long count() {
        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));

            return em.createQuery(cq).getSingleResult();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al count", ex);
        }
    }

    public void crear(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            em.persist(registro);
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear", ex);
        }
    }

    public T modificar(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            return em.merge(registro);
        } catch (Exception ex) {
            throw new RuntimeException("Error al modificar", ex);
        }
    }

    public void eliminar(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }

            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);
            em.flush();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            if (esViolacionLlaveForanea(ex)) {
                throw new IllegalStateException(
                        "No se puede eliminar porque tiene registros relacionados", ex);
            }
            throw new RuntimeException("Error al eliminar", ex);
        }
    }

    protected record Filtro(String campo, String parametro, Object valor) {
    }

    protected boolean existe(String jpql, Map<String, Object> parametros) {
        TypedQuery<Long> q = getEntityManager().createQuery(jpql, Long.class);
        parametros.forEach(q::setParameter);
        return q.getSingleResult() > 0;
    }

    protected boolean existeTexto(String campo, String idCampo, String parametroValor,
            String valor, UUID excluirId) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName()
                + " e WHERE LOWER(e." + campo + ") = LOWER(:" + parametroValor + ")"
                + (excluirId == null ? "" : " AND e." + idCampo + " <> :excluir");
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put(parametroValor, valor.trim());
        if (excluirId != null) {
            parametros.put("excluir", excluirId);
        }
        return existe(jpql, parametros);
    }

    protected boolean existeTextoIgual(String alias, String campo, String idCampo,
            String parametroValor, String valor, UUID excluirId) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put(parametroValor, valor.trim());
        if (excluirId != null) {
            parametros.put("excluir", excluirId);
        }
        String jpql = "SELECT COUNT(" + alias + ") FROM " + entityClass.getSimpleName()
                + " " + alias + " WHERE " + alias + "." + campo + " = :" + parametroValor
                + (excluirId == null ? "" : " AND " + alias + "." + idCampo + " <> :excluir");
        return existe(jpql, parametros);
    }

    protected boolean existeConFiltros(String alias, String idCampo, UUID excluirId, Filtro... filtros) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(" + alias + ") FROM ")
                .append(entityClass.getSimpleName()).append(" ").append(alias).append(" WHERE ");
        Map<String, Object> parametros = new LinkedHashMap<>();
        for (int i = 0; i < filtros.length; i++) {
            Filtro filtro = filtros[i];
            if (i > 0) {
                jpql.append(" AND ");
            }
            jpql.append(alias).append(".").append(filtro.campo()).append(" = :").append(filtro.parametro());
            parametros.put(filtro.parametro(), filtro.valor());
        }
        if (excluirId != null) {
            jpql.append(" AND ").append(alias).append(".").append(idCampo).append(" <> :excluir");
            parametros.put("excluir", excluirId);
        }
        return existe(jpql.toString(), parametros);
    }

    private boolean esViolacionLlaveForanea(Throwable ex) {
        while (ex != null) {
            String nombre = ex.getClass().getSimpleName();
            String mensaje = String.valueOf(ex.getMessage());
            if (nombre.contains("ConstraintViolation") || nombre.contains("RollbackException")
                    || mensaje.contains("viola la llave foranea") || mensaje.contains("foreign key")
                    || mensaje.contains("FK_") || mensaje.contains("fk_")) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }
}
