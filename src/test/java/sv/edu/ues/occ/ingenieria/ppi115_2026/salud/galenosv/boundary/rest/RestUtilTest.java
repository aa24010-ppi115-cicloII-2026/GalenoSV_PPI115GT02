package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Conversión de parámetros de la API: el frontend muestra el campo que devuelve un 422 y reintenta sin duplicar. */
public class RestUtilTest {

    @Test
    void idNuevoUsaElDelClienteParaNoDuplicarAlReintentarSinConexion() {
        UUID enviado = UUID.randomUUID();
        assertEquals(enviado, RestUtil.idNuevo(enviado.toString(), "idOrdenExamen"));
        assertNotNull(RestUtil.idNuevo(null, "idOrdenExamen"));
        assertNotEquals(RestUtil.idNuevo("", "idOrdenExamen"), RestUtil.idNuevo("", "idOrdenExamen"));
        ParametroInvalidoException error = assertThrows(ParametroInvalidoException.class,
                () -> RestUtil.idNuevo("no-es-uuid", "idOrdenExamen"));
        assertEquals("idOrdenExamen", error.getParametro());
    }

    @Test
    void parsearIdDevuelveNullSiElIdentificadorDeLaRutaNoEsUuid() {
        UUID id = UUID.randomUUID();
        assertEquals(id, RestUtil.parsearId(id.toString()));
        assertNull(RestUtil.parsearId("abc"));
        assertNull(RestUtil.parsearId(null));
    }

    @Test
    void rangoAplicaLosValoresPorDefectoYRechazaLosFueraDelContrato() {
        assertEquals(new RestUtil.Rango(0, 20), RestUtil.rango(null, null));
        assertEquals(new RestUtil.Rango(5, 10), RestUtil.rango(5, 10));
        assertNull(RestUtil.rango(-1, 10));
        assertNull(RestUtil.rango(0, 0));
        assertNull(RestUtil.rango(0, 101));
    }

    @Test
    void fechaAceptaConZonaSinZonaOSoloElDiaYNombraElCampoInvalido() {
        assertEquals(OffsetDateTime.parse("2026-11-12T08:00-06:00"), RestUtil.fecha("2026-11-12T08:00-06:00", "fechaInicio"));
        assertEquals(OffsetDateTime.parse("2026-11-12T08:00Z"), RestUtil.fecha("2026-11-12T08:00", "fechaInicio"));
        assertEquals(OffsetDateTime.parse("2026-11-12T00:00Z"), RestUtil.fecha("2026-11-12", "fechaInicio"));
        assertNull(RestUtil.fecha(" ", "fechaInicio"));
        ParametroInvalidoException error = assertThrows(ParametroInvalidoException.class,
                () -> RestUtil.fecha("12/11/2026", "fechaFin"));
        assertEquals("fechaFin", error.getParametro());
    }

    @Test
    void rangoAceptaLosLimitesDelContratoYRechazaCombinacionesInvalidas() {
        assertEquals(new RestUtil.Rango(0, 100), RestUtil.rango(0, 100));
        assertEquals(new RestUtil.Rango(100, 1), RestUtil.rango(100, 1));
        assertNull(RestUtil.rango(null, 101));
        assertNull(RestUtil.rango(0, -1));
    }

    @Test
    void esConflictoReconoceLaLlaveForaneaAunqueVengaEnvueltaEnOtraExcepcion() {
        RuntimeException envuelta = new RuntimeException("Error al confirmar",
                new SQLException("ERROR: update or delete on table violates foreign key constraint", "23503"));
        assertTrue(RestUtil.esConflicto(envuelta));
        assertFalse(RestUtil.esConflicto(new IllegalStateException("Sin conexion")));
        assertFalse(RestUtil.esConflicto(null));
    }

    @Test
    void esConflictoCubreLasVariantesDelContratoSinConfundirOtrosErrores() {
        assertTrue(RestUtil.esConflicto(new RuntimeException("ERROR: FK_CONSULTA")));
        assertTrue(RestUtil.esConflicto(new RuntimeException("El registro tiene relacionados")));
        assertTrue(RestUtil.esConflicto(new SQLException("23503")));
        assertTrue(RestUtil.esConflicto(new java.sql.SQLIntegrityConstraintViolationException("otra")));
        assertFalse(RestUtil.esConflicto(new RuntimeException("Error de red", new IllegalStateException("timeout"))));
    }

}
