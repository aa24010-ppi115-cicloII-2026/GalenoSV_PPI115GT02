package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores.ExamenConverter;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores.RolConverter;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores.TipoExamenConverter;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores.UUIDConverter;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConvertersTest {
    @Mock EntityManager em;

    private void inject(Object target, String field, Object value) throws Exception {
        Field f = null;
        Class<?> c = target.getClass();
        while (c != null && f == null) {
            try { f = c.getDeclaredField(field); } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test void uuidConverter_todo() {
        UUIDConverter c = new UUIDConverter();
        UUID id = UUID.randomUUID();
        assertEquals(id, c.convertToDatabaseColumn(id));
        assertEquals(id, c.convertToEntityAttribute(id));
        assertEquals(id, c.convertToEntityAttribute(id.toString()));
        assertNull(c.convertToEntityAttribute(null));
        assertThrows(IllegalArgumentException.class, () -> c.convertToEntityAttribute(12345));
    }

    @Test void examenConverter() throws Exception {
        ExamenConverter conv = new ExamenConverter();
        var dao = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        inject(conv, "dao", dao);
        assertNull(conv.getAsObject(null, null, null));
        assertNull(conv.getAsObject(null, null, "  "));
        assertNull(conv.getAsObject(null, null, "no-es-uuid"));
        Examen e = new Examen(); e.setIdExamen(UUID.randomUUID());
        when(em.find(any(), any())).thenReturn(e);
        assertNotNull(conv.getAsObject(null, null, e.getIdExamen().toString()));
        assertEquals("", conv.getAsString(null, null, null));
        Examen sinId = new Examen();
        assertEquals("", conv.getAsString(null, null, sinId));
        assertEquals(e.getIdExamen().toString(), conv.getAsString(null, null, e));
    }

    @Test void rolConverter() throws Exception {
        RolConverter conv = new RolConverter();
        var dao = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        inject(conv, "dao", dao);
        assertNull(conv.getAsObject(null, null, null));
        assertNull(conv.getAsObject(null, null, "xxx"));
        Rol r = new Rol(); r.setIdRol(UUID.randomUUID());
        when(em.find(any(), any())).thenReturn(r);
        assertNotNull(conv.getAsObject(null, null, r.getIdRol().toString()));
        assertEquals("", conv.getAsString(null, null, null));
        assertEquals("", conv.getAsString(null, null, new Rol()));
        assertEquals(r.getIdRol().toString(), conv.getAsString(null, null, r));
    }

    @Test void tipoExamenConverter() throws Exception {
        TipoExamenConverter conv = new TipoExamenConverter();
        var dao = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        inject(conv, "dao", dao);
        assertNull(conv.getAsObject(null, null, null));
        assertNull(conv.getAsObject(null, null, "zzz"));
        TipoExamen t = new TipoExamen(); t.setIdTipoExamen(UUID.randomUUID());
        when(em.find(any(), any())).thenReturn(t);
        assertNotNull(conv.getAsObject(null, null, t.getIdTipoExamen().toString()));
        assertEquals("", conv.getAsString(null, null, null));
        assertEquals(t.getIdTipoExamen().toString(), conv.getAsString(null, null, t));
    }
}
