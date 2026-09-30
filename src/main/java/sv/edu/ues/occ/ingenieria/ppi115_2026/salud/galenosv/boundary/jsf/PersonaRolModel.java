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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
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

    @Inject
    ConsultaDAO consultaDAO;

    @Inject
    ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    private String idPersonaSeleccionada;
    private String idRolSeleccionado;
    private String idClinicaSeleccionada;
    private UUID idClinicaMaestro;

    public void establecerClinicaMaestro(UUID id) {
        if (java.util.Objects.equals(idClinicaMaestro, id)) {
            return;
        }
        idClinicaMaestro = id;
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
    }

    @Override
    public void inicializarRegistros() {
        if (idClinicaMaestro == null) {
            super.inicializarRegistros();
            return;
        }
        modelo = new org.primefaces.model.LazyDataModel<PersonaRol>() {
            @Override
            public String getRowKey(PersonaRol dato) {
                return getIdAsText(dato);
            }

            @Override
            public PersonaRol getRowData(String id) {
                PersonaRol dato = getIdByText(id);
                return dato != null && dato.getIdClinica() != null
                        && idClinicaMaestro.equals(dato.getIdClinica().getIdClinica()) ? dato : null;
            }

            @Override
            public int count(java.util.Map<String, org.primefaces.model.FilterMeta> filtros) {
                return dao.countByClinica(idClinicaMaestro).intValue();
            }

            @Override
            public List<PersonaRol> load(int first, int max,
                    java.util.Map<String, org.primefaces.model.SortMeta> orden,
                    java.util.Map<String, org.primefaces.model.FilterMeta> filtros) {
                return dao.findByClinica(idClinicaMaestro, first, max);
            }
        };
    }

    @Override
    protected void validarUnicidad(PersonaRol r, boolean esModificacion) {
        if (r.getIdPersona() == null || r.getIdRol() == null || r.getIdClinica() == null) {
            return;
        }
        java.util.UUID excluir = esModificacion ? r.getIdPersonaRol() : null;
        if (dao.existeAsignacion(r.getIdPersona().getIdPersona(), r.getIdRol().getIdRol(),
                r.getIdClinica().getIdClinica(), excluir)) {
            throw new IllegalArgumentException("Esa asignación de rol ya existe para esa persona en esa clínica");
        }
    }

    @Override
    protected void validarEliminacion(PersonaRol r) {
        if (r.getIdPersonaRol() == null) {
            return;
        }
        long cons = 0;
        long pasos = 0;
        try {
            cons = consultaDAO.countByPersonaRol(r.getIdPersonaRol());
        } catch (Exception ignored) {
        }
        try {
            pasos = consultaProcedimientoPasoDAO.countByPersonaRol(r.getIdPersonaRol());
        } catch (Exception ignored) {
        }
        if (cons + pasos > 0) {
            throw new IllegalArgumentException("No se puede eliminar la asignación porque tiene " + cons + " consulta(s) y " + pasos + " paso(s) asociados");
        }
    }

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
                validarUnicidad(this.registro, false);
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
                validarUnicidad(this.registro, true);
                dao.modificar(this.registro);
                limpiar("Registro modificado");
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    private void prepararRelaciones() {
        if (idClinicaMaestro != null) {
            idClinicaSeleccionada = idClinicaMaestro.toString();
        }
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
        if (registro != null && registro.getIdPersona() != null && registro.getIdPersona().getIdPersona() != null) {
            idPersonaSeleccionada = registro.getIdPersona().getIdPersona().toString();
        } else {
            idPersonaSeleccionada = null;
        }
        if (registro != null && registro.getIdRol() != null && registro.getIdRol().getIdRol() != null) {
            idRolSeleccionado = registro.getIdRol().getIdRol().toString();
        } else {
            idRolSeleccionado = null;
        }
        if (registro != null && registro.getIdClinica() != null && registro.getIdClinica().getIdClinica() != null) {
            idClinicaSeleccionada = registro.getIdClinica().getIdClinica().toString();
        } else {
            idClinicaSeleccionada = null;
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
        idClinicaSeleccionada = idClinicaMaestro == null ? null : idClinicaMaestro.toString();
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
