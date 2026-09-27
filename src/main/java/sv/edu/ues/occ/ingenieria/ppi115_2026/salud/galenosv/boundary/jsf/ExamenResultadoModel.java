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
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenResultado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

@Named("examenResultadoModel")
@ViewScoped
public class ExamenResultadoModel extends AbstractModel<ExamenResultado> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    FacesContext facesContext;
    @Inject
    ExamenResultadoDAO dao;
    @Inject
    OrdenExamenDAO ordenExamenDAO;

    // Para el selector de Orden de Examen
    private String idOrdenExamenSeleccionada;

    public ExamenResultadoModel() {
        this.nombreBean = "ExamenResultado";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<ExamenResultado> getDao() {
        return dao;
    }

    @Override
    protected ExamenResultado nuevoRegistro() {
        ExamenResultado r = new ExamenResultado();
        r.setIdExamenResultado(UUID.randomUUID());
        r.setFechaCreacion(OffsetDateTime.now());
        return r;
    }

    @Override
    protected ExamenResultado buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(ExamenResultado r) {
        return r != null && r.getIdExamenResultado() != null ? r.getIdExamenResultado().toString() : null;
    }

    @Override
    protected ExamenResultado getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ExamenResultado> r) {
        super.selectionHandler(r);
        sincronizarSeleccion();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        idOrdenExamenSeleccionada = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        idOrdenExamenSeleccionada = null;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.crear(this.registro);
                limpiar("Resultado de examen guardado exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.modificar(this.registro);
                limpiar("Resultado de examen modificado exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    private void prepararRelaciones() {
        if (idOrdenExamenSeleccionada == null || idOrdenExamenSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una Orden de Examen");
        }
        OrdenExamen orden = ordenExamenDAO.find(UUID.fromString(idOrdenExamenSeleccionada));
        registro.setIdOrdenExamen(orden);
    }

    private void sincronizarSeleccion() {
        if (registro != null && registro.getIdOrdenExamen() != null) {
            idOrdenExamenSeleccionada = registro.getIdOrdenExamen().getIdOrdenExamen().toString();
        } else {
            idOrdenExamenSeleccionada = null;
        }
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        idOrdenExamenSeleccionada = null;
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    // ─── Métodos de utilidad para mostrar info legible ──────────────────────────

    /**
     * Devuelve una descripción amigable para la orden de examen:
     * "Paciente: Juan Pérez | Procedimiento: Limpieza Dental | Fecha: dd/MM/yyyy"
     */
    public String descripcionOrden(OrdenExamen orden) {
        if (orden == null) return "N/A";
        try {
            StringBuilder sb = new StringBuilder();
            ConsultaProcedimientoPaso paso = orden.getIdConsultaProcedimientoPaso();
            if (paso != null) {
                // Paciente
                if (paso.getIdConsultaProcedimiento() != null
                        && paso.getIdConsultaProcedimiento().getIdConsulta() != null
                        && paso.getIdConsultaProcedimiento().getIdConsulta().getIdPersonaRol() != null
                        && paso.getIdConsultaProcedimiento().getIdConsulta().getIdPersonaRol().getIdPersona() != null) {
                    var persona = paso.getIdConsultaProcedimiento().getIdConsulta().getIdPersonaRol().getIdPersona();
                    sb.append(persona.getNombres()).append(" ").append(persona.getApellidos());
                } else {
                    sb.append("Paciente desconocido");
                }
                sb.append(" | Estado: ").append(paso.getEstado() != null ? paso.getEstado() : "N/A");
            }
            if (orden.getFechaCreacion() != null) {
                sb.append(" | ").append(orden.getFechaCreacion().atZoneSameInstant(ZONA_LOCAL).format(FORMATO_FECHA));
            }
            if (orden.getIndicaciones() != null && !orden.getIndicaciones().isBlank()) {
                sb.append(" | ").append(orden.getIndicaciones().length() > 40
                        ? orden.getIndicaciones().substring(0, 40) + "..."
                        : orden.getIndicaciones());
            }
            return sb.toString();
        } catch (Exception e) {
            return orden.getIdOrdenExamen() != null ? orden.getIdOrdenExamen().toString() : "N/A";
        }
    }

    public String formatearFecha(OffsetDateTime fecha) {
        return fecha == null ? "" : fecha.atZoneSameInstant(ZONA_LOCAL).format(FORMATO_FECHA);
    }

    // ─── Lista de órdenes disponibles ───────────────────────────────────────────
    public List<OrdenExamen> getOrdenesExamen() {
        return ordenExamenDAO.findAll();
    }

    // ─── Getters y Setters ───────────────────────────────────────────────────────
    public String getIdOrdenExamenSeleccionada() { return idOrdenExamenSeleccionada; }
    public void setIdOrdenExamenSeleccionada(String idOrdenExamenSeleccionada) { this.idOrdenExamenSeleccionada = idOrdenExamenSeleccionada; }
}
