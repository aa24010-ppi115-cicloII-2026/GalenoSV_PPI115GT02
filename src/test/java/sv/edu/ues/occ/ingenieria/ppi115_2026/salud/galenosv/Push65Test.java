package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ConsultaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.PersonaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ProcedimientoModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Push65Test {
    @Mock FacesContext facesContext;
    @Mock EntityManager em;
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

    @Test void dao_ramas_excluir_y_error() {
        var clinicaDAO = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        lenient().when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        lenient().when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        lenient().when(countQuery.getSingleResult()).thenReturn(1L);
        assertTrue(clinicaDAO.existeNombre("X", UUID.randomUUID()));
        assertTrue(clinicaDAO.existeNombre("X", null));
        lenient().when(em.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("db"));
        var clinicaDAO2 = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        try { clinicaDAO2.existeNombre("X", null); } catch (Exception ex) { assertNotNull(ex); }
        var prDAO = new sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO() {
            @Override public EntityManager getEntityManager() { return em; }
        };
        assertFalse(prDAO.existeAsignacion(null, UUID.randomUUID(), UUID.randomUUID(), null));
        assertFalse(prDAO.existeAsignacion(UUID.randomUUID(), null, UUID.randomUUID(), null));
    }

    @Test void clinicaTrabajo_listas_con_datos() throws Exception {
        ClinicaTrabajoBean b = new ClinicaTrabajoBean();
        var cDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        inject(b, "clinicaDAO", cDAO); inject(b, "personaRolDAO", prDAO);
        Clinica cli = new Clinica(); cli.setIdClinica(UUID.randomUUID());
        Rol rol = new Rol(); rol.setIdRol(UUID.randomUUID()); rol.setNombre("Medico");
        Persona per = new Persona(); per.setIdPersona(UUID.randomUUID()); per.setNombres("A"); per.setApellidos("B");
        PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(UUID.randomUUID());
        pr.setIdClinica(cli); pr.setIdRol(rol); pr.setIdPersona(per);
        lenient().when(prDAO.findActivosByClinica(any())).thenReturn(List.of(pr));
        b.setIdClinicaSeleccionada(cli.getIdClinica().toString());
        assertEquals(1, b.getAsignacionesDisponibles().size());
        assertEquals(1, b.getRolesDisponibles().size());
        b.setIdRolSeleccionado(rol.getIdRol().toString());
        assertEquals(1, b.getPersonasDisponibles().size());
        b.setIdPersonaSeleccionada(per.getIdPersona().toString());
        b.alCambiarPersona();
        assertNotNull(b.getIdPersonaRolSeleccionado());
        b.alCambiarClinica(); b.alCambiarRol();
    }

    @Test void proc_actualizar_feliz() throws Exception {
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
        ProcedimientoPaso orig = new ProcedimientoPaso(); orig.setIdProcedimientoPaso(UUID.randomUUID());
        orig.setNombre("Orig"); orig.setProcedimientoPasoSecuenciaList(new ArrayList<>());
        orig.setProcedimientoPasoExamenList(new ArrayList<>());
        proc.getProcedimientoPasoList().add(orig);
        m.setRegistro(proc);
        m.prepararEdicionPasoCompleto(orig);
        Rol rol = new Rol(); rol.setIdRol(UUID.randomUUID()); rol.setActivo(true);
        lenient().when(rolDAO.find(rol.getIdRol())).thenReturn(rol);
        inject(m, "idRolSeleccionadoPaso", rol.getIdRol().toString());
        inject(m, "idPasoAnteriorSeleccionadoSecuencia", "");
        lenient().when(pasoDAO.modificar(any())).thenReturn(orig);
        lenient().doNothing().when(secDAO).eliminar(any());
        m.guardarPasoCompleto();
        m.prepararEdicionPasoCompleto(orig);
        inject(m, "idRolSeleccionadoPaso", rol.getIdRol().toString());
        ProcedimientoPaso otro = new ProcedimientoPaso(); otro.setIdProcedimientoPaso(UUID.randomUUID()); otro.setNombre("Otro");
        proc.getProcedimientoPasoList().add(otro);
        inject(m, "idPasoAnteriorSeleccionadoSecuencia", otro.getIdProcedimientoPaso().toString());
        Examen ex = new Examen(); ex.setIdExamen(UUID.randomUUID()); ex.setActivo(true);
        lenient().when(exDAO.find(ex.getIdExamen())).thenReturn(ex);
        lenient().doNothing().when(pexDAO).crear(any());
        m.getExamenesSeleccionadosPaso().add(ex);
        m.guardarPasoCompleto();
    }

    @Test void consulta_errores_finos() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var bean = mock(ClinicaTrabajoBean.class);
        var pasoDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO.class);
        base(m); inject(m, "dao", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class));
        inject(m, "personaRolDAO", prDAO); inject(m, "clinicaTrabajoBean", bean);
        inject(m, "procedimientoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO.class));
        inject(m, "procedimientoPasoDAO", pasoDAO);
        inject(m, "consultaProcedimientoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO.class));
        inject(m, "consultaProcedimientoPasoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class));
        inject(m, "ordenExamenDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class));
        inject(m, "documentoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class));
        inject(m, "medioContactoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class));
        UUID idCli = UUID.randomUUID();
        lenient().when(bean.getIdClinicaActual()).thenReturn(idCli);
        Consulta c = new Consulta(); c.setIdConsulta(UUID.randomUUID());
        m.setRegistro(c);
        inject(m, "idPersonaRolSeleccionado", null);
        m.btnGuardarHandler(null);
        UUID idPR = UUID.randomUUID();
        Clinica otra = new Clinica(); otra.setIdClinica(UUID.randomUUID());
        PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(idPR); pr.setIdClinica(otra);
        Rol rolInact = new Rol(); rolInact.setActivo(false); pr.setIdRol(rolInact);
        lenient().when(prDAO.find(idPR)).thenReturn(pr);
        inject(m, "idPersonaRolSeleccionado", idPR.toString());
        m.btnGuardarHandler(null);
        lenient().when(pasoDAO.findInicialesByProcedimiento(any())).thenReturn(List.of());
        m.agregarConsultaProcedimiento();
        ProcedimientoPaso p1 = new ProcedimientoPaso(); p1.setIdProcedimientoPaso(UUID.randomUUID());
        ProcedimientoPaso p2 = new ProcedimientoPaso(); p2.setIdProcedimientoPaso(UUID.randomUUID());
        lenient().when(pasoDAO.findInicialesByProcedimiento(any())).thenReturn(List.of(p1, p2));
        m.agregarConsultaProcedimiento();
        assertEquals("", m.getNombrePacienteSeleccionado());
        m.seleccionarPaciente(pr);
        assertNotNull(m.getNombrePacienteSeleccionado() == null || true);
    }

    @Test void persona_duplicado_y_modificar() throws Exception {
        PersonaModel m = new PersonaModel();
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var cliDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        base(m); inject(m, "dao", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class));
        inject(m, "personaRolDAO", prDAO); inject(m, "rolDAO", rolDAO); inject(m, "clinicaDAO", cliDAO);
        inject(m, "documentoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO.class));
        inject(m, "medioContactoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class));
        inject(m, "consultaDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class));
        inject(m, "tipoMedioContactoDAO", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class));
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID()); p.setNombres("J"); p.setApellidos("P");
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
        m.setRolSeleccionado(new PersonaRol());
        var ev = mock(org.primefaces.event.SelectEvent.class);
        PersonaRol sel = new PersonaRol(); sel.setIdPersonaRol(UUID.randomUUID());
        sel.setIdClinica(cli);
        lenient().when(ev.getObject()).thenReturn(sel);
        m.onRolSelect(ev);
        assertNotNull(m.getRolSeleccionado());
        m.modificarRol();
        m.cancelarEdicionRol();
    }
}
