package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de MedioContacto sin relaciones JPA. */
public class MedioContactoDTO {
    private String idMedioContacto;
    private String valor;
    private String fechaCreacion;
    private String idPersona;
    private String idTipoMedioContacto;

    public MedioContactoDTO() {
    }

    public String getIdMedioContacto() { return idMedioContacto; }
    public void setIdMedioContacto(String idMedioContacto) { this.idMedioContacto = idMedioContacto; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }
    public String getIdTipoMedioContacto() { return idTipoMedioContacto; }
    public void setIdTipoMedioContacto(String idTipoMedioContacto) { this.idTipoMedioContacto = idTipoMedioContacto; }
}
