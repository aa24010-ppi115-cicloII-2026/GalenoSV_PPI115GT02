package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Named("fechaBean")
@ApplicationScoped
public class FechaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");

    public String fecha(OffsetDateTime valor) {
        return valor == null ? "" : valor.format(FECHA);
    }

    public String fechaHora(OffsetDateTime valor) {
        return valor == null ? "" : valor.format(FECHA_HORA);
    }

    /** Año en curso, tope de los calendarios (yearRange) para no fijarlo a mano. */
    public int getAnioActual() {
        return LocalDate.now(ZONA_LOCAL).getYear();
    }
}
