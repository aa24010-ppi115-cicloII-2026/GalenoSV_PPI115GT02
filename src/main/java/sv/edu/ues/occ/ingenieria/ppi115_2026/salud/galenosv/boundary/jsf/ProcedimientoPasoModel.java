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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

@Named("procedimientoPasoModel")
@ViewScoped
public class ProcedimientoPasoModel extends AbstractModel<ProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    ProcedimientoPasoDAO dao;
    @Inject
    ProcedimientoDAO procedimientoDAO;
    @Inject
    RolDAO rolDAO;

    private String idProcedimientoSeleccionado;
    private String idRolSeleccionado;

    public ProcedimientoPasoModel() {
        this.nombreBean = "ProcedimientoPaso";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<ProcedimientoPaso> getDao() {
        return dao;
    }

    @Override
    protected ProcedimientoPaso nuevoRegistro() {
        ProcedimientoPaso r = new ProcedimientoPaso();
        r.setIdProcedimientoPaso(UUID.randomUUID());
        r.setIndicaFin(false);
        return r;
    }

    @Override
    protected ProcedimientoPaso buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(ProcedimientoPaso r) {
        return r != null && r.getIdProcedimientoPaso() != null ? r.getIdProcedimientoPaso().toString() : null;
    }

    @Override
    protected ProcedimientoPaso getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ProcedimientoPaso> r) {
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
            if (registro.getNombre() == null || registro.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacio");
            }
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
        if (idProcedimientoSeleccionado == null || idProcedimientoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un procedimiento");
        }
        if (idRolSeleccionado == null || idRolSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }
        registro.setIdProcedimiento(procedimientoDAO.find(UUID.fromString(idProcedimientoSeleccionado)));
        registro.setIdRol(rolDAO.find(UUID.fromString(idRolSeleccionado)));
    }

    private void sincronizarSeleccion() {
        idProcedimientoSeleccionado = registro != null && registro.getIdProcedimiento() != null ? registro.getIdProcedimiento().getIdProcedimiento().toString() : null;
        idRolSeleccionado = registro != null && registro.getIdRol() != null ? registro.getIdRol().getIdRol().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idProcedimientoSeleccionado = null;
        idRolSeleccionado = null;
    }

    public List<Procedimiento> getProcedimientos() {
        return procedimientoDAO.findAll();
    }

    public List<Rol> getRoles() {
        return rolDAO.findAll();
    }

    public String getIdProcedimientoSeleccionado() {
        return idProcedimientoSeleccionado;
    }

    public void setIdProcedimientoSeleccionado(String idProcedimientoSeleccionado) {
        this.idProcedimientoSeleccionado = idProcedimientoSeleccionado;
    }

    public String getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(String idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }
}
