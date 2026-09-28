package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv;

import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractModelTest {

    @Test
    void validaNombreCuandoLaEntidadTieneEseCampo() {
        ModeloPrueba<EntidadConNombre> modelo = new ModeloPrueba<>();

        assertTrue(modelo.nombreVacio(new EntidadConNombre(null)));
        assertTrue(modelo.nombreVacio(new EntidadConNombre("   ")));
        assertFalse(modelo.nombreVacio(new EntidadConNombre("Examen")));
    }

    @Test
    void noExigeNombreAEntidadesQueNoTienenEseCampo() {
        ModeloPrueba<EntidadSinNombre> modelo = new ModeloPrueba<>();

        assertFalse(modelo.nombreVacio(new EntidadSinNombre()));
    }

    public static class EntidadConNombre {

        private final String nombre;

        EntidadConNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    public static class EntidadSinNombre {
    }

    private static class ModeloPrueba<T> extends AbstractModel<T> {

        boolean nombreVacio(T registro) {
            return esNombreVacio(registro);
        }

        @Override
        protected FacesContext getFacesContext() {
            return null;
        }

        @Override
        protected DefaultDAO<T> getDao() {
            return null;
        }

        @Override
        protected T nuevoRegistro() {
            return null;
        }

        @Override
        protected T buscarRegistroPorId(Object id) {
            return null;
        }

        @Override
        protected String getIdAsText(T registro) {
            return null;
        }

        @Override
        protected T getIdByText(String id) {
            return null;
        }
    }
}
