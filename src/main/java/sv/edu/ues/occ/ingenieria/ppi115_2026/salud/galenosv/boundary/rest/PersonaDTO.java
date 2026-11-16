package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de Persona sin relaciones JPA. */
public class PersonaDTO {
    private String idPersona;
    private String nombres;
    private String apellidos;
    private String fechaNacimiento;
    private String fechaCreacion;

    public PersonaDTO() {
    }

    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
