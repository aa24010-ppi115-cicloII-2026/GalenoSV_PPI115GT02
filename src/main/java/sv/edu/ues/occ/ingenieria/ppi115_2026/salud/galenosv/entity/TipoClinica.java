package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity;

/**
 * Tipos de clínica disponibles para el combo de {@link Clinica}.
 * El valor se guarda como texto en la columna {@code clinica.tipo} (varchar(20)).
 */
public enum TipoClinica {

    CENTRAL("Central"),
    DEPARTAMENTAL("Departamental"),
    MUNICIPAL("Municipal");

    private final String etiqueta;

    TipoClinica(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getValor() {
        return etiqueta;
    }
}
