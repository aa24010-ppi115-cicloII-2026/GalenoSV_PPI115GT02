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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenResultado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;

@Named("examenResultadoModel")
@ViewScoped
public class ExamenResultadoModel extends AbstractModel<ExamenResultado> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    ExamenResultadoDAO dao;
    @Inject
    OrdenExamenDAO ordenExamenDAO;

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
        if (idOrdenExamenSeleccionada == null || idOrdenExamenSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una orden de examen");
        }
        registro.setIdOrdenExamen(ordenExamenDAO.find(UUID.fromString(idOrdenExamenSeleccionada)));
    }

    private void sincronizarSeleccion() {
        idOrdenExamenSeleccionada = registro != null && registro.getIdOrdenExamen() != null ? registro.getIdOrdenExamen().getIdOrdenExamen().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        idOrdenExamenSeleccionada = null;
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    public List<OrdenExamen> getOrdenesExamen() {
        return ordenExamenDAO.findAll();
    }

    public String getIdOrdenExamenSeleccionada() {
        return idOrdenExamenSeleccionada;
    }

    public void setIdOrdenExamenSeleccionada(String idOrdenExamenSeleccionada) {
        this.idOrdenExamenSeleccionada = idOrdenExamenSeleccionada;
    }
}
