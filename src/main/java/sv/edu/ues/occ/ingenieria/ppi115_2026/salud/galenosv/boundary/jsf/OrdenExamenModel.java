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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;


@Named("ordenExamenModel")
@ViewScoped
public class OrdenExamenModel extends AbstractModel<OrdenExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    OrdenExamenDAO dao;

    @Inject
    ExamenResultadoDAO examenResultadoDAO;
    @Inject
    ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    private String idConsultaProcedimientoPasoSeleccionado;

    @Override
    protected void validarEliminacion(OrdenExamen r) {
        long n = r.getIdOrdenExamen() == null ? 0 : examenResultadoDAO.countByOrden(r.getIdOrdenExamen());
        if (n > 0) {
            throw new IllegalArgumentException("No se puede eliminar la orden porque tiene " + n + " resultado(s) registrado(s)");
        }
    }

    public OrdenExamenModel() {
        this.nombreBean = "OrdenExamen";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<OrdenExamen> getDao() {
        return dao;
    }

    @Override
    protected OrdenExamen nuevoRegistro() {
        OrdenExamen r = new OrdenExamen();
        r.setIdOrdenExamen(UUID.randomUUID());
        r.setFechaCreacion(OffsetDateTime.now());
        return r;
    }

    @Override
    protected OrdenExamen buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(OrdenExamen r) {
        return r != null && r.getIdOrdenExamen() != null ? r.getIdOrdenExamen().toString() : null;
    }

    @Override
    protected OrdenExamen getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<OrdenExamen> r) {
        super.selectionHandler(r);
        sincronizarSeleccion();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        idConsultaProcedimientoPasoSeleccionado = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        idConsultaProcedimientoPasoSeleccionado = null;
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
        if (idConsultaProcedimientoPasoSeleccionado == null || idConsultaProcedimientoPasoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un paso de consulta");
        }
        registro.setIdConsultaProcedimientoPaso(consultaProcedimientoPasoDAO.find(UUID.fromString(idConsultaProcedimientoPasoSeleccionado)));
    }

    private void sincronizarSeleccion() {
        idConsultaProcedimientoPasoSeleccionado = registro != null && registro.getIdConsultaProcedimientoPaso() != null ? registro.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        idConsultaProcedimientoPasoSeleccionado = null;
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    public List<ConsultaProcedimientoPaso> getConsultaProcedimientoPasos() {
        return consultaProcedimientoPasoDAO.findAll();
    }

    public String getIdConsultaProcedimientoPasoSeleccionado() {
        return idConsultaProcedimientoPasoSeleccionado;
    }

    public void setIdConsultaProcedimientoPasoSeleccionado(String idConsultaProcedimientoPasoSeleccionado) {
        this.idConsultaProcedimientoPasoSeleccionado = idConsultaProcedimientoPasoSeleccionado;
    }
}
