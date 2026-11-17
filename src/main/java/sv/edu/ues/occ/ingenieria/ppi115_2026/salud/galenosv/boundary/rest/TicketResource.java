package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService.Datos;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService.PasoTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService.ProcedimientoTicket;

/**
 * Descarga el comprobante de una cita en formato PDF. El documento lo arma el
 * backend con la informacion de la consulta, sus procedimientos y sus pasos.
 */
@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
public class TicketResource {

    private static final Color ACENTO = new Color(15, 123, 108);
    private static final Color ACENTO_SUAVE = new Color(238, 245, 242);
    private static final Color TEXTO_TENUE = new Color(88, 112, 137);
    private static final Color BORDE = new Color(215, 222, 231);

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
            Datos datos = ticketService.datos(uuid);
            if (datos == null) return RestUtil.error(404, "id", "No existe el registro");
            byte[] pdf = generar(datos);
            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=\"ticket-" + datos.codigo + ".pdf\"")
                    .build();
        } catch (RuntimeException ex) {
            return RestUtil.error(500, "", "No se pudo generar el comprobante");
        }
    }

    /** Construye el PDF con un formato similar a la vista de impresion del frontend. */
    public byte[] generar(Datos datos) {
        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter escritor = PdfWriter.getInstance(documento, salida);
        escritor.setCompressionLevel(0);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(20, 32, 43));
        Font codigo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, ACENTO);
        Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA, 7, TEXTO_TENUE);
        Font valor = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(20, 32, 43));
        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(44, 74, 99));
        Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(44, 74, 99));
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(20, 32, 43));
        Font pie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, TEXTO_TENUE);

        Paragraph encabezado = new Paragraph("GalenoSV · Comprobante de cita", titulo);
        encabezado.setSpacingAfter(2);
        documento.add(encabezado);
        Paragraph meta = new Paragraph();
        meta.add(new Phrase("Ticket ", normal));
        meta.add(new Phrase("#" + datos.codigo, codigo));
        meta.add(new Phrase("  ·  " + datos.fecha, normal));
        meta.setSpacingAfter(6);
        documento.add(meta);
        LineSeparator linea = new LineSeparator(2f, 100f, ACENTO, Element.ALIGN_CENTER, -2);
        documento.add(linea);
        documento.add(new Paragraph(" "));

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingAfter(10);
        agregarDato(info, "Paciente", datos.paciente, etiqueta, valor);
        agregarDato(info, "Clinica", datos.clinica, etiqueta, valor);
        agregarDato(info, "Motivo", datos.motivo, etiqueta, valor);
        agregarDato(info, "Estado", datos.estado, etiqueta, valor);
        documento.add(info);

        Paragraph seccion = new Paragraph("Procedimientos y pasos", subtitulo);
        seccion.setSpacingAfter(6);
        documento.add(seccion);

        PdfPTable tabla = new PdfPTable(new float[]{2.2f, 2.2f, 1.2f, 1.6f, 2f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(10);
        for (String col : new String[]{"Procedimiento", "Paso", "Estado", "Fecha", "Realizado por / Notas"}) {
            PdfPCell celda = new PdfPCell(new Phrase(col, cabecera));
            celda.setBackgroundColor(ACENTO_SUAVE);
            celda.setBorderColor(BORDE);
            celda.setPadding(6);
            tabla.addCell(celda);
        }
        boolean vacio = true;
        for (ProcedimientoTicket proc : datos.procedimientos) {
            if (proc.pasos.isEmpty()) {
                vacio = false;
                agregarFila(tabla, proc.nombre, "Sin pasos registrados.", "-", "-", "-", normal);
            }
            for (PasoTicket paso : proc.pasos) {
                vacio = false;
                String detalle = paso.realizadoPor;
                if (paso.observaciones != null && !paso.observaciones.isBlank()) {
                    detalle = detalle + "\nObs.: " + paso.observaciones;
                }
                agregarFila(tabla, proc.nombre, paso.nombre, paso.estado, paso.fecha, detalle, normal);
            }
            for (String orden : proc.ordenes) {
                vacio = false;
                agregarFila(tabla, proc.nombre, orden, "-", "-", "-", normal);
            }
        }
        if (vacio) {
            PdfPCell celda = new PdfPCell(new Phrase("Sin procedimientos ni pasos registrados.", normal));
            celda.setColspan(5);
            celda.setBorderColor(BORDE);
            celda.setPadding(8);
            tabla.addCell(celda);
        }
        documento.add(tabla);

        documento.add(new Paragraph("Documento generado desde GalenoSV. Use este comprobante como resumen de la atencion registrada.", pie));
        documento.close();
        return salida.toByteArray();
    }

    private static void agregarDato(PdfPTable tabla, String nombre, String contenido, Font etiqueta, Font valor) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPaddingBottom(6);
        Paragraph p = new Paragraph();
        p.add(new Phrase(nombre.toUpperCase(), etiqueta));
        p.add(new Phrase("\n" + (contenido == null || contenido.isBlank() ? "-" : contenido), valor));
        celda.addElement(p);
        tabla.addCell(celda);
    }

    private static void agregarFila(PdfPTable tabla, String procedimiento, String paso, String estado, String fecha,
            String detalle, Font normal) {
        for (String texto : new String[]{procedimiento, paso, estado, fecha, detalle}) {
            PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "-" : texto, normal));
            celda.setBorderColor(BORDE);
            celda.setPadding(5);
            celda.setVerticalAlignment(Element.ALIGN_TOP);
            tabla.addCell(celda);
        }
    }
}
