package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest.TicketResource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TicketResourceTest {

    private final TicketResource recurso = new TicketResource();

    @Test
    void testGenerar_producePdfValido() {
        byte[] pdf = recurso.generar(List.of(
                "Paciente: Ana Lopez",
                "Clinica: Clinica Dental",
                "Fecha de la cita: 25/09/2026 09:00",
                "PROCEDIMIENTOS Y PASOS",
                "- Limpieza dental",
                "   Evaluacion clinica | FINALIZADO | 25/09/2026 09:10 | Maria Lopez"
        ));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "El PDF debe tener contenido");
        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(texto.startsWith("%PDF-"));
        assertTrue(texto.contains("%%EOF"));
    }

    @Test
    void testGenerar_incluyeElTextoDelComprobante() {
        byte[] pdf = recurso.generar(List.of("Paciente: Jose Torres", "Estado: Completada"));

        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(texto.contains("Jose Torres"));
        assertTrue(texto.contains("Completada"));
    }

    @Test
    void testGenerar_incluyeSeccionesTipoImpresion() {
        byte[] pdf = recurso.generar(List.of(
                "Paciente: Ana Lopez",
                "Clinica: Clinica Dental",
                "Fecha de la cita: 25/09/2026 09:00",
                "Motivo: Control",
                "Estado: Completada",
                "Ticket: #abc-123",
                "PROCEDIMIENTOS Y PASOS",
                "- Limpieza dental",
                "   Evaluacion clinica | Completado | 25/09/2026 09:10 | Maria Lopez"
        ));

        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(texto.contains("Comprobante de cita"));
        assertTrue(texto.contains("Procedimientos y pasos"));
        assertTrue(texto.contains("Limpieza dental"));
        assertTrue(texto.contains("Evaluacion clinica"));
        assertTrue(texto.contains("abc-123"));
    }

    @Test
    void testGenerar_aceptaListaVacia() {
        byte[] pdf = recurso.generar(List.of());

        assertNotNull(pdf);
        assertTrue(new String(pdf, StandardCharsets.ISO_8859_1).startsWith("%PDF-"));
    }
}
