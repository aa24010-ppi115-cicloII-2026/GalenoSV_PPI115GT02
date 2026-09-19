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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;


@Named("consultaModel")
@ViewScoped
public class ConsultaModel extends AbstractModel<Consulta> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    ConsultaDAO dao;

    @Inject
    PersonaRolDAO personaRolDAO;

    private String idPersonaRolSeleccionado;

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
        inicializarRegistros();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    public List<PersonaRol> getPacientes() {
        return personaRolDAO.findAll();
    }

    public String getIdPersonaRolSeleccionado() {
        return idPersonaRolSeleccionado;
    }

    public void setIdPersonaRolSeleccionado(String idPersonaRolSeleccionado) {
        this.idPersonaRolSeleccionado = idPersonaRolSeleccionado;
    }
}
