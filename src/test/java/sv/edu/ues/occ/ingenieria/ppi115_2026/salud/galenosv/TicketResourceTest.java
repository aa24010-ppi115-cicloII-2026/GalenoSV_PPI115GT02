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
<<<<<<< HEAD
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
=======
    void listaCadaPasoConLoRegistradoYLasOrdenesDeExamen() {
        PasoTicket evaluacion = new PasoTicket("Evaluacion clinica", "Completado", "25/09/2026 09:10", "Maria Lopez",
                "CIE-10: K02.1 | Notas: Encia sana");
        PasoTicket profilaxis = new PasoTicket("Profilaxis", "Por iniciar", "Sin fecha", "Sin asignar", "");
        byte[] pdf = recurso.generar(cita(List.of(new ProcedimientoTicket("Limpieza dental",
                List.of(evaluacion, profilaxis), List.of("Examen ordenado: Radiografia periapical")))));
>>>>>>> v2modificada

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
