package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
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
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");

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

    @Inject
    DocumentoDAO documentoDAO;

    @Inject
    DocumentoModel documentoModel;

    // Para la pestaña 1 (Roles)
    private PersonaRol nuevoRol;
    private PersonaRol rolSeleccionado;
    private String idRolSeleccionado;
    private String idClinicaSeleccionadaRol;
    private boolean editandoRol = false;
    private boolean capturandoRol = false;

    // Para la pestaña 2 (MedioContacto)
    private MedioContacto nuevoMedioContacto;
    private MedioContacto medioSeleccionado;
    private String idTipoMedioContactoSeleccionado;
    private boolean editandoMedio = false;
    private boolean capturandoMedio = false;

    @Override
    protected void validarEliminacion(Persona r) {
        if (r.getIdPersona() == null) {
            return;
        }
        long docs = documentoDAO.countByPersona(r.getIdPersona());
        long medios = medioContactoDAO.countByPersona(r.getIdPersona());
        long roles = personaRolDAO.countByPersona(r.getIdPersona());
        if (docs + medios + roles > 0) {
            throw new IllegalArgumentException("No se puede eliminar la persona porque tiene "
                    + docs + " documento(s), " + medios + " medio(s) de contacto y " + roles + " rol(es) asignados");
        }
    }

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
        p.setFechaCreacion(java.time.OffsetDateTime.now());
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
        refrescarListasHijas();
        cancelarEdicionRol();
        cancelarEdicionMedio();
    }

    private void refrescarListasHijas() {
        if (this.registro == null || this.registro.getIdPersona() == null) {
            return;
        }
        try {
            this.registro.setMedioContactoList(
                    medioContactoDAO.findByPersona(this.registro.getIdPersona(), 0, 1000));
        } catch (Exception ignored) {
        }
        try {
            this.registro.setPersonaRolList(
                    personaRolDAO.findByPersona(this.registro.getIdPersona()));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                validarPersona();
                if (this.registro.getFechaCreacion() == null) {
                    this.registro.setFechaCreacion(java.time.OffsetDateTime.now());
                }
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
                validarPersona();
                dao.modificar(this.registro);
                limpiar("Persona modificada exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            }
        }
    }

    private void validarPersona() {
        if (registro.getNombres() == null || registro.getNombres().isBlank()) {
            throw new IllegalArgumentException("Los nombres son requeridos");
        }
        if (registro.getApellidos() == null || registro.getApellidos().isBlank()) {
            throw new IllegalArgumentException("Los apellidos son requeridos");
        }
        registro.setNombres(registro.getNombres().trim());
        registro.setApellidos(registro.getApellidos().trim());
        if (registro.getNombres().length() < 2) {
            throw new IllegalArgumentException("Los nombres deben tener al menos 2 caracteres");
        }
        if (registro.getApellidos().length() < 2) {
            throw new IllegalArgumentException("Los apellidos deben tener al menos 2 caracteres");
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
        capturandoRol = false;
    }

    public void iniciarCapturaRol() {
        prepararNuevoRol();
        capturandoRol = true;
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
        capturandoRol = true;
    }

    public void cancelarEdicionRol() {
        rolSeleccionado = null;
        editandoRol = false;
        capturandoRol = false;
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
            if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden asignar roles activos");
            }
            if (clinica == null || !Boolean.TRUE.equals(clinica.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden asignar clínicas activas");
            }
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
            if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden asignar roles activos");
            }
            if (clinica == null || !Boolean.TRUE.equals(clinica.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden asignar clínicas activas");
            }
            nuevoRol.setIdRol(rol);
            nuevoRol.setIdClinica(clinica);
            nuevoRol.setIdPersona(this.registro);
            nuevoRol.setFechaCreacion(java.time.OffsetDateTime.now());
            if (personaRolDAO.existeAsignacion(this.registro.getIdPersona(), rol.getIdRol(), clinica.getIdClinica(), null)) {
                throw new IllegalArgumentException("Esa asignación de rol ya existe para esa persona en esa clínica");
            }

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
        nuevoMedioContacto.setFechaCreacion(java.time.OffsetDateTime.now());
        idTipoMedioContactoSeleccionado = null;
        capturandoMedio = false;
    }

    public void iniciarCapturaMedio() {
        prepararNuevoMedioContacto();
        capturandoMedio = true;
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
        capturandoMedio = true;
    }

    public void cancelarEdicionMedio() {
        medioSeleccionado = null;
        editandoMedio = false;
        capturandoMedio = false;
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
            if (tipo == null || !Boolean.TRUE.equals(tipo.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden asignar tipos de medio de contacto activos");
            }
            sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ValidacionFormato.validar(
                    nuevoMedioContacto.getValor(), tipo.getExpresionRegular(), tipo.getIndicaciones());
            if (nuevoMedioContacto.getFechaCreacion() == null) {
                nuevoMedioContacto.setFechaCreacion(java.time.OffsetDateTime.now());
            }
            if (nuevoMedioContacto.getValor() != null) {
                nuevoMedioContacto.setValor(nuevoMedioContacto.getValor().trim());
            }
            if (medioContactoDAO.existePersonaValor(this.registro.getIdPersona(), nuevoMedioContacto.getValor(), nuevoMedioContacto.getIdMedioContacto())) {
                throw new IllegalArgumentException("Esa persona ya tiene registrado ese medio de contacto");
            }
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
            if (tipo == null || !Boolean.TRUE.equals(tipo.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden asignar tipos de medio de contacto activos");
            }
            sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ValidacionFormato.validar(
                    nuevoMedioContacto.getValor(), tipo.getExpresionRegular(), tipo.getIndicaciones());
            if (nuevoMedioContacto.getFechaCreacion() == null) {
                nuevoMedioContacto.setFechaCreacion(java.time.OffsetDateTime.now());
            }
            if (nuevoMedioContacto.getValor() != null) {
                nuevoMedioContacto.setValor(nuevoMedioContacto.getValor().trim());
            }
            if (medioContactoDAO.existePersonaValor(this.registro.getIdPersona(), nuevoMedioContacto.getValor(), null)) {
                throw new IllegalArgumentException("Esa persona ya tiene registrado ese medio de contacto");
            }
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
        return rolDAO.findActivos();
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        return tipoMedioContactoDAO.findActivos();
    }

    // Getters y Setters
    public PersonaRol getNuevoRol() { return nuevoRol; }
    public void setNuevoRol(PersonaRol nuevoRol) { this.nuevoRol = nuevoRol; }
    public String getIdRolSeleccionado() { return idRolSeleccionado; }
    public void setIdRolSeleccionado(String idRolSeleccionado) { this.idRolSeleccionado = idRolSeleccionado; }

    public String getIdClinicaSeleccionadaRol() { return idClinicaSeleccionadaRol; }
    public void setIdClinicaSeleccionadaRol(String idClinicaSeleccionadaRol) { this.idClinicaSeleccionadaRol = idClinicaSeleccionadaRol; }

    public List<Clinica> getClinicas() {
        return clinicaDAO.findActivas();
    }

    public LocalDate getFechaNacimiento() {
        if (registro == null || registro.getFechaNacimiento() == null) {
            return null;
        }
        return registro.getFechaNacimiento().atZoneSameInstant(ZONA_LOCAL).toLocalDate();
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (registro != null) {
            registro.setFechaNacimiento(fechaNacimiento == null ? null
                    : fechaNacimiento.atStartOfDay(ZONA_LOCAL).toOffsetDateTime());
        }
    }

    public DocumentoModel getDocumentoModel() {
        documentoModel.establecerPersonaMaestro(
                estado == ESTADO_CRUD.MODIFICAR && registro != null ? registro.getIdPersona() : null);
        return documentoModel;
    }
    public PersonaRol getRolSeleccionado() { return rolSeleccionado; }
    public void setRolSeleccionado(PersonaRol rolSeleccionado) { this.rolSeleccionado = rolSeleccionado; }
    public boolean isEditandoRol() { return editandoRol; }
    public void setEditandoRol(boolean editandoRol) { this.editandoRol = editandoRol; }
    public boolean isCapturandoRol() { return capturandoRol; }
    public void setCapturandoRol(boolean capturandoRol) { this.capturandoRol = capturandoRol; }

    public MedioContacto getNuevoMedioContacto() { return nuevoMedioContacto; }
    public void setNuevoMedioContacto(MedioContacto nuevoMedioContacto) { this.nuevoMedioContacto = nuevoMedioContacto; }
    public String getIdTipoMedioContactoSeleccionado() { return idTipoMedioContactoSeleccionado; }
    public void setIdTipoMedioContactoSeleccionado(String idTipoMedioContactoSeleccionado) { this.idTipoMedioContactoSeleccionado = idTipoMedioContactoSeleccionado; }
    public MedioContacto getMedioSeleccionado() { return medioSeleccionado; }
    public void setMedioSeleccionado(MedioContacto medioSeleccionado) { this.medioSeleccionado = medioSeleccionado; }
    public boolean isEditandoMedio() { return editandoMedio; }
    public void setEditandoMedio(boolean editandoMedio) { this.editandoMedio = editandoMedio; }
    public boolean isCapturandoMedio() { return capturandoMedio; }
    public void setCapturandoMedio(boolean capturandoMedio) { this.capturandoMedio = capturandoMedio; }
}
