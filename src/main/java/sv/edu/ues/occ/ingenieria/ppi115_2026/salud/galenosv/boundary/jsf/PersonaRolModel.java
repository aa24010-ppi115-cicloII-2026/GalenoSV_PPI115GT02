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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;


@Named("personaRolModel")
@ViewScoped
public class PersonaRolModel extends AbstractModel<PersonaRol> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    PersonaRolDAO dao;

    @Inject
    PersonaDAO personaDAO;

    @Inject
    RolDAO rolDAO;

    @Inject
    ClinicaDAO clinicaDAO;

    private String idPersonaSeleccionada;
    private String idRolSeleccionado;
    private String idClinicaSeleccionada;

    public PersonaRolModel() {
        this.nombreBean = "PersonaRol";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<PersonaRol> getDao() {
        return dao;
    }

    @Override
    protected PersonaRol nuevoRegistro() {
        PersonaRol r = new PersonaRol();
        r.setIdPersonaRol(UUID.randomUUID());
        r.setFechaCreacion(OffsetDateTime.now());
        return r;
    }

    @Override
    protected PersonaRol buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado) {
            return dao.find(buscado);
        }
        return null;
    }

    @Override
    protected String getIdAsText(PersonaRol r) {
        if (r != null && r.getIdPersonaRol() != null) {
            return r.getIdPersonaRol().toString();
        }
        return null;
    }

    @Override
    protected PersonaRol getIdByText(String id) {
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
    public void selectionHandler(SelectEvent<PersonaRol> r) {
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
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.crear(this.registro);
                limpiar("Registro guardado");
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
                limpiar("Registro modificado");
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    private void prepararRelaciones() {
        if (idPersonaSeleccionada == null || idPersonaSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una persona");
        }
        if (idRolSeleccionado == null || idRolSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }
        if (idClinicaSeleccionada == null || idClinicaSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una clinica");
        }
        registro.setIdPersona(personaDAO.find(UUID.fromString(idPersonaSeleccionada)));
        registro.setIdRol(rolDAO.find(UUID.fromString(idRolSeleccionado)));
        registro.setIdClinica(clinicaDAO.find(UUID.fromString(idClinicaSeleccionada)));
    }

    private void sincronizarSeleccion() {
        if (registro != null && registro.getIdPersona() != null) {
            idPersonaSeleccionada = registro.getIdPersona().getIdPersona().toString();
        }
        if (registro != null && registro.getIdRol() != null) {
            idRolSeleccionado = registro.getIdRol().getIdRol().toString();
        }
        if (registro != null && registro.getIdClinica() != null) {
            idClinicaSeleccionada = registro.getIdClinica().getIdClinica().toString();
        }
    }

    private void limpiar(String mensaje) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idPersonaSeleccionada = null;
        idRolSeleccionado = null;
        idClinicaSeleccionada = null;
    }

    public List<Persona> getPersonas() {
        return personaDAO.findAll();
    }

    public List<Rol> getRoles() {
        return rolDAO.findAll();
    }

    public List<Clinica> getClinicas() {
        return clinicaDAO.findAll();
    }

    public String getIdPersonaSeleccionada() {
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public String getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(String idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public String getIdClinicaSeleccionada() {
        return idClinicaSeleccionada;
    }

    public void setIdClinicaSeleccionada(String idClinicaSeleccionada) {
        this.idClinicaSeleccionada = idClinicaSeleccionada;
    }
}
