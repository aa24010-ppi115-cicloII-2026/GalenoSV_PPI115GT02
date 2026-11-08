package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

/** Senala que un parametro de la peticion falta o no tiene un formato valido. */
public class ParametroInvalidoException extends RuntimeException {

    private final String parametro;

    public ParametroInvalidoException(String parametro, String mensaje) {
        super(mensaje);
        this.parametro = parametro;
    }

    public String getParametro() {
        return parametro;
    }
}
