package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ConsultaProcedimiento sin relaciones JPA. */
public class ConsultaProcedimientoDTO {
    private String idConsultaProcedimiento;
    private String idProcedimiento;
    private String fechaInicio;
    private String fechaFin;
    private String observaciones;
    private String idConsulta;

    public ConsultaProcedimientoDTO() {
    }

    public String getIdConsultaProcedimiento() { return idConsultaProcedimiento; }
    public void setIdConsultaProcedimiento(String idConsultaProcedimiento) { this.idConsultaProcedimiento = idConsultaProcedimiento; }
    public String getIdProcedimiento() { return idProcedimiento; }
    public void setIdProcedimiento(String idProcedimiento) { this.idProcedimiento = idProcedimiento; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getIdConsulta() { return idConsulta; }
    public void setIdConsulta(String idConsulta) { this.idConsulta = idConsulta; }
}
