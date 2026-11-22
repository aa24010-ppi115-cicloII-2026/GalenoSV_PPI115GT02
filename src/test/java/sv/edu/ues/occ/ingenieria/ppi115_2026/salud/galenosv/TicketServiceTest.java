package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TicketService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.OrdenExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** El comprobante del backend debe contar los pasos igual que la tarjeta de la cita en el frontend. */
@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock private ConsultaDAO consultaDAO;
    @Mock private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Mock private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;
    @Mock private ProcedimientoDAO procedimientoDAO;
    @Mock private ProcedimientoPasoDAO procedimientoPasoDAO;
    @Mock private OrdenExamenDAO ordenExamenDAO;
    @Mock private ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    @InjectMocks private TicketService servicio;

    private final UUID idConsulta = UUID.randomUUID();
    private final UUID idProcedimiento = UUID.randomUUID();
    private final ConsultaProcedimiento procedimiento = new ConsultaProcedimiento();
    private final ProcedimientoPaso evaluacion = paso("Evaluación inicial");
    private final ProcedimientoPaso profilaxis = paso("Profilaxis");
    private final ProcedimientoPaso indicaciones = paso("Indicaciones finales");

    @BeforeEach
    void preparar() {
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        procedimiento.setIdConsultaProcedimiento(UUID.randomUUID());
        procedimiento.setIdProcedimiento(idProcedimiento);
        when(consultaDAO.find(idConsulta)).thenReturn(consulta);
        when(consultaProcedimientoDAO.findByConsulta(idConsulta)).thenReturn(List.of(procedimiento));
        // El DAO ordena por UUID: los pasos llegan en un orden distinto al de la secuencia.
        when(procedimientoPasoDAO.findByProcedimiento(idProcedimiento)).thenReturn(List.of(profilaxis, indicaciones, evaluacion));
        when(secuenciaDAO.findAll()).thenReturn(List.of(despuesDe(profilaxis, evaluacion), despuesDe(indicaciones, profilaxis)));
    }

    @Test
    void ordenaLosPasosPorSecuenciaYMarcaElSiguientePorIniciar() {
        registros(registro("FINALIZADO", 9));

        TicketService.Datos datos = servicio.datos(idConsulta);

<<<<<<< HEAD
        List<TicketService.PasoTicket> pasos = datos.procedimientos.get(0).pasos;
        assertEquals("Evaluación inicial", pasos.get(0).nombre);
        assertEquals("Completado", pasos.get(0).estado);
        assertEquals("14/11/2026 09:00", pasos.get(0).fecha);
        assertEquals("Profilaxis", pasos.get(1).nombre);
        assertEquals("En curso", pasos.get(1).estado);
        assertEquals("Indicaciones finales", pasos.get(2).nombre);
        assertEquals("Pendiente", pasos.get(2).estado);
        assertEquals("En proceso", datos.estado);
        assertTrue(servicio.lineas(idConsulta).contains("Estado: En proceso"));
=======
        assertEquals(List.of(
                "Evaluación inicial | Completado | 14/11/2026 09:00",
                "Profilaxis | Por iniciar | Sin fecha",
                "Indicaciones finales | Pendiente | Sin fecha"), resumen(datos));
        assertEquals("En proceso", datos.estado, "Faltan dos pasos: la cita no está completada");
        assertEquals(idConsulta.toString().substring(0, 8).toUpperCase(), datos.codigo,
                "El ticket usa el mismo código corto que el frontend");
>>>>>>> v2modificada
    }

    @Test
    void emparejaLosRegistrosPorFechaYNoPorIdentificador() {
        registros(registro("FINALIZADO", 10), registro("FINALIZADO", 9));

<<<<<<< HEAD
        List<TicketService.PasoTicket> pasos = servicio.datos(idConsulta).procedimientos.get(0).pasos;

        assertEquals("Evaluación inicial", pasos.get(0).nombre);
        assertEquals("14/11/2026 09:00", pasos.get(0).fecha);
        assertEquals("Profilaxis", pasos.get(1).nombre);
        assertEquals("14/11/2026 10:00", pasos.get(1).fecha);
        assertTrue(pasos(servicio.lineas(idConsulta)).get(0).startsWith("   Evaluación inicial | Completado | 14/11/2026 09:00"));
=======
        List<String> pasos = resumen(servicio.datos(idConsulta));

        assertEquals("Evaluación inicial | Completado | 14/11/2026 09:00", pasos.get(0));
        assertEquals("Profilaxis | Completado | 14/11/2026 10:00", pasos.get(1));
    }

    @Test
    void elPasoIniciadoSinTerminarQuedaEnCurso() {
        registros(registro("FINALIZADO", 9), registro("EN_PROCESO", 10));

        List<String> pasos = resumen(servicio.datos(idConsulta));

        assertEquals("Profilaxis | En curso | 14/11/2026 10:00", pasos.get(1));
        assertEquals("Indicaciones finales | Pendiente | Sin fecha", pasos.get(2));
    }

    @Test
    void trasRetrocederSoloElPrimerPasoSinTerminarQuedaEnCursoYElSiguienteSinFecha() {
        // Se retrocedió desde «Indicaciones finales»: conserva su registro, pero vuelve a estar pendiente.
        registros(registro("FINALIZADO", 9), registro("EN_PROCESO", 10), registro("EN_PROCESO", 11));

        List<String> pasos = resumen(servicio.datos(idConsulta));

        assertEquals("Profilaxis | En curso | 14/11/2026 10:00", pasos.get(1));
        assertEquals("Indicaciones finales | Pendiente | Sin fecha", pasos.get(2));
        assertEquals("Sin asignar", servicio.datos(idConsulta).procedimientos.get(0).pasos.get(2).realizadoPor);
    }

    @Test
    void lasOrdenesDeExamenNoRepitenLaPalabraExamen() {
        ConsultaProcedimientoPaso evaluado = registro("FINALIZADO", 9);
        registros(evaluado);
        OrdenExamen orden = new OrdenExamen();
        orden.setIndicaciones("Examen: Hemograma completo — En ayunas");
        when(ordenExamenDAO.findByConsultaProcedimientoPaso(evaluado.getIdConsultaProcedimientoPaso())).thenReturn(List.of(orden));

        assertEquals(List.of("Examen ordenado: Hemograma completo — En ayunas"),
                servicio.datos(idConsulta).procedimientos.get(0).ordenes);
    }

    @Test
    void muestraLoRegistradoEnCadaPasoYNoElBorradorDelPasoEnCurso() {
        procedimiento.setObservaciones("{\"notas\":\"Borrador sin guardar\",\"diagnostico\":\"K00.0 · Anodoncia\","
                + "\"pasos\":{\"" + evaluacion.getIdProcedimientoPaso() + "\":{\"diagnostico\":\"K02.1 · Caries de la dentina\","
                + "\"notas\":\"Encía sana\",\"plan\":\"\"}}}");
        registros(registro("FINALIZADO", 9), registro("EN_PROCESO", 10));

        List<TicketService.PasoTicket> pasos = servicio.datos(idConsulta).procedimientos.get(0).pasos;

        assertEquals("CIE-10: K02.1 · Caries de la dentina | Notas: Encía sana", pasos.get(0).observaciones);
        assertEquals("", pasos.get(1).observaciones, "El borrador del paso en curso no es parte del comprobante");
    }

    @Test
    void toleraObservacionesEnTextoLibreDeRegistrosAntiguos() {
        procedimiento.setObservaciones("Paciente refiere sensibilidad");
        registros(registro("FINALIZADO", 9));

        assertEquals("", servicio.datos(idConsulta).procedimientos.get(0).pasos.get(0).observaciones);
>>>>>>> v2modificada
    }

    @Test
    void laCitaSoloQuedaCompletadaCuandoTodosLosPasosTerminaron() {
        registros(registro("FINALIZADO", 9), registro("FINALIZADO", 10), registro("FINALIZADO", 11));

        assertEquals("Completada", servicio.datos(idConsulta).estado);
<<<<<<< HEAD
        assertTrue(servicio.lineas(idConsulta).contains("Estado: Completada"));
=======
>>>>>>> v2modificada
    }

    private void registros(ConsultaProcedimientoPaso... lista) {
        when(consultaProcedimientoPasoDAO.findByConsultaProcedimiento(procedimiento.getIdConsultaProcedimiento()))
                .thenReturn(List.of(lista));
    }

    private static List<String> resumen(TicketService.Datos datos) {
        return datos.procedimientos.get(0).pasos.stream()
                .map(paso -> paso.nombre + " | " + paso.estado + " | " + paso.fecha)
                .toList();
    }

    private static ProcedimientoPaso paso(String nombre) {
        ProcedimientoPaso paso = new ProcedimientoPaso();
        paso.setIdProcedimientoPaso(UUID.randomUUID());
        paso.setNombre(nombre);
        return paso;
    }

    private static ProcedimientoPasoSecuencia despuesDe(ProcedimientoPaso paso, ProcedimientoPaso referencia) {
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPaso(paso);
        secuencia.setIdProcedimientoPasoReferencia(referencia.getIdProcedimientoPaso());
        secuencia.setTipoSecuencia("DESPUES_DE");
        return secuencia;
    }

    private static ConsultaProcedimientoPaso registro(String estado, int hora) {
        ConsultaProcedimientoPaso registro = new ConsultaProcedimientoPaso();
        registro.setIdConsultaProcedimientoPaso(UUID.randomUUID());
        registro.setEstado(estado);
        registro.setFechaInicio(OffsetDateTime.parse(String.format("2026-11-14T%02d:00-06:00", hora)));
        return registro;
    }
}
