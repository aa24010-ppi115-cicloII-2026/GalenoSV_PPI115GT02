package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected ESTADO_CRUD estado = ESTADO_CRUD.NADA;
    protected String nombreBean;
    protected LazyDataModel<T> modelo;
    protected T registro;
    protected int cantidadRegistros = 50;

    protected abstract FacesContext getFacesContext();

    protected abstract DefaultDAO<T> getDao();

    protected abstract T nuevoRegistro();

    protected abstract T buscarRegistroPorId(Object id);

    protected abstract String getIdAsText(T r);

    protected abstract T getIdByText(String id);

    @PostConstruct
    public void inicializar() {
        inicializarRegistros();
    }

    public void inicializarRegistros() {
        this.modelo = new LazyDataModel<T>() {

            @Override
            public String getRowKey(T object) {
                if (object != null) {
                    try {
                        return getIdByRegistro(object);
                    } catch (Exception e) {
                        Logger.getLogger(AbstractModel.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }

            @Override
            public T getRowData(String rowKey) {
                if (rowKey != null) {
                    try {
                        return getRegistroById(rowKey);
                    } catch (Exception e) {
                        Logger.getLogger(AbstractModel.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }

            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    Long total = getDao().count();
                    return total.intValue();
                } catch (Exception e) {
                    Logger.getLogger(AbstractModel.class.getName()).log(Level.SEVERE, null, e);
                }
                return 0;
            }

            @Override
            public List<T> load(int first, int max, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    return getDao().findRange(first, max);
                } catch (Exception e) {
                    Logger.getLogger(AbstractModel.class.getName()).log(Level.SEVERE, null, e);
                }
                return Collections.emptyList();
            }
        };
    }

    public String getIdByRegistro(T dato) {
        String id = getIdAsText(dato);
        if (id != null) {
            return id;
        }
        return null;
    }

    public T getRegistroById(String rowKey) {
        return getIdByText(rowKey);
    }

    public void selectionHandler(SelectEvent<T> r) {
        if (r != null && r.getObject() != null) {
            this.registro = r.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    public void btnNuevoHandler(ActionEvent e) {
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
    }

    public void btnCancelarHandler(ActionEvent e) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
    }

    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "El nombre no puede estar vacio"));
                    return;
                }

                validarUnicidad(this.registro, false);

                getDao().crear(this.registro);
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", "Registro guardado"));
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        }
    }

    public void btnEliminarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                validarEliminacion(this.registro);
                getDao().eliminar(this.registro);
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", "Registro eliminado"));
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
            }
        }
    }

    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atencion", "El nombre no puede estar vacio"));
                    return;
                }

                validarUnicidad(this.registro, true);

                getDao().modificar(this.registro);
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito", "Registro modificado"));
            } catch (Exception e) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    /**
     * Verifica que el registro no duplique a otro existente.
     * Por defecto no hace nada; cada modelo la sobrescribe según sus reglas.
     * Debe lanzar IllegalArgumentException si hay duplicado.
     */
    protected void validarUnicidad(T registro, boolean esModificacion) {
    }

    /**
     * Verifica que el registro se puede eliminar (sin hijos que lo referencien).
     * Por defecto no hace nada; cada modelo la sobrescribe para contar hijos
     * y lanzar IllegalArgumentException con mensaje claro. Como red de
     * seguridad, DefaultDAO.eliminar traduce la violacion de FK a mensaje amable.
     */
    protected void validarEliminacion(T registro) {
    }

    protected boolean esNombreVacio(T registro) {        if (registro == null) {
            return true;
        }
        try {
            java.lang.reflect.Method m = registro.getClass().getMethod("getNombre");
            String nombre = (String) m.invoke(registro);
            return nombre == null || nombre.trim().isEmpty();
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException("No se pudo validar el nombre del registro", e);
        }
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD e) {
        this.estado = e;
    }

    public String getNombreBean() {
        return nombreBean;
    }

    public void setNombreBean(String n) {
        this.nombreBean = n;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T r) {
        this.registro = r;
    }

    public LazyDataModel<T> getModelo() {
        return modelo;
    }

    public void setModelo(LazyDataModel<T> m) {
        this.modelo = m;
    }

    public int getCantidadRegistros() {
        return cantidadRegistros;
    }

    public void setCantidadRegistros(int cantidadRegistros) {
        this.cantidadRegistros = cantidadRegistros;
    }
}
