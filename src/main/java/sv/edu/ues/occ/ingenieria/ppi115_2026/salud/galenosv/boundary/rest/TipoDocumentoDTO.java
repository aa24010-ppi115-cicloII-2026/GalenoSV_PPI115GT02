package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de TipoDocumento sin relaciones JPA. */
public class TipoDocumentoDTO {
    private String idTipoDocumento;
    private String nombre;
    private String indicaciones;
    private String expresionRegular;
    private Boolean activo;

    public TipoDocumentoDTO() {
    }

    public String getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(String idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public String getExpresionRegular() { return expresionRegular; }
    public void setExpresionRegular(String expresionRegular) { this.expresionRegular = expresionRegular; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
