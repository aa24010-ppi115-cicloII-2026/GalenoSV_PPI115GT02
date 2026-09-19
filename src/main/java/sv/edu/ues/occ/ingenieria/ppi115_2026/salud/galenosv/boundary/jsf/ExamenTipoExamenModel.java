package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ExamenTipoExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

@Named("examenTipoExamenModel")
@Dependent
public class ExamenTipoExamenModel extends AbstractModel<ExamenTipoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    ExamenTipoExamenDAO dao;

    @Inject
    ExamenDAO examenDAO;

    @Inject
    TipoExamenDAO tipoExamenDAO;

    private String idExamenSeleccionado;
    private String idTipoExamenSeleccionado;
    private UUID idExamen;
    private TipoExamen tipoExamenSeleccionado;

    public ExamenTipoExamenModel() {
        this.nombreBean = "ExamenTipoExamen";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<ExamenTipoExamen> getDao() {
        return dao;
    }

    @Override
    public void inicializarRegistros() {
        this.modelo = new LazyDataModel<ExamenTipoExamen>() {

            @Override
            public String getRowKey(ExamenTipoExamen object) {
                return getIdAsText(object);
            }

            @Override
            public ExamenTipoExamen getRowData(String rowKey) {
                return getIdByText(rowKey);
            }

            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    if (idExamen != null) {
                        return dao.countByIdExamen(idExamen);
                    }
                    return dao.count().intValue();
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public List<ExamenTipoExamen> load(int first, int max, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    if (idExamen != null) {
                        return dao.findByIdExamen(idExamen, first, max);
                    }
                    return dao.findRange(first, max);
                } catch (Exception e) {
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ExamenTipoExamen nuevoRegistro() {
        ExamenTipoExamen r = new ExamenTipoExamen();
        r.setIdExamenTipoExamen(UUID.randomUUID());
        r.setFechaCreacion(OffsetDateTime.now());
        if (idExamen != null) {
            r.setIdExamen(examenDAO.find(idExamen));
            idExamenSeleccionado = idExamen.toString();
        }
        return r;
    }

    @Override
    protected ExamenTipoExamen buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado) {
            for (ExamenTipoExamen e : dao.findAll()) {
                if (e.getIdExamenTipoExamen() != null && e.getIdExamenTipoExamen().equals(buscado)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(ExamenTipoExamen r) {
        if (r != null && r.getIdExamenTipoExamen() != null) {
            return r.getIdExamenTipoExamen().toString();
        }
        return null;
    }

    @Override
    protected ExamenTipoExamen getIdByText(String id) {
        if (id != null) {
            try {
                return buscarRegistroPorId(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void selectionHandler(SelectEvent<ExamenTipoExamen> r) {
        super.selectionHandler(r);
        sincronizarSeleccion();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        if (idExamen != null) {
            this.idExamenSeleccionado = idExamen.toString();
        } else {
            this.idExamenSeleccionado = null;
        }
        this.idTipoExamenSeleccionado = null;
        this.tipoExamenSeleccionado = null;
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        this.idExamenSeleccionado = null;
        this.idTipoExamenSeleccionado = null;
        this.tipoExamenSeleccionado = null;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                prepararRelaciones();
                dao.crear(this.registro);
                limpiarDespuesDeGuardar();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", "Registro guardado"));
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
                prepararRelaciones();
                dao.modificar(this.registro);
                limpiarDespuesDeGuardar();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", "Registro modificado"));
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    private void prepararRelaciones() {
        if (idExamenSeleccionado == null || idExamenSeleccionado.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un examen");
        }
        if (tipoExamenSeleccionado == null && (idTipoExamenSeleccionado == null || idTipoExamenSeleccionado.isBlank())) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de examen");
        }

        Examen examen = examenDAO.find(UUID.fromString(idExamenSeleccionado));
        TipoExamen tipoExamen = tipoExamenSeleccionado;
        if (tipoExamen == null) {
            tipoExamen = tipoExamenDAO.find(UUID.fromString(idTipoExamenSeleccionado));
        }
        registro.setIdExamen(examen);
        registro.setIdTipoExamen(tipoExamen);
    }

    private void sincronizarSeleccion() {
        if (this.registro != null && this.registro.getIdExamen() != null) {
            this.idExamenSeleccionado = this.registro.getIdExamen().getIdExamen().toString();
        }
        if (this.registro != null && this.registro.getIdTipoExamen() != null) {
            this.tipoExamenSeleccionado = this.registro.getIdTipoExamen();
            this.idTipoExamenSeleccionado = this.registro.getIdTipoExamen().getIdTipoExamen().toString();
        }
    }

    private void limpiarDespuesDeGuardar() {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        this.idExamenSeleccionado = null;
        this.idTipoExamenSeleccionado = null;
        this.tipoExamenSeleccionado = null;
        inicializarRegistros();
    }

    public List<TipoExamen> buscarTipoPorNombre(String filtro) {
        try {
            return tipoExamenDAO.findByNombreLike(filtro, 0, 30);
        } catch (Exception e) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "Ingrese al menos tres caracteres"));
            return Collections.emptyList();
        }
    }

    public List<Examen> getExamenes() {
        return examenDAO.findAll();
    }

    public List<TipoExamen> getTiposExamen() {
        return tipoExamenDAO.findAll();
    }

    public String getIdExamenSeleccionado() {
        return idExamenSeleccionado;
    }

    public void setIdExamenSeleccionado(String idExamenSeleccionado) {
        this.idExamenSeleccionado = idExamenSeleccionado;
    }

    public String getIdTipoExamenSeleccionado() {
        return idTipoExamenSeleccionado;
    }

    public void setIdTipoExamenSeleccionado(String idTipoExamenSeleccionado) {
        this.idTipoExamenSeleccionado = idTipoExamenSeleccionado;
    }

    public UUID getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(UUID idExamen) {
        if ((this.idExamen == null && idExamen == null)
                || (this.idExamen != null && this.idExamen.equals(idExamen))) {
            return;
        }
        this.idExamen = idExamen;
        if (idExamen != null) {
            this.idExamenSeleccionado = idExamen.toString();
        } else {
            this.idExamenSeleccionado = null;
        }
        inicializarRegistros();
    }

    public TipoExamen getTipoExamenSeleccionado() {
        return tipoExamenSeleccionado;
    }

    public String getNombreTipoExamenSeleccionado() {
        if (tipoExamenSeleccionado != null && tipoExamenSeleccionado.getNombre() != null) {
            return tipoExamenSeleccionado.getNombre();
        }
        return "";
    }

    public void setTipoExamenSeleccionado(TipoExamen tipoExamenSeleccionado) {
        this.tipoExamenSeleccionado = tipoExamenSeleccionado;
        if (tipoExamenSeleccionado != null && tipoExamenSeleccionado.getIdTipoExamen() != null) {
            this.idTipoExamenSeleccionado = tipoExamenSeleccionado.getIdTipoExamen().toString();
        } else {
            this.idTipoExamenSeleccionado = null;
        }
    }
}
