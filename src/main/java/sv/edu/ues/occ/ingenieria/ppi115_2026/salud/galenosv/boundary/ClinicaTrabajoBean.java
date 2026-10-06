package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.PersonaRol;

@Named("clinicaTrabajoBean")
@SessionScoped
public class ClinicaTrabajoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    ClinicaDAO clinicaDAO;

    @Inject
    PersonaRolDAO personaRolDAO;

    private String idClinicaSeleccionada;
    private String idPersonaRolSeleccionado;
    private String idRolSeleccionado;
    private String idPersonaSeleccionada;
    private Clinica clinicaActual;
    private PersonaRol personaRolActual;

    public List<Clinica> getClinicas() {
        return clinicaDAO.findActivas();
    }

    public List<PersonaRol> getAsignacionesDisponibles() {
        UUID id = getIdClinicaActualOSeleccionada();
        return id == null ? Collections.emptyList() : personaRolDAO.findActivosByClinica(id);
    }

    public List<sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol> getRolesDisponibles() {
        java.util.Map<UUID, sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol> únicos = new java.util.LinkedHashMap<>();
        for (PersonaRol pr : getAsignacionesDisponibles()) {
            if (pr.getIdRol() != null && pr.getIdRol().getIdRol() != null) {
                únicos.putIfAbsent(pr.getIdRol().getIdRol(), pr.getIdRol());
            }
        }
        return new java.util.ArrayList<>(únicos.values());
    }

    public List<sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona> getPersonasDisponibles() {
        if (idRolSeleccionado == null || idRolSeleccionado.isBlank()) {
            return Collections.emptyList();
        }
        java.util.Map<UUID, sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona> únicas = new java.util.LinkedHashMap<>();
        for (PersonaRol pr : getAsignacionesDisponibles()) {
            if (pr.getIdRol() != null && idRolSeleccionado.equals(pr.getIdRol().getIdRol().toString())
                    && pr.getIdPersona() != null && pr.getIdPersona().getIdPersona() != null) {
                únicas.putIfAbsent(pr.getIdPersona().getIdPersona(), pr.getIdPersona());
            }
        }
        return new java.util.ArrayList<>(únicas.values());
    }

    public void alCambiarClinica() {
        idPersonaRolSeleccionado = null;
        idRolSeleccionado = null;
        idPersonaSeleccionada = null;
    }

    public void alCambiarRol() {
        idPersonaSeleccionada = null;
        idPersonaRolSeleccionado = null;
    }

    public void alCambiarPersona() {
        idPersonaRolSeleccionado = null;
        if (idPersonaSeleccionada == null || idPersonaSeleccionada.isBlank()
                || idRolSeleccionado == null || idRolSeleccionado.isBlank()) {
            return;
        }
        for (PersonaRol pr : getAsignacionesDisponibles()) {
            if (pr.getIdPersona() != null && idPersonaSeleccionada.equals(pr.getIdPersona().getIdPersona().toString())
                    && pr.getIdRol() != null && idRolSeleccionado.equals(pr.getIdRol().getIdRol().toString())) {
                idPersonaRolSeleccionado = pr.getIdPersonaRol().toString();
                break;
            }
        }
    }

    public String aplicar() {
        try {
            if (idClinicaSeleccionada == null || idClinicaSeleccionada.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar una clínica de trabajo");
            }
            Clinica clinica = clinicaDAO.find(UUID.fromString(idClinicaSeleccionada));
            if (clinica == null || !Boolean.TRUE.equals(clinica.getActivo())) {
                throw new IllegalArgumentException("La clínica seleccionada no está activa");
            }

            PersonaRol asignacion = null;
            if (idPersonaRolSeleccionado != null && !idPersonaRolSeleccionado.isBlank()) {
                asignacion = personaRolDAO.find(UUID.fromString(idPersonaRolSeleccionado));
                if (asignacion == null || asignacion.getIdClinica() == null
                        || !clinica.getIdClinica().equals(asignacion.getIdClinica().getIdClinica())
                        || asignacion.getIdRol() == null || !Boolean.TRUE.equals(asignacion.getIdRol().getActivo())) {
                    throw new IllegalArgumentException("La persona y el rol no pertenecen a la clínica seleccionada");
                }
            }

            clinicaActual = clinica;
            personaRolActual = asignacion;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Clínica de trabajo",
                            "Ahora trabaja en " + clinica.getNombre()));
            return "/paginas/Consulta?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo cambiar de clínica", ex.getMessage()));
            return null;
        }
    }

    public boolean isSeleccionada() {
        return clinicaActual != null;
    }

    public UUID getIdClinicaActual() {
        return clinicaActual == null ? null : clinicaActual.getIdClinica();
    }

    private UUID getIdClinicaActualOSeleccionada() {
        try {
            return idClinicaSeleccionada == null || idClinicaSeleccionada.isBlank()
                    ? getIdClinicaActual() : UUID.fromString(idClinicaSeleccionada);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public String getIdClinicaSeleccionada() {
        if ((idClinicaSeleccionada == null || idClinicaSeleccionada.isBlank()) && clinicaActual != null) {
            idClinicaSeleccionada = clinicaActual.getIdClinica().toString();
        }
        return idClinicaSeleccionada;
    }

    public void setIdClinicaSeleccionada(String idClinicaSeleccionada) {
        this.idClinicaSeleccionada = idClinicaSeleccionada;
    }

    public String getIdPersonaRolSeleccionado() {
        if ((idPersonaRolSeleccionado == null || idPersonaRolSeleccionado.isBlank()) && personaRolActual != null) {
            idPersonaRolSeleccionado = personaRolActual.getIdPersonaRol().toString();
        }
        return idPersonaRolSeleccionado;
    }

    public void setIdPersonaRolSeleccionado(String idPersonaRolSeleccionado) {
        this.idPersonaRolSeleccionado = idPersonaRolSeleccionado;
    }

    public String getIdRolSeleccionado() {
        if ((idRolSeleccionado == null || idRolSeleccionado.isBlank()) && personaRolActual != null
                && personaRolActual.getIdRol() != null) {
            idRolSeleccionado = personaRolActual.getIdRol().getIdRol().toString();
        }
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(String idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public String getIdPersonaSeleccionada() {
        if ((idPersonaSeleccionada == null || idPersonaSeleccionada.isBlank()) && personaRolActual != null
                && personaRolActual.getIdPersona() != null) {
            idPersonaSeleccionada = personaRolActual.getIdPersona().getIdPersona().toString();
        }
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public Clinica getClinicaActual() {
        return clinicaActual;
    }

    public PersonaRol getPersonaRolActual() {
        return personaRolActual;
    }

    public String getNombreAsignacionActual() {
        if (personaRolActual != null && personaRolActual.getIdPersona() != null
                && personaRolActual.getIdRol() != null) {
            return personaRolActual.getIdPersona().getNombres() + " "
                    + personaRolActual.getIdPersona().getApellidos() + " - "
                    + personaRolActual.getIdRol().getNombre();
        }
        return clinicaActual != null ? clinicaActual.getNombre() : "Cambiar de Rol";
    }
}
