package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Push70FullTest {
    @Mock FacesContext facesContext;

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

    @Test void descripcionOrden_full() throws Exception {
        ExamenResultadoModel m = new ExamenResultadoModel();
        base(m);
        inject(m, "ordenExamenDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class));
        Persona per = new Persona(); per.setNombres("Juan"); per.setApellidos("Perez");
        PersonaRol pr = new PersonaRol(); pr.setIdPersona(per);
        Consulta c = new Consulta(); c.setIdPersonaRol(pr);
        ConsultaProcedimiento cp = new ConsultaProcedimiento(); cp.setIdConsulta(c);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setIdConsultaProcedimiento(cp); paso.setEstado("REALIZADO");
        OrdenExamen o = new OrdenExamen(); o.setIdOrdenExamen(UUID.randomUUID());
        o.setIdConsultaProcedimientoPaso(paso);
        o.setFechaCreacion(OffsetDateTime.now());
        o.setIndicaciones("Esta es una indicacion muy larga que supera los cuarenta caracteres facilmente");
        String d = m.descripcionOrden(o);
        assertTrue(d.contains("Juan Perez"));
        assertTrue(d.contains("REALIZADO"));
        assertTrue(d.contains("..."));
        o.setIndicaciones("corta");
        assertTrue(m.descripcionOrden(o).contains("corta"));
        OrdenExamen sinPaso = new OrdenExamen(); sinPaso.setIdOrdenExamen(UUID.randomUUID());
        assertNotNull(m.descripcionOrden(sinPaso));
    }

    @Test void doc_med_exTipo_preparar_ok() throws Exception {
        DocumentoModel dm = new DocumentoModel();
        var dDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        var pDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        var tdDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO.class);
        base(dm); inject(dm, "dao", dDAO); inject(dm, "personaDAO", pDAO); inject(dm, "tipoDocumentoDAO", tdDAO);
        UUID idPer = UUID.randomUUID(), idTipo = UUID.randomUUID();
        Persona per = new Persona(); per.setIdPersona(idPer);
        TipoDocumento td = new TipoDocumento(); td.setIdTipoDocumento(idTipo);
        td.setNombre("DUI"); td.setExpresionRegular(".*"); td.setActivo(true);
        lenient().when(pDAO.find(idPer)).thenReturn(per);
        lenient().when(tdDAO.find(idTipo)).thenReturn(td);
        lenient().when(dDAO.existeValor(any(), any())).thenReturn(false);
        lenient().doNothing().when(dDAO).crear(any());
        Documento d = new Documento(); d.setIdDocumento(UUID.randomUUID()); d.setValor("123");
        dm.setRegistro(d); dm.setEstado(ESTADO_CRUD.CREAR);
        inject(dm, "idPersonaSeleccionada", idPer.toString());
        inject(dm, "idTipoDocumentoSeleccionado", idTipo.toString());
        dm.btnGuardarHandler(null);

        MedioContactoModel mm = new MedioContactoModel();
        var mDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        var tmDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class);
        base(mm); inject(mm, "dao", mDAO); inject(mm, "personaDAO", pDAO); inject(mm, "tipoMedioContactoDAO", tmDAO);
        UUID idTm = UUID.randomUUID();
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setIdTipoMedioContacto(idTm);
        tm.setNombre("Tel"); tm.setExpresionRegular(".*"); tm.setActivo(true);
        lenient().when(tmDAO.find(idTm)).thenReturn(tm);
        lenient().when(mDAO.existePersonaValor(any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(mDAO).crear(any());
        MedioContacto mc = new MedioContacto(); mc.setIdMedioContacto(UUID.randomUUID()); mc.setValor("7777");
        mm.setRegistro(mc); mm.setEstado(ESTADO_CRUD.CREAR);
        inject(mm, "idPersonaSeleccionada", idPer.toString());
        inject(mm, "idTipoMedioContactoSeleccionado", idTm.toString());
        mm.btnGuardarHandler(null);

        ExamenTipoExamenModel em = new ExamenTipoExamenModel();
        var vDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO.class);
        var eDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO.class);
        var tDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO.class);
        base(em); inject(em, "dao", vDAO); inject(em, "examenDAO", eDAO); inject(em, "tipoExamenDAO", tDAO);
        UUID idEx = UUID.randomUUID(), idTe = UUID.randomUUID();
        Examen ex = new Examen(); ex.setIdExamen(idEx);
        TipoExamen te = new TipoExamen(); te.setIdTipoExamen(idTe);
        lenient().when(eDAO.find(idEx)).thenReturn(ex);
        lenient().when(tDAO.find(idTe)).thenReturn(te);
        lenient().when(vDAO.existeVinculo(any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(vDAO).crear(any());
        ExamenTipoExamen v = new ExamenTipoExamen(); v.setIdExamenTipoExamen(UUID.randomUUID());
        v.setIdExamen(ex); v.setIdTipoExamen(te);
        em.setRegistro(v); em.setEstado(ESTADO_CRUD.CREAR);
        em.btnGuardarHandler(null);
        em.setTipoExamenSeleccionado(te);
        assertNotNull(em.getNombreTipoExamenSeleccionado());
        em.setIdExamen(idEx);
        assertNotNull(em.buscarTipoPorNombre("abc"));
    }

    @Test void pasoSecuencia_pasoExamen_consultaProc_ok() throws Exception {
        ProcedimientoPasoModel pm = new ProcedimientoPasoModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var procDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        base(pm); inject(pm, "dao", dao); inject(pm, "procedimientoDAO", procDAO); inject(pm, "rolDAO", rolDAO);
        UUID idProc = UUID.randomUUID(), idRol = UUID.randomUUID();
        Procedimiento proc = new Procedimiento(); proc.setIdProcedimiento(idProc);
        Rol rol = new Rol(); rol.setIdRol(idRol); rol.setActivo(true);
        lenient().when(procDAO.find(idProc)).thenReturn(proc);
        lenient().when(rolDAO.find(idRol)).thenReturn(rol);
        lenient().doNothing().when(dao).crear(any());
        ProcedimientoPaso paso = new ProcedimientoPaso(); paso.setIdProcedimientoPaso(UUID.randomUUID()); paso.setNombre("P1");
        pm.setRegistro(paso); pm.setEstado(ESTADO_CRUD.CREAR);
        inject(pm, "idProcedimientoSeleccionado", idProc.toString());
        inject(pm, "idRolSeleccionado", idRol.toString());
        pm.btnGuardarHandler(null);
        pm.selectionHandler(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class), mock(jakarta.faces.component.behavior.Behavior.class), paso));
        lenient().when(dao.modificar(any())).thenReturn(paso);
        pm.setRegistro(paso); pm.setEstado(ESTADO_CRUD.MODIFICAR);
        pm.btnModificarHandler(null);
    }
}
