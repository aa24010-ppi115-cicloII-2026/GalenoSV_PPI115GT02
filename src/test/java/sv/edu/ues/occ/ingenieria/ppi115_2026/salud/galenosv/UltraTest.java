package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ConsultaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.PersonaModel;
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
public class UltraTest {
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

    @Test void proc_actualizar_y_ciclo_y_eliminar_con_hijos() throws Exception {
        ProcedimientoModel m = new ProcedimientoModel();
        var pasoDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        var secDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO.class);
        var pexDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var exDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO.class);
        base(m); inject(m, "dao", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class));
        inject(m, "pasoDAO", pasoDAO); inject(m, "secuenciaDAO", secDAO);
        inject(m, "pasoExamenDAO", pexDAO); inject(m, "rolDAO", rolDAO); inject(m, "examenDAO", exDAO);
        Procedimiento proc = new Procedimiento(); proc.setIdProcedimiento(UUID.randomUUID());
        proc.setProcedimientoPasoList(new ArrayList<>());
        ProcedimientoPaso a = new ProcedimientoPaso(); a.setIdProcedimientoPaso(UUID.randomUUID());
        a.setNombre("A"); a.setIndicaFin(false);
        a.setProcedimientoPasoSecuenciaList(new ArrayList<>());
        a.setProcedimientoPasoExamenList(new ArrayList<>());
        Rol rol = new Rol(); rol.setIdRol(UUID.randomUUID()); rol.setActivo(true); rol.setNombre("Doctor");
        a.setIdRol(rol);
        proc.getProcedimientoPasoList().add(a);
        m.setRegistro(proc);
        m.prepararEdicionPasoCompleto(a);
        assertTrue(m.isEditandoPaso());
        lenient().when(secDAO.findByPaso(any())).thenReturn(List.of());
        lenient().when(rolDAO.find(rol.getIdRol())).thenReturn(rol);
        inject(m, "idRolSeleccionadoPaso", rol.getIdRol().toString());
        inject(m, "idPasoAnteriorSeleccionadoSecuencia", a.getIdProcedimientoPaso().toString());
        m.guardarPasoCompleto();
        inject(m, "idPasoAnteriorSeleccionadoSecuencia", "");
        m.prepararEdicionPasoCompleto(a);
        inject(m, "idRolSeleccionadoPaso", rol.getIdRol().toString());
        m.guardarPasoCompleto();
        ProcedimientoPaso conHijos = new ProcedimientoPaso();
        conHijos.setIdProcedimientoPaso(UUID.randomUUID());
        lenient().when(secDAO.findDependientes(conHijos.getIdProcedimientoPaso())).thenReturn(List.of(new ProcedimientoPasoSecuencia()));
        lenient().when(pexDAO.findByPaso(any())).thenReturn(List.of());
        lenient().when(secDAO.findByPaso(any())).thenReturn(List.of());
        m.eliminarPasoCompleto(conHijos);
        m.buscarRolPorNombre("xx");
        m.buscarExamenPorNombre("xx");
        m.seleccionarRolPaso(rol);
        m.confirmarRolAutocompletado();
        assertNotNull(m.getNombreRolPaso());
    }

    @Test void consulta_filtros_match_y_errores() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var docDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class);
        var medDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        var bean = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean.class);
        base(m); inject(m, "dao", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class));
        inject(m, "personaRolDAO", prDAO); inject(m, "documentoDAO", docDAO);
        inject(m, "medioContactoDAO", medDAO); inject(m, "clinicaTrabajoBean", bean);
        inject(m, "procedimientoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class));
        Persona per = new Persona(); per.setIdPersona(UUID.randomUUID()); per.setNombres("Juan Perez"); per.setApellidos("Lopez");
        Rol r = new Rol(); r.setNombre("Paciente");
        PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(UUID.randomUUID()); pr.setIdPersona(per); pr.setIdRol(r);
        lenient().when(bean.getIdClinicaActual()).thenReturn(UUID.randomUUID());
        lenient().when(prDAO.findPersonasAtendiblesByClinica(any())).thenReturn(List.of(pr));
        Documento d = new Documento(); d.setValor("12345678-9");
        TipoDocumento td = new TipoDocumento(); td.setNombre("DUI"); d.setIdTipoDocumento(td);
        lenient().when(docDAO.findByPersona(any(), anyInt(), anyInt())).thenReturn(List.of(d));
        MedioContacto mc = new MedioContacto(); mc.setValor("77778888");
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setNombre("Tel"); mc.setIdTipoMedioContacto(tm);
        lenient().when(medDAO.findByPersona(any(), anyInt(), anyInt())).thenReturn(List.of(mc));
        m.setFiltroPaciente("juan"); m.setFiltroDocumento("1234"); m.setFiltroRol("paci"); m.setFiltroMedio("7777");
        assertEquals(1, m.getPacientesFiltrados().size());
        assertTrue(m.documentosPaciente(pr).contains("12345678-9"));
        assertTrue(m.mediosPaciente(pr).contains("77778888"));
        m.setFiltroPaciente("zzz");
        assertTrue(m.getPacientesFiltrados().isEmpty());
        m.limpiarFiltrosPaciente();
        assertNotNull(m.getPacientesFiltrados());
        ConsultaProcedimiento cp = new ConsultaProcedimiento(); cp.setIdConsultaProcedimiento(UUID.randomUUID());
        cp.setIdProcedimiento(UUID.randomUUID());
        assertEquals("Desconocido", m.nombreProcedimiento(UUID.randomUUID()));
        m.eliminarConsultaProcedimiento(cp);
        m.cancelarEdicionProc();
        m.setFechaDesde(null); m.setFechaHasta(null);
        m.aplicarFiltros();
    }

    @Test void persona_modificar_eliminar_feliz() throws Exception {
        PersonaModel m = new PersonaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var cliDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        var medDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        var tmDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class);
        base(m); inject(m, "dao", dao); inject(m, "personaRolDAO", prDAO);
        inject(m, "rolDAO", rolDAO); inject(m, "clinicaDAO", cliDAO);
        inject(m, "medioContactoDAO", medDAO); inject(m, "tipoMedioContactoDAO", tmDAO);
        inject(m, "documentoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class));
        inject(m, "consultaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class));
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID()); p.setNombres("Juan"); p.setApellidos("P");
        p.setPersonaRolList(new ArrayList<>()); p.setFechaCreacion(OffsetDateTime.now());
        m.setRegistro(p);
        m.iniciarCapturaRol();
        UUID idRol = UUID.randomUUID(), idCli = UUID.randomUUID();
        Rol rol = new Rol(); rol.setIdRol(idRol); rol.setActivo(true);
        Clinica cli = new Clinica(); cli.setIdClinica(idCli); cli.setActivo(true);
        lenient().when(rolDAO.find(idRol)).thenReturn(rol);
        lenient().when(cliDAO.find(idCli)).thenReturn(cli);
        lenient().when(prDAO.existeAsignacion(any(), any(), any(), any())).thenReturn(true);
        inject(m, "idRolSeleccionado", idRol.toString());
        inject(m, "idClinicaSeleccionadaRol", idCli.toString());
        m.agregarRol();
        lenient().when(prDAO.existeAsignacion(any(), any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(prDAO).crear(any());
        m.agregarRol();
        assertFalse(m.getRolesDePersona().isEmpty());
        m.modificarRol();
        PersonaRol pr = m.getRolesDePersona().get(0);
        m.eliminarRol(pr);
        m.iniciarCapturaMedio();
        UUID idTm = UUID.randomUUID();
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setIdTipoMedioContacto(idTm);
        tm.setExpresionRegular(".*"); tm.setActivo(true);
        lenient().when(tmDAO.find(idTm)).thenReturn(tm);
        lenient().when(medDAO.existePersonaValor(any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(medDAO).crear(any());
        inject(m, "idTipoMedioContactoSeleccionado", idTm.toString());
        inject(m, "nuevoMedioContacto", new MedioContacto());
        m.getNuevoMedioContacto().setValor("abc");
        m.agregarMedioContacto();
        m.modificarMedioContacto();
        m.eliminarMedioContacto(new MedioContacto());
        m.setFechaNacimiento(java.time.LocalDate.now());
        assertNotNull(m.getFechaNacimiento());
        m.btnModificarHandler(null);
    }
}
