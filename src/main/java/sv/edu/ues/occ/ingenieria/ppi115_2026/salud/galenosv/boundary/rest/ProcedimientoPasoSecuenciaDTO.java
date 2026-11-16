package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ProcedimientoPasoSecuencia sin relaciones JPA. */
public class ProcedimientoPasoSecuenciaDTO {
    private String idProcedimientoPasoSecuencia;
    private String idProcedimientoPasoReferencia;
    private String tipoSecuencia;
    private String idProcedimientoPaso;

    public ProcedimientoPasoSecuenciaDTO() {
    }

    public String getIdProcedimientoPasoSecuencia() { return idProcedimientoPasoSecuencia; }
    public void setIdProcedimientoPasoSecuencia(String idProcedimientoPasoSecuencia) { this.idProcedimientoPasoSecuencia = idProcedimientoPasoSecuencia; }
    public String getIdProcedimientoPasoReferencia() { return idProcedimientoPasoReferencia; }
    public void setIdProcedimientoPasoReferencia(String idProcedimientoPasoReferencia) { this.idProcedimientoPasoReferencia = idProcedimientoPasoReferencia; }
    public String getTipoSecuencia() { return tipoSecuencia; }
    public void setTipoSecuencia(String tipoSecuencia) { this.tipoSecuencia = tipoSecuencia; }
    public String getIdProcedimientoPaso() { return idProcedimientoPaso; }
    public void setIdProcedimientoPaso(String idProcedimientoPaso) { this.idProcedimientoPaso = idProcedimientoPaso; }
}
