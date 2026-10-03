package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Procedimiento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

@Named("procedimientoModel")
@ViewScoped
public class ProcedimientoModel extends AbstractModel<Procedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;
    @Inject
    ProcedimientoDAO dao;

    @Inject
    sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject
    ProcedimientoPasoDAO pasoDAO;
    @Inject
    ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    @Inject
    ProcedimientoPasoExamenDAO pasoExamenDAO;
    @Inject
    RolDAO rolDAO;
    @Inject
    ExamenDAO examenDAO;

    // Propiedades Pestaña 1
    private ProcedimientoPaso nuevoPaso;
    private ProcedimientoPaso pasoSeleccionado;
    private String idRolSeleccionadoPaso;
    private boolean editandoPaso = false;

    // Propiedades Pestaña 2
    private ProcedimientoPasoSecuencia nuevaSecuencia;
    private ProcedimientoPasoSecuencia secuenciaSeleccionada;
    private String idPasoSeleccionadoSecuencia;
    private String idPasoAnteriorSeleccionadoSecuencia;
    private boolean editandoSecuencia = false;

    // Propiedades Pestaña 3
    private ProcedimientoPasoExamen nuevoPasoExamen;
    private ProcedimientoPasoExamen examenSeleccionado;
    private String idPasoSeleccionadoExamen;
    private String idExamenSeleccionado;
    private boolean editandoExamen = false;

    private void refrescarListasHijas() {
        if (this.registro == null || this.registro.getIdProcedimiento() == null) {
            return;
        }
        try {
            java.util.List<ProcedimientoPaso> pasos =
                    pasoDAO.findByProcedimiento(this.registro.getIdProcedimiento());
            this.registro.setProcedimientoPasoList(pasos);
            for (ProcedimientoPaso paso : pasos) {
                if (paso.getIdProcedimientoPaso() == null) {
                    continue;
                }
                paso.setProcedimientoPasoSecuenciaList(
                        secuenciaDAO.findByPaso(paso.getIdProcedimientoPaso()));
                paso.setProcedimientoPasoExamenList(
                        pasoExamenDAO.findByPaso(paso.getIdProcedimientoPaso()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void validarEliminacion(Procedimiento r) {
        if (r.getIdProcedimiento() == null) {
            return;
        }
        long pasos = pasoDAO.countByProcedimiento(r.getIdProcedimiento());
        long cons = consultaProcedimientoDAO.countByProcedimiento(r.getIdProcedimiento());
        if (pasos + cons > 0) {
            throw new IllegalArgumentException("No se puede eliminar el procedimiento porque tiene " + pasos + " paso(s) y " + cons + " consulta(s) asociadas");
        }
    }

    public ProcedimientoModel() {
        this.nombreBean = "Procedimiento";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected void validarUnicidad(Procedimiento r, boolean esModificacion) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return;
        }
        r.setNombre(r.getNombre().trim());
        java.util.UUID excluir = esModificacion ? r.getIdProcedimiento() : null;
        if (dao.existeNombre(r.getNombre(), excluir)) {
            throw new IllegalArgumentException("Ya existe un procedimiento con ese nombre");
        }
    }

    @Override
    protected DefaultDAO<Procedimiento> getDao() {
        return dao;
    }

    @Override
    protected Procedimiento nuevoRegistro() {
        Procedimiento r = new Procedimiento();
        r.setIdProcedimiento(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    protected Procedimiento buscarRegistroPorId(Object id) {
        return id instanceof UUID buscado ? dao.find(buscado) : null;
    }

    @Override
    protected String getIdAsText(Procedimiento r) {
        return r != null && r.getIdProcedimiento() != null ? r.getIdProcedimiento().toString() : null;
    }

    @Override
    protected Procedimiento getIdByText(String id) {
        try {
            return id != null ? dao.find(UUID.fromString(id)) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void selectionHandler(SelectEvent<Procedimiento> r) {
        super.selectionHandler(r);
        refrescarListasHijas();
        cancelarEdicionPaso();
        cancelarEdicionSecuencia();
        cancelarEdicionExamen();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                validarUnicidad(this.registro, false);
                dao.crear(this.registro);
                limpiar("Procedimiento guardado exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                validarUnicidad(this.registro, true);
                dao.modificar(this.registro);
                limpiar("Procedimiento modificado exitosamente");
            } catch (Exception e) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }

    private void limpiar(String mensaje) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        cancelarEdicionPaso();
        cancelarEdicionSecuencia();
        cancelarEdicionExamen();
        inicializarRegistros();
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    // ─── PESTAÑA 1: PASOS ───────────────────────────────────────────────────────

    public void prepararNuevoPaso() {
        nuevoPaso = new ProcedimientoPaso();
        nuevoPaso.setIdProcedimientoPaso(UUID.randomUUID());
        nuevoPaso.setIndicaFin(false);
        idRolSeleccionadoPaso = null;
    }

    public void onPasoSelect(SelectEvent<ProcedimientoPaso> event) {
        pasoSeleccionado = event.getObject();
        nuevoPaso = new ProcedimientoPaso();
        nuevoPaso.setIdProcedimientoPaso(pasoSeleccionado.getIdProcedimientoPaso());
        nuevoPaso.setNombre(pasoSeleccionado.getNombre());
        nuevoPaso.setIndicaFin(pasoSeleccionado.getIndicaFin());
        nuevoPaso.setIdProcedimiento(pasoSeleccionado.getIdProcedimiento());
        if (pasoSeleccionado.getIdRol() != null) {
            idRolSeleccionadoPaso = pasoSeleccionado.getIdRol().getIdRol().toString();
        } else {
            idRolSeleccionadoPaso = null;
        }
        editandoPaso = true;
    }

    public void cancelarEdicionPaso() {
        pasoSeleccionado = null;
        editandoPaso = false;
        prepararNuevoPaso();
    }

    public void modificarPaso() {
        if (this.registro == null) return;
        try {
            if (idRolSeleccionadoPaso != null && !idRolSeleccionadoPaso.isBlank()) {
                Rol rol = rolDAO.find(UUID.fromString(idRolSeleccionadoPaso));
                nuevoPaso.setIdRol(rol);
            } else {
                nuevoPaso.setIdRol(null);
            }
            pasoDAO.modificar(nuevoPaso);

            if (this.registro.getProcedimientoPasoList() != null) {
                int index = -1;
                for (int i = 0; i < this.registro.getProcedimientoPasoList().size(); i++) {
                    if (this.registro.getProcedimientoPasoList().get(i).getIdProcedimientoPaso().equals(nuevoPaso.getIdProcedimientoPaso())) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    this.registro.getProcedimientoPasoList().set(index, nuevoPaso);
                }
            }

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paso modificado"));
            cancelarEdicionPaso();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
        }
    }

    public void agregarPaso() {
        if (this.registro == null) return;
        try {
            if (idRolSeleccionadoPaso != null && !idRolSeleccionadoPaso.isBlank()) {
                Rol rol = rolDAO.find(UUID.fromString(idRolSeleccionadoPaso));
                nuevoPaso.setIdRol(rol);
            }
            nuevoPaso.setIdProcedimiento(this.registro);
            pasoDAO.crear(nuevoPaso);

            if (this.registro.getProcedimientoPasoList() == null) {
                this.registro.setProcedimientoPasoList(new ArrayList<>());
            }
            this.registro.getProcedimientoPasoList().add(nuevoPaso);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paso agregado"));
            prepararNuevoPaso();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", e.getMessage()));
        }
    }

    public void eliminarPaso(ProcedimientoPaso p) {
        if (p == null) return;
        try {
            pasoDAO.eliminar(p);
            if (this.registro.getProcedimientoPasoList() != null) {
                this.registro.getProcedimientoPasoList().remove(p);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paso eliminado"));
            cancelarEdicionPaso();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }

    public List<ProcedimientoPaso> getPasosDelProcedimiento() {
        return this.registro != null && this.registro.getProcedimientoPasoList() != null ? this.registro.getProcedimientoPasoList() : new ArrayList<>();
    }

    // ─── PESTAÑA 2: SECUENCIA ───────────────────────────────────────────────────

    public void prepararNuevaSecuencia() {
        nuevaSecuencia = new ProcedimientoPasoSecuencia();
        nuevaSecuencia.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
        nuevaSecuencia.setTipoSecuencia("DESPUES_DE");
        idPasoSeleccionadoSecuencia = null;
        idPasoAnteriorSeleccionadoSecuencia = null;
    }

    public void onSecuenciaSelect(SelectEvent<ProcedimientoPasoSecuencia> event) {
        secuenciaSeleccionada = event.getObject();
        nuevaSecuencia = new ProcedimientoPasoSecuencia();
        nuevaSecuencia.setIdProcedimientoPasoSecuencia(secuenciaSeleccionada.getIdProcedimientoPasoSecuencia());
        nuevaSecuencia.setTipoSecuencia(secuenciaSeleccionada.getTipoSecuencia());
        
        if (secuenciaSeleccionada.getIdProcedimientoPaso() != null) {
            idPasoSeleccionadoSecuencia = secuenciaSeleccionada.getIdProcedimientoPaso().getIdProcedimientoPaso().toString();
        }
        if (secuenciaSeleccionada.getIdProcedimientoPasoReferencia() != null) {
            idPasoAnteriorSeleccionadoSecuencia = secuenciaSeleccionada.getIdProcedimientoPasoReferencia().toString();
        } else {
            idPasoAnteriorSeleccionadoSecuencia = null;
        }
        editandoSecuencia = true;
    }

    public void cancelarEdicionSecuencia() {
        secuenciaSeleccionada = null;
        editandoSecuencia = false;
        prepararNuevaSecuencia();
    }

    public void modificarSecuencia() {
        try {
            if (idPasoSeleccionadoSecuencia == null || idPasoSeleccionadoSecuencia.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un paso principal");
            }
            ProcedimientoPaso pasoBase = buscarPasoEnMemoria(idPasoSeleccionadoSecuencia);
            if (pasoBase == null) throw new IllegalArgumentException("Paso no encontrado");

            nuevaSecuencia.setIdProcedimientoPaso(pasoBase);

            if (idPasoAnteriorSeleccionadoSecuencia != null && !idPasoAnteriorSeleccionadoSecuencia.isBlank()) {
                nuevaSecuencia.setIdProcedimientoPasoReferencia(UUID.fromString(idPasoAnteriorSeleccionadoSecuencia));
            } else {
                nuevaSecuencia.setIdProcedimientoPasoReferencia(null);
            }

            secuenciaDAO.modificar(nuevaSecuencia);

            if (secuenciaSeleccionada != null && secuenciaSeleccionada.getIdProcedimientoPaso() != null) {
                secuenciaSeleccionada.getIdProcedimientoPaso().getProcedimientoPasoSecuenciaList().remove(secuenciaSeleccionada);
            }
            if (pasoBase.getProcedimientoPasoSecuenciaList() == null) {
                pasoBase.setProcedimientoPasoSecuenciaList(new ArrayList<>());
            }
            pasoBase.getProcedimientoPasoSecuenciaList().add(nuevaSecuencia);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Secuencia modificada"));
            cancelarEdicionSecuencia();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
        }
    }

    public void agregarSecuencia() {
        if (this.registro == null) return;
        try {
            if (idPasoSeleccionadoSecuencia == null || idPasoSeleccionadoSecuencia.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un paso principal");
            }
            ProcedimientoPaso pasoBase = buscarPasoEnMemoria(idPasoSeleccionadoSecuencia);
            if (pasoBase == null) throw new IllegalArgumentException("Paso no encontrado");

            nuevaSecuencia.setIdProcedimientoPaso(pasoBase);

            if (idPasoAnteriorSeleccionadoSecuencia != null && !idPasoAnteriorSeleccionadoSecuencia.isBlank()) {
                nuevaSecuencia.setIdProcedimientoPasoReferencia(UUID.fromString(idPasoAnteriorSeleccionadoSecuencia));
            }

            secuenciaDAO.crear(nuevaSecuencia);

            if (pasoBase.getProcedimientoPasoSecuenciaList() == null) {
                pasoBase.setProcedimientoPasoSecuenciaList(new ArrayList<>());
            }
            pasoBase.getProcedimientoPasoSecuenciaList().add(nuevaSecuencia);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Secuencia agregada"));
            prepararNuevaSecuencia();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", e.getMessage()));
        }
    }

    public void eliminarSecuencia(ProcedimientoPasoSecuencia pps) {
        if (pps == null) return;
        try {
            secuenciaDAO.eliminar(pps);
            if (pps.getIdProcedimientoPaso() != null && pps.getIdProcedimientoPaso().getProcedimientoPasoSecuenciaList() != null) {
                pps.getIdProcedimientoPaso().getProcedimientoPasoSecuenciaList().remove(pps);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Secuencia eliminada"));
            cancelarEdicionSecuencia();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }

    public List<ProcedimientoPasoSecuencia> getSecuenciasDelProcedimiento() {
        List<ProcedimientoPasoSecuencia> sec = new ArrayList<>();
        if (this.registro != null && this.registro.getProcedimientoPasoList() != null) {
            for (ProcedimientoPaso p : this.registro.getProcedimientoPasoList()) {
                if (p.getProcedimientoPasoSecuenciaList() != null) {
                    sec.addAll(p.getProcedimientoPasoSecuenciaList());
                }
            }
        }
        return sec;
    }

    public String nombrePaso(UUID idPaso) {
        if (idPaso == null) return "Inicio / Ninguno";
        ProcedimientoPaso p = buscarPasoEnMemoria(idPaso.toString());
        if (p == null) p = pasoDAO.find(idPaso);
        return p != null ? p.getNombre() : "Desconocido";
    }

    // ─── PESTAÑA 3: EXAMENES ────────────────────────────────────────────────────

    public void prepararNuevoPasoExamen() {
        nuevoPasoExamen = new ProcedimientoPasoExamen();
        nuevoPasoExamen.setIdProcedimientoPasoExamen(UUID.randomUUID());
        nuevoPasoExamen.setFechaCreacion(OffsetDateTime.now());
        nuevoPasoExamen.setActivo(true);
        idPasoSeleccionadoExamen = null;
        idExamenSeleccionado = null;
    }

    public void onExamenSelect(SelectEvent<ProcedimientoPasoExamen> event) {
        examenSeleccionado = event.getObject();
        nuevoPasoExamen = new ProcedimientoPasoExamen();
        nuevoPasoExamen.setIdProcedimientoPasoExamen(examenSeleccionado.getIdProcedimientoPasoExamen());
        nuevoPasoExamen.setFechaCreacion(examenSeleccionado.getFechaCreacion());
        nuevoPasoExamen.setActivo(examenSeleccionado.getActivo());
        nuevoPasoExamen.setObservaciones(examenSeleccionado.getObservaciones());

        if (examenSeleccionado.getIdProcedimientoPaso() != null) {
            idPasoSeleccionadoExamen = examenSeleccionado.getIdProcedimientoPaso().getIdProcedimientoPaso().toString();
        }
        if (examenSeleccionado.getIdExamen() != null) {
            idExamenSeleccionado = examenSeleccionado.getIdExamen().getIdExamen().toString();
        }
        editandoExamen = true;
    }

    public void cancelarEdicionExamen() {
        examenSeleccionado = null;
        editandoExamen = false;
        prepararNuevoPasoExamen();
    }

    public void modificarPasoExamen() {
        try {
            if (idPasoSeleccionadoExamen == null || idPasoSeleccionadoExamen.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un paso principal");
            }
            if (idExamenSeleccionado == null || idExamenSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un examen");
            }

            ProcedimientoPaso pasoBase = buscarPasoEnMemoria(idPasoSeleccionadoExamen);
            if (pasoBase == null) throw new IllegalArgumentException("Paso no encontrado");

            Examen ex = examenDAO.find(UUID.fromString(idExamenSeleccionado));

            nuevoPasoExamen.setIdProcedimientoPaso(pasoBase);
            nuevoPasoExamen.setIdExamen(ex);

            pasoExamenDAO.modificar(nuevoPasoExamen);

            if (examenSeleccionado != null && examenSeleccionado.getIdProcedimientoPaso() != null) {
                examenSeleccionado.getIdProcedimientoPaso().getProcedimientoPasoExamenList().remove(examenSeleccionado);
            }
            if (pasoBase.getProcedimientoPasoExamenList() == null) {
                pasoBase.setProcedimientoPasoExamenList(new ArrayList<>());
            }
            pasoBase.getProcedimientoPasoExamenList().add(nuevoPasoExamen);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Examen de paso modificado"));
            cancelarEdicionExamen();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
        }
    }

    public void agregarPasoExamen() {
        if (this.registro == null) return;
        try {
            if (idPasoSeleccionadoExamen == null || idPasoSeleccionadoExamen.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un paso principal");
            }
            if (idExamenSeleccionado == null || idExamenSeleccionado.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un examen");
            }

            ProcedimientoPaso pasoBase = buscarPasoEnMemoria(idPasoSeleccionadoExamen);
            if (pasoBase == null) throw new IllegalArgumentException("Paso no encontrado");

            Examen ex = examenDAO.find(UUID.fromString(idExamenSeleccionado));

            nuevoPasoExamen.setIdProcedimientoPaso(pasoBase);
            nuevoPasoExamen.setIdExamen(ex);

            pasoExamenDAO.crear(nuevoPasoExamen);

            if (pasoBase.getProcedimientoPasoExamenList() == null) {
                pasoBase.setProcedimientoPasoExamenList(new ArrayList<>());
            }
            pasoBase.getProcedimientoPasoExamenList().add(nuevoPasoExamen);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Examen agregado al paso"));
            prepararNuevoPasoExamen();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al agregar", e.getMessage()));
        }
    }

    public void eliminarPasoExamen(ProcedimientoPasoExamen ppe) {
        if (ppe == null) return;
        try {
            pasoExamenDAO.eliminar(ppe);
            if (ppe.getIdProcedimientoPaso() != null && ppe.getIdProcedimientoPaso().getProcedimientoPasoExamenList() != null) {
                ppe.getIdProcedimientoPaso().getProcedimientoPasoExamenList().remove(ppe);
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Examen eliminado del paso"));
            cancelarEdicionExamen();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }

    public List<ProcedimientoPasoExamen> getExamenesDelProcedimiento() {
        List<ProcedimientoPasoExamen> examenes = new ArrayList<>();
        if (this.registro != null && this.registro.getProcedimientoPasoList() != null) {
            for (ProcedimientoPaso p : this.registro.getProcedimientoPasoList()) {
                if (p.getProcedimientoPasoExamenList() != null) {
                    examenes.addAll(p.getProcedimientoPasoExamenList());
                }
            }
        }
        return examenes;
    }

    // ─── UTILIDADES Y LISTAS ────────────────────────────────────────────────────

    private ProcedimientoPaso buscarPasoEnMemoria(String idPaso) {
        if (this.registro.getProcedimientoPasoList() != null) {
            for (ProcedimientoPaso p : this.registro.getProcedimientoPasoList()) {
                if (p.getIdProcedimientoPaso().toString().equals(idPaso)) {
                    return p;
                }
            }
        }
        return null;
    }

    public List<Rol> getRoles() {
        return rolDAO.findActivo();
    }

    public List<Examen> getExamenes() {
        return examenDAO.findActivo();
    }

    // Getters y Setters
    public ProcedimientoPaso getNuevoPaso() { return nuevoPaso; }
    public void setNuevoPaso(ProcedimientoPaso nuevoPaso) { this.nuevoPaso = nuevoPaso; }
    public ProcedimientoPaso getPasoSeleccionado() { return pasoSeleccionado; }
    public void setPasoSeleccionado(ProcedimientoPaso pasoSeleccionado) { this.pasoSeleccionado = pasoSeleccionado; }
    public String getIdRolSeleccionadoPaso() { return idRolSeleccionadoPaso; }
    public void setIdRolSeleccionadoPaso(String idRolSeleccionadoPaso) { this.idRolSeleccionadoPaso = idRolSeleccionadoPaso; }
    public boolean isEditandoPaso() { return editandoPaso; }
    public void setEditandoPaso(boolean editandoPaso) { this.editandoPaso = editandoPaso; }

    public ProcedimientoPasoSecuencia getNuevaSecuencia() { return nuevaSecuencia; }
    public void setNuevaSecuencia(ProcedimientoPasoSecuencia nuevaSecuencia) { this.nuevaSecuencia = nuevaSecuencia; }
    public ProcedimientoPasoSecuencia getSecuenciaSeleccionada() { return secuenciaSeleccionada; }
    public void setSecuenciaSeleccionada(ProcedimientoPasoSecuencia secuenciaSeleccionada) { this.secuenciaSeleccionada = secuenciaSeleccionada; }
    public String getIdPasoSeleccionadoSecuencia() { return idPasoSeleccionadoSecuencia; }
    public void setIdPasoSeleccionadoSecuencia(String idPasoSeleccionadoSecuencia) { this.idPasoSeleccionadoSecuencia = idPasoSeleccionadoSecuencia; }
    public String getIdPasoAnteriorSeleccionadoSecuencia() { return idPasoAnteriorSeleccionadoSecuencia; }
    public void setIdPasoAnteriorSeleccionadoSecuencia(String idPasoAnteriorSeleccionadoSecuencia) { this.idPasoAnteriorSeleccionadoSecuencia = idPasoAnteriorSeleccionadoSecuencia; }
    public boolean isEditandoSecuencia() { return editandoSecuencia; }
    public void setEditandoSecuencia(boolean editandoSecuencia) { this.editandoSecuencia = editandoSecuencia; }

    public ProcedimientoPasoExamen getNuevoPasoExamen() { return nuevoPasoExamen; }
    public void setNuevoPasoExamen(ProcedimientoPasoExamen nuevoPasoExamen) { this.nuevoPasoExamen = nuevoPasoExamen; }
    public ProcedimientoPasoExamen getExamenSeleccionado() { return examenSeleccionado; }
    public void setExamenSeleccionado(ProcedimientoPasoExamen examenSeleccionado) { this.examenSeleccionado = examenSeleccionado; }
    public String getIdPasoSeleccionadoExamen() { return idPasoSeleccionadoExamen; }
    public void setIdPasoSeleccionadoExamen(String idPasoSeleccionadoExamen) { this.idPasoSeleccionadoExamen = idPasoSeleccionadoExamen; }
    public String getIdExamenSeleccionado() { return idExamenSeleccionado; }
    public void setIdExamenSeleccionado(String idExamenSeleccionado) { this.idExamenSeleccionado = idExamenSeleccionado; }
    public boolean isEditandoExamen() { return editandoExamen; }
    public void setEditandoExamen(boolean editandoExamen) { this.editandoExamen = editandoExamen; }
}
