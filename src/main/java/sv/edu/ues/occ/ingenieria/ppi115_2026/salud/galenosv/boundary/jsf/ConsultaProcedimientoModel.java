package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;

@Named("consultaProcedimientoModel")
@ViewScoped
public class ConsultaProcedimientoModel extends AbstractModel<ConsultaProcedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    ConsultaProcedimientoDAO dao;
    @Inject
    ConsultaDAO consultaDAO;
    @Inject
    ProcedimientoDAO procedimientoDAO;

    private String idConsultaSeleccionada;
    private String idProcedimientoSeleccionado;

    public ConsultaProcedimientoModel() {
        this.nombreBean = "ConsultaProcedimiento";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<ConsultaProcedimiento> getDao() {
        return dao;
    }

    @Override
    protected ConsultaProcedimiento nuevoRegistro() {
        ConsultaProcedimiento r = new ConsultaProcedimiento();
        r.setIdConsultaProcedimiento(UUID.randomUUID());
        r.setFechaInicio(OffsetDateTime.now());
        return r;
    }

    @Override
    protected ConsultaProcedimiento buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(ConsultaProcedimiento r) {
        return r != null && r.getIdConsultaProcedimiento() != null ? r.getIdConsultaProcedimiento().toString() : null;
    }

    @Override
    protected ConsultaProcedimiento getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ConsultaProcedimiento> r) {
        super.selectionHandler(r);
        sincronizarSeleccion();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        limpiarSeleccion();
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        limpiarSeleccion();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        guardar(false);
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        guardar(true);
    }

    private void guardar(boolean modificar) {
        try {
            prepararRelaciones();
            if (modificar) {
                dao.modificar(registro);
                limpiar("Registro modificado");
            } else {
                dao.crear(registro);
                limpiar("Registro guardado");
            }
        } catch (Exception e) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, modificar ? "Error al modificar" : "Error al guardar", e.getMessage()));
        }
    }

    private void prepararRelaciones() {
        if (idConsultaSeleccionada == null || idConsultaSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una consulta");
        }
        if (idProcedimientoSeleccionado == null || idProcedimientoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un procedimiento");
        }
        registro.setIdConsulta(consultaDAO.find(UUID.fromString(idConsultaSeleccionada)));
        registro.setIdProcedimiento(UUID.fromString(idProcedimientoSeleccionado));
    }

    private void sincronizarSeleccion() {
        idConsultaSeleccionada = registro != null && registro.getIdConsulta() != null ? registro.getIdConsulta().getIdConsulta().toString() : null;
        idProcedimientoSeleccionado = registro != null && registro.getIdProcedimiento() != null ? registro.getIdProcedimiento().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idConsultaSeleccionada = null;
        idProcedimientoSeleccionado = null;
    }

    public List<Consulta> getConsultas() {
        return consultaDAO.findAll();
    }

    public List<Procedimiento> getProcedimientos() {
        return procedimientoDAO.findAll();
    }

    public String getIdConsultaSeleccionada() {
        return idConsultaSeleccionada;
    }

    public void setIdConsultaSeleccionada(String idConsultaSeleccionada) {
        this.idConsultaSeleccionada = idConsultaSeleccionada;
    }

    public String getIdProcedimientoSeleccionado() {
        return idProcedimientoSeleccionado;
    }

    public void setIdProcedimientoSeleccionado(String idProcedimientoSeleccionado) {
        this.idProcedimientoSeleccionado = idProcedimientoSeleccionado;
    }
}
