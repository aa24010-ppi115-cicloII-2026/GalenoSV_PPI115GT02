package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ConsultaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.PersonaModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.PersonaRolModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf.ProcedimientoModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValidDataTest {
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

    @Test void persona_agregarRol_valido() throws Exception {
        PersonaModel m = new PersonaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class);
        var rolDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO.class);
        var cliDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        base(m); inject(m, "dao", dao); inject(m, "rolDAO", rolDAO);
        inject(m, "clinicaDAO", cliDAO); inject(m, "personaRolDAO", prDAO);
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID()); p.setNombres("Juan");
        m.setRegistro(p);
        m.iniciarCapturaRol();
        UUID idRol = UUID.randomUUID(), idCli = UUID.randomUUID();
        Rol rol = new Rol(); rol.setIdRol(idRol); rol.setActivo(true); rol.setNombre("Paciente");
        Clinica cli = new Clinica(); cli.setIdClinica(idCli); cli.setActivo(true); cli.setNombre("Central");
        lenient().when(rolDAO.find(idRol)).thenReturn(rol);
        lenient().when(cliDAO.find(idCli)).thenReturn(cli);
        lenient().when(prDAO.existeAsignacion(any(), any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(prDAO).crear(any());
        inject(m, "idRolSeleccionado", idRol.toString());
        inject(m, "idClinicaSeleccionadaRol", idCli.toString());
        m.agregarRol();
        m.modificarRol();
        PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(UUID.randomUUID());
        m.eliminarRol(pr);
        m.setRolSeleccionado(pr);
        var ev = mock(org.primefaces.event.SelectEvent.class);
        lenient().when(ev.getObject()).thenReturn(pr);
        m.onRolSelect(ev);
        assertNotNull(m.getRolSeleccionado());
    }

    @Test void persona_agregarMedio_valido() throws Exception {
        PersonaModel m = new PersonaModel();
        var tmDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO.class);
        var medDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO.class);
        base(m); inject(m, "dao", mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO.class));
        inject(m, "tipoMedioContactoDAO", tmDAO);
        Persona p = new Persona(); p.setIdPersona(UUID.randomUUID());
        m.setRegistro(p);
        m.iniciarCapturaMedio();
        UUID idTipo = UUID.randomUUID();
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setIdTipoMedioContacto(idTipo);
        tm.setNombre("Tel"); tm.setExpresionRegular("^[0-9]+$"); tm.setActivo(true);
        lenient().when(tmDAO.find(idTipo)).thenReturn(tm);
        lenient().when(medDAO.existePersonaValor(any(), any(), any())).thenReturn(false);
        lenient().doNothing().when(medDAO).crear(any());
        inject(m, "idTipoMedioContactoSeleccionado", idTipo.toString());
        var nuevo = new MedioContacto(); nuevo.setValor("77778888");
        inject(m, "nuevoMedioContacto", nuevo);
        m.agregarMedioContacto();
        m.modificarMedioContacto();
        MedioContacto mc = new MedioContacto(); mc.setIdMedioContacto(UUID.randomUUID()); mc.setValor("7777");
        m.eliminarMedioContacto(mc);
    }

    @Test void consulta_guardar_valido() throws Exception {
        ConsultaModel m = new ConsultaModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO.class);
        var prDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        var bean = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean.class);
        var cpDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO.class);
        var cppDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO.class);
        var ordDAO = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO.class);
        base(m); inject(m, "dao", dao); inject(m, "personaRolDAO", prDAO);
        inject(m, "clinicaTrabajoBean", bean); inject(m, "consultaProcedimientoDAO", cpDAO);
        inject(m, "consultaProcedimientoPasoDAO", cppDAO); inject(m, "ordenExamenDAO", ordDAO);
        UUID idCli = UUID.randomUUID(), idPR = UUID.randomUUID();
        Clinica cli = new Clinica(); cli.setIdClinica(idCli);
        Rol rol = new Rol(); rol.setActivo(true);
        PersonaRol pr = new PersonaRol(); pr.setIdPersonaRol(idPR); pr.setIdClinica(cli); pr.setIdRol(rol);
        lenient().when(bean.getIdClinicaActual()).thenReturn(idCli);
        lenient().when(bean.isSeleccionada()).thenReturn(true);
        lenient().when(prDAO.find(idPR)).thenReturn(pr);
        lenient().doNothing().when(dao).crear(any());
        lenient().when(cpDAO.findByConsulta(any())).thenReturn(List.of());
        Consulta c = new Consulta(); c.setIdConsulta(UUID.randomUUID()); c.setFechaInicio(OffsetDateTime.now());
        m.setRegistro(c);
        inject(m, "idPersonaRolSeleccionado", idPR.toString());
        m.btnGuardarHandler(null);
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
        lenient().when(dao.modificar(any())).thenReturn(c);
        m.btnModificarHandler(null);
        m.selectionHandler(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class), mock(jakarta.faces.component.behavior.Behavior.class), c));
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
    }

    @Test void procedimiento_guardarPaso_valido() throws Exception {
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
        proc.setNombre("Limpieza"); proc.setProcedimientoPasoList(new java.util.ArrayList<>());
        m.setRegistro(proc);
        m.prepararNuevoPaso();
        var paso = m.getNuevoPaso(); paso.setNombre("Recepcion");
        UUID idRol = UUID.randomUUID();
        Rol rol = new Rol(); rol.setIdRol(idRol); rol.setActivo(true);
        lenient().when(rolDAO.find(idRol)).thenReturn(rol);
        inject(m, "idRolSeleccionadoPaso", idRol.toString());
        lenient().doNothing().when(pasoDAO).crear(any());
        m.guardarPasoCompleto();
        m.eliminarPasoCompleto(new ProcedimientoPaso());
    }

    @Test void abstract_lazy_y_personaRol_validar() throws Exception {
        PersonaRolModel m = new PersonaRolModel();
        var dao = mock(sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO.class);
        base(m); inject(m, "dao", dao);
        m.inicializarRegistros();
        assertNotNull(m.getModelo());
        m.getModelo().getRowKey(null);
        m.getModelo().getRowData(null);
        assertEquals(0, m.getModelo().count(null));
        assertTrue(m.getModelo().load(0, 10, null, null).isEmpty());
        lenient().when(dao.count()).thenReturn(5L);
        assertEquals(5, m.getModelo().count(null));
        m.selectionHandler(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class), mock(jakarta.faces.component.behavior.Behavior.class), new PersonaRol()));
        assertEquals(ESTADO_CRUD.MODIFICAR, m.getEstado());
        m.setRegistro(new PersonaRol()); m.setEstado(ESTADO_CRUD.CREAR);
        m.btnGuardarHandler(null);
    }
}
