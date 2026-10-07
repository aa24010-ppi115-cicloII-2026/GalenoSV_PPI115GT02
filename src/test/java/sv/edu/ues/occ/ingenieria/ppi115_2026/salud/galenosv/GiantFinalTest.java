package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ConsultaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ProcedimientoModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GiantFinalTest {
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

    @Test void procedimiento_paso_con_dependencia_y_examen() throws Exception {
        ProcedimientoModel m = new ProcedimientoModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        var pasoDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var secDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO.class);
        var pexDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var exDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO.class);
        base(m); inject(m, "dao", dao); inject(m, "pasoDAO", pasoDAO);
        inject(m, "secuenciaDAO", secDAO); inject(m, "pasoExamenDAO", pexDAO);
        inject(m, "rolDAO", rolDAO); inject(m, "examenDAO", exDAO);
        Procedimiento proc = new Procedimiento(); proc.setIdProcedimiento(UUID.randomUUID());
        proc.setNombre("P"); proc.setProcedimientoPasoList(new ArrayList<>());
        ProcedimientoPaso padre = new ProcedimientoPaso();
        padre.setIdProcedimientoPaso(UUID.randomUUID()); padre.setNombre("Padre");
        padre.setIndicaFin(false);
        proc.getProcedimientoPasoList().add(padre);
        m.setRegistro(proc);
        m.prepararNuevoPaso();
        m.getNuevoPaso().setNombre("Hijo");
        UUID idRol = UUID.randomUUID();
        Rol rol = new Rol(); rol.setIdRol(idRol); rol.setActivo(true);
        lenient().when(rolDAO.find(idRol)).thenReturn(rol);
        inject(m, "idRolSeleccionadoPaso", idRol.toString());
        inject(m, "idPasoAnteriorSeleccionadoSecuencia", padre.getIdProcedimientoPaso().toString());
        Examen ex = new Examen(); ex.setIdExamen(UUID.randomUUID()); ex.setActivo(true); ex.setNombre("Rx");
        lenient().when(exDAO.find(ex.getIdExamen())).thenReturn(ex);
        m.agregarExamenPaso(ex);
        lenient().doNothing().when(pasoDAO).crear(any());
        lenient().doNothing().when(secDAO).crear(any());
        lenient().doNothing().when(pexDAO).crear(any());
        m.guardarPasoCompleto();
        assertFalse(m.isCapturandoPaso());
        m.eliminarPasoCompleto(padre);
        ProcedimientoPaso fin = new ProcedimientoPaso();
        fin.setIdProcedimientoPaso(UUID.randomUUID()); fin.setNombre("Fin"); fin.setIndicaFin(true);
        m.prepararPasoDependiente(fin);
        m.prepararEdicionPasoCompleto(padre);
        assertTrue(m.isEditandoPaso());
        m.cancelarEdicionPaso();
    }

    @Test void consulta_agregarProc_feliz() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        var cpDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO.class);
        var cppDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class);
        var ordDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var procDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class);
        var pasoDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var bean = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean.class);
        base(m); inject(m, "dao", dao); inject(m, "consultaProcedimientoDAO", cpDAO);
        inject(m, "consultaProcedimientoPasoDAO", cppDAO); inject(m, "ordenExamenDAO", ordDAO);
        inject(m, "personaRolDAO", prDAO); inject(m, "procedimientoDAO", procDAO);
        inject(m, "procedimientoPasoDAO", pasoDAO); inject(m, "clinicaTrabajoBean", bean);
        UUID idCli = UUID.randomUUID(), idProc = UUID.randomUUID();
        lenient().when(bean.getIdClinicaActual()).thenReturn(idCli);
        Consulta c = new Consulta(); c.setIdConsulta(UUID.randomUUID());
        c.setFechaInicio(OffsetDateTime.now()); c.setConsultaProcedimientoList(new ArrayList<>());
        m.setRegistro(c);
        m.iniciarCapturaProc();
        Procedimiento proc = new Procedimiento(); proc.setIdProcedimiento(idProc); proc.setActivo(true); proc.setNombre("Limpieza");
        lenient().when(procDAO.find(idProc)).thenReturn(proc);
        ProcedimientoPaso inicial = new ProcedimientoPaso();
        inicial.setIdProcedimientoPaso(UUID.randomUUID()); inicial.setNombre("Inicial");
        Rol rol = new Rol(); rol.setIdRol(UUID.randomUUID()); rol.setActivo(true); rol.setNombre("Medico");
        inicial.setIdRol(rol);
        lenient().when(pasoDAO.findInicialesByProcedimiento(idProc)).thenReturn(List.of(inicial));
        PersonaRol resp = new PersonaRol(); resp.setIdPersonaRol(UUID.randomUUID());
        Persona per = new Persona(); per.setNombres("Juan"); per.setApellidos("P");
        resp.setIdPersona(per);
        lenient().when(prDAO.findResponsables(idCli, rol.getIdRol())).thenReturn(List.of(resp));
        lenient().doNothing().when(cpDAO).crear(any());
        lenient().doNothing().when(cppDAO).crear(any());
        inject(m, "idProcedimientoSeleccionado", idProc.toString());
        m.agregarConsultaProcedimiento();
        assertFalse(m.getConsultaProcedimientosDeConsulta().isEmpty());
        ConsultaProcedimiento cp = m.getConsultaProcedimientosDeConsulta().get(0);
        m.setProcSeleccionado(cp);
        var ev = mock(org.primefaces.event.SelectEvent.class);
        lenient().when(ev.getObject()).thenReturn(cp);
        m.onProcSelect(ev);
        inject(m, "idProcedimientoSeleccionado", idProc.toString());
        lenient().when(cpDAO.modificar(any())).thenReturn(null);
        m.modificarConsultaProcedimiento();
        m.eliminarConsultaProcedimiento(cp);
        assertEquals("Desconocido", m.nombreProcedimiento(UUID.randomUUID()));
        assertEquals("Limpieza", m.nombreProcedimiento(idProc));
    }
}
