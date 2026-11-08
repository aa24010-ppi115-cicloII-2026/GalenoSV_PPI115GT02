package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/** Permite que el frontend local (BrowserSync o Live Server) consuma la API. */
@Provider
public class CorsResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response)
            throws IOException {
        String origin = request.getHeaderString("Origin");
        if (esOrigenLocal(origin)) {
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            response.getHeaders().putSingle("Vary", "Origin");
            response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.getHeaders().putSingle("Access-Control-Allow-Headers", "Accept, Content-Type");
            response.getHeaders().putSingle("Access-Control-Expose-Headers", "X-Total-Count, X-Parametro-Error, Location");
            response.getHeaders().putSingle("Access-Control-Max-Age", "3600");
        }
    }

    private boolean esOrigenLocal(String origin) {
        if (origin == null) {
            return false;
        }
        return origin.startsWith("http://localhost:") || origin.startsWith("http://127.0.0.1:");
    }
}
