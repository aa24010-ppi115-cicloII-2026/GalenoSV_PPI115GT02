package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 * Arma el contenido del comprobante de una cita: datos del paciente, clinica,
 * procedimientos, pasos realizados, quien los realizo y cuando. Solo consulta
 * las tablas existentes; no modifica informacion.
 */
@Stateless
public class TicketService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private ConsultaDAO consultaDAO;
    @Inject
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;
    @Inject
    private ProcedimientoDAO procedimientoDAO;
    @Inject
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @Inject
    private ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    @Inject
    private OrdenExamenDAO ordenExamenDAO;

    /** Lineas del comprobante; devuelve null cuando la consulta no existe. */
    public List<String> lineas(UUID idConsulta) {
        Consulta consulta = consultaDAO.find(idConsulta);
        if (consulta == null) {
            return null;
        }
        List<String> lineas = new ArrayList<>();
        PersonaRol personaRol = consulta.getIdPersonaRol();
        Persona persona = personaRol == null ? null : personaRol.getIdPersona();
        Clinica clinica = personaRol == null ? null : personaRol.getIdClinica();

        lineas.add("Paciente: " + nombre(persona));
        lineas.add("Clinica: " + (clinica == null ? "-" : clinica.getNombre()));
        lineas.add("Fecha de la cita: " + fecha(consulta.getFechaInicio()));
        lineas.add("Motivo: " + (consulta.getObservaciones() == null || consulta.getObservaciones().isBlank()
                ? "Consulta registrada" : consulta.getObservaciones()));
        lineas.add("Estado: " + estado(consulta));
        lineas.add("Ticket: #" + consulta.getIdConsulta());
        lineas.add("");
        lineas.add("PROCEDIMIENTOS Y PASOS");

        List<ConsultaProcedimiento> procedimientos = consultaProcedimientoDAO.findByConsulta(idConsulta);
        if (procedimientos.isEmpty()) {
            lineas.add("La consulta no tiene procedimientos asignados.");
        }
        for (ConsultaProcedimiento procedimiento : procedimientos) {
            Procedimiento definicion = procedimiento.getIdProcedimiento() == null
                    ? null : procedimientoDAO.find(procedimiento.getIdProcedimiento());
            lineas.add("- " + (definicion == null ? "Procedimiento" : definicion.getNombre()));
            for (Object[] paso : pasosDe(procedimiento)) {
                ProcedimientoPaso definicionPaso = (ProcedimientoPaso) paso[0];
                ConsultaProcedimientoPaso registro = (ConsultaProcedimientoPaso) paso[1];
                lineas.add("   " + (definicionPaso == null ? "Paso" : definicionPaso.getNombre())
                        + " | " + (registro == null ? "Pendiente" : registro.getEstado())
                        + " | " + (registro == null ? "Sin fecha" : fecha(registro.getFechaInicio()))
                        + " | " + quien(registro));
            }
            for (String orden : ordenesDe(procedimiento)) {
                lineas.add("   " + orden);
            }
        }
        lineas.add("");
        lineas.add("Documento generado por el backend de GalenoSV.");
        return lineas;
    }

    /** Empareja cada paso del procedimiento con su registro de atencion. */
    private List<Object[]> pasosDe(ConsultaProcedimiento procedimiento) {
        List<ProcedimientoPaso> definiciones = procedimiento.getIdProcedimiento() == null
                ? new ArrayList<>()
                : procedimientoPasoDAO.findByProcedimiento(procedimiento.getIdProcedimiento());
        List<ProcedimientoPaso> ordenados = ordenar(definiciones);
        List<ConsultaProcedimientoPaso> registros = consultaProcedimientoPasoDAO
                .findByConsultaProcedimiento(procedimiento.getIdConsultaProcedimiento());

        List<Object[]> resultado = new ArrayList<>();
        for (int indice = 0; indice < ordenados.size(); indice++) {
            ProcedimientoPaso definicion = ordenados.get(indice);
            ConsultaProcedimientoPaso registro = indice < registros.size() ? registros.get(indice) : null;
            resultado.add(new Object[]{definicion, registro});
        }
        for (int indice = ordenados.size(); indice < registros.size(); indice++) {
            resultado.add(new Object[]{null, registros.get(indice)});
        }
        return resultado;
    }

    /** Ordena los pasos siguiendo las relaciones DESPUES_DE ya registradas. */
    private List<ProcedimientoPaso> ordenar(List<ProcedimientoPaso> definiciones) {
        List<ProcedimientoPasoSecuencia> secuencias = secuenciaDAO.findAll();
        Map<UUID, UUID> siguiente = new LinkedHashMap<>();
        for (ProcedimientoPasoSecuencia secuencia : secuencias) {
            if (secuencia.getIdProcedimientoPaso() == null || secuencia.getIdProcedimientoPasoReferencia() == null) {
                continue;
            }
            if (!"DESPUES_DE".equalsIgnoreCase(String.valueOf(secuencia.getTipoSecuencia()))) {
                continue;
            }
            siguiente.put(secuencia.getIdProcedimientoPasoReferencia(),
                    secuencia.getIdProcedimientoPaso().getIdProcedimientoPaso());
        }
        Map<UUID, ProcedimientoPaso> porId = new LinkedHashMap<>();
        for (ProcedimientoPaso definicion : definiciones) {
            porId.put(definicion.getIdProcedimientoPaso(), definicion);
        }
        List<ProcedimientoPaso> resultado = new ArrayList<>();
        java.util.Set<UUID> visitados = new java.util.HashSet<>();
        for (ProcedimientoPaso definicion : definiciones) {
            recorrer(porId, siguiente, definicion.getIdProcedimientoPaso(), visitados, resultado);
        }
        for (ProcedimientoPaso definicion : definiciones) {
            recorrer(porId, siguiente, definicion.getIdProcedimientoPaso(), visitados, resultado);
        }
        return resultado;
    }

    private void recorrer(Map<UUID, ProcedimientoPaso> porId, Map<UUID, UUID> siguiente, UUID id,
            java.util.Set<UUID> visitados, List<ProcedimientoPaso> resultado) {
        if (id == null || !visitados.add(id)) {
            return;
        }
        ProcedimientoPaso definicion = porId.get(id);
        if (definicion != null) {
            resultado.add(definicion);
        }
        recorrer(porId, siguiente, siguiente.get(id), visitados, resultado);
    }

    /** Ordenes de examen generadas en los pasos de un procedimiento. */
    private List<String> ordenesDe(ConsultaProcedimiento procedimiento) {
        List<String> ordenes = new ArrayList<>();
        for (ConsultaProcedimientoPaso registro : consultaProcedimientoPasoDAO
                .findByConsultaProcedimiento(procedimiento.getIdConsultaProcedimiento())) {
            for (OrdenExamen orden : ordenExamenDAO.findByConsultaProcedimientoPaso(registro.getIdConsultaProcedimientoPaso())) {
                ordenes.add("Examen ordenado: " + (orden.getIndicaciones() == null ? "Sin indicaciones" : orden.getIndicaciones()));
            }
        }
        return ordenes;
    }

    private String estado(Consulta consulta) {
        List<ConsultaProcedimientoPaso> registros = new ArrayList<>();
        for (ConsultaProcedimiento procedimiento : consultaProcedimientoDAO.findByConsulta(consulta.getIdConsulta())) {
            registros.addAll(consultaProcedimientoPasoDAO.findByConsultaProcedimiento(procedimiento.getIdConsultaProcedimiento()));
        }
        if (registros.isEmpty()) {
            return "Programada";
        }
        for (ConsultaProcedimientoPaso registro : registros) {
            if (!"FINALIZADO".equalsIgnoreCase(String.valueOf(registro.getEstado()))) {
                return "En proceso";
            }
        }
        return consulta.getFechaFin() != null ? "Finalizada" : "Completada";
    }

    private String quien(ConsultaProcedimientoPaso registro) {
        if (registro == null || registro.getIdPersonaRol() == null) {
            return "Sin asignar";
        }
        Persona persona = registro.getIdPersonaRol().getIdPersona();
        return persona == null ? "Sin asignar" : nombre(persona);
    }

    private String nombre(Persona persona) {
        if (persona == null) {
            return "-";
        }
        String completo = ((persona.getNombres() == null ? "" : persona.getNombres()) + " "
                + (persona.getApellidos() == null ? "" : persona.getApellidos())).trim();
        return completo.isEmpty() ? "-" : completo;
    }

    private String fecha(OffsetDateTime valor) {
        return valor == null ? "-" : FORMATO.format(valor);
    }
}
