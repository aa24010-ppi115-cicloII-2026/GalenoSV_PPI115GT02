package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Las pruebas corren sin servidor y sin una implementacion de JAX-RS, asi que
 * {@code Response.ok()} no tendria quien construya la respuesta. Este RuntimeDelegate de
 * Mockito solo anota estado, cuerpo y cabeceras para que las pruebas los comprueben.
 */
final class RespuestasDePrueba {

    private RespuestasDePrueba() {
    }

    /** Se llama en el @BeforeEach de las pruebas que construyen respuestas. */
    static void instalar() {
        RuntimeDelegate delegado = mock(RuntimeDelegate.class);
        when(delegado.createResponseBuilder()).thenAnswer(invocacion -> nuevoConstructor());
        RuntimeDelegate.setInstance(delegado);
    }

    private static ResponseBuilder nuevoConstructor() {
        int[] estado = {200};
        Object[] cuerpo = {null};
        Map<String, Object> cabeceras = new HashMap<>();
        return mock(ResponseBuilder.class, invocacion -> {
            Object[] args = invocacion.getArguments();
            switch (invocacion.getMethod().getName()) {
                case "status" -> estado[0] = args[0] instanceof Response.StatusType tipo ? tipo.getStatusCode() : (int) args[0];
                case "entity" -> cuerpo[0] = args[0];
                case "header" -> cabeceras.put((String) args[0], args[1]);
                case "location" -> cabeceras.put("Location", args[0]);
                case "build" -> {
                    return respuesta(estado[0], cuerpo[0], cabeceras);
                }
                default -> {
                    // Ningun otro metodo del constructor se usa en los recursos.
                }
            }
            return invocacion.getMock();
        });
    }

    private static Response respuesta(int estado, Object cuerpo, Map<String, Object> cabeceras) {
        Response respuesta = mock(Response.class);
        when(respuesta.getStatus()).thenReturn(estado);
        when(respuesta.getEntity()).thenReturn(cuerpo);
        when(respuesta.getHeaderString(org.mockito.ArgumentMatchers.anyString())).thenAnswer(invocacion -> {
            Object valor = cabeceras.get((String) invocacion.getArgument(0));
            return valor == null ? null : String.valueOf(valor);
        });
        when(respuesta.getLocation()).thenAnswer(invocacion -> (URI) cabeceras.get("Location"));
        return respuesta;
    }
}
