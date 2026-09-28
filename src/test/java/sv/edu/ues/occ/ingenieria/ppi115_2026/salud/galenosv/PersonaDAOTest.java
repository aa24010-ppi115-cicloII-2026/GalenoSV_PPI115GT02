package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonaDAOTest {

    @Mock EntityManager em;
    @Mock TypedQuery<Persona> query;

    PersonaDAO dao;

    @BeforeEach void setUp(){
        dao = new PersonaDAO(){ @Override public EntityManager getEntityManager(){ return em; } };
    }

    @Test void testFindByNombre_ok(){
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID()); p.setNombres("Juan");
        when(em.createQuery(anyString(), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(p));
        List<Persona> r = dao.buscarPorNombre("Juan");
        assertEquals(1, r.size());
        verify(em).createQuery(contains("p.nombres"), eq(Persona.class));
    }

    @Test void testFindByNombre_paramInvalido(){
        assertThrows(IllegalArgumentException.class, ()-> dao.buscarPorNombre(null));
        assertThrows(IllegalArgumentException.class, ()-> dao.buscarPorNombre("  "));
    }

    @Test void testFindByNombre_errorDeBaseNoSeOculta(){
        when(em.createQuery(anyString(), eq(Persona.class))).thenThrow(new IllegalStateException("Sin conexion"));

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> dao.buscarPorNombre("Juan"));

        assertTrue(error.getMessage().contains("buscar personas por nombre"));
    }

    @Test void testFindByApellido_ok(){
        Persona p = new Persona(); p.setApellidos("Perez");
        when(em.createQuery(anyString(), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(p));
        assertEquals(1, dao.buscarPorApellido("Perez").size());
    }

    @Test void testFindByDocumento_ok_conTypedQuery(){
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID());
        when(em.createQuery(contains("JOIN p.documentoList"), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(eq("valor"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(p));
        List<Persona> r = dao.buscarPorDocumento("123456");
        assertEquals(1, r.size());
        verify(query).setParameter("valor","123456");
    }

    @Test void testFindByDocumento_invalido(){
        assertThrows(IllegalArgumentException.class, ()-> dao.buscarPorDocumento(null));
    }

    @Test void testFindByNombreYApellido_ok(){
        when(em.createQuery(contains("p.nombres"), eq(Persona.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Persona()));
        assertEquals(1, dao.buscarPorNombreYApellido("Juan","Perez").size());
    }
}
