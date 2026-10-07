package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EntitiesTest {
    @Test void clinica() {
        Clinica c = new Clinica();
        UUID id = UUID.randomUUID();
        c.setIdClinica(id); c.setNombre("Central"); c.setActivo(true);
        c.setTipo("Publica"); c.setComentarios("ok");
        assertEquals(id, c.getIdClinica());
        assertEquals("Central", c.getNombre());
        assertTrue(c.getActivo());
        assertEquals("Publica", c.getTipo());
        assertNotNull(c.toString());
    }

    @Test void persona() {
        Persona p = new Persona();
        p.setIdPersona(UUID.randomUUID()); p.setNombres("Juan"); p.setApellidos("Perez");
        p.setFechaNacimiento(OffsetDateTime.now()); p.setFechaCreacion(OffsetDateTime.now());
        assertEquals("Juan", p.getNombres());
        assertEquals("Perez", p.getApellidos());
    }

    @Test void rol_tipoClinica() {
        Rol r = new Rol(); r.setIdRol(UUID.randomUUID()); r.setNombre("Paciente"); r.setActivo(true);
        assertEquals("Paciente", r.getNombre());
        assertNotNull(TipoClinica.values());
        assertNotNull(TipoClinica.valueOf(TipoClinica.values()[0].name()).getEtiqueta());
        assertNotNull(TipoClinica.values()[0].getValor());
    }

    @Test void personaRol() {
        PersonaRol pr = new PersonaRol();
        pr.setIdPersonaRol(UUID.randomUUID()); pr.setFechaCreacion(OffsetDateTime.now());
        pr.setIdClinica(new Clinica()); pr.setIdPersona(new Persona()); pr.setIdRol(new Rol());
        assertNotNull(pr.getIdPersonaRol());
    }

    @Test void consulta() {
        Consulta c = new Consulta();
        c.setIdConsulta(UUID.randomUUID()); c.setFechaInicio(OffsetDateTime.now());
        c.setObservaciones("dolor"); c.setReferenciaExterna("ref");
        assertNotNull(c.getIdConsulta());
    }

    @Test void procedimiento() {
        Procedimiento p = new Procedimiento();
        p.setIdProcedimiento(UUID.randomUUID()); p.setNombre("Limpieza"); p.setActivo(true);
        assertEquals("Limpieza", p.getNombre());
        ProcedimientoPaso paso = new ProcedimientoPaso();
        paso.setIdProcedimientoPaso(UUID.randomUUID()); paso.setNombre("Paso1");
        assertEquals("Paso1", paso.getNombre());
        ProcedimientoPasoSecuencia s = new ProcedimientoPasoSecuencia();
        s.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
        assertNotNull(s.getIdProcedimientoPasoSecuencia());
        ProcedimientoPasoExamen pe = new ProcedimientoPasoExamen();
        pe.setIdProcedimientoPasoExamen(UUID.randomUUID());
        assertNotNull(pe.getIdProcedimientoPasoExamen());
    }

    @Test void examen_flujo() {
        Examen e = new Examen(); e.setIdExamen(UUID.randomUUID()); e.setNombre("Rx"); e.setActivo(true);
        assertEquals("Rx", e.getNombre());
        TipoExamen t = new TipoExamen(); t.setIdTipoExamen(UUID.randomUUID()); t.setNombre("Lab");
        assertEquals("Lab", t.getNombre());
        ExamenTipoExamen v = new ExamenTipoExamen(); v.setIdExamenTipoExamen(UUID.randomUUID());
        assertNotNull(v.getIdExamenTipoExamen());
        OrdenExamen o = new OrdenExamen(); o.setIdOrdenExamen(UUID.randomUUID());
        assertNotNull(o.getIdOrdenExamen());
        ExamenResultado r = new ExamenResultado(); r.setIdExamenResultado(UUID.randomUUID());
        r.setResultado("Normal");
        assertEquals("Normal", r.getResultado());
        ConsultaProcedimiento cp = new ConsultaProcedimiento();
        cp.setIdConsultaProcedimiento(UUID.randomUUID());
        assertNotNull(cp.getIdConsultaProcedimiento());
        ConsultaProcedimientoPaso cpp = new ConsultaProcedimientoPaso();
        cpp.setIdConsultaProcedimientoPaso(UUID.randomUUID());
        assertNotNull(cpp.getIdConsultaProcedimientoPaso());
    }

    @Test void documento_medio() {
        Documento d = new Documento(); d.setIdDocumento(UUID.randomUUID()); d.setValor("123");
        assertEquals("123", d.getValor());
        TipoDocumento td = new TipoDocumento(); td.setIdTipoDocumento(UUID.randomUUID()); td.setNombre("DUI");
        assertEquals("DUI", td.getNombre());
        MedioContacto m = new MedioContacto(); m.setIdMedioContacto(UUID.randomUUID()); m.setValor("7777");
        assertEquals("7777", m.getValor());
        TipoMedioContacto tm = new TipoMedioContacto(); tm.setIdTipoMedioContacto(UUID.randomUUID());
        tm.setNombre("Tel");
        assertEquals("Tel", tm.getNombre());
    }
}
