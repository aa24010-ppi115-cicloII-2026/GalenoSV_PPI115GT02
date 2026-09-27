package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named("personaModel")
@ViewScoped
public class PersonaModel extends AbstractModel<Persona> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    PersonaDAO dao;

    @Inject
    RolDAO rolDAO;

    @Inject
    PersonaRolDAO personaRolDAO;

    @Inject
    TipoMedioContactoDAO tipoMedioContactoDAO;

    @Inject
    MedioContactoDAO medioContactoDAO;

    @Inject
    ClinicaDAO clinicaDAO;

    // Para la pestaña 1 (Roles)
    private PersonaRol nuevoRol;
    private PersonaRol rolSeleccionado;
    private String idRolSeleccionado;
    private String idClinicaSeleccionadaRol;
    private boolean editandoRol = false;

    // Para la pestaña 2 (MedioContacto)
    private MedioContacto nuevoMedioContacto;
    private MedioContacto medioSeleccionado;
    private String idTipoMedioContactoSeleccionado;
    private boolean editandoMedio = false;

    public PersonaModel() {
        this.nombreBean = "Persona";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<Persona> getDao() {
        return dao;
    }

    @Override
    protected Persona nuevoRegistro() {
        Persona p = new Persona();
        p.setIdPersona(UUID.randomUUID());
        return p;
    }

    @Override
    protected Persona buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(Persona r) {
        return r != null && r.getIdPersona() != null ? r.getIdPersona().toString() : null;
    }

    @Override
    protected Persona getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<Persona> r) {
        super.selectionHandler(r);
        cancelarEdicionRol();
        cancelarEdicionMedio();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                dao.crear(this.registro);
                limpiar("Persona guardada exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                dao.modificar(this.registro);
                limpiar("Persona modificada exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            }
        }
    }

    private void limpiar(String mensaje) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        cancelarEdicionRol();
        cancelarEdicionMedio();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    // ─── PESTAÑA 1: ROLES ────────────────────────────────────────────────────────
    
    public void prepararNuevoRol() {
        nuevoRol = new PersonaRol();
        nuevoRol.setIdPersonaRol(UUID.randomUUID());
        idRolSeleccionado = null;
        idClinicaSeleccionadaRol = null;
    }

    public void onRolSelect(SelectEvent<PersonaRol> event) {
        rolSeleccionado = event.getObject();
        nuevoRol = new PersonaRol();
        nuevoRol.setIdPersonaRol(rolSeleccionado.getIdPersonaRol());
        nuevoRol.setIdPersona(rolSeleccionado.getIdPersona());
        if (rolSeleccionado.getIdRol() != null) {
            idRolSeleccionado = rolSeleccionado.getIdRol().getIdRol().toString();
        } else {
            idRolSeleccionado = null;
        }
        if (rolSeleccionado.getIdClinica() != null) {
            idClinicaSeleccionadaRol = rolSeleccionado.getIdClinica().getIdClinica().toString();
        } else {
            idClinicaSeleccionadaRol = null;
        }
        editandoRol = true;
    }

    public void cancelarEdicionRol() {
        rolSeleccionado = null;
        editandoRol = false;
        prepararNuevoRol();
    }

    public void modificarRol() {
        if (this.registro == null) return;
        try {
            if (idRolSeleccionado == null || idRolSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un Rol válido");
            }
            if (idClinicaSeleccionadaRol == null || idClinicaSeleccionadaRol.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar una Clínica válida");
            }
            Rol rol = rolDAO.find(UUID.fromString(idRolSeleccionado));
            Clinica clinica = clinicaDAO.find(UUID.fromString(idClinicaSeleccionadaRol));
            nuevoRol.setIdRol(rol);
            nuevoRol.setIdClinica(clinica);
            nuevoRol.setIdPersona(this.registro);
            
            personaRolDAO.modificar(nuevoRol);

            if (rolSeleccionado != null && this.registro.getPersonaRolList() != null) {
                this.registro.getPersonaRolList().remove(rolSeleccionado);
            }
            if (this.registro.getPersonaRolList() == null) {
                this.registro.setPersonaRolList(new ArrayList<>());
            }
            this.registro.getPersonaRolList().add(nuevoRol);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Rol modificado"));
            cancelarEdicionRol();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
        }
    }

    public void agregarRol() {
        if (this.registro == null) return;
        try {
            if (idRolSeleccionado == null || idRolSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un Rol válido");
            }
            if (idClinicaSeleccionadaRol == null || idClinicaSeleccionadaRol.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar una Clínica válida");
            }
            Rol rol = rolDAO.find(UUID.fromString(idRolSeleccionado));
            Clinica clinica = clinicaDAO.find(UUID.fromString(idClinicaSeleccionadaRol));
            nuevoRol.setIdRol(rol);
            nuevoRol.setIdClinica(clinica);
            nuevoRol.setIdPersona(this.registro);
            nuevoRol.setFechaCreacion(java.time.OffsetDateTime.now());

            personaRolDAO.crear(nuevoRol);

            if (this.registro.getPersonaRolList() == null) {
                this.registro.setPersonaRolList(new ArrayList<>());
            }
            this.registro.getPersonaRolList().add(nuevoRol);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Rol asignado a la persona"));
            prepararNuevoRol();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void eliminarRol(PersonaRol pr) {
        if (pr == null) return;
        try {
            personaRolDAO.eliminar(pr);
            if (this.registro.getPersonaRolList() != null) {
                this.registro.getPersonaRolList().remove(pr);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Rol removido"));
            cancelarEdicionRol();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public List<PersonaRol> getRolesDePersona() {
        return this.registro != null && this.registro.getPersonaRolList() != null ? this.registro.getPersonaRolList() : new ArrayList<>();
    }


    // ─── PESTAÑA 2: MEDIOS DE CONTACTO ───────────────────────────────────────────

    public void prepararNuevoMedioContacto() {
        nuevoMedioContacto = new MedioContacto();
        nuevoMedioContacto.setIdMedioContacto(UUID.randomUUID());
        idTipoMedioContactoSeleccionado = null;
    }

    public void onMedioSelect(SelectEvent<MedioContacto> event) {
        medioSeleccionado = event.getObject();
        nuevoMedioContacto = new MedioContacto();
        nuevoMedioContacto.setIdMedioContacto(medioSeleccionado.getIdMedioContacto());
        nuevoMedioContacto.setValor(medioSeleccionado.getValor());
        nuevoMedioContacto.setIdPersona(medioSeleccionado.getIdPersona());
        if (medioSeleccionado.getIdTipoMedioContacto() != null) {
            idTipoMedioContactoSeleccionado = medioSeleccionado.getIdTipoMedioContacto().getIdTipoMedioContacto().toString();
        } else {
            idTipoMedioContactoSeleccionado = null;
        }
        editandoMedio = true;
    }

    public void cancelarEdicionMedio() {
        medioSeleccionado = null;
        editandoMedio = false;
        prepararNuevoMedioContacto();
    }

    public void modificarMedioContacto() {
        if (this.registro == null) return;
        try {
            if (idTipoMedioContactoSeleccionado == null || idTipoMedioContactoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de medio válido");
            }
            if (nuevoMedioContacto.getValor() == null || nuevoMedioContacto.getValor().isBlank()) {
                throw new IllegalArgumentException("El valor es requerido");
            }
            TipoMedioContacto tipo = tipoMedioContactoDAO.find(UUID.fromString(idTipoMedioContactoSeleccionado));
            nuevoMedioContacto.setIdTipoMedioContacto(tipo);
            nuevoMedioContacto.setIdPersona(this.registro);

            medioContactoDAO.modificar(nuevoMedioContacto);

            if (medioSeleccionado != null && this.registro.getMedioContactoList() != null) {
                this.registro.getMedioContactoList().remove(medioSeleccionado);
            }
            if (this.registro.getMedioContactoList() == null) {
                this.registro.setMedioContactoList(new ArrayList<>());
            }
            this.registro.getMedioContactoList().add(nuevoMedioContacto);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Medio de contacto modificado"));
            cancelarEdicionMedio();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void agregarMedioContacto() {
        if (this.registro == null) return;
        try {
            if (idTipoMedioContactoSeleccionado == null || idTipoMedioContactoSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de medio válido");
            }
            if (nuevoMedioContacto.getValor() == null || nuevoMedioContacto.getValor().isBlank()) {
                throw new IllegalArgumentException("El valor es requerido");
            }
            TipoMedioContacto tipo = tipoMedioContactoDAO.find(UUID.fromString(idTipoMedioContactoSeleccionado));
            nuevoMedioContacto.setIdTipoMedioContacto(tipo);
            nuevoMedioContacto.setIdPersona(this.registro);

            medioContactoDAO.crear(nuevoMedioContacto);

            if (this.registro.getMedioContactoList() == null) {
                this.registro.setMedioContactoList(new ArrayList<>());
            }
            this.registro.getMedioContactoList().add(nuevoMedioContacto);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Medio de contacto agregado"));
            prepararNuevoMedioContacto();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void eliminarMedioContacto(MedioContacto mc) {
        if (mc == null) return;
        try {
            medioContactoDAO.eliminar(mc);
            if (this.registro.getMedioContactoList() != null) {
                this.registro.getMedioContactoList().remove(mc);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Medio de contacto eliminado"));
            cancelarEdicionMedio();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public List<MedioContacto> getMediosDeContacto() {
        return this.registro != null && this.registro.getMedioContactoList() != null ? this.registro.getMedioContactoList() : new ArrayList<>();
    }

    // ─── LISTAS PARA COMBOS ──────────────────────────────────────────────────────

    public List<Rol> getRolesDisponibles() {
        return rolDAO.findAll();
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        return tipoMedioContactoDAO.findAll();
    }

    // Getters y Setters
    public PersonaRol getNuevoRol() { return nuevoRol; }
    public void setNuevoRol(PersonaRol nuevoRol) { this.nuevoRol = nuevoRol; }
    public String getIdRolSeleccionado() { return idRolSeleccionado; }
    public void setIdRolSeleccionado(String idRolSeleccionado) { this.idRolSeleccionado = idRolSeleccionado; }

    public String getIdClinicaSeleccionadaRol() { return idClinicaSeleccionadaRol; }
    public void setIdClinicaSeleccionadaRol(String idClinicaSeleccionadaRol) { this.idClinicaSeleccionadaRol = idClinicaSeleccionadaRol; }

    public List<Clinica> getClinicas() {
        return clinicaDAO.findAll();
    }
    public PersonaRol getRolSeleccionado() { return rolSeleccionado; }
    public void setRolSeleccionado(PersonaRol rolSeleccionado) { this.rolSeleccionado = rolSeleccionado; }
    public boolean isEditandoRol() { return editandoRol; }
    public void setEditandoRol(boolean editandoRol) { this.editandoRol = editandoRol; }

    public MedioContacto getNuevoMedioContacto() { return nuevoMedioContacto; }
    public void setNuevoMedioContacto(MedioContacto nuevoMedioContacto) { this.nuevoMedioContacto = nuevoMedioContacto; }
    public String getIdTipoMedioContactoSeleccionado() { return idTipoMedioContactoSeleccionado; }
    public void setIdTipoMedioContactoSeleccionado(String idTipoMedioContactoSeleccionado) { this.idTipoMedioContactoSeleccionado = idTipoMedioContactoSeleccionado; }
    public MedioContacto getMedioSeleccionado() { return medioSeleccionado; }
    public void setMedioSeleccionado(MedioContacto medioSeleccionado) { this.medioSeleccionado = medioSeleccionado; }
    public boolean isEditandoMedio() { return editandoMedio; }
    public void setEditandoMedio(boolean editandoMedio) { this.editandoMedio = editandoMedio; }
}
