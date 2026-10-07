package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractFullTest {
    @Mock FacesContext facesContext;

    static class Item {
        String nombre;
        Item(String n) { nombre = n; }
        public String getNombre() { return nombre; }
    }

    static class M extends AbstractModel<Item> {
        FacesContext ctx; DefaultDAO<Item> dao;
        boolean unicoFail, elimFail;
        M(FacesContext c, DefaultDAO<Item> d) { ctx = c; dao = d; nombreBean = "T"; }
        @Override protected FacesContext getFacesContext() { return ctx; }
        @Override protected DefaultDAO<Item> getDao() { return dao; }
        @Override protected Item nuevoRegistro() { return new Item("nuevo"); }
        @Override protected Item buscarRegistroPorId(Object id) { return new Item("x"); }
        @Override protected String getIdAsText(Item r) { return r == null ? null : r.nombre; }
        @Override protected Item getIdByText(String id) { return new Item(id); }
        @Override protected void validarUnicidad(Item r, boolean mod) {
            if (unicoFail) throw new IllegalArgumentException("duplicado");
        }
        @Override protected void validarEliminacion(Item r) {
            if (elimFail) throw new IllegalArgumentException("con hijos");
        }
    }

    private M modelo(DefaultDAO<Item> dao) {
        lenient().doNothing().when(facesContext).addMessage(any(), any());
        return new M(facesContext, dao);
    }

    @Test void nuevo_cancelar_selection() {
        M m = modelo(mock(DefaultDAO.class));
        m.btnNuevoHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
        assertNotNull(m.getRegistro());
        m.btnCancelarHandler(null);
        assertEquals(ESTADO_CRUD.NADA, m.getEstado());
        assertNull(m.getRegistro());
        m.selectionHandler(null);
        assertEquals(ESTADO_CRUD.NADA, m.getEstado());
        var ev = mock(SelectEvent.class);
        lenient().when(ev.getObject()).thenReturn(null);
        m.selectionHandler(ev);
        lenient().when(ev.getObject()).thenReturn(new Item("a"));
        m.selectionHandler((SelectEvent<Item>) ev);
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
    }

    @Test void guardar_ok_y_errores() {
        var dao = mock(DefaultDAO.class);
        M m = modelo(dao);
        m.btnGuardarHandler(null);
        m.setRegistro(new Item("  "));
        m.setEstado(ESTADO_CRUD.CREAR);
        m.btnGuardarHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
        m.setRegistro(new Item("ok"));
        m.setEstado(ESTADO_CRUD.CREAR);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.setRegistro(new Item("x")); m.setEstado(ESTADO_CRUD.CREAR); m.unicoFail = true;
        m.btnGuardarHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
        m.unicoFail = false;
        lenient().doThrow(new RuntimeException("db")).when(dao).crear(any());
        m.setRegistro(new Item("y")); m.setEstado(ESTADO_CRUD.CREAR);
        m.btnGuardarHandler(null);
    }

    @Test void modificar_eliminar() {
        var dao = mock(DefaultDAO.class);
        M m = modelo(dao);
        m.setRegistro(new Item("  ")); m.setEstado(ESTADO_CRUD.MODIFICAR);
        m.btnModificarHandler(null);
        m.setRegistro(new Item("ok")); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(dao.modificar(any())).thenReturn(new Item("ok"));
        m.btnModificarHandler(null);
        m.setRegistro(new Item("e")); m.setEstado(ESTADO_CRUD.MODIFICAR); m.elimFail = true;
        m.btnEliminarHandler(null);
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
        m.elimFail = false;
        lenient().doNothing().when(dao).eliminar(any());
        m.btnEliminarHandler(null);
        assertEquals(ESTADO_CRUD.NADA, m.getEstado());
        m.setRegistro(null);
        m.btnModificarHandler(null); m.btnEliminarHandler(null);
    }

    @Test void lazy_e_inicializar() {
        var dao = mock(DefaultDAO.class);
        M m = modelo(dao);
        m.inicializar();
        assertNotNull(m.getModelo());
        m.getModelo().getRowKey(null);
        m.getModelo().getRowData(null);
        lenient().when(dao.count()).thenReturn(7L);
        assertEquals(7, m.getModelo().count(null));
        lenient().when(dao.count()).thenThrow(new RuntimeException("x"));
        assertEquals(0, m.getModelo().count(null));
        lenient().when(dao.findRange(0, 10)).thenReturn(java.util.List.of(new Item("a")));
        assertFalse(m.getModelo().load(0, 10, null, null).isEmpty());
        lenient().when(dao.findRange(0, 10)).thenThrow(new RuntimeException("x"));
        assertTrue(m.getModelo().load(0, 10, null, null).isEmpty());
        assertNull(m.getIdByRegistro(null));
        assertNotNull(m.getIdByRegistro(new Item("z")));
        assertNotNull(m.getRegistroById("z"));
        m.setNombreBean("B"); assertEquals("B", m.getNombreBean());
        m.setCantidadRegistros(20); assertEquals(20, m.getCantidadRegistros());
        m.setModelo(null); assertNull(m.getModelo());
        m.setEstado(ESTADO_CRUD.CREAR); assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
    }

    @Test void nombre_mala_reflexion() {
        M m = modelo(mock(DefaultDAO.class));
        m.setRegistro(null);
        m.btnGuardarHandler(null);
        assertNull(m.getRegistro());
    }
}
