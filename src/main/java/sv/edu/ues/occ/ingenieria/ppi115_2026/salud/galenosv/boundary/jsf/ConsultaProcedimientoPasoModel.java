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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

@Named("consultaProcedimientoPasoModel")
@ViewScoped
public class ConsultaProcedimientoPasoModel extends AbstractModel<ConsultaProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    ConsultaProcedimientoPasoDAO dao;
    @Inject
    ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject
    PersonaRolDAO personaRolDAO;

    private String idConsultaProcedimientoSeleccionado;
    private String idPersonaRolSeleccionado;

    public ConsultaProcedimientoPasoModel() {
        this.nombreBean = "ConsultaProcedimientoPaso";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<ConsultaProcedimientoPaso> getDao() {
        return dao;
    }

    @Override
    protected ConsultaProcedimientoPaso nuevoRegistro() {
        ConsultaProcedimientoPaso r = new ConsultaProcedimientoPaso();
        r.setIdConsultaProcedimientoPaso(UUID.randomUUID());
        r.setFechaInicio(OffsetDateTime.now());
        r.setEstado("PENDIENTE");
        return r;
    }

    @Override
    protected ConsultaProcedimientoPaso buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(ConsultaProcedimientoPaso r) {
        return r != null && r.getIdConsultaProcedimientoPaso() != null ? r.getIdConsultaProcedimientoPaso().toString() : null;
    }

    @Override
    protected ConsultaProcedimientoPaso getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ConsultaProcedimientoPaso> r) {
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
        if (idConsultaProcedimientoSeleccionado == null || idConsultaProcedimientoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una consulta-procedimiento");
        }
        if (idPersonaRolSeleccionado == null || idPersonaRolSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una persona con rol");
        }
        registro.setIdConsultaProcedimiento(consultaProcedimientoDAO.find(UUID.fromString(idConsultaProcedimientoSeleccionado)));
        registro.setIdPersonaRol(personaRolDAO.find(UUID.fromString(idPersonaRolSeleccionado)));
    }

    private void sincronizarSeleccion() {
        idConsultaProcedimientoSeleccionado = registro != null && registro.getIdConsultaProcedimiento() != null ? registro.getIdConsultaProcedimiento().getIdConsultaProcedimiento().toString() : null;
        idPersonaRolSeleccionado = registro != null && registro.getIdPersonaRol() != null ? registro.getIdPersonaRol().getIdPersonaRol().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idConsultaProcedimientoSeleccionado = null;
        idPersonaRolSeleccionado = null;
    }

    public List<ConsultaProcedimiento> getConsultaProcedimientos() {
        return consultaProcedimientoDAO.findAll();
    }

    public List<PersonaRol> getPersonasRol() {
        return personaRolDAO.findAll();
    }

    public String getIdConsultaProcedimientoSeleccionado() {
        return idConsultaProcedimientoSeleccionado;
    }

    public void setIdConsultaProcedimientoSeleccionado(String idConsultaProcedimientoSeleccionado) {
        this.idConsultaProcedimientoSeleccionado = idConsultaProcedimientoSeleccionado;
    }

    public String getIdPersonaRolSeleccionado() {
        return idPersonaRolSeleccionado;
    }

    public void setIdPersonaRolSeleccionado(String idPersonaRolSeleccionado) {
        this.idPersonaRolSeleccionado = idPersonaRolSeleccionado;
    }
}
