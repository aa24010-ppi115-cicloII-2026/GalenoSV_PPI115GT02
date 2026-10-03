package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.PersonaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.MedioContacto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Persona;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named("medioContactoModel")
@ViewScoped
public class MedioContactoModel extends AbstractModel<MedioContacto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    MedioContactoDAO dao;
    @Inject
    PersonaDAO personaDAO;
    @Inject
    TipoMedioContactoDAO tipoMedioContactoDAO;

    private String idPersonaSeleccionada;
    private String idTipoMedioContactoSeleccionado;
    private UUID idPersonaMaestro;

    @Override
    protected void validarUnicidad(MedioContacto r, boolean esModificacion) {
        if (r.getIdPersona() == null || r.getIdPersona().getIdPersona() == null
                || r.getValor() == null || r.getValor().isBlank()) {
            return;
        }
        java.util.UUID excluir = esModificacion ? r.getIdMedioContacto() : null;
        if (dao.existePersonaValor(r.getIdPersona().getIdPersona(), r.getValor(), excluir)) {
            throw new IllegalArgumentException("Esa persona ya tiene registrado ese medio de contacto");
        }
    }

    public MedioContactoModel() {
        this.nombreBean = "MedioContacto";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<MedioContacto> getDao() {
        return dao;
    }

    @Override
    protected MedioContacto nuevoRegistro() {
        MedioContacto r = new MedioContacto();
        r.setIdMedioContacto(UUID.randomUUID());
        r.setFechaCreacion(OffsetDateTime.now());
        if (idPersonaMaestro != null) {
            r.setIdPersona(personaDAO.find(idPersonaMaestro));
        }
        return r;
    }

    @Override
    public void inicializarRegistros() {
        if (idPersonaMaestro == null) {
            super.inicializarRegistros();
            return;
        }
        this.modelo = new LazyDataModel<MedioContacto>() {
            @Override
            public String getRowKey(MedioContacto contacto) {
                return getIdAsText(contacto);
            }

            @Override
            public MedioContacto getRowData(String rowKey) {
                return getIdByText(rowKey);
            }

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return dao.countByPersona(idPersonaMaestro).intValue();
            }

            @Override
            public List<MedioContacto> load(int first, int pageSize, Map<String, SortMeta> sortBy,
                    Map<String, FilterMeta> filterBy) {
                return dao.findByPersona(idPersonaMaestro, first, pageSize);
            }
        };
    }

    @Override
    protected MedioContacto buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(MedioContacto r) {
        return r != null && r.getIdMedioContacto() != null ? r.getIdMedioContacto().toString() : null;
    }

    @Override
    protected MedioContacto getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<MedioContacto> r) {
        super.selectionHandler(r);
        sincronizarSeleccion();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        idPersonaSeleccionada = idPersonaMaestro == null ? null : idPersonaMaestro.toString();
        idTipoMedioContactoSeleccionado = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        limpiarSeleccion();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        guardar(false);
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        guardar(true);
    }

    private void guardar(boolean modificar) {
        try {
            prepararRelaciones();
            validarUnicidad(registro, modificar);
            if (modificar) {
                dao.modificar(registro);
                limpiar("Registro modificado");
            } else {
                dao.crear(registro);
                limpiar("Registro guardado");
            }
        } catch (Exception e) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, modificar ? "Error al modificar" : "Error al guardar", e.getMessage()));
        }
    }

    private void prepararRelaciones() {
        if (idPersonaMaestro != null) {
            idPersonaSeleccionada = idPersonaMaestro.toString();
        }
        if (idPersonaSeleccionada == null || idPersonaSeleccionada.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una persona");
        }
        if (idTipoMedioContactoSeleccionado == null || idTipoMedioContactoSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de medio de contacto");
        }
        registro.setIdPersona(personaDAO.find(UUID.fromString(idPersonaSeleccionada)));
        TipoMedioContacto tipo = tipoMedioContactoDAO.find(UUID.fromString(idTipoMedioContactoSeleccionado));
        if (tipo == null || !Boolean.TRUE.equals(tipo.getActivo())) {
            throw new IllegalArgumentException("Solo se pueden asignar tipos de medio de contacto activos");
        }
        sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ValidacionFormato.validar(
                registro.getValor(), tipo.getExpresionRegular(), tipo.getIndicaciones());
        registro.setValor(registro.getValor() == null ? null : registro.getValor().trim());
        registro.setIdTipoMedioContacto(tipo);
    }

    private void sincronizarSeleccion() {
        idPersonaSeleccionada = registro != null && registro.getIdPersona() != null && registro.getIdPersona().getIdPersona() != null ? registro.getIdPersona().getIdPersona().toString() : null;
        idTipoMedioContactoSeleccionado = registro != null && registro.getIdTipoMedioContacto() != null && registro.getIdTipoMedioContacto().getIdTipoMedioContacto() != null ? registro.getIdTipoMedioContacto().getIdTipoMedioContacto().toString() : null;
    }

    private void limpiar(String mensaje) {
        registro = null;
        estado = ESTADO_CRUD.NADA;
        limpiarSeleccion();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", mensaje));
    }

    private void limpiarSeleccion() {
        idPersonaSeleccionada = idPersonaMaestro == null ? null : idPersonaMaestro.toString();
        idTipoMedioContactoSeleccionado = null;
    }

    public void establecerPersonaMaestro(UUID idPersona) {
        if (java.util.Objects.equals(this.idPersonaMaestro, idPersona)) {
            return;
        }
        this.idPersonaMaestro = idPersona;
        this.idPersonaSeleccionada = idPersona == null ? null : idPersona.toString();
        this.idTipoMedioContactoSeleccionado = null;
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        inicializarRegistros();
    }

    public boolean isIntegradoEnPersona() {
        return idPersonaMaestro != null;
    }

    public List<Persona> getPersonas() {
        return personaDAO.findAll();
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        return tipoMedioContactoDAO.findActivos();
    }

    public String getIdPersonaSeleccionada() {
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public String getIdTipoMedioContactoSeleccionado() {
        return idTipoMedioContactoSeleccionado;
    }

    public void setIdTipoMedioContactoSeleccionado(String idTipoMedioContactoSeleccionado) {
        this.idTipoMedioContactoSeleccionado = idTipoMedioContactoSeleccionado;
    }
}
