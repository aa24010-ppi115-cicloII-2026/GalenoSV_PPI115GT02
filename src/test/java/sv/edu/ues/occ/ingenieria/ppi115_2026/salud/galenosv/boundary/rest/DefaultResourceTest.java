package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.persistence.RollbackException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

/**
 * Contrato REST comun a todos los recursos: listado paginado, busqueda y eliminacion por
 * UUID, con el codigo HTTP, el mensaje y el header X-Parametro-Error de cada caso.
 */
public class DefaultResourceTest {

    private DefaultDAO<Rol> dao;
    private DefaultResource<Rol, String> recurso;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        RespuestasDePrueba.instalar();
        dao = mock(DefaultDAO.class);
        recurso = new DefaultResource<>() {
            @Override
            protected DefaultDAO<Rol> dao() {
                return dao;
            }

            @Override
            protected String aDTO(Rol entidad) {
                return "dto:" + entidad.getNombre();
            }
        };
    }

    private static Rol rol(String nombre) {
        Rol rol = new Rol(UUID.randomUUID());
        rol.setNombre(nombre);
        return rol;
    }

    private static void assertError(Response respuesta, int estado, String parametro, String mensaje) {
        assertEquals(estado, respuesta.getStatus());
        assertEquals(parametro, respuesta.getHeaderString("X-Parametro-Error"));
        assertEquals(Map.of("mensaje", mensaje), respuesta.getEntity());
    }

    @Test
    public void testOpcionesRespondeSinContenidoParaCors() {
        assertEquals(204, recurso.opciones().getStatus());
    }

    @Test
    public void testListarDevuelveLaPaginaEnDTOYElTotal() {
        when(dao.findRange(10, 2)).thenReturn(List.of(rol("Paciente"), rol("Odontologo")));
        when(dao.count()).thenReturn(12L);

        Response respuesta = recurso.listar(10, 2);

        assertEquals(200, respuesta.getStatus());
        assertEquals(List.of("dto:Paciente", "dto:Odontologo"), respuesta.getEntity());
        assertEquals("12", respuesta.getHeaderString("X-Total-Count"));
    }

    @Test
    public void testListarSinParametrosUsaLaPrimeraPagina() {
        when(dao.findRange(0, 20)).thenReturn(List.of());
        when(dao.count()).thenReturn(0L);

        assertEquals(200, recurso.listar(null, null).getStatus());
        verify(dao).findRange(0, 20);
    }

    @Test
    public void testListarConRangoInvalidoNoConsulta() {
        assertError(recurso.listar(-1, 5), 422, "first/max", "first debe ser >= 0 y max entre 1 y 100");
        assertError(recurso.listar(0, 101), 422, "first/max", "first debe ser >= 0 y max entre 1 y 100");
        verifyNoInteractions(dao);
    }

    @Test
    public void testListarConFalloDeLaBaseNoMuestraElErrorTecnico() {
        when(dao.findRange(0, 5)).thenThrow(new IllegalStateException("Connection refused"));

        assertError(recurso.listar(0, 5), 500, null, "No se pudo consultar el registro");
    }

    @Test
    public void testBuscarDevuelveElDTO() {
        UUID id = UUID.randomUUID();
        when(dao.find(id)).thenReturn(rol("Paciente"));

        Response respuesta = recurso.buscar(id.toString());

        assertEquals(200, respuesta.getStatus());
        assertEquals("dto:Paciente", respuesta.getEntity());
    }

    @Test
    public void testBuscarConUuidInvalidoONoExistente() {
        when(dao.find(any())).thenReturn(null);

        assertError(recurso.buscar("abc"), 422, "id", "El UUID no es valido");
        assertError(recurso.buscar(UUID.randomUUID().toString()), 404, "id", "No existe el registro");
    }

    @Test
    public void testBuscarConFalloDeLaBase() {
        when(dao.find(any())).thenThrow(new IllegalStateException("timeout"));

        assertError(recurso.buscar(UUID.randomUUID().toString()), 500, null, "No se pudo consultar el registro");
    }

    @Test
    public void testEliminarBorraElRegistro() {
        UUID id = UUID.randomUUID();
        Rol existente = rol("Paciente");
        when(dao.find(id)).thenReturn(existente);

        assertEquals(204, recurso.eliminar(id.toString()).getStatus());
        verify(dao).eliminar(existente);
    }

    @Test
    public void testEliminarConUuidInvalidoONoExistenteNoBorra() {
        when(dao.find(any())).thenReturn(null);

        assertError(recurso.eliminar("abc"), 422, "id", "El UUID no es valido");
        assertError(recurso.eliminar(UUID.randomUUID().toString()), 404, "id", "No existe el registro");
        verify(dao, never()).eliminar(any());
    }

    @Test
    public void testEliminarRegistroRelacionadoEsConflicto() {
        when(dao.find(any())).thenReturn(rol("Paciente"));
        doThrow(new IllegalStateException("No se pudo eliminar", new RollbackException("fk_persona_rol_rol")))
                .when(dao).eliminar(any());

        assertError(recurso.eliminar(UUID.randomUUID().toString()), 409, "id",
                "El registro esta relacionado y no se puede eliminar");
    }

    @Test
    public void testEliminarConOtroFalloDeLaBase() {
        when(dao.find(any())).thenReturn(rol("Paciente"));
        doThrow(new IllegalStateException("timeout")).when(dao).eliminar(any());

        assertError(recurso.eliminar(UUID.randomUUID().toString()), 500, null, "No se pudo eliminar el registro");
    }
}
