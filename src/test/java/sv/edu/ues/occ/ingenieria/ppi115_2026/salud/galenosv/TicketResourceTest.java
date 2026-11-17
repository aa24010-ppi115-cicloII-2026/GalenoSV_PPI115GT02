package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest.TicketResource;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService.Datos;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService.PasoTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService.ProcedimientoTicket;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** El PDF del backend presenta los mismos datos que el comprobante del frontend. */
public class TicketResourceTest {

    private final TicketResource recurso = new TicketResource();

    private static Datos cita(List<ProcedimientoTicket> procedimientos) {
        return new Datos("36CD68F2", "Ana Lopez", "Clinica Dental", "25/09/2026 09:00", "Control", "En proceso",
                procedimientos);
    }

    private static String texto(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    @Test
    void generaUnPdfConLosDatosDeLaCitaYElCodigoCorto() {
        byte[] pdf = recurso.generar(cita(List.of()));

        assertTrue(texto(pdf).startsWith("%PDF-"));
        assertTrue(texto(pdf).trim().endsWith("%%EOF"));
        for (String dato : List.of("Comprobante de cita", "#36CD68F2", "Ana Lopez", "Clinica Dental", "Control", "En proceso")) {
            assertTrue(texto(pdf).contains(dato), "Falta en el PDF: " + dato);
        }
    }

    @Test
    void listaCadaPasoConLoRegistradoYLasOrdenesDeExamen() {
        PasoTicket evaluacion = new PasoTicket("Evaluacion clinica", "Completado", "25/09/2026 09:10", "Maria Lopez",
                "CIE-10: K02.1 | Notas: Encia sana");
        PasoTicket profilaxis = new PasoTicket("Profilaxis", "Por iniciar", "Sin fecha", "Sin asignar", "");
        byte[] pdf = recurso.generar(cita(List.of(new ProcedimientoTicket("Limpieza dental",
                List.of(evaluacion, profilaxis), List.of("Examen ordenado: Radiografia periapical")))));

        // La columna es angosta: el PDF parte el texto largo en líneas, así que se buscan sus partes.
        for (String dato : List.of("Procedimientos y pasos", "Limpieza dental", "Evaluacion clinica", "Maria Lopez",
                "K02.1", "Encia sana", "Profilaxis", "Por iniciar", "Examen ordenado: Radiografia")) {
            assertTrue(texto(pdf).contains(dato), "Falta en el PDF: " + dato);
        }
    }

    @Test
    void avisaCuandoLaConsultaNoTieneProcedimientos() {
        assertTrue(texto(recurso.generar(cita(List.of()))).contains("Sin procedimientos ni pasos registrados."));
    }
}
