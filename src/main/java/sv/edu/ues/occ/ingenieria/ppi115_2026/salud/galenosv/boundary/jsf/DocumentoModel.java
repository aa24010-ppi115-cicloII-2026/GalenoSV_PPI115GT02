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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Documento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;


@Named("documentoModel")
@ViewScoped
public class DocumentoModel extends AbstractModel<Documento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    DocumentoDAO dao;
    @Inject
    PersonaDAO personaDAO;
    @Inject
    TipoDocumentoDAO tipoDocumentoDAO;

    private String idPersonaSeleccionada;
    private String idTipoDocumentoSeleccionado;

    public DocumentoModel() {
        this.nombreBean = "Documento";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<Documento> getDao() {
        return dao;
    }

    @Override
    protected Documento nuevoRegistro() {
        Documento r = new Documento();
        r.setIdDocumento(UUID.randomUUID());
        return r;
    }

    @Override
    protected Documento buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(Documento r) {
        return r != null && r.getIdDocumento() != null ? r.getIdDocumento().toString() : null;
    }

    @Override
    protected Documento getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<Documento> r) {
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
        if (idPersonaSeleccionada == null || idPersonaSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una persona");
        }
        if (idTipoDocumentoSeleccionado == null || idTipoDocumentoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de documento");
        }
        registro.setIdPersona(personaDAO.find(UUID.fromString(idPersonaSeleccionada)));
        registro.setIdTipoDocumento(tipoDocumentoDAO.find(UUID.fromString(idTipoDocumentoSeleccionado)));
    }

    private void sincronizarSeleccion() {
        idPersonaSeleccionada = registro != null && registro.getIdPersona() != null ? registro.getIdPersona().getIdPersona().toString() : null;
        idTipoDocumentoSeleccionado = registro != null && registro.getIdTipoDocumento() != null ? registro.getIdTipoDocumento().getIdTipoDocumento().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idPersonaSeleccionada = null;
        idTipoDocumentoSeleccionado = null;
    }

    public List<Persona> getPersonas() {
        return personaDAO.findAll();
    }

    public List<TipoDocumento> getTiposDocumento() {
        return tipoDocumentoDAO.findAll();
    }

    public String getIdPersonaSeleccionada() {
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public String getIdTipoDocumentoSeleccionado() {
        return idTipoDocumentoSeleccionado;
    }

    public void setIdTipoDocumentoSeleccionado(String idTipoDocumentoSeleccionado) {
        this.idTipoDocumentoSeleccionado = idTipoDocumentoSeleccionado;
    }
}
