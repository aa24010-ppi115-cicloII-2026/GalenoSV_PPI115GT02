package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ProcedimientoPasoExamen sin relaciones JPA. */
public class ProcedimientoPasoExamenDTO {
    private String idProcedimientoPasoExamen;
    private String fechaCreacion;
    private Boolean activo;
    private String observaciones;
    private String idExamen;
    private String idProcedimientoPaso;

    public ProcedimientoPasoExamenDTO() {
    }

    public String getIdProcedimientoPasoExamen() { return idProcedimientoPasoExamen; }
    public void setIdProcedimientoPasoExamen(String idProcedimientoPasoExamen) { this.idProcedimientoPasoExamen = idProcedimientoPasoExamen; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getIdExamen() { return idExamen; }
    public void setIdExamen(String idExamen) { this.idExamen = idExamen; }
    public String getIdProcedimientoPaso() { return idProcedimientoPaso; }
    public void setIdProcedimientoPaso(String idProcedimientoPaso) { this.idProcedimientoPaso = idProcedimientoPaso; }
}
