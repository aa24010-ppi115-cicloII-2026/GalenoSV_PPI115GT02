package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ProcedimientoPaso sin relaciones JPA. */
public class ProcedimientoPasoDTO {
    private String idProcedimientoPaso;
    private String nombre;
    private Boolean indicaFin;
    private String idProcedimiento;
    private String idRol;

    public ProcedimientoPasoDTO() {
    }

    public String getIdProcedimientoPaso() { return idProcedimientoPaso; }
    public void setIdProcedimientoPaso(String idProcedimientoPaso) { this.idProcedimientoPaso = idProcedimientoPaso; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getIndicaFin() { return indicaFin; }
    public void setIndicaFin(Boolean indicaFin) { this.indicaFin = indicaFin; }
    public String getIdProcedimiento() { return idProcedimiento; }
    public void setIdProcedimiento(String idProcedimiento) { this.idProcedimiento = idProcedimiento; }
    public String getIdRol() { return idRol; }
    public void setIdRol(String idRol) { this.idRol = idRol; }
}
