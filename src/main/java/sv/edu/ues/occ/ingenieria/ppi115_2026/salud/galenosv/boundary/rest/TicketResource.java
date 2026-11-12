package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService;

/**
 * Descarga el comprobante de una cita en formato PDF. El documento lo arma el
 * backend con la informacion de la consulta, sus procedimientos y sus pasos.
 */
@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
public class TicketResource {

    @Inject
    private TicketService ticketService;

    /** Comprobante en PDF de la consulta indicada. */
    @GET
    @Path("/consultas/{id}")
    @Produces("application/pdf")
    public Response ticket(@PathParam("id") String id) {
        UUID uuid = RestUtil.parsearId(id);
        if (uuid == null) return RestUtil.error(422, "id", "El UUID no es valido");
        try {
            List<String> lineas = ticketService.lineas(uuid);
            if (lineas == null) return RestUtil.error(404, "id", "No existe el registro");
            byte[] pdf = generar(lineas);
            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=\"ticket-" + uuid + ".pdf\"")
                    .build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo generar el comprobante");
        }
    }

    /** Construye el PDF con OpenPDF a partir de las lineas del comprobante. */
    public byte[] generar(List<String> lineas) {
        Document documento = new Document(PageSize.A4, 48, 48, 48, 48);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter escritor = PdfWriter.getInstance(documento, salida);
        // Sin compresion para que el comprobante pueda inspeccionarse y extraerse texto.
        escritor.setCompressionLevel(0);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        documento.add(new Paragraph("GalenoSV - Comprobante de cita", titulo));
        documento.add(new Paragraph("Sistema de gestion clinica odontologica", normal));
        documento.add(new Paragraph(" "));

        for (String linea : lineas) {
            String texto = linea == null ? "" : linea;
            if (texto.isBlank()) {
                documento.add(new Paragraph(" "));
            } else if (texto.equals("PROCEDIMIENTOS Y PASOS")) {
                documento.add(new Paragraph(texto, subtitulo));
            } else if (texto.startsWith("- ")) {
                documento.add(new Paragraph(texto, subtitulo));
            } else if (texto.startsWith("   ")) {
                PdfPTable tabla = new PdfPTable(1);
                tabla.setWidthPercentage(100);
                PdfPCell celda = new PdfPCell(new Phrase(texto.trim(), normal));
                celda.setPadding(4);
                celda.setBorderWidth(0);
                celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabla.addCell(celda);
                documento.add(tabla);
            } else {
                documento.add(new Paragraph(texto, normal));
            }
        }

        documento.add(new Paragraph(" "));
        documento.add(new Paragraph("Este comprobante resume la atencion registrada en GalenoSV.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)));
        documento.close();
        return salida.toByteArray();
    }
}
