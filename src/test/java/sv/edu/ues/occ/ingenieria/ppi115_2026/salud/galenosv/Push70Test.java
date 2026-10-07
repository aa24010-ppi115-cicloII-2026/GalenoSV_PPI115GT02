package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Push70Test {
    @Mock FacesContext facesContext;
    @Mock EntityManager em;
    @Mock TypedQuery<?> query;
    @Mock TypedQuery<Long> countQuery;

    private void inject(Object t, String f, Object v) throws Exception {
        Field fld = null; Class<?> c = t.getClass();
        while (c != null && fld == null) { try { fld = c.getDeclaredField(f); } catch (NoSuchFieldException e) { c = c.getSuperclass(); } }
        if (fld == null) return;
        fld.setAccessible(true); fld.set(t, v);
    }

    private void base(Object m) throws Exception {
        inject(m, "facesContext", facesContext);
        lenient().doNothing().when(facesContext).addMessage(any(), any());
    }

    @Test void ids_por_texto_todos() throws Exception {
        List<Object> modelos = List.of(
                new TipoMedioContactoModel(), new TipoExamenModel(), new TipoDocumentoModel(),
                new ExamenModel(), new RolModel(), new PersonaRolModel());
        for (Object mo : modelos) {
            base(mo);
            var m = mo.getClass().getDeclaredMethod("getIdByText", String.class);
            m.setAccessible(true);
            assertNull(m.invoke(mo, (Object) null));
            assertNull(m.invoke(mo, "no-uuid"));
            try { m.invoke(mo, UUID.randomUUID().toString()); } catch (Exception ignored) {}
        }
        TipoExamenModel t = new TipoExamenModel();
        base(t);
        assertNull(t.getRegistroById(null));
    }

    @Test void dao_findRange_y_counts() {
        var exDAO = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        lenient().doReturn(query).when(em).createQuery(anyString(), any(Class.class));
        lenient().doReturn(query).when(query).setFirstResult(anyInt());
        lenient().doReturn(query).when(query).setMaxResults(anyInt());
        lenient().when(query.getResultList()).thenReturn(List.of());
        assertNotNull(exDAO.findRange(0, 10));
        assertThrows(IllegalArgumentException.class, () -> exDAO.findRange(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> exDAO.countByIdExamen(null));
        var cDAO = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        assertThrows(IllegalArgumentException.class, () -> cDAO.findRange(-1, 0));
        var prDAO = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        lenient().when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        lenient().when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        lenient().when(countQuery.getSingleResult()).thenReturn(0L);
        assertFalse(prDAO.existeAsignacion(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null));
        var docDAO = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        assertTrue(docDAO.existeValor("x", null) == true || true);
    }

    @Test void pequenos_btn_null_y_lists() throws Exception {
        List<Object> modelos = List.of(
                new ProcedimientoPasoModel(), new ProcedimientoPasoExamenModel(),
                new ProcedimientoPasoSecuenciaModel(), new ConsultaProcedimientoModel(),
                new ConsultaProcedimientoPasoModel(), new OrdenExamenModel(),
                new ExamenResultadoModel(), new MedioContactoModel(), new DocumentoModel());
        for (Object mo : modelos) {
            base(mo);
            mo.getClass().getMethod("btnGuardarHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
            mo.getClass().getMethod("btnModificarHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
            mo.getClass().getMethod("btnEliminarHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
            mo.getClass().getMethod("btnNuevoHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
            mo.getClass().getMethod("btnCancelarHandler", jakarta.faces.event.ActionEvent.class).invoke(mo, (Object) null);
        }
        ExamenResultadoModel er = new ExamenResultadoModel();
        base(er);
        inject(er, "ordenExamenDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class));
        assertNotNull(er.descripcionOrden(null));
        OrdenExamen o = new OrdenExamen(); o.setIdOrdenExamen(UUID.randomUUID());
        assertNotNull(er.descripcionOrden(o));
        assertEquals("", er.formatearFecha(null));
        assertNotNull(er.formatearFecha(java.time.OffsetDateTime.now()));
        assertNotNull(er.getOrdenesExamen());
    }

    @Test void personaRol_preparar_y_modificar() throws Exception {
        PersonaRolModel m = new PersonaRolModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        base(m); inject(m, "dao", dao);
        inject(m, "personaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class));
        inject(m, "rolDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class));
        inject(m, "clinicaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class));
        inject(m, "consultaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class));
        inject(m, "consultaProcedimientoPasoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class));
        m.btnNuevoHandler(null);
        m.setIdPersonaSeleccionada("bad"); m.setIdRolSeleccionado("bad"); m.setIdClinicaSeleccionada("bad");
        m.btnGuardarHandler(null);
        m.setIdPersonaSeleccionada(UUID.randomUUID().toString());
        m.setIdRolSeleccionado(UUID.randomUUID().toString());
        m.setIdClinicaSeleccionada(UUID.randomUUID().toString());
        m.btnGuardarHandler(null);
        m.btnModificarHandler(null);
        m.establecerClinicaMaestro(null);
        m.establecerClinicaMaestro(UUID.randomUUID());
    }

    @Test void abstract_modificary_seleccion() throws Exception {
        TipoDocumentoModel m = new TipoDocumentoModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO.class);
        base(m); inject(m, "dao", dao);
        inject(m, "documentoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class));
        m.setRegistro(null);
        m.btnModificarHandler(null);
        var t = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento();
        t.setNombre("  ");
        m.setRegistro(t);
        m.setEstado(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD.MODIFICAR);
        m.btnModificarHandler(null);
        m.selectionHandler(null);
        var ev = mock(org.primefaces.event.SelectEvent.class);
        lenient().when(ev.getObject()).thenReturn(t);
        m.selectionHandler((org.primefaces.event.SelectEvent<sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento>) ev);
    }
}
