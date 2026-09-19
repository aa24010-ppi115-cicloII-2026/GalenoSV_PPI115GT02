package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;


@Named("procedimientoPasoSecuenciaModel")
@ViewScoped
public class ProcedimientoPasoSecuenciaModel extends AbstractModel<ProcedimientoPasoSecuencia> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    ProcedimientoPasoSecuenciaDAO dao;
    @Inject
    ProcedimientoPasoDAO procedimientoPasoDAO;

    private String idProcedimientoPasoSeleccionado;
    private String idProcedimientoPasoReferenciaTexto;

    public ProcedimientoPasoSecuenciaModel() {
        this.nombreBean = "ProcedimientoPasoSecuencia";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<ProcedimientoPasoSecuencia> getDao() {
        return dao;
    }

    @Override
    protected ProcedimientoPasoSecuencia nuevoRegistro() {
        ProcedimientoPasoSecuencia r = new ProcedimientoPasoSecuencia();
        r.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
        return r;
    }

    @Override
    protected ProcedimientoPasoSecuencia buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(ProcedimientoPasoSecuencia r) {
        return r != null && r.getIdProcedimientoPasoSecuencia() != null ? r.getIdProcedimientoPasoSecuencia().toString() : null;
    }

    @Override
    protected ProcedimientoPasoSecuencia getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ProcedimientoPasoSecuencia> r) {
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
        if (idProcedimientoPasoSeleccionado == null || idProcedimientoPasoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un paso");
        }
        registro.setIdProcedimientoPaso(procedimientoPasoDAO.find(UUID.fromString(idProcedimientoPasoSeleccionado)));
        registro.setIdProcedimientoPasoReferencia(idProcedimientoPasoReferenciaTexto == null || idProcedimientoPasoReferenciaTexto.isBlank()
                ? null : UUID.fromString(idProcedimientoPasoReferenciaTexto));
    }

    private void sincronizarSeleccion() {
        idProcedimientoPasoSeleccionado = registro != null && registro.getIdProcedimientoPaso() != null ? registro.getIdProcedimientoPaso().getIdProcedimientoPaso().toString() : null;
        idProcedimientoPasoReferenciaTexto = registro != null && registro.getIdProcedimientoPasoReferencia() != null ? registro.getIdProcedimientoPasoReferencia().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idProcedimientoPasoSeleccionado = null;
        idProcedimientoPasoReferenciaTexto = null;
    }

    public List<ProcedimientoPaso> getPasos() {
        return procedimientoPasoDAO.findAll();
    }

    public String nombrePasoReferencia(UUID idReferencia) {
        if (idReferencia == null) {
            return "";
        }
        try {
            ProcedimientoPaso paso = procedimientoPasoDAO.find(idReferencia);
            return paso != null && paso.getNombre() != null ? paso.getNombre() : idReferencia.toString();
        } catch (Exception e) {
            return idReferencia.toString();
        }
    }

    public String getIdProcedimientoPasoSeleccionado() {
        return idProcedimientoPasoSeleccionado;
    }

    public void setIdProcedimientoPasoSeleccionado(String idProcedimientoPasoSeleccionado) {
        this.idProcedimientoPasoSeleccionado = idProcedimientoPasoSeleccionado;
    }

    public String getIdProcedimientoPasoReferenciaTexto() {
        return idProcedimientoPasoReferenciaTexto;
    }

    public void setIdProcedimientoPasoReferenciaTexto(String idProcedimientoPasoReferenciaTexto) {
        this.idProcedimientoPasoReferenciaTexto = idProcedimientoPasoReferenciaTexto;
    }
}
