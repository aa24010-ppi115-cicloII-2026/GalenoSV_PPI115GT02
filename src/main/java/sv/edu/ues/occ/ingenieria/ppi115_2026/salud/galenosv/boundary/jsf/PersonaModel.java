package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;


@Named("personaModel")
@ViewScoped
public class PersonaModel extends AbstractModel<Persona> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/El_Salvador");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    FacesContext facesContext;

    @Inject
    PersonaDAO dao;

    private LocalDate fechaNacimientoSeleccionada;

    public PersonaModel() {
        this.nombreBean = "Persona";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<Persona> getDao() {
        return dao;
    }

    @Override
    protected Persona nuevoRegistro() {
        Persona r = new Persona();
        r.setIdPersona(UUID.randomUUID());
        r.setFechaCreacion(OffsetDateTime.now());
        return r;
    }

    @Override
    protected Persona buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado) {
            return dao.find(buscado);
        }
        return null;
    }

    @Override
    protected String getIdAsText(Persona r) {
        if (r != null && r.getIdPersona() != null) {
            return r.getIdPersona().toString();
        }
        return null;
    }

    @Override
    protected Persona getIdByText(String id) {
        if (id != null) {
            try {
                return dao.find(UUID.fromString(id));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void selectionHandler(SelectEvent<Persona> r) {
        super.selectionHandler(r);
        sincronizarFechaNacimiento();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        fechaNacimientoSeleccionada = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        fechaNacimientoSeleccionada = null;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                validarPersona();
                prepararFechaNacimiento();
                dao.crear(this.registro);
                limpiar("Registro guardado");
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                validarPersona();
                prepararFechaNacimiento();
                dao.modificar(this.registro);
                limpiar("Registro modificado");
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    private void validarPersona() {
        if (registro.getNombres() == null || registro.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("Los nombres son requeridos");
        }
        if (registro.getApellidos() == null || registro.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos son requeridos");
        }
    }

    private void prepararFechaNacimiento() {
        registro.setFechaNacimiento(fechaNacimientoSeleccionada == null
                ? null
                : fechaNacimientoSeleccionada.atStartOfDay(ZONA_LOCAL).toOffsetDateTime());
    }

    private void sincronizarFechaNacimiento() {
        fechaNacimientoSeleccionada = registro != null && registro.getFechaNacimiento() != null
                ? registro.getFechaNacimiento().toLocalDate()
                : null;
    }

    private void limpiar(String mensaje) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        this.fechaNacimientoSeleccionada = null;
        inicializarRegistros();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    public String formatearFecha(OffsetDateTime fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public LocalDate getFechaNacimientoSeleccionada() {
        return fechaNacimientoSeleccionada;
    }

    public void setFechaNacimientoSeleccionada(LocalDate fechaNacimientoSeleccionada) {
        this.fechaNacimientoSeleccionada = fechaNacimientoSeleccionada;
    }
}
