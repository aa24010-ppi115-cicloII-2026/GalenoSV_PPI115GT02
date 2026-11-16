package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ConsultaProcedimientoPaso sin relaciones JPA. */
public class ConsultaProcedimientoPasoDTO {
    private String idConsultaProcedimientoPaso;
    private String fechaInicio;
    private String fechaFin;
    private String estado;
    private String idConsultaProcedimiento;
    private String idPersonaRol;

    public ConsultaProcedimientoPasoDTO() {
    }

    public String getIdConsultaProcedimientoPaso() { return idConsultaProcedimientoPaso; }
    public void setIdConsultaProcedimientoPaso(String idConsultaProcedimientoPaso) { this.idConsultaProcedimientoPaso = idConsultaProcedimientoPaso; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getIdConsultaProcedimiento() { return idConsultaProcedimiento; }
    public void setIdConsultaProcedimiento(String idConsultaProcedimiento) { this.idConsultaProcedimiento = idConsultaProcedimiento; }
    public String getIdPersonaRol() { return idPersonaRol; }
    public void setIdPersonaRol(String idPersonaRol) { this.idPersonaRol = idPersonaRol; }
}
