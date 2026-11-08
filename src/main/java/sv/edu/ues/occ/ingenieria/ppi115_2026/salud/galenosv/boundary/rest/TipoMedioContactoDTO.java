package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de TipoMedioContacto sin relaciones JPA. */
public class TipoMedioContactoDTO {
    private String idTipoMedioContacto;
    private String nombre;
    private String indicaciones;
    private String expresionRegular;
    private Boolean activo;

    public TipoMedioContactoDTO() {
    }

    public String getIdTipoMedioContacto() { return idTipoMedioContacto; }
    public void setIdTipoMedioContacto(String idTipoMedioContacto) { this.idTipoMedioContacto = idTipoMedioContacto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public String getExpresionRegular() { return expresionRegular; }
    public void setExpresionRegular(String expresionRegular) { this.expresionRegular = expresionRegular; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
