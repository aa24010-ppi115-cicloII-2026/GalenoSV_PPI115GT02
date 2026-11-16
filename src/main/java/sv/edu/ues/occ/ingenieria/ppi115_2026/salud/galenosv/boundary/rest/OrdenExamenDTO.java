package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de OrdenExamen sin relaciones JPA. */
public class OrdenExamenDTO {
    private String idOrdenExamen;
    private String fechaCreacion;
    private String indicaciones;
    private String idConsultaProcedimientoPaso;

    public OrdenExamenDTO() {
    }

    public String getIdOrdenExamen() { return idOrdenExamen; }
    public void setIdOrdenExamen(String idOrdenExamen) { this.idOrdenExamen = idOrdenExamen; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public String getIdConsultaProcedimientoPaso() { return idConsultaProcedimientoPaso; }
    public void setIdConsultaProcedimientoPaso(String idConsultaProcedimientoPaso) { this.idConsultaProcedimientoPaso = idConsultaProcedimientoPaso; }
}
