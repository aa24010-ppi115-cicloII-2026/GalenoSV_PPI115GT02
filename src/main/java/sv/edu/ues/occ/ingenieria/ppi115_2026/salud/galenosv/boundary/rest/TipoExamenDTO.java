package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de TipoExamen sin relaciones JPA. */
public class TipoExamenDTO {
    private String idTipoExamen;
    private String nombre;
    private Boolean activo;
    private String observaciones;

    public TipoExamenDTO() {
    }

    public String getIdTipoExamen() { return idTipoExamen; }
    public void setIdTipoExamen(String idTipoExamen) { this.idTipoExamen = idTipoExamen; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
