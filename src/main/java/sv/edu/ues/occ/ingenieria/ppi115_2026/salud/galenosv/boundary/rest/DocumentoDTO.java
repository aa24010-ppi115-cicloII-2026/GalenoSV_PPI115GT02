package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de Documento sin relaciones JPA. */
public class DocumentoDTO {
    private String idDocumento;
    private String valor;
    private String rutaFisica;
    private String idPersona;
    private String idTipoDocumento;

    public DocumentoDTO() {
    }

    public String getIdDocumento() { return idDocumento; }
    public void setIdDocumento(String idDocumento) { this.idDocumento = idDocumento; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getRutaFisica() { return rutaFisica; }
    public void setRutaFisica(String rutaFisica) { this.rutaFisica = rutaFisica; }
    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }
    public String getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(String idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }
}
