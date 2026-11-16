package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de Consulta sin relaciones JPA. */
public class ConsultaDTO {
    private String idConsulta;
    private String fechaInicio;
    private String fechaFin;
    private String referenciaExterna;
    private String observaciones;
    private String idPersonaRol;

    public ConsultaDTO() {
    }

    public String getIdConsulta() { return idConsulta; }
    public void setIdConsulta(String idConsulta) { this.idConsulta = idConsulta; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public String getReferenciaExterna() { return referenciaExterna; }
    public void setReferenciaExterna(String referenciaExterna) { this.referenciaExterna = referenciaExterna; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getIdPersonaRol() { return idPersonaRol; }
    public void setIdPersonaRol(String idPersonaRol) { this.idPersonaRol = idPersonaRol; }
}
