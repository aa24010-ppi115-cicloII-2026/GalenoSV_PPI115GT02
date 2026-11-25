package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ClinicaTrabajoBean;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.MensajeError;

@Named("consultaModel")
@ViewScoped
public class ConsultaModel extends AbstractModel<Consulta> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    FacesContext facesContext;

    @Inject
    ConsultaDAO dao;

    @Inject
    PersonaRolDAO personaRolDAO;

    @Inject
    sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO documentoDAO;

    @Inject
    sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO medioContactoDAO;

    @Inject
    ConsultaProcedimientoDAO consultaProcedimientoDAO;

    @Inject
    ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    @Inject
    OrdenExamenDAO ordenExamenDAO;

    @Inject
    ProcedimientoDAO procedimientoDAO;

    @Inject
    ProcedimientoPasoDAO procedimientoPasoDAO;

    @Inject
    ClinicaTrabajoBean clinicaTrabajoBean;

    private LocalDate fechaDesde = LocalDate.now(ZONA_LOCAL);
    private LocalDate fechaHasta = LocalDate.now(ZONA_LOCAL);

    // Selección del maestro
    private String idPersonaRolSeleccionado;
    private PersonaRol pacienteSeleccionado;
    /** Cuantos pacientes muestra como maximo una busqueda del dialogo. */
    static final int MAX_PACIENTES_BUSQUEDA = 50;

    /** null mientras no se haya buscado: el dialogo carga la lista la primera vez que se muestra. */
    private List<PersonaRol> pacientesEncontrados;
    private Map<UUID, String> documentosPorPersona = new HashMap<>();
    private Map<UUID, String> mediosPorPersona = new HashMap<>();

    private String filtroPaciente;
    private String filtroDocumento;
    private String filtroRol;
    private String filtroMedio;

    // Propiedades Pestaña 1 (Procedimiento)
    private ConsultaProcedimiento nuevoConsultaProcedimiento;
    private ConsultaProcedimiento procSeleccionado;
    private String idProcedimientoSeleccionado;
    private boolean editandoProc = false;
    private boolean capturandoProc = false;

    private void refrescarListasHijas() {
        if (this.registro == null || this.registro.getIdConsulta() == null) {
            return;
        }
        try {
            java.util.List<ConsultaProcedimiento> cps =
                    consultaProcedimientoDAO.findByConsulta(this.registro.getIdConsulta());
            this.registro.setConsultaProcedimientoList(cps);
            for (ConsultaProcedimiento cp : cps) {
                if (cp.getIdConsultaProcedimiento() == null) {
                    continue;
                }
                java.util.List<ConsultaProcedimientoPaso> pasos =
                        consultaProcedimientoPasoDAO.findByProcedimiento(cp.getIdConsultaProcedimiento());
                cp.setConsultaProcedimientoPasoList(pasos);
                for (ConsultaProcedimientoPaso paso : pasos) {
                    if (paso.getIdConsultaProcedimientoPaso() == null) {
                        continue;
                    }
                    paso.setOrdenExamenList(
                            ordenExamenDAO.findByPaso(paso.getIdConsultaProcedimientoPaso()));
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void validarEliminacion(Consulta r) {
        long n = r.getIdConsulta() == null ? 0 : consultaProcedimientoDAO.countByConsulta(r.getIdConsulta());
        if (n > 0) {
            throw new IllegalArgumentException("No se puede eliminar la consulta porque tiene " + n + " procedimiento(s) asociado(s)");
        }
    }

    public ConsultaModel() {
        this.nombreBean = "Consulta";
    }

    @Override
    public void inicializarRegistros() {
        this.modelo = new LazyDataModel<Consulta>() {
            @Override
            public String getRowKey(Consulta object) {
                return getIdAsText(object);
            }

            @Override
            public Consulta getRowData(String rowKey) {
                return getIdByText(rowKey);
            }

            @Override
            public int count(Map<String, FilterMeta> filtros) {
                return Math.toIntExact(dao.countByClinicaYFechas(clinicaTrabajoBean.getIdClinicaActual(),
                        inicioFiltro(), finFiltroExclusivo()));
            }

            @Override
            public List<Consulta> load(int first, int max, Map<String, SortMeta> orden,
                    Map<String, FilterMeta> filtros) {
                return dao.findByClinicaYFechas(clinicaTrabajoBean.getIdClinicaActual(),
                        inicioFiltro(), finFiltroExclusivo(), first, max);
            }
        };
    }

    public void aplicarFiltros() {
        if (fechaDesde == null || fechaHasta == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Faltan fechas", "Seleccione la fecha inicial y la final para filtrar"));
            return;
        }
        if (fechaHasta.isBefore(fechaDesde)) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Rango inválido", "La fecha final debe ser igual o posterior a la inicial"));
            return;
        }
        inicializarRegistros();
    }

    private OffsetDateTime inicioFiltro() {
        LocalDate desde = fechaDesde == null ? LocalDate.now(ZONA_LOCAL) : fechaDesde;
        return desde.atStartOfDay(ZONA_LOCAL).toOffsetDateTime();
    }

    private OffsetDateTime finFiltroExclusivo() {
        LocalDate hasta = fechaHasta == null ? LocalDate.now(ZONA_LOCAL) : fechaHasta;
        return hasta.plusDays(1).atStartOfDay(ZONA_LOCAL).toOffsetDateTime();
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<Consulta> getDao() {
        return dao;
    }

    @Override
    protected Consulta nuevoRegistro() {
        Consulta r = new Consulta();
        r.setIdConsulta(UUID.randomUUID());
        r.setFechaInicio(OffsetDateTime.now());
        return r;
    }

    @Override
    protected Consulta buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado) {
            return dao.find(buscado);
        }
        return null;
    }

    @Override
    protected String getIdAsText(Consulta r) {
        if (r != null && r.getIdConsulta() != null) {
            return r.getIdConsulta().toString();
        }
        return null;
    }

    @Override
    protected Consulta getIdByText(String id) {
        if (id != null) {
            try {
                return dao.find(UUID.fromString(id));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void selectionHandler(SelectEvent<Consulta> r) {
        super.selectionHandler(r);
        refrescarListasHijas();
        sincronizarSeleccion();
        cancelarEdicionProc();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        if (!clinicaTrabajoBean.isSeleccionada()) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Clínica requerida", "Seleccione primero la clínica de trabajo"));
            return;
        }
        super.btnNuevoHandler(e);
        this.idPersonaRolSeleccionado = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        this.idPersonaRolSeleccionado = null;
        cancelarEdicionProc();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.crear(this.registro);
                this.estado = ESTADO_CRUD.MODIFICAR;
                refrescarListasHijas();
                cancelarEdicionProc();
                                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Consulta guardada", "Ahora puede agregar los procedimientos de la consulta"));
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", MensajeError.de(e)));
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.modificar(this.registro);
                limpiar("Consulta modificada exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", MensajeError.de(e)));
            }
        }
    }

    // ─── PESTAÑA 1: ConsultaProcedimiento ───────────────────────────────────────

    public void prepararNuevoConsultaProcedimiento() {
        nuevoConsultaProcedimiento = new ConsultaProcedimiento();
        nuevoConsultaProcedimiento.setIdConsultaProcedimiento(UUID.randomUUID());
        nuevoConsultaProcedimiento.setFechaInicio(OffsetDateTime.now());
        idProcedimientoSeleccionado = null;
        capturandoProc = false;
    }

    public void iniciarCapturaProc() {
        prepararNuevoConsultaProcedimiento();
        capturandoProc = true;
    }

    public void onProcSelect(SelectEvent<ConsultaProcedimiento> event) {
        procSeleccionado = event.getObject();
        nuevoConsultaProcedimiento = new ConsultaProcedimiento();
        nuevoConsultaProcedimiento.setIdConsultaProcedimiento(procSeleccionado.getIdConsultaProcedimiento());
        nuevoConsultaProcedimiento.setFechaInicio(procSeleccionado.getFechaInicio());
        nuevoConsultaProcedimiento.setIdConsulta(procSeleccionado.getIdConsulta());
        nuevoConsultaProcedimiento.setObservaciones(procSeleccionado.getObservaciones());
        
        if (procSeleccionado.getIdProcedimiento() != null) {
            idProcedimientoSeleccionado = procSeleccionado.getIdProcedimiento().toString();
        }
        editandoProc = true;
        capturandoProc = true;
    }

    public void cancelarEdicionProc() {
        procSeleccionado = null;
        editandoProc = false;
        capturandoProc = false;
        prepararNuevoConsultaProcedimiento();
    }

    public void modificarConsultaProcedimiento() {
        if (this.registro == null) return;
        try {
            if (idProcedimientoSeleccionado == null || idProcedimientoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un procedimiento");
            }
            nuevoConsultaProcedimiento.setIdProcedimiento(UUID.fromString(idProcedimientoSeleccionado));
            consultaProcedimientoDAO.modificar(nuevoConsultaProcedimiento);

            if (procSeleccionado != null && this.registro.getConsultaProcedimientoList() != null) {
                this.registro.getConsultaProcedimientoList().remove(procSeleccionado);
            }
            if (this.registro.getConsultaProcedimientoList() == null) {
                this.registro.setConsultaProcedimientoList(new ArrayList<>());
            }
            this.registro.getConsultaProcedimientoList().add(nuevoConsultaProcedimiento);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Procedimiento modificado"));
            cancelarEdicionProc();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", MensajeError.de(e)));
        }
    }

    public void agregarConsultaProcedimiento() {
        if (this.registro == null || this.registro.getIdConsulta() == null) return;
        try {
            if (idProcedimientoSeleccionado == null || idProcedimientoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un procedimiento");
            }
            Procedimiento procedimiento = procedimientoDAO.find(UUID.fromString(idProcedimientoSeleccionado));
            if (procedimiento == null || !Boolean.TRUE.equals(procedimiento.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden agregar procedimientos activos");
            }
            ProcedimientoPaso pasoInicial = obtenerPasoInicial(procedimiento);
            List<PersonaRol> responsables = personaRolDAO.findResponsables(
                    clinicaTrabajoBean.getIdClinicaActual(), pasoInicial.getIdRol().getIdRol());
            if (responsables.isEmpty()) {
                throw new IllegalArgumentException("No hay personal activo con el rol "
                        + pasoInicial.getIdRol().getNombre() + " en la clínica de trabajo");
            }

            nuevoConsultaProcedimiento.setIdConsulta(this.registro);
            nuevoConsultaProcedimiento.setIdProcedimiento(procedimiento.getIdProcedimiento());
            consultaProcedimientoDAO.crear(nuevoConsultaProcedimiento);

            ConsultaProcedimientoPaso instanciaInicial = new ConsultaProcedimientoPaso();
            instanciaInicial.setIdConsultaProcedimientoPaso(UUID.randomUUID());
            instanciaInicial.setIdConsultaProcedimiento(nuevoConsultaProcedimiento);
            instanciaInicial.setIdPersonaRol(responsables.get(0));
            instanciaInicial.setFechaInicio(OffsetDateTime.now());
            instanciaInicial.setEstado("PENDIENTE");
            consultaProcedimientoPasoDAO.crear(instanciaInicial);
            nuevoConsultaProcedimiento.setConsultaProcedimientoPasoList(new ArrayList<>());
            nuevoConsultaProcedimiento.getConsultaProcedimientoPasoList().add(instanciaInicial);
            
            if (this.registro.getConsultaProcedimientoList() == null) {
                this.registro.setConsultaProcedimientoList(new ArrayList<>());
            }
            this.registro.getConsultaProcedimientoList().add(nuevoConsultaProcedimiento);
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    "Procedimiento agregado; el paso inicial '" + pasoInicial.getNombre()
                            + "' fue asignado a " + responsables.get(0).getIdPersona().getNombres()));
            prepararNuevoConsultaProcedimiento();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", MensajeError.de(e)));
        }
    }

    public List<ConsultaProcedimiento> getConsultaProcedimientosDeConsulta() {
        if (this.registro != null && this.registro.getConsultaProcedimientoList() != null) {
            return this.registro.getConsultaProcedimientoList();
        }
        return new ArrayList<>();
    }

    // ─── UTILIDADES ─────────────────────────────────────────────────────────────

    private void prepararRelaciones() {
        if (idPersonaRolSeleccionado == null || idPersonaRolSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un paciente");
        }
        PersonaRol personaRol = personaRolDAO.find(UUID.fromString(idPersonaRolSeleccionado));
        if (personaRol == null || personaRol.getIdClinica() == null
                || clinicaTrabajoBean.getIdClinicaActual() == null
                || !clinicaTrabajoBean.getIdClinicaActual().equals(personaRol.getIdClinica().getIdClinica())
                || personaRol.getIdRol() == null || !Boolean.TRUE.equals(personaRol.getIdRol().getActivo())) {
            throw new IllegalArgumentException("La persona debe tener un rol activo en la clínica de trabajo");
        }
        registro.setIdPersonaRol(personaRol);
    }

    private ProcedimientoPaso obtenerPasoInicial(Procedimiento procedimiento) {
        List<ProcedimientoPaso> iniciales = procedimientoPasoDAO
                .findInicialesByProcedimiento(procedimiento.getIdProcedimiento());
        if (iniciales.size() != 1) {
            throw new IllegalArgumentException("El procedimiento debe tener exactamente un paso inicial");
        }
        ProcedimientoPaso inicial = iniciales.get(0);
        if (inicial.getIdRol() == null || !Boolean.TRUE.equals(inicial.getIdRol().getActivo())) {
            throw new IllegalArgumentException("El paso inicial debe tener un rol activo responsable");
        }
        return inicial;
    }

    private void sincronizarSeleccion() {
        if (this.registro != null && this.registro.getIdPersonaRol() != null) {
            this.idPersonaRolSeleccionado = this.registro.getIdPersonaRol().getIdPersonaRol().toString();
        }
    }

    private void limpiar(String mensaje) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        this.idPersonaRolSeleccionado = null;
        cancelarEdicionProc();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    public String formatearFecha(OffsetDateTime fecha) {
        return fecha == null ? "" : fecha.atZoneSameInstant(ZONA_LOCAL).format(FORMATO_FECHA);
    }

    // ─── LISTAS PARA COMBOS ──────────────────────────────────────────────────────

    /**
     * Busca en el dialogo "Buscar paciente" con lo escrito en sus filtros; sin filtros lista a los
     * pacientes de la clinica. La base filtra en una sola consulta con un maximo de resultados, y
     * los documentos y contactos de los resultados se cargan juntos, no uno por fila.
     */
    public void buscarPacientes() {
        limpiarResultadosPaciente();
        try {
            List<PersonaRol> encontrados = personaRolDAO.buscarAtendibles(clinicaTrabajoBean.getIdClinicaActual(),
                    filtroPaciente, filtroDocumento, filtroRol, filtroMedio, MAX_PACIENTES_BUSQUEDA);
            List<UUID> idsPersona = encontrados.stream()
                    .filter(pr -> pr.getIdPersona() != null && pr.getIdPersona().getIdPersona() != null)
                    .map(pr -> pr.getIdPersona().getIdPersona()).distinct().toList();
            documentosPorPersona = textosPorPersona(documentoDAO.findByPersonas(idsPersona),
                    Documento::getIdPersona, d -> textoConTipo(
                            d.getIdTipoDocumento() == null ? null : d.getIdTipoDocumento().getNombre(), d.getValor()));
            mediosPorPersona = textosPorPersona(medioContactoDAO.findByPersonas(idsPersona),
                    MedioContacto::getIdPersona, m -> textoConTipo(
                            m.getIdTipoMedioContacto() == null ? null : m.getIdTipoMedioContacto().getNombre(), m.getValor()));
            pacientesEncontrados = encontrados;
        } catch (RuntimeException e) {
            limpiarResultadosPaciente();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al buscar pacientes", MensajeError.de(e)));
        }
    }

    /** Resultado de la ultima busqueda; la vista lo lee varias veces sin volver a consultar. */
    public List<PersonaRol> getPacientesFiltrados() {
        if (pacientesEncontrados == null) {
            buscarPacientes();
        }
        return pacientesEncontrados;
    }

    /** Aviso cuando la busqueda llego al maximo y puede haber mas pacientes; vacio si no. */
    public String getAvisoLimitePacientes() {
        return getPacientesFiltrados().size() < MAX_PACIENTES_BUSQUEDA ? ""
                : "Se muestran los primeros " + MAX_PACIENTES_BUSQUEDA + " pacientes; escriba mas datos para acotar.";
    }

    /** Borra los filtros; la tabla vuelve a listar a los pacientes de la clinica al mostrarse. */
    public void limpiarFiltrosPaciente() {
        filtroPaciente = null;
        filtroDocumento = null;
        filtroRol = null;
        filtroMedio = null;
        limpiarResultadosPaciente();
        pacientesEncontrados = null;
    }

    private void limpiarResultadosPaciente() {
        pacientesEncontrados = List.of();
        documentosPorPersona = new HashMap<>();
        mediosPorPersona = new HashMap<>();
    }

    private static <T> Map<UUID, String> textosPorPersona(List<T> registros,
            Function<T, Persona> persona, Function<T, String> texto) {
        return new HashMap<>(registros.stream()
                .filter(registro -> persona.apply(registro) != null && persona.apply(registro).getIdPersona() != null)
                .collect(Collectors.groupingBy(registro -> persona.apply(registro).getIdPersona(),
                        Collectors.mapping(texto, Collectors.joining(", ")))));
    }

    private static String textoConTipo(String tipo, String valor) {
        return (tipo == null ? "" : tipo + ": ") + valor;
    }

    public void seleccionarPaciente(PersonaRol personaRol) {
        idPersonaRolSeleccionado = personaRol == null ? null : personaRol.getIdPersonaRol().toString();
    }

    public void onPacienteSelect(SelectEvent<PersonaRol> event) {
        seleccionarPaciente(event.getObject());
    }

    public String getNombrePacienteSeleccionado() {
        if (idPersonaRolSeleccionado == null || idPersonaRolSeleccionado.isBlank()) {
            return "";
        }
        PersonaRol personaRol = personaRolDAO.find(UUID.fromString(idPersonaRolSeleccionado));
        return personaRol == null || personaRol.getIdPersona() == null ? ""
                : personaRol.getIdPersona().getNombres() + " " + personaRol.getIdPersona().getApellidos();
    }

    public String documentosPaciente(PersonaRol personaRol) {
        return textoDePersona(documentosPorPersona, personaRol);
    }

    public String mediosPaciente(PersonaRol personaRol) {
        return textoDePersona(mediosPorPersona, personaRol);
    }

    private static String textoDePersona(Map<UUID, String> textos, PersonaRol personaRol) {
        if (personaRol == null || personaRol.getIdPersona() == null) {
            return "";
        }
        return textos.getOrDefault(personaRol.getIdPersona().getIdPersona(), "");
    }

    public List<Procedimiento> getProcedimientos() {
        return procedimientoDAO.findActivos();
    }
    
    // Solo las personas cuyo rol sea "Médico" (o variaciones)

    public String nombreProcedimiento(UUID idProc) {
        if (idProc == null) return "N/A";
        Procedimiento p = procedimientoDAO.find(idProc);
        return p != null ? p.getNombre() : "Desconocido";
    }

    public String nombrePasoInicial(ConsultaProcedimiento cp) {
        try {
            Procedimiento p = procedimientoDAO.find(cp.getIdProcedimiento());
            return obtenerPasoInicial(p).getNombre();
        } catch (Exception ex) {
            return "Paso inicial";
        }
    }

    // ─── GETTERS Y SETTERS ───────────────────────────────────────────────────────

    public String getIdPersonaRolSeleccionado() { return idPersonaRolSeleccionado; }
    public void setIdPersonaRolSeleccionado(String idPersonaRolSeleccionado) { this.idPersonaRolSeleccionado = idPersonaRolSeleccionado; }
    public PersonaRol getPacienteSeleccionado() { return pacienteSeleccionado; }
    public void setPacienteSeleccionado(PersonaRol pacienteSeleccionado) { this.pacienteSeleccionado = pacienteSeleccionado; }
    public String getFiltroPaciente() { return filtroPaciente; }
    public void setFiltroPaciente(String filtroPaciente) { this.filtroPaciente = filtroPaciente; }
    public String getFiltroDocumento() { return filtroDocumento; }
    public void setFiltroDocumento(String filtroDocumento) { this.filtroDocumento = filtroDocumento; }
    public String getFiltroRol() { return filtroRol; }
    public void setFiltroRol(String filtroRol) { this.filtroRol = filtroRol; }
    public String getFiltroMedio() { return filtroMedio; }
    public void setFiltroMedio(String filtroMedio) { this.filtroMedio = filtroMedio; }

    public ConsultaProcedimiento getNuevoConsultaProcedimiento() { return nuevoConsultaProcedimiento; }
    public void setNuevoConsultaProcedimiento(ConsultaProcedimiento nuevoConsultaProcedimiento) { this.nuevoConsultaProcedimiento = nuevoConsultaProcedimiento; }
    public ConsultaProcedimiento getProcSeleccionado() { return procSeleccionado; }
    public void setProcSeleccionado(ConsultaProcedimiento procSeleccionado) { this.procSeleccionado = procSeleccionado; }
    public String getIdProcedimientoSeleccionado() { return idProcedimientoSeleccionado; }
    public void setIdProcedimientoSeleccionado(String idProcedimientoSeleccionado) { this.idProcedimientoSeleccionado = idProcedimientoSeleccionado; }
    public boolean isEditandoProc() { return editandoProc; }
    public void setEditandoProc(boolean editandoProc) { this.editandoProc = editandoProc; }
    public boolean isCapturandoProc() { return capturandoProc; }
    public void setCapturandoProc(boolean capturandoProc) { this.capturandoProc = capturandoProc; }


    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }
    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }
}
