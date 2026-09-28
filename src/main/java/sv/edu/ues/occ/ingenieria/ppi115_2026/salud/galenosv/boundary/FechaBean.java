package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Named("fechaBean")
@ApplicationScoped
public class FechaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String fecha(OffsetDateTime valor) {
        return valor == null ? "" : valor.format(FECHA);
    }

    public String fechaHora(OffsetDateTime valor) {
        return valor == null ? "" : valor.format(FECHA_HORA);
    }
}
