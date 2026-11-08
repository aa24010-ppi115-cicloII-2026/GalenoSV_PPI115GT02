package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de Procedimiento sin relaciones JPA. */
public class ProcedimientoDTO {
    private String idProcedimiento;
    private String nombre;
    private Boolean activo;
    private String observaciones;

    public ProcedimientoDTO() {
    }

    public String getIdProcedimiento() { return idProcedimiento; }
    public void setIdProcedimiento(String idProcedimiento) { this.idProcedimiento = idProcedimiento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
