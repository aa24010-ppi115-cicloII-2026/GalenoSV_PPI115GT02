package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ExamenTipoExamen sin relaciones JPA. */
public class ExamenTipoExamenDTO {
    private String idExamenTipoExamen;
    private String fechaCreacion;
    private String observaciones;
    private String idExamen;
    private String idTipoExamen;

    public ExamenTipoExamenDTO() {
    }

    public String getIdExamenTipoExamen() { return idExamenTipoExamen; }
    public void setIdExamenTipoExamen(String idExamenTipoExamen) { this.idExamenTipoExamen = idExamenTipoExamen; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getIdExamen() { return idExamen; }
    public void setIdExamen(String idExamen) { this.idExamen = idExamen; }
    public String getIdTipoExamen() { return idTipoExamen; }
    public void setIdTipoExamen(String idTipoExamen) { this.idTipoExamen = idTipoExamen; }
}
