package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultDAOTest {

    public DefaultDAOTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getEntityManager method, of class DefaultDAO.
     */
    @Test
    public void testGetEntityManager() {
        EntityManager em = mock(EntityManager.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        assertSame(em, prueba.getEntityManager());
    }

    /**
     * Test of getEntityClass method, of class DefaultDAO.
     */
    @Test
    public void testGetEntityClass() {
        DefaultDAOImpl cadena = new DefaultDAOImpl();
        Class<String> result = cadena.getEntityClass();
        assertEquals(String.class, result);
    }

    /**
     * Test of find method, of class DefaultDAO.
     */
    @Test
    public void testFindIdnull() {
        System.out.println("find");
        DefaultDAOImpl prueba = new DefaultDAOImpl();
        assertThrows(IllegalArgumentException.class, () -> {
            prueba.find(null);
        });
    }

    @Test
    public void testFindEntityManagerNulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();
        assertThrows(IllegalStateException.class, () -> {
            prueba.find(1);
        });
    }

    @Test
    public void testFind() {
        EntityManager em = mock(EntityManager.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        when(em.find(String.class, 1)).thenReturn("hola");

        String resultado = (String) prueba.find(1);

        verify(em).find(String.class, 1);

        assertEquals("hola", resultado);
    }

    /**
     * Test of findAll method, of class DefaultDAO.
     */
    @Test
    public void testFindAll() {
        EntityManager em = mock(EntityManager.class);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        CriteriaQuery<String> cq = mock(CriteriaQuery.class);

        Root<String> root = mock(Root.class);

        TypedQuery<String> query = mock(TypedQuery.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        when(em.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(String.class)).thenReturn(cq);

        when(cq.from(String.class)).thenReturn(root);

        when(cq.select(root)).thenReturn(cq);

        when(em.createQuery(cq)).thenReturn(query);

        when(query.getResultList()).thenReturn(List.of("Hola", "Mundo"));

        List<String> resultado = prueba.findAll();

        verify(em).getCriteriaBuilder();
        verify(cb).createQuery(String.class);
        verify(cq).from(String.class);
        verify(cq).select(root);
        verify(em).createQuery(cq);
        verify(query).getResultList();

        assertEquals(2, resultado.size());
        assertEquals(List.of("Hola", "Mundo"), resultado);
    }

    @Test
    public void testFindAllEntitymanagenulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();
        assertThrows(IllegalStateException.class, () -> {
            prueba.findAll();
        });
    }

    /**
     * Test of findRange method, of class DefaultDAO.
     */
    @Test
    public void testFindRange() {
        EntityManager em = mock(EntityManager.class);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        CriteriaQuery<String> cq = mock(CriteriaQuery.class);

        Root<String> root = mock(Root.class);

        TypedQuery<String> query = mock(TypedQuery.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(String.class)).thenReturn(cq);
        when(cq.from(String.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(query);
        when(query.setFirstResult(5)).thenReturn(query);
        when(query.setMaxResults(10)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of("A", "B", "C"));

        List<String> resultado = prueba.findRange(5, 10);

        verify(em).getCriteriaBuilder();
        verify(cb).createQuery(String.class);
        verify(cq).from(String.class);
        verify(cq).select(root);
        verify(em).createQuery(cq);
        verify(query).setFirstResult(5);
        verify(query).setMaxResults(10);
        verify(query).getResultList();

        assertEquals(List.of("A", "B", "C"), resultado);
        assertEquals(3, resultado.size());
    }

    @Test
    public void testFindRangeFirstNegativo() {
        System.out.println("find");
        DefaultDAOImpl prueba = new DefaultDAOImpl();
        assertThrows(IllegalArgumentException.class, () -> {
            prueba.findRange(-1, 10);
        });
    }

    @Test
    public void testFindRangePageSizeCero() {
        System.out.println("find");
        DefaultDAOImpl prueba = new DefaultDAOImpl();
        assertThrows(IllegalArgumentException.class, () -> {
            prueba.findRange(0, 0);
        });
    }

    /**
     * Test of count method, of class DefaultDAO.
     */
    @Test
    public void testCount() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
        Root<String> root = mock(Root.class);

        Expression<Long> expresion = mock(Expression.class);
        TypedQuery<Long> query = mock(TypedQuery.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        when(em.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(Long.class)).thenReturn(cq);

        when(cq.from(String.class)).thenReturn(root);

        when(cb.count(root)).thenReturn(expresion);

        when(cq.select(expresion)).thenReturn(cq);

        when(em.createQuery(cq)).thenReturn(query);

        when(query.getSingleResult()).thenReturn(25L);

        Long resultado = prueba.count();

        verify(em).getCriteriaBuilder();
        verify(cb).createQuery(Long.class);
        verify(cq).from(String.class);
        verify(cb).count(root);
        verify(cq).select(expresion);
        verify(em).createQuery(cq);
        verify(query).getSingleResult();

        assertEquals(25L, resultado);
    }

    @Test
    public void testCountEntityManagerNulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();
        assertThrows(IllegalStateException.class, () -> {
            prueba.count();
        });
    }

    /**
     * Test of crear method, of class DefaultDAO.
     */
    @Test
    public void testCrear() {
        EntityManager em = mock(EntityManager.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        String registro = "Hola";

        prueba.crear(registro);

        verify(em).persist(registro);
    }

    @Test
    public void testCrearRegistroNulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();

        assertThrows(IllegalArgumentException.class, () -> {
            prueba.crear(null);
        });
    }

    @Test
    public void testCrearEntityManagerNulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();

        assertThrows(RuntimeException.class, () -> {
            prueba.crear("Prueba");
        });
    }

    /**
     * Test of modificar method, of class DefaultDAO.
     */
    @Test
    public void modificar() {
        EntityManager em = mock(EntityManager.class);
        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        String registro = "Hola";

        when(em.merge(registro)).thenReturn("Resultado");

        String resultado = (String) prueba.modificar(registro);

        verify(em).merge(registro);

        assertEquals("Resultado", resultado);
    }

    @Test
    public void testModificarRegistronulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();

        assertThrows(IllegalArgumentException.class, () -> {
            prueba.modificar(null);
        });
    }

    @Test
    public void testModificarEntityManagerNulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();

        assertThrows(RuntimeException.class, () -> {
            prueba.modificar("Prueba");
        });
    }

    /**
     * Test of eliminar method, of class DefaultDAO.
     */
    @Test
    public void testEliminarRegistronulo() {

        EntityManager em = mock(EntityManager.class);

        DefaultDAOImpl prueba = new DefaultDAOImpl(em);

        String registro = "Hola";

        when(em.contains(registro)).thenReturn(false);
        when(em.merge(registro)).thenReturn(registro);

        prueba.eliminar(registro);

        verify(em).contains(registro);
        verify(em).merge(registro);
        verify(em).remove(registro);
        verify(em).flush();
    }

    @Test
    public void testEliminarEntityManagerNulo() {
        DefaultDAOImpl prueba = new DefaultDAOImpl();

        assertThrows(IllegalStateException.class, () -> {
            prueba.eliminar("Prueba");
        });
    }

    @Test
    public void testEliminarViolacionLlaveForanea() {
        EntityManager em = mock(EntityManager.class);
        DefaultDAOImpl prueba = new DefaultDAOImpl(em);
        String registro = "Hola";

        when(em.contains(registro)).thenReturn(true);

        doThrow(new RuntimeException("foreign key constraint"))
                .when(em).flush();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> prueba.eliminar(registro)
        );

        assertEquals(
                "No se puede eliminar porque tiene registros relacionados",
                ex.getMessage()
        );

        verify(em).contains(registro);
        verify(em).remove(registro);
        verify(em).flush();
    }

    public class DefaultDAOImpl extends DefaultDAO {

        private EntityManager em;

        public DefaultDAOImpl(EntityManager em) {
            super(String.class);
            this.em = em;
        }

        public DefaultDAOImpl() {
            super(String.class);
        }

        @Override
        public EntityManager getEntityManager() {
            return em;
        }
    }

}
