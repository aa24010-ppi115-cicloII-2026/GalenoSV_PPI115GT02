package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Consulta;

/**
 * Datos de la pantalla de inicio. Se consultan una vez por peticion al crear el bean:
 * JSF evalua los getters varias veces en un mismo render (la tabla lee su value en cada fase),
 * y consultar ahi repetia las mismas lecturas a la base.
 */
@Named("inicioBean")
@RequestScoped
public class InicioBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(InicioBean.class.getName());

    /** Cuantas consultas recientes muestra el inicio. */
    static final int CONSULTAS_RECIENTES = 5;

    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private ConsultaDAO consultaDAO;
    @Inject
    private OrdenExamenDAO ordenExamenDAO;
    @Inject
    private ExamenResultadoDAO examenResultadoDAO;

    private long totalPacientes;
    private long totalConsultas;
    private long totalOrdenes;
    private long totalResultados;
    private List<Consulta> ultimasConsultas = Collections.emptyList();

    /** Carga los totales y las consultas recientes; un dato que falla no impide los demas. */
    @PostConstruct
    public void cargar() {
        totalPacientes = total(personaDAO::count);
        totalConsultas = total(consultaDAO::count);
        totalOrdenes = total(ordenExamenDAO::count);
        totalResultados = total(examenResultadoDAO::count);
        try {
            List<Consulta> recientes = consultaDAO.findRecientes(CONSULTAS_RECIENTES);
            ultimasConsultas = recientes == null ? Collections.emptyList() : recientes;
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "No se pudieron cargar las consultas recientes", ex);
            ultimasConsultas = Collections.emptyList();
        }
    }

    private static long total(Supplier<Long> contar) {
        try {
            Long cantidad = contar.get();
            return cantidad == null ? 0 : cantidad;
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "No se pudo contar un total del inicio", ex);
            return 0;
        }
    }

    public long getTotalPacientes() {
        return totalPacientes;
    }

    public long getTotalConsultas() {
        return totalConsultas;
    }

    public long getTotalOrdenes() {
        return totalOrdenes;
    }

    public long getTotalResultados() {
        return totalResultados;
    }

    public List<Consulta> getUltimasConsultas() {
        return ultimasConsultas;
    }
}
