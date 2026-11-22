package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

    /** Datos estructurados del comprobante; null cuando la consulta no existe. */
    public Datos datos(UUID idConsulta) {
        Consulta consulta = consultaDAO.find(idConsulta);
        if (consulta == null) {
            return null;
        }
        PersonaRol personaRol = consulta.getIdPersonaRol();
        Persona persona = personaRol == null ? null : personaRol.getIdPersona();
        Clinica clinica = personaRol == null ? null : personaRol.getIdClinica();

        List<ProcedimientoTicket> procedimientos = new ArrayList<>();
        for (ConsultaProcedimiento cp : consultaProcedimientoDAO.findByConsulta(idConsulta)) {
            Procedimiento def = cp.getIdProcedimiento() == null ? null : procedimientoDAO.find(cp.getIdProcedimiento());
            String nombreProc = def == null ? "Procedimiento" : def.getNombre();
            List<PasoTicket> pasos = new ArrayList<>();
<<<<<<< HEAD
=======
            Map<String, String> notas = notasPorPaso(cp.getObservaciones());
>>>>>>> v2modificada
            boolean actualAsignado = false;
            for (Object[] par : pasosDe(cp)) {
                ProcedimientoPaso dp = (ProcedimientoPaso) par[0];
                ConsultaProcedimientoPaso registro = (ConsultaProcedimientoPaso) par[1];
<<<<<<< HEAD
=======
                // Mismas etiquetas que la tarjeta de la cita en el frontend: solo el primer paso sin
                // terminar está en curso. Un paso posterior puede conservar su registro de antes de
                // retroceder; queda pendiente y ese registro no se muestra.
>>>>>>> v2modificada
                String etiqueta = "Pendiente";
                ConsultaProcedimientoPaso visible = registro;
                if (finalizado(registro)) {
                    etiqueta = "Completado";
                } else if (!actualAsignado) {
                    etiqueta = registro != null ? "En curso" : "Por iniciar";
                    actualAsignado = true;
                } else {
                    visible = null;
                }
                pasos.add(new PasoTicket(
                        dp == null ? "Paso" : dp.getNombre(),
                        etiqueta,
<<<<<<< HEAD
                        registro == null ? "Sin fecha" : fecha(registro.getFechaInicio()),
                        quien(registro),
                        ""));
            }
            procedimientos.add(new ProcedimientoTicket(nombreProc, pasos, ordenesDe(cp)));
        }

        String motivo = consulta.getObservaciones() == null || consulta.getObservaciones().isBlank()
                ? "Consulta registrada" : consulta.getObservaciones();
        return new Datos(String.valueOf(consulta.getIdConsulta()), nombre(persona),
                clinica == null ? "-" : clinica.getNombre(),
                fecha(consulta.getFechaInicio()), motivo, estado(consulta), procedimientos);
    }

    /** Lineas del comprobante; devuelve null cuando la consulta no existe. */
    public List<String> lineas(UUID idConsulta) {
        Datos datos = datos(idConsulta);
        if (datos == null) {
            return null;
        }
        List<String> lineas = new ArrayList<>();
        lineas.add("Paciente: " + datos.paciente);
        lineas.add("Clinica: " + datos.clinica);
        lineas.add("Fecha de la cita: " + datos.fecha);
        lineas.add("Motivo: " + datos.motivo);
        lineas.add("Estado: " + datos.estado);
        lineas.add("Ticket: #" + datos.idConsulta);
        lineas.add("");
        lineas.add("PROCEDIMIENTOS Y PASOS");
        if (datos.procedimientos.isEmpty()) {
            lineas.add("La consulta no tiene procedimientos asignados.");
        }
        for (ProcedimientoTicket p : datos.procedimientos) {
            lineas.add("- " + p.nombre);
            for (PasoTicket paso : p.pasos) {
                lineas.add("   " + paso.nombre + " | " + paso.estado + " | " + paso.fecha + " | " + paso.realizadoPor);
            }
            for (String orden : p.ordenes) {
                lineas.add("   " + orden);
=======
                        visible == null ? "Sin fecha" : fecha(visible.getFechaInicio()),
                        quien(visible),
                        dp == null ? "" : notas.getOrDefault(String.valueOf(dp.getIdProcedimientoPaso()), "")));
>>>>>>> v2modificada
            }
            procedimientos.add(new ProcedimientoTicket(nombreProc, pasos, ordenesDe(cp)));
        }

        String motivo = consulta.getObservaciones() == null || consulta.getObservaciones().isBlank()
                ? "Consulta registrada" : consulta.getObservaciones();
        // Mismo código corto que muestra el frontend.
        String codigo = String.valueOf(consulta.getIdConsulta()).split("-")[0].toUpperCase();
        return new Datos(codigo, nombre(persona),
                clinica == null ? "-" : clinica.getNombre(),
                fecha(consulta.getFechaInicio()), motivo, estado(consulta), procedimientos);
    }

    public static class Datos {
        public final String codigo;
        public final String paciente;
        public final String clinica;
        public final String fecha;
        public final String motivo;
        public final String estado;
        public final List<ProcedimientoTicket> procedimientos;

        public Datos(String codigo, String paciente, String clinica, String fecha, String motivo, String estado,
                List<ProcedimientoTicket> procedimientos) {
            this.codigo = codigo;
            this.paciente = paciente;
            this.clinica = clinica;
            this.fecha = fecha;
            this.motivo = motivo;
            this.estado = estado;
            this.procedimientos = procedimientos;
        }
    }

    public static class ProcedimientoTicket {
        public final String nombre;
        public final List<PasoTicket> pasos;
        public final List<String> ordenes;

        public ProcedimientoTicket(String nombre, List<PasoTicket> pasos, List<String> ordenes) {
            this.nombre = nombre;
            this.pasos = pasos;
            this.ordenes = ordenes;
        }
    }

    public static class PasoTicket {
        public final String nombre;
        public final String estado;
        public final String fecha;
        public final String realizadoPor;
        public final String observaciones;

        public PasoTicket(String nombre, String estado, String fecha, String realizadoPor, String observaciones) {
            this.nombre = nombre;
            this.estado = estado;
            this.fecha = fecha;
            this.realizadoPor = realizadoPor;
            this.observaciones = observaciones;
        }
    }

    public static class Datos {
        public final String idConsulta;
        public final String paciente;
        public final String clinica;
        public final String fecha;
        public final String motivo;
        public final String estado;
        public final List<ProcedimientoTicket> procedimientos;

        public Datos(String idConsulta, String paciente, String clinica, String fecha, String motivo, String estado,
                List<ProcedimientoTicket> procedimientos) {
            this.idConsulta = idConsulta;
            this.paciente = paciente;
            this.clinica = clinica;
            this.fecha = fecha;
            this.motivo = motivo;
            this.estado = estado;
            this.procedimientos = procedimientos;
        }
    }

    public static class ProcedimientoTicket {
        public final String nombre;
        public final List<PasoTicket> pasos;
        public final List<String> ordenes;

        public ProcedimientoTicket(String nombre, List<PasoTicket> pasos, List<String> ordenes) {
            this.nombre = nombre;
            this.pasos = pasos;
            this.ordenes = ordenes;
        }
    }

    public static class PasoTicket {
        public final String nombre;
        public final String estado;
        public final String fecha;
        public final String realizadoPor;
        public final String observaciones;

        public PasoTicket(String nombre, String estado, String fecha, String realizadoPor, String observaciones) {
            this.nombre = nombre;
            this.estado = estado;
            this.fecha = fecha;
            this.realizadoPor = realizadoPor;
            this.observaciones = observaciones;
        }
    }

    /** Empareja cada paso del procedimiento con su registro de atencion. */
    private List<Object[]> pasosDe(ConsultaProcedimiento procedimiento) {
        List<ProcedimientoPaso> definiciones = procedimiento.getIdProcedimiento() == null
                ? new ArrayList<>()
                : procedimientoPasoDAO.findByProcedimiento(procedimiento.getIdProcedimiento());
        List<ProcedimientoPaso> ordenados = ordenar(definiciones);
        // El DAO ordena por UUID; los registros se emparejan con los pasos en el orden en que se atendieron.
        List<ConsultaProcedimientoPaso> registros = new ArrayList<>(consultaProcedimientoPasoDAO
                .findByConsultaProcedimiento(procedimiento.getIdConsultaProcedimiento()));
        registros.sort(Comparator.comparing(ConsultaProcedimientoPaso::getFechaInicio,
                Comparator.nullsFirst(Comparator.naturalOrder())));

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
        java.util.Set<UUID> conPredecesor = new java.util.HashSet<>();
        for (Map.Entry<UUID, UUID> enlace : siguiente.entrySet()) {
            if (porId.containsKey(enlace.getKey())) {
                conPredecesor.add(enlace.getValue());
            }
        }
        List<ProcedimientoPaso> resultado = new ArrayList<>();
        java.util.Set<UUID> visitados = new java.util.HashSet<>();
        // Primero se recorre desde los pasos que no van después de otro.
        for (ProcedimientoPaso definicion : definiciones) {
            if (!conPredecesor.contains(definicion.getIdProcedimientoPaso())) {
                recorrer(porId, siguiente, definicion.getIdProcedimientoPaso(), visitados, resultado);
            }
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
                String indicaciones = orden.getIndicaciones() == null ? "Sin indicaciones" : orden.getIndicaciones();
                ordenes.add("Examen ordenado: " + indicaciones.replaceFirst("^Examen:\\s*", ""));
            }
        }
        return ordenes;
    }

    /**
     * Lo registrado al completar cada paso, guardado por el frontend en las observaciones del procedimiento:
     * {"pasos": {"idProcedimientoPaso": {"diagnostico", "notas", "plan"}}}. El borrador del paso en curso
     * (campos de primer nivel) no forma parte del comprobante.
     */
    static Map<String, String> notasPorPaso(String observaciones) {
        Map<String, String> resultado = new HashMap<>();
        if (observaciones == null || observaciones.isBlank()) {
            return resultado;
        }
        try (JsonReader lector = Json.createReader(new StringReader(observaciones))) {
            JsonValue valor = lector.readValue();
            JsonValue pasos = valor.getValueType() == JsonValue.ValueType.OBJECT ? valor.asJsonObject().get("pasos") : null;
            if (pasos == null || pasos.getValueType() != JsonValue.ValueType.OBJECT) {
                return resultado;
            }
            for (Map.Entry<String, JsonValue> paso : pasos.asJsonObject().entrySet()) {
                if (paso.getValue().getValueType() != JsonValue.ValueType.OBJECT) {
                    continue;
                }
                JsonObject registro = paso.getValue().asJsonObject();
                List<String> partes = new ArrayList<>();
                String diagnostico = registro.getString("diagnostico", "");
                String notas = registro.getString("notas", "");
                String plan = registro.getString("plan", "");
                if (!diagnostico.isBlank()) partes.add("CIE-10: " + diagnostico);
                if (!notas.isBlank()) partes.add("Notas: " + notas);
                if (!plan.isBlank()) partes.add("Plan: " + plan);
                resultado.put(paso.getKey(), String.join(" | ", partes));
            }
        } catch (JsonException ex) {
            // Observaciones en texto libre (registros antiguos): no hay notas por paso.
        }
        return resultado;
    }

    /** La cita está completada solo cuando cada paso definido tiene un registro finalizado. */
    private String estado(Consulta consulta) {
        boolean hayRegistros = false;
        boolean todosFinalizados = true;
        for (ConsultaProcedimiento procedimiento : consultaProcedimientoDAO.findByConsulta(consulta.getIdConsulta())) {
            for (Object[] paso : pasosDe(procedimiento)) {
                ConsultaProcedimientoPaso registro = (ConsultaProcedimientoPaso) paso[1];
                hayRegistros |= registro != null;
                todosFinalizados &= finalizado(registro);
            }
        }
        if (!hayRegistros) {
            return "Programada";
        }
        if (!todosFinalizados) {
            return "En proceso";
        }
        return consulta.getFechaFin() != null ? "Finalizada" : "Completada";
    }

    private static boolean finalizado(ConsultaProcedimientoPaso registro) {
        return registro != null && "FINALIZADO".equalsIgnoreCase(String.valueOf(registro.getEstado()));
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
