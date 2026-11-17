package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Representacion JSON de ExamenResultado sin relaciones JPA. */
public class ExamenResultadoDTO {
    private String idExamenResultado;
    private String fechaCreacion;
    private String resultado;
    private String interpretacion;
    private String rutaAtestado;
    private String idOrdenExamen;

    public ExamenResultadoDTO() {
    }

    public String getIdExamenResultado() { return idExamenResultado; }
    public void setIdExamenResultado(String idExamenResultado) { this.idExamenResultado = idExamenResultado; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getInterpretacion() { return interpretacion; }
    public void setInterpretacion(String interpretacion) { this.interpretacion = interpretacion; }
    public String getRutaAtestado() { return rutaAtestado; }
    public void setRutaAtestado(String rutaAtestado) { this.rutaAtestado = rutaAtestado; }
    public String getIdOrdenExamen() { return idOrdenExamen; }
    public void setIdOrdenExamen(String idOrdenExamen) { this.idOrdenExamen = idOrdenExamen; }
}
