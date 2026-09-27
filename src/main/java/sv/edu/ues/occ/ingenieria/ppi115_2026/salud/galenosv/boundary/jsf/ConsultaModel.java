package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;

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
    ConsultaProcedimientoDAO consultaProcedimientoDAO;

    @Inject
    ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    @Inject
    OrdenExamenDAO ordenExamenDAO;

    @Inject
    ProcedimientoDAO procedimientoDAO;

    // Selección del maestro
    private String idPersonaRolSeleccionado;

    // Propiedades Pestaña 1 (Procedimiento)
    private ConsultaProcedimiento nuevoConsultaProcedimiento;
    private ConsultaProcedimiento procSeleccionado;
    private String idProcedimientoSeleccionado;
    private boolean editandoProc = false;

    // Propiedades Pestaña 2 (Paso)
    private ConsultaProcedimientoPaso nuevoConsultaProcedimientoPaso;
    private ConsultaProcedimientoPaso pasoSeleccionado;
    private String idConsultaProcedimientoSeleccionado;
    private String idPersonaRolPasoSeleccionado;
    private boolean editandoPaso = false;

    // Propiedades Pestaña 3 (Orden)
    private OrdenExamen nuevoOrdenExamen;
    private OrdenExamen ordenSeleccionada;
    private String idConsultaProcedimientoPasoSeleccionado;
    private boolean editandoOrden = false;

    public ConsultaModel() {
        this.nombreBean = "Consulta";
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
        sincronizarSeleccion();
        cancelarEdicionProc();
        cancelarEdicionPaso();
        cancelarEdicionOrden();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        this.idPersonaRolSeleccionado = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        this.idPersonaRolSeleccionado = null;
        cancelarEdicionProc();
        cancelarEdicionPaso();
        cancelarEdicionOrden();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.crear(this.registro);
                limpiar("Consulta guardada exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
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
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    // ─── PESTAÑA 1: ConsultaProcedimiento ───────────────────────────────────────

    public void prepararNuevoConsultaProcedimiento() {
        nuevoConsultaProcedimiento = new ConsultaProcedimiento();
        nuevoConsultaProcedimiento.setIdConsultaProcedimiento(UUID.randomUUID());
        nuevoConsultaProcedimiento.setFechaInicio(OffsetDateTime.now());
        idProcedimientoSeleccionado = null;
    }

    public void onProcSelect(SelectEvent<ConsultaProcedimiento> event) {
        procSeleccionado = event.getObject();
        nuevoConsultaProcedimiento = new ConsultaProcedimiento();
        nuevoConsultaProcedimiento.setIdConsultaProcedimiento(procSeleccionado.getIdConsultaProcedimiento());
        nuevoConsultaProcedimiento.setFechaInicio(procSeleccionado.getFechaInicio());
        nuevoConsultaProcedimiento.setIdConsulta(procSeleccionado.getIdConsulta());
        
        if (procSeleccionado.getIdProcedimiento() != null) {
            idProcedimientoSeleccionado = procSeleccionado.getIdProcedimiento().toString();
        }
        editandoProc = true;
    }

    public void cancelarEdicionProc() {
        procSeleccionado = null;
        editandoProc = false;
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
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void agregarConsultaProcedimiento() {
        if (this.registro == null || this.registro.getIdConsulta() == null) return;
        try {
            if (idProcedimientoSeleccionado == null || idProcedimientoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un procedimiento");
            }
            nuevoConsultaProcedimiento.setIdConsulta(this.registro);
            nuevoConsultaProcedimiento.setIdProcedimiento(UUID.fromString(idProcedimientoSeleccionado));
            consultaProcedimientoDAO.crear(nuevoConsultaProcedimiento);
            
            if (this.registro.getConsultaProcedimientoList() == null) {
                this.registro.setConsultaProcedimientoList(new ArrayList<>());
            }
            this.registro.getConsultaProcedimientoList().add(nuevoConsultaProcedimiento);
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Procedimiento agregado"));
            prepararNuevoConsultaProcedimiento();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", e.getMessage()));
        }
    }

    public void eliminarConsultaProcedimiento(ConsultaProcedimiento cp) {
        if (cp == null) return;
        try {
            consultaProcedimientoDAO.eliminar(cp);
            if (this.registro.getConsultaProcedimientoList() != null) {
                this.registro.getConsultaProcedimientoList().remove(cp);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Procedimiento eliminado"));
            cancelarEdicionProc();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }

    public List<ConsultaProcedimiento> getConsultaProcedimientosDeConsulta() {
        if (this.registro != null && this.registro.getConsultaProcedimientoList() != null) {
            return this.registro.getConsultaProcedimientoList();
        }
        return new ArrayList<>();
    }

    // ─── PESTAÑA 2: ConsultaProcedimientoPaso ───────────────────────────────────

    public void prepararNuevoConsultaProcedimientoPaso() {
        nuevoConsultaProcedimientoPaso = new ConsultaProcedimientoPaso();
        nuevoConsultaProcedimientoPaso.setIdConsultaProcedimientoPaso(UUID.randomUUID());
        nuevoConsultaProcedimientoPaso.setFechaInicio(OffsetDateTime.now());
        nuevoConsultaProcedimientoPaso.setEstado("PENDIENTE");
        idConsultaProcedimientoSeleccionado = null;
        idPersonaRolPasoSeleccionado = null;
    }

    public void onPasoSelect(SelectEvent<ConsultaProcedimientoPaso> event) {
        pasoSeleccionado = event.getObject();
        nuevoConsultaProcedimientoPaso = new ConsultaProcedimientoPaso();
        nuevoConsultaProcedimientoPaso.setIdConsultaProcedimientoPaso(pasoSeleccionado.getIdConsultaProcedimientoPaso());
        nuevoConsultaProcedimientoPaso.setFechaInicio(pasoSeleccionado.getFechaInicio());
        nuevoConsultaProcedimientoPaso.setFechaFin(pasoSeleccionado.getFechaFin());
        nuevoConsultaProcedimientoPaso.setEstado(pasoSeleccionado.getEstado());

        if (pasoSeleccionado.getIdConsultaProcedimiento() != null) {
            idConsultaProcedimientoSeleccionado = pasoSeleccionado.getIdConsultaProcedimiento().getIdConsultaProcedimiento().toString();
        }
        if (pasoSeleccionado.getIdPersonaRol() != null) {
            idPersonaRolPasoSeleccionado = pasoSeleccionado.getIdPersonaRol().getIdPersonaRol().toString();
        }
        editandoPaso = true;
    }

    public void cancelarEdicionPaso() {
        pasoSeleccionado = null;
        editandoPaso = false;
        prepararNuevoConsultaProcedimientoPaso();
    }

    public void modificarConsultaProcedimientoPaso() {
        try {
            if (idConsultaProcedimientoSeleccionado == null || idConsultaProcedimientoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un procedimiento principal");
            }
            if (idPersonaRolPasoSeleccionado == null || idPersonaRolPasoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un médico responsable");
            }

            ConsultaProcedimiento cp = null;
            if (this.registro.getConsultaProcedimientoList() != null) {
                for (ConsultaProcedimiento proc : this.registro.getConsultaProcedimientoList()) {
                    if (proc.getIdConsultaProcedimiento().toString().equals(idConsultaProcedimientoSeleccionado)) {
                        cp = proc;
                        break;
                    }
                }
            }
            if (cp == null) {
                cp = consultaProcedimientoDAO.find(UUID.fromString(idConsultaProcedimientoSeleccionado));
            }
            PersonaRol pr = personaRolDAO.find(UUID.fromString(idPersonaRolPasoSeleccionado));

            nuevoConsultaProcedimientoPaso.setIdConsultaProcedimiento(cp);
            nuevoConsultaProcedimientoPaso.setIdPersonaRol(pr);

            consultaProcedimientoPasoDAO.modificar(nuevoConsultaProcedimientoPaso);

            if (pasoSeleccionado != null && pasoSeleccionado.getIdConsultaProcedimiento() != null) {
                pasoSeleccionado.getIdConsultaProcedimiento().getConsultaProcedimientoPasoList().remove(pasoSeleccionado);
            }
            if (cp.getConsultaProcedimientoPasoList() == null) {
                cp.setConsultaProcedimientoPasoList(new ArrayList<>());
            }
            cp.getConsultaProcedimientoPasoList().add(nuevoConsultaProcedimientoPaso);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paso modificado"));
            cancelarEdicionPaso();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void agregarConsultaProcedimientoPaso() {
        if (this.registro == null || this.registro.getIdConsulta() == null) return;
        try {
            if (idConsultaProcedimientoSeleccionado == null || idConsultaProcedimientoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un procedimiento");
            }
            if (idPersonaRolPasoSeleccionado == null || idPersonaRolPasoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un médico responsable");
            }
            
            ConsultaProcedimiento cp = null;
            if (this.registro.getConsultaProcedimientoList() != null) {
                for (ConsultaProcedimiento proc : this.registro.getConsultaProcedimientoList()) {
                    if (proc.getIdConsultaProcedimiento().toString().equals(idConsultaProcedimientoSeleccionado)) {
                        cp = proc;
                        break;
                    }
                }
            }
            if (cp == null) {
                cp = consultaProcedimientoDAO.find(UUID.fromString(idConsultaProcedimientoSeleccionado));
            }
            PersonaRol pr = personaRolDAO.find(UUID.fromString(idPersonaRolPasoSeleccionado));
            
            nuevoConsultaProcedimientoPaso.setIdConsultaProcedimiento(cp);
            nuevoConsultaProcedimientoPaso.setIdPersonaRol(pr);
            
            consultaProcedimientoPasoDAO.crear(nuevoConsultaProcedimientoPaso);
            
            if (cp.getConsultaProcedimientoPasoList() == null) {
                cp.setConsultaProcedimientoPasoList(new ArrayList<>());
            }
            cp.getConsultaProcedimientoPasoList().add(nuevoConsultaProcedimientoPaso);
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paso agregado"));
            prepararNuevoConsultaProcedimientoPaso();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", e.getMessage()));
        }
    }

    public void eliminarConsultaProcedimientoPaso(ConsultaProcedimientoPaso cpp) {
        if (cpp == null) return;
        try {
            consultaProcedimientoPasoDAO.eliminar(cpp);
            if (cpp.getIdConsultaProcedimiento() != null && cpp.getIdConsultaProcedimiento().getConsultaProcedimientoPasoList() != null) {
                cpp.getIdConsultaProcedimiento().getConsultaProcedimientoPasoList().remove(cpp);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paso eliminado"));
            cancelarEdicionPaso();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }

    public List<ConsultaProcedimientoPaso> getPasosDeConsulta() {
        List<ConsultaProcedimientoPaso> pasos = new ArrayList<>();
        if (this.registro != null && this.registro.getConsultaProcedimientoList() != null) {
            for (ConsultaProcedimiento cp : this.registro.getConsultaProcedimientoList()) {
                if (cp.getConsultaProcedimientoPasoList() != null) {
                    pasos.addAll(cp.getConsultaProcedimientoPasoList());
                }
            }
        }
        return pasos;
    }

    // ─── PESTAÑA 3: OrdenExamen ─────────────────────────────────────────────────

    public void prepararNuevoOrdenExamen() {
        nuevoOrdenExamen = new OrdenExamen();
        nuevoOrdenExamen.setIdOrdenExamen(UUID.randomUUID());
        nuevoOrdenExamen.setFechaCreacion(OffsetDateTime.now());
        idConsultaProcedimientoPasoSeleccionado = null;
    }

    public void onOrdenSelect(SelectEvent<OrdenExamen> event) {
        ordenSeleccionada = event.getObject();
        nuevoOrdenExamen = new OrdenExamen();
        nuevoOrdenExamen.setIdOrdenExamen(ordenSeleccionada.getIdOrdenExamen());
        nuevoOrdenExamen.setFechaCreacion(ordenSeleccionada.getFechaCreacion());
        nuevoOrdenExamen.setIndicaciones(ordenSeleccionada.getIndicaciones());

        if (ordenSeleccionada.getIdConsultaProcedimientoPaso() != null) {
            idConsultaProcedimientoPasoSeleccionado = ordenSeleccionada.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso().toString();
        }
        editandoOrden = true;
    }

    public void cancelarEdicionOrden() {
        ordenSeleccionada = null;
        editandoOrden = false;
        prepararNuevoOrdenExamen();
    }

    public void modificarOrdenExamen() {
        try {
            if (idConsultaProcedimientoPasoSeleccionado == null || idConsultaProcedimientoPasoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar a qué paso pertenece la orden");
            }

            ConsultaProcedimientoPaso paso = null;
            if (this.registro.getConsultaProcedimientoList() != null) {
                for (ConsultaProcedimiento proc : this.registro.getConsultaProcedimientoList()) {
                    if (proc.getConsultaProcedimientoPasoList() != null) {
                        for (ConsultaProcedimientoPaso p : proc.getConsultaProcedimientoPasoList()) {
                            if (p.getIdConsultaProcedimientoPaso().toString().equals(idConsultaProcedimientoPasoSeleccionado)) {
                                paso = p;
                                break;
                            }
                        }
                    }
                }
            }
            if (paso == null) {
                paso = consultaProcedimientoPasoDAO.find(UUID.fromString(idConsultaProcedimientoPasoSeleccionado));
            }
            nuevoOrdenExamen.setIdConsultaProcedimientoPaso(paso);

            ordenExamenDAO.modificar(nuevoOrdenExamen);

            if (ordenSeleccionada != null && ordenSeleccionada.getIdConsultaProcedimientoPaso() != null) {
                ordenSeleccionada.getIdConsultaProcedimientoPaso().getOrdenExamenList().remove(ordenSeleccionada);
            }
            if (paso.getOrdenExamenList() == null) {
                paso.setOrdenExamenList(new ArrayList<>());
            }
            paso.getOrdenExamenList().add(nuevoOrdenExamen);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden modificada"));
            cancelarEdicionOrden();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void agregarOrdenExamen() {
        if (this.registro == null || this.registro.getIdConsulta() == null) return;
        try {
            if (idConsultaProcedimientoPasoSeleccionado == null || idConsultaProcedimientoPasoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar a qué paso pertenece la orden");
            }
            
            ConsultaProcedimientoPaso paso = null;
            if (this.registro.getConsultaProcedimientoList() != null) {
                for (ConsultaProcedimiento proc : this.registro.getConsultaProcedimientoList()) {
                    if (proc.getConsultaProcedimientoPasoList() != null) {
                        for (ConsultaProcedimientoPaso p : proc.getConsultaProcedimientoPasoList()) {
                            if (p.getIdConsultaProcedimientoPaso().toString().equals(idConsultaProcedimientoPasoSeleccionado)) {
                                paso = p;
                                break;
                            }
                        }
                    }
                }
            }
            if (paso == null) {
                paso = consultaProcedimientoPasoDAO.find(UUID.fromString(idConsultaProcedimientoPasoSeleccionado));
            }
            nuevoOrdenExamen.setIdConsultaProcedimientoPaso(paso);
            
            ordenExamenDAO.crear(nuevoOrdenExamen);
            
            if (paso.getOrdenExamenList() == null) {
                paso.setOrdenExamenList(new ArrayList<>());
            }
            paso.getOrdenExamenList().add(nuevoOrdenExamen);
            
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden agregada"));
            prepararNuevoOrdenExamen();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", e.getMessage()));
        }
    }

    public void eliminarOrdenExamen(OrdenExamen oe) {
        if (oe == null) return;
        try {
            ordenExamenDAO.eliminar(oe);
            if (oe.getIdConsultaProcedimientoPaso() != null && oe.getIdConsultaProcedimientoPaso().getOrdenExamenList() != null) {
                oe.getIdConsultaProcedimientoPaso().getOrdenExamenList().remove(oe);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden eliminada"));
            cancelarEdicionOrden();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }

    public List<OrdenExamen> getOrdenesDeConsulta() {
        List<OrdenExamen> ordenes = new ArrayList<>();
        if (this.registro == null || this.registro.getIdConsulta() == null) return ordenes;
        try {
            if (this.registro.getConsultaProcedimientoList() != null) {
                for (ConsultaProcedimiento cp : this.registro.getConsultaProcedimientoList()) {
                    if (cp.getConsultaProcedimientoPasoList() != null) {
                        for (ConsultaProcedimientoPaso paso : cp.getConsultaProcedimientoPasoList()) {
                            if (paso.getOrdenExamenList() != null) {
                                ordenes.addAll(paso.getOrdenExamenList());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return ordenes;
    }

    // ─── UTILIDADES ─────────────────────────────────────────────────────────────

    private void prepararRelaciones() {
        if (idPersonaRolSeleccionado == null || idPersonaRolSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un paciente");
        }
        PersonaRol personaRol = personaRolDAO.find(UUID.fromString(idPersonaRolSeleccionado));
        registro.setIdPersonaRol(personaRol);
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
        cancelarEdicionPaso();
        cancelarEdicionOrden();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    public String formatearFecha(OffsetDateTime fecha) {
        return fecha == null ? "" : fecha.atZoneSameInstant(ZONA_LOCAL).format(FORMATO_FECHA);
    }

    // ─── LISTAS PARA COMBOS ──────────────────────────────────────────────────────

    public List<PersonaRol> getPacientes() {
        List<PersonaRol> todos = personaRolDAO.findAll();
        List<PersonaRol> pacientes = new java.util.ArrayList<>();
        if (todos != null) {
            for (PersonaRol pr : todos) {
                if (pr.getIdRol() != null && pr.getIdRol().getNombre() != null) {
                    String nombreRol = pr.getIdRol().getNombre().toLowerCase();
                    if (nombreRol.contains("paciente") || nombreRol.contains("cliente")) {
                        pacientes.add(pr);
                    }
                }
            }
        }
        return pacientes;
    }

    public List<Procedimiento> getProcedimientos() {
        return procedimientoDAO.findAll();
    }
    
    // Solo las personas cuyo rol sea "Médico" (o variaciones)
    public List<PersonaRol> getPersonalMedico() {
        List<PersonaRol> todos = personaRolDAO.findAll();
        List<PersonaRol> medicos = new java.util.ArrayList<>();
        if (todos != null) {
            for (PersonaRol pr : todos) {
                if (pr.getIdRol() != null && pr.getIdRol().getNombre() != null) {
                    String nombreRol = pr.getIdRol().getNombre().toLowerCase();
                    if (nombreRol.contains("medic") || nombreRol.contains("médic") 
                        || nombreRol.contains("doctor") || nombreRol.contains("enfermer") 
                        || nombreRol.contains("especialista")) {
                        medicos.add(pr);
                    }
                }
            }
        }
        return medicos;
    }

    public String nombreProcedimiento(UUID idProc) {
        if (idProc == null) return "N/A";
        Procedimiento p = procedimientoDAO.find(idProc);
        return p != null ? p.getNombre() : "Desconocido";
    }

    // ─── GETTERS Y SETTERS ───────────────────────────────────────────────────────

    public String getIdPersonaRolSeleccionado() { return idPersonaRolSeleccionado; }
    public void setIdPersonaRolSeleccionado(String idPersonaRolSeleccionado) { this.idPersonaRolSeleccionado = idPersonaRolSeleccionado; }

    public ConsultaProcedimiento getNuevoConsultaProcedimiento() { return nuevoConsultaProcedimiento; }
    public void setNuevoConsultaProcedimiento(ConsultaProcedimiento nuevoConsultaProcedimiento) { this.nuevoConsultaProcedimiento = nuevoConsultaProcedimiento; }
    public ConsultaProcedimiento getProcSeleccionado() { return procSeleccionado; }
    public void setProcSeleccionado(ConsultaProcedimiento procSeleccionado) { this.procSeleccionado = procSeleccionado; }
    public String getIdProcedimientoSeleccionado() { return idProcedimientoSeleccionado; }
    public void setIdProcedimientoSeleccionado(String idProcedimientoSeleccionado) { this.idProcedimientoSeleccionado = idProcedimientoSeleccionado; }
    public boolean isEditandoProc() { return editandoProc; }
    public void setEditandoProc(boolean editandoProc) { this.editandoProc = editandoProc; }

    public ConsultaProcedimientoPaso getNuevoConsultaProcedimientoPaso() { return nuevoConsultaProcedimientoPaso; }
    public void setNuevoConsultaProcedimientoPaso(ConsultaProcedimientoPaso nuevoConsultaProcedimientoPaso) { this.nuevoConsultaProcedimientoPaso = nuevoConsultaProcedimientoPaso; }
    public ConsultaProcedimientoPaso getPasoSeleccionado() { return pasoSeleccionado; }
    public void setPasoSeleccionado(ConsultaProcedimientoPaso pasoSeleccionado) { this.pasoSeleccionado = pasoSeleccionado; }
    public String getIdConsultaProcedimientoSeleccionado() { return idConsultaProcedimientoSeleccionado; }
    public void setIdConsultaProcedimientoSeleccionado(String idConsultaProcedimientoSeleccionado) { this.idConsultaProcedimientoSeleccionado = idConsultaProcedimientoSeleccionado; }
    public String getIdPersonaRolPasoSeleccionado() { return idPersonaRolPasoSeleccionado; }
    public void setIdPersonaRolPasoSeleccionado(String idPersonaRolPasoSeleccionado) { this.idPersonaRolPasoSeleccionado = idPersonaRolPasoSeleccionado; }
    public boolean isEditandoPaso() { return editandoPaso; }
    public void setEditandoPaso(boolean editandoPaso) { this.editandoPaso = editandoPaso; }

    public OrdenExamen getNuevoOrdenExamen() { return nuevoOrdenExamen; }
    public void setNuevoOrdenExamen(OrdenExamen nuevoOrdenExamen) { this.nuevoOrdenExamen = nuevoOrdenExamen; }
    public OrdenExamen getOrdenSeleccionada() { return ordenSeleccionada; }
    public void setOrdenSeleccionada(OrdenExamen ordenSeleccionada) { this.ordenSeleccionada = ordenSeleccionada; }
    public String getIdConsultaProcedimientoPasoSeleccionado() { return idConsultaProcedimientoPasoSeleccionado; }
    public void setIdConsultaProcedimientoPasoSeleccionado(String idConsultaProcedimientoPasoSeleccionado) { this.idConsultaProcedimientoPasoSeleccionado = idConsultaProcedimientoPasoSeleccionado; }
    public boolean isEditandoOrden() { return editandoOrden; }
    public void setEditandoOrden(boolean editandoOrden) { this.editandoOrden = editandoOrden; }
}
