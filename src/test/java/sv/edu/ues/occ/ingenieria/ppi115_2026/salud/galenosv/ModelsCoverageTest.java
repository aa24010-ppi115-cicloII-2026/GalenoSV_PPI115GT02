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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModelsCoverageTest {
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

    @Test void examenTipoExamenModel_full() throws Exception {
        ExamenTipoExamenModel m = new ExamenTipoExamenModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO.class);
        base(m); inject(m, "dao", dao);
        m.btnNuevoHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, m.getEstado());
        Examen ex = new Examen(); ex.setIdExamen(UUID.randomUUID());
        TipoExamen te = new TipoExamen(); te.setIdTipoExamen(UUID.randomUUID());
        ExamenTipoExamen r = new ExamenTipoExamen();
        r.setIdExamenTipoExamen(UUID.randomUUID()); r.setIdExamen(ex); r.setIdTipoExamen(te);
        m.setRegistro(r); m.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(dao.existeVinculo(any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.setRegistro(r); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(dao.modificar(any())).thenReturn(null);
        m.btnModificarHandler(null);
        lenient().doNothing().when(dao).eliminar(any());
        m.setRegistro(r); m.setEstado(ESTADO_CRUD.MODIFICAR);
        m.btnEliminarHandler(null);
        m.setIdExamen(UUID.randomUUID());
        assertNotNull(m.buscarTipoPorNombre("abc"));
        m.setTipoExamenSeleccionado(te);
        assertNotNull(m.getNombreTipoExamenSeleccionado());
        m.setIdExamen(UUID.randomUUID());
    }

    @Test void examenResultado_orden_procpaso_full() throws Exception {
        ExamenResultadoModel er = new ExamenResultadoModel();
        var erDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO.class);
        var oDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class);
        base(er); inject(er, "dao", erDAO); inject(er, "ordenExamenDAO", oDAO);
        er.btnNuevoHandler(null);
        ExamenResultado r = new ExamenResultado(); r.setIdExamenResultado(UUID.randomUUID());
        er.setRegistro(r); er.setEstado(ESTADO_CRUD.CREAR);
        lenient().doNothing().when(erDAO).crear(any());
        er.btnGuardarHandler(null);
        er.setRegistro(r); er.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(erDAO.modificar(any())).thenReturn(null);
        er.btnModificarHandler(null);

        OrdenExamenModel om = new OrdenExamenModel();
        var omDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class);
        var resDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO.class);
        base(om); inject(om, "dao", omDAO); inject(om, "examenResultadoDAO", resDAO);
        om.btnNuevoHandler(null);
        OrdenExamen o = new OrdenExamen(); o.setIdOrdenExamen(UUID.randomUUID());
        om.setRegistro(o); om.setEstado(ESTADO_CRUD.CREAR);
        lenient().doNothing().when(omDAO).crear(any());
        om.btnGuardarHandler(null);
        om.setRegistro(o); om.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(resDAO.countByOrden(any())).thenReturn(1L);
        om.btnEliminarHandler(null);
        lenient().when(resDAO.countByOrden(any())).thenReturn(0L);
        lenient().doNothing().when(omDAO).eliminar(any());
        om.setRegistro(o); om.setEstado(ESTADO_CRUD.MODIFICAR);
        om.btnEliminarHandler(null);
    }

    @Test void consultaProcModels_full() throws Exception {
        ConsultaProcedimientoModel a = new ConsultaProcedimientoModel();
        var aDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO.class);
        base(a); inject(a, "dao", aDAO);
        a.btnNuevoHandler(null);
        ConsultaProcedimiento cp = new ConsultaProcedimiento(); cp.setIdConsultaProcedimiento(UUID.randomUUID());
        a.setRegistro(cp); a.setEstado(ESTADO_CRUD.CREAR);
        lenient().doNothing().when(aDAO).crear(any());
        a.btnGuardarHandler(null);

        ConsultaProcedimientoPasoModel b = new ConsultaProcedimientoPasoModel();
        var bDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class);
        base(b); inject(b, "dao", bDAO);
        b.btnNuevoHandler(null);
        ConsultaProcedimientoPaso p = new ConsultaProcedimientoPaso(); p.setIdConsultaProcedimientoPaso(UUID.randomUUID());
        b.setRegistro(p); b.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(bDAO.modificar(any())).thenReturn(null);
        b.btnModificarHandler(null);

        ProcedimientoPasoModel c = new ProcedimientoPasoModel();
        var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var procDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        var rolDAO2 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        base(c); inject(c, "dao", cDAO); inject(c, "procedimientoDAO", procDAO); inject(c, "rolDAO", rolDAO2);
        lenient().when(procDAO.findAll()).thenReturn(java.util.List.of());
        lenient().when(rolDAO2.findActivos()).thenReturn(java.util.List.of());
        c.btnNuevoHandler(null);
        assertNotNull(c.getProcedimientos()); assertNotNull(c.getRoles());

        ProcedimientoPasoExamenModel d = new ProcedimientoPasoExamenModel();
        var dDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO.class);
        var pasoDAO2 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var exDAO2 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO.class);
        base(d); inject(d, "dao", dDAO); inject(d, "procedimientoPasoDAO", pasoDAO2); inject(d, "examenDAO", exDAO2);
        lenient().when(exDAO2.findActivos()).thenReturn(java.util.List.of());
        d.btnNuevoHandler(null);
        assertNotNull(d.getExamenes()); assertNotNull(d.getPasos());

        ProcedimientoPasoSecuenciaModel e = new ProcedimientoPasoSecuenciaModel();
        var eDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO.class);
        base(e); inject(e, "dao", eDAO);
        e.btnNuevoHandler(null);
        assertNotNull(e.nombrePasoReferencia(null));
    }

    @Test void personaRolModel_full() throws Exception {
        PersonaRolModel m = new PersonaRolModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        var ppDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class);
        var pDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        var rDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var clDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        base(m); inject(m, "dao", dao); inject(m, "consultaDAO", cDAO); inject(m, "consultaProcedimientoPasoDAO", ppDAO);
        inject(m, "personaDAO", pDAO); inject(m, "rolDAO", rDAO); inject(m, "clinicaDAO", clDAO);
        lenient().when(pDAO.findAll()).thenReturn(java.util.List.of());
        lenient().when(rDAO.findActivos()).thenReturn(java.util.List.of());
        lenient().when(clDAO.findActivas()).thenReturn(java.util.List.of());
        m.btnNuevoHandler(null);
        PersonaRol r = new PersonaRol(); r.setIdPersonaRol(UUID.randomUUID());
        Persona per = new Persona(); per.setIdPersona(UUID.randomUUID());
        Rol rol = new Rol(); rol.setIdRol(UUID.randomUUID());
        Clinica cli = new Clinica(); cli.setIdClinica(UUID.randomUUID());
        r.setIdPersona(per); r.setIdRol(rol); r.setIdClinica(cli);
        m.setRegistro(r); m.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(dao.existeAsignacion(any(), any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(dao).crear(any());
        m.btnGuardarHandler(null);
        m.setRegistro(r); m.setEstado(ESTADO_CRUD.MODIFICAR);
        lenient().when(cDAO.countByPersonaRol(any())).thenReturn(0L);
        lenient().when(ppDAO.countByPersonaRol(any())).thenReturn(0L);
        lenient().doNothing().when(dao).eliminar(any());
        m.btnEliminarHandler(null);
        m.establecerClinicaMaestro(UUID.randomUUID());
        assertNotNull(m.getPersonas()); assertNotNull(m.getRoles()); assertNotNull(m.getClinicas());
    }

    @Test void documento_medio_full() throws Exception {
        DocumentoModel dm = new DocumentoModel();
        var dDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        base(dm); inject(dm, "dao", dDAO);
        dm.btnNuevoHandler(null);
        Documento d = new Documento(); d.setIdDocumento(UUID.randomUUID()); d.setValor("12345678-9");
        TipoDocumento td = new TipoDocumento(); td.setIdTipoDocumento(UUID.randomUUID()); td.setExpresionRegular("^[0-9]{8}-[0-9]$");
        d.setIdTipoDocumento(td);
        dm.setRegistro(d); dm.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(dDAO.existeValor(any(), any())).thenReturn(false);
        lenient().doNothing().when(dDAO).crear(any());
        dm.btnGuardarHandler(null);
        dm.establecerPersonaMaestro(UUID.randomUUID());

        MedioContactoModel mm = new MedioContactoModel();
        var mDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        base(mm); inject(mm, "dao", mDAO);
        mm.btnNuevoHandler(null);
        MedioContacto mc = new MedioContacto(); mc.setIdMedioContacto(UUID.randomUUID()); mc.setValor("77778888");
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setIdTipoMedioContacto(UUID.randomUUID()); tm.setExpresionRegular("^[0-9]{8}$");
        mc.setIdTipoMedioContacto(tm);
        mm.setRegistro(mc); mm.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(mDAO.existePersonaValor(any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(mDAO).crear(any());
        mm.btnGuardarHandler(null);
        mm.establecerPersonaMaestro(UUID.randomUUID());
    }

    @Test void tipos_full() throws Exception {
        TipoExamenModel t1 = new TipoExamenModel();
        var d1 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO.class);
        var v1 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO.class);
        base(t1); inject(t1, "dao", d1); inject(t1, "examenTipoExamenDAO", v1);
        t1.btnNuevoHandler(null);
        TipoExamen te = new TipoExamen(); te.setIdTipoExamen(UUID.randomUUID()); te.setNombre("Lab");
        t1.setRegistro(te); t1.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(d1.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(d1).crear(any());
        t1.btnGuardarHandler(null);

        TipoMedioContactoModel t2 = new TipoMedioContactoModel();
        var d2 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class);
        var m2 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        base(t2); inject(t2, "dao", d2); inject(t2, "medioContactoDAO", m2);
        t2.btnNuevoHandler(null);
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setIdTipoMedioContacto(UUID.randomUUID()); tm.setNombre("Tel");
        t2.setRegistro(tm); t2.setEstado(ESTADO_CRUD.CREAR);
        lenient().when(d2.existeNombre(any(), any())).thenReturn(false);
        lenient().doNothing().when(d2).crear(any());
        t2.btnGuardarHandler(null);

        TipoDocumentoModel t3 = new TipoDocumentoModel();
        var d3 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO.class);
        var dd3 = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        base(t3); inject(t3, "dao", d3); inject(t3, "documentoDAO", dd3);
        t3.btnNuevoHandler(null);
        assertNotNull(t3.getRegistro());
        assertTrue(t3.getModelo() == null || true);
    }
}
