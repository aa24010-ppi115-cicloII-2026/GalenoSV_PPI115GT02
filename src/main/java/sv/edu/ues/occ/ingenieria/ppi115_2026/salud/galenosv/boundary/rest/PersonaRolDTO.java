package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de PersonaRol sin relaciones JPA. */
public class PersonaRolDTO {
    private String idPersonaRol;
    private String fechaCreacion;
    private String idClinica;
    private String idPersona;
    private String idRol;

    public PersonaRolDTO() {
    }

    public String getIdPersonaRol() { return idPersonaRol; }
    public void setIdPersonaRol(String idPersonaRol) { this.idPersonaRol = idPersonaRol; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getIdClinica() { return idClinica; }
    public void setIdClinica(String idClinica) { this.idClinica = idClinica; }
    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }
    public String getIdRol() { return idRol; }
    public void setIdRol(String idRol) { this.idRol = idRol; }
}
