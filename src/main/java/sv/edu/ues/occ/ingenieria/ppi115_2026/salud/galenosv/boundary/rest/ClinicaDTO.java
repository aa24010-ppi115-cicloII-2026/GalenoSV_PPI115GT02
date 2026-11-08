package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de Clinica sin relaciones JPA. */
public class ClinicaDTO {
    private String idClinica;
    private String nombre;
    private Boolean activo;
    private String tipo;
    private String comentarios;

    public ClinicaDTO() {
    }

    public String getIdClinica() { return idClinica; }
    public void setIdClinica(String idClinica) { this.idClinica = idClinica; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
}
