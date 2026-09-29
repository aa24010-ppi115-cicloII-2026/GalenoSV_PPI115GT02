package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;

@Named("inicioBean")
@RequestScoped
public class InicioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private ConsultaDAO consultaDAO;
    @Inject
    private OrdenExamenDAO ordenExamenDAO;
    @Inject
    private ExamenResultadoDAO examenResultadoDAO;

    public long getTotalPacientes() {
        try {
            Long c = personaDAO.count();
            return c == null ? 0 : c;
        } catch (Exception ex) {
            return 0;
        }
    }

    public long getTotalConsultas() {
        try {
            Long c = consultaDAO.count();
            return c == null ? 0 : c;
        } catch (Exception ex) {
            return 0;
        }
    }

    public long getTotalOrdenes() {
        try {
            Long c = ordenExamenDAO.count();
            return c == null ? 0 : c;
        } catch (Exception ex) {
            return 0;
        }
    }

    public long getTotalResultados() {
        try {
            Long c = examenResultadoDAO.count();
            return c == null ? 0 : c;
        } catch (Exception ex) {
            return 0;
        }
    }

    public List<Consulta> getUltimasConsultas() {
        try {
            List<Consulta> todas = consultaDAO.findRange(0, 5);
            return todas == null ? Collections.emptyList() : todas;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
