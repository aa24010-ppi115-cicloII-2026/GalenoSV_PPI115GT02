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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
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
    private boolean capturandoPaso = false;
    private List<String> idsExamenesPaso = new ArrayList<>();
    private List<Examen> examenesTemporalesPaso = new ArrayList<>();
    private Examen examenPasoSeleccionadoTemporal;
    private transient TreeNode<ProcedimientoPaso> arbolPasos;
    private TreeNode<ProcedimientoPaso> pasoNodoSeleccionado;

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
        arbolPasos = null;
        pasoNodoSeleccionado = null;
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
                this.estado = ESTADO_CRUD.MODIFICAR;
                refrescarListasHijas();
                cancelarEdicionPaso();
                cancelarEdicionSecuencia();
                cancelarEdicionExamen();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Procedimiento guardado", "Ahora puede configurar los pasos del procedimiento"));
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
        idPasoAnteriorSeleccionadoSecuencia = null;
        idsExamenesPaso = new ArrayList<>();
        examenesTemporalesPaso = new ArrayList<>();
        examenPasoSeleccionadoTemporal = null;
    }

    public void prepararCapturaPaso() {
        if (!getPasosDelProcedimiento().isEmpty()) {
            capturandoPaso = false;
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Paso inicial existente",
                    "El procedimiento ya tiene un paso inicial. Seleccione un paso del árbol para agregar uno dependiente"));
            return;
        }
        cancelarEdicionPaso();
        capturandoPaso = true;
    }

    public boolean isPuedeCrearPasoInicial() {
        return registro != null && getPasosDelProcedimiento().isEmpty();
    }

    public void prepararPasoDependiente(ProcedimientoPaso pasoPadre) {
        if (pasoPadre == null || pasoPadre.getIdProcedimientoPaso() == null) {
            return;
        }
        cancelarEdicionPaso();
        idPasoAnteriorSeleccionadoSecuencia = pasoPadre.getIdProcedimientoPaso().toString();
        capturandoPaso = true;
    }

    public void prepararPasoDependienteSeleccionado() {
        if (pasoNodoSeleccionado == null || pasoNodoSeleccionado.getData() == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Seleccione un paso", "Seleccione en el árbol el paso del que dependerá el nuevo paso"));
            return;
        }
        prepararPasoDependiente(pasoNodoSeleccionado.getData());
    }

    @SuppressWarnings("unchecked")
    public void onPasoNodoSelect(NodeSelectEvent event) {
        pasoNodoSeleccionado = (TreeNode<ProcedimientoPaso>) event.getTreeNode();
    }

    public void cancelarCapturaPaso() {
        capturandoPaso = false;
        cancelarEdicionPaso();
    }

    public void seleccionarRolPaso(Rol rol) {
        idRolSeleccionadoPaso = rol == null || rol.getIdRol() == null
                ? null : rol.getIdRol().toString();
    }

    public void agregarExamenPaso(Examen examen) {
        if (examen == null || examen.getIdExamen() == null || !Boolean.TRUE.equals(examen.getActivo())) {
            return;
        }
        String id = examen.getIdExamen().toString();
        boolean existe = examenesTemporalesPaso.stream()
                .anyMatch(actual -> actual.getIdExamen().equals(examen.getIdExamen()));
        if (!existe) {
            examenesTemporalesPaso.add(examen);
            idsExamenesPaso.add(id);
        }
    }

    public void eliminarExamenSeleccionadoPaso() {
        if (examenPasoSeleccionadoTemporal != null && examenPasoSeleccionadoTemporal.getIdExamen() != null) {
            idsExamenesPaso.remove(examenPasoSeleccionadoTemporal.getIdExamen().toString());
            examenesTemporalesPaso.removeIf(examen -> examen.getIdExamen()
                    .equals(examenPasoSeleccionadoTemporal.getIdExamen()));
            examenPasoSeleccionadoTemporal = null;
        }
    }

    public List<Examen> getExamenesSeleccionadosPaso() {
        return examenesTemporalesPaso;
    }

    public void prepararEdicionPasoCompleto(ProcedimientoPaso paso) {
        if (paso == null) {
            return;
        }
        pasoSeleccionado = paso;
        nuevoPaso = new ProcedimientoPaso();
        nuevoPaso.setIdProcedimientoPaso(paso.getIdProcedimientoPaso());
        nuevoPaso.setIdProcedimiento(paso.getIdProcedimiento());
        nuevoPaso.setNombre(paso.getNombre());
        nuevoPaso.setIndicaFin(paso.getIndicaFin());
        nuevoPaso.setIdRol(paso.getIdRol());
        idRolSeleccionadoPaso = paso.getIdRol() == null ? null : paso.getIdRol().getIdRol().toString();

        List<ProcedimientoPasoSecuencia> secuencias = paso.getProcedimientoPasoSecuenciaList();
        if (secuencias == null || secuencias.isEmpty()) {
            secuencias = secuenciaDAO.findByPaso(paso.getIdProcedimientoPaso());
        }
        nuevoPaso.setProcedimientoPasoSecuenciaList(new ArrayList<>(secuencias));
        idPasoAnteriorSeleccionadoSecuencia = secuencias.isEmpty()
                ? null : secuencias.get(0).getIdProcedimientoPasoReferencia().toString();

        List<ProcedimientoPasoExamen> relaciones = paso.getProcedimientoPasoExamenList();
        if (relaciones == null) {
            relaciones = pasoExamenDAO.findByPaso(paso.getIdProcedimientoPaso());
        }
        nuevoPaso.setProcedimientoPasoExamenList(new ArrayList<>(relaciones));
        idsExamenesPaso = relaciones.stream()
                .filter(rel -> rel.getIdExamen() != null && Boolean.TRUE.equals(rel.getActivo()))
                .map(rel -> rel.getIdExamen().getIdExamen().toString())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        examenesTemporalesPaso = relaciones.stream()
                .filter(rel -> rel.getIdExamen() != null && Boolean.TRUE.equals(rel.getActivo()))
                .map(ProcedimientoPasoExamen::getIdExamen)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        editandoPaso = true;
        capturandoPaso = true;
    }

    public String getNombreRolPaso() {
        if (idRolSeleccionadoPaso == null || idRolSeleccionadoPaso.isBlank()) {
            return "";
        }
        Rol rol = rolDAO.find(UUID.fromString(idRolSeleccionadoPaso));
        return rol == null ? "" : rol.getNombre();
    }

    public void guardarPasoCompleto() {
        if (registro == null) {
            return;
        }
        try {
            idsExamenesPaso = examenesTemporalesPaso.stream()
                    .map(examen -> examen.getIdExamen().toString())
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            UUID idPasoGuardado = nuevoPaso.getIdProcedimientoPaso();
            boolean yaHayPasos = !getPasosDelProcedimiento().isEmpty();
            boolean teniaDependencia = editandoPaso && nuevoPaso.getProcedimientoPasoSecuenciaList() != null
                    && !nuevoPaso.getProcedimientoPasoSecuenciaList().isEmpty();
            if (nuevoPaso.getNombre() == null || nuevoPaso.getNombre().isBlank()) {
                throw new IllegalArgumentException("El nombre del paso es obligatorio");
            }
            if ((!editandoPaso && yaHayPasos || editandoPaso && teniaDependencia)
                    && (idPasoAnteriorSeleccionadoSecuencia == null
                    || idPasoAnteriorSeleccionadoSecuencia.isBlank())) {
                throw new IllegalArgumentException("Los pasos posteriores deben depender de un paso anterior");
            }
            if (!editandoPaso && yaHayPasos) {
                ProcedimientoPaso padre = buscarPasoEnMemoria(idPasoAnteriorSeleccionadoSecuencia);
                if (padre == null) {
                    throw new IllegalArgumentException(
                            "El procedimiento ya tiene un paso inicial; el nuevo paso debe depender de un paso existente");
                }
            }

            for (ProcedimientoPaso existente : getPasosDelProcedimiento()) {
                if (!existente.getIdProcedimientoPaso().equals(nuevoPaso.getIdProcedimientoPaso())
                        && existente.getNombre() != null
                        && existente.getNombre().trim().equalsIgnoreCase(nuevoPaso.getNombre().trim())) {
                    throw new IllegalArgumentException("Ya existe un paso con ese nombre en el procedimiento");
                }
            }

            nuevoPaso.setNombre(nuevoPaso.getNombre().trim());
            nuevoPaso.setIdRol(obtenerRolActivoSeleccionado());
            nuevoPaso.setIdProcedimiento(registro);
            if (editandoPaso) {
                actualizarPasoCompleto();
            } else {
                pasoDAO.crear(nuevoPaso);
                if (registro.getProcedimientoPasoList() == null) {
                    registro.setProcedimientoPasoList(new ArrayList<>());
                }
                registro.getProcedimientoPasoList().add(nuevoPaso);
            }

            if (!editandoPaso && yaHayPasos) {
                idPasoSeleccionadoSecuencia = nuevoPaso.getIdProcedimientoPaso().toString();
                validarReferenciaSecuencia(nuevoPaso);
                ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
                secuencia.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
                secuencia.setIdProcedimientoPaso(nuevoPaso);
                secuencia.setIdProcedimientoPasoReferencia(UUID.fromString(idPasoAnteriorSeleccionadoSecuencia));
                secuencia.setTipoSecuencia("DESPUES_DE");
                secuenciaDAO.crear(secuencia);
                nuevoPaso.setProcedimientoPasoSecuenciaList(new ArrayList<>(List.of(secuencia)));
            }

            if (!editandoPaso && idsExamenesPaso != null) {
                for (String id : idsExamenesPaso) {
                    Examen examen = examenDAO.find(UUID.fromString(id));
                    if (examen == null || !Boolean.TRUE.equals(examen.getActivo())) {
                        continue;
                    }
                    ProcedimientoPasoExamen relacion = new ProcedimientoPasoExamen();
                    relacion.setIdProcedimientoPasoExamen(UUID.randomUUID());
                    relacion.setIdProcedimientoPaso(nuevoPaso);
                    relacion.setIdExamen(examen);
                    relacion.setFechaCreacion(OffsetDateTime.now());
                    relacion.setActivo(true);
                    pasoExamenDAO.crear(relacion);
                    if (nuevoPaso.getProcedimientoPasoExamenList() == null) {
                        nuevoPaso.setProcedimientoPasoExamenList(new ArrayList<>());
                    }
                    nuevoPaso.getProcedimientoPasoExamenList().add(relacion);
                }
            }
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Paso guardado", editandoPaso ? "El paso fue actualizado"
                            : "El paso, su dependencia y sus exámenes fueron guardados"));
            capturandoPaso = false;
            editandoPaso = false;
            refrescarListasHijas();
            seleccionarPasoEnArbol(idPasoGuardado);
            prepararNuevoPaso();
        } catch (Exception ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo guardar el paso", ex.getMessage()));
        }
    }

    private void actualizarPasoCompleto() {
        if (pasoSeleccionado == null) {
            throw new IllegalArgumentException("No se seleccionó el paso a modificar");
        }
        if (idPasoAnteriorSeleccionadoSecuencia != null
                && nuevoPaso.getIdProcedimientoPaso().toString().equals(idPasoAnteriorSeleccionadoSecuencia)) {
            throw new IllegalArgumentException("Un paso no puede depender de sí mismo");
        }
        if (creariaCiclo(nuevoPaso.getIdProcedimientoPaso(), idPasoAnteriorSeleccionadoSecuencia)) {
            throw new IllegalArgumentException("La dependencia seleccionada produciría un ciclo");
        }

        pasoDAO.modificar(nuevoPaso);
        List<ProcedimientoPasoSecuencia> actuales = new ArrayList<>(
                nuevoPaso.getProcedimientoPasoSecuenciaList() == null
                        ? List.of() : nuevoPaso.getProcedimientoPasoSecuenciaList());
        if (idPasoAnteriorSeleccionadoSecuencia == null || idPasoAnteriorSeleccionadoSecuencia.isBlank()) {
            for (ProcedimientoPasoSecuencia secuencia : actuales) {
                secuenciaDAO.eliminar(secuencia);
            }
            nuevoPaso.setProcedimientoPasoSecuenciaList(new ArrayList<>());
        } else if (actuales.isEmpty()) {
            ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
            secuencia.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
            secuencia.setIdProcedimientoPaso(nuevoPaso);
            secuencia.setIdProcedimientoPasoReferencia(UUID.fromString(idPasoAnteriorSeleccionadoSecuencia));
            secuencia.setTipoSecuencia("DESPUES_DE");
            secuenciaDAO.crear(secuencia);
            nuevoPaso.setProcedimientoPasoSecuenciaList(new ArrayList<>(List.of(secuencia)));
        } else {
            ProcedimientoPasoSecuencia secuencia = actuales.get(0);
            secuencia.setIdProcedimientoPaso(nuevoPaso);
            secuencia.setIdProcedimientoPasoReferencia(UUID.fromString(idPasoAnteriorSeleccionadoSecuencia));
            secuenciaDAO.modificar(secuencia);
        }

        List<ProcedimientoPasoExamen> actualesExamen = new ArrayList<>(
                nuevoPaso.getProcedimientoPasoExamenList() == null
                        ? List.of() : nuevoPaso.getProcedimientoPasoExamenList());
        for (ProcedimientoPasoExamen relacion : actualesExamen) {
            String id = relacion.getIdExamen().getIdExamen().toString();
            if (!idsExamenesPaso.contains(id)) {
                pasoExamenDAO.eliminar(relacion);
            }
        }
        for (String id : idsExamenesPaso) {
            boolean existe = actualesExamen.stream().anyMatch(rel -> rel.getIdExamen() != null
                    && rel.getIdExamen().getIdExamen().toString().equals(id));
            if (!existe) {
                Examen examen = examenDAO.find(UUID.fromString(id));
                ProcedimientoPasoExamen relacion = new ProcedimientoPasoExamen();
                relacion.setIdProcedimientoPasoExamen(UUID.randomUUID());
                relacion.setIdProcedimientoPaso(nuevoPaso);
                relacion.setIdExamen(examen);
                relacion.setFechaCreacion(OffsetDateTime.now());
                relacion.setActivo(true);
                pasoExamenDAO.crear(relacion);
            }
        }
        int posicion = registro.getProcedimientoPasoList().indexOf(pasoSeleccionado);
        if (posicion >= 0) {
            registro.getProcedimientoPasoList().set(posicion, nuevoPaso);
        }
    }

    private boolean creariaCiclo(UUID idPaso, String idPadre) {
        if (idPadre == null || idPadre.isBlank()) {
            return false;
        }
        UUID actual = UUID.fromString(idPadre);
        Set<UUID> visitados = new HashSet<>();
        while (actual != null && visitados.add(actual)) {
            if (actual.equals(idPaso)) {
                return true;
            }
            ProcedimientoPaso paso = buscarPasoEnMemoria(actual.toString());
            UUID siguiente = null;
            if (paso != null) {
                List<ProcedimientoPasoSecuencia> secuencias = paso.getProcedimientoPasoSecuenciaList();
                if (secuencias == null || secuencias.isEmpty()) {
                    secuencias = secuenciaDAO.findByPaso(actual);
                }
                if (!secuencias.isEmpty()) {
                    siguiente = secuencias.get(0).getIdProcedimientoPasoReferencia();
                }
            }
            actual = siguiente;
        }
        return false;
    }

    public void eliminarPasoCompleto(ProcedimientoPaso paso) {
        if (paso == null) {
            return;
        }
        try {
            List<ProcedimientoPasoSecuencia> dependientes = secuenciaDAO.findDependientes(paso.getIdProcedimientoPaso());
            if (!dependientes.isEmpty()) {
                throw new IllegalArgumentException("No puede eliminarse porque otros pasos dependen de este paso");
            }
            for (ProcedimientoPasoExamen relacion : pasoExamenDAO.findByPaso(paso.getIdProcedimientoPaso())) {
                pasoExamenDAO.eliminar(relacion);
            }
            for (ProcedimientoPasoSecuencia secuencia : secuenciaDAO.findByPaso(paso.getIdProcedimientoPaso())) {
                secuenciaDAO.eliminar(secuencia);
            }
            pasoDAO.eliminar(paso);
            registro.getProcedimientoPasoList().remove(paso);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Paso eliminado", "El paso fue eliminado correctamente"));
            cancelarCapturaPaso();
            refrescarListasHijas();
        } catch (Exception ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo eliminar el paso", ex.getMessage()));
        }
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
            nuevoPaso.setIdRol(obtenerRolActivoSeleccionado());
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
            nuevoPaso.setIdRol(obtenerRolActivoSeleccionado());
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

            validarReferenciaSecuencia(pasoBase);

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

            validarReferenciaSecuencia(pasoBase);

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
            if (ex == null || !Boolean.TRUE.equals(ex.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden vincular exámenes activos");
            }

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
            if (ex == null || !Boolean.TRUE.equals(ex.getActivo())) {
                throw new IllegalArgumentException("Solo se pueden vincular exámenes activos");
            }

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

    private Rol obtenerRolActivoSeleccionado() {
        if (idRolSeleccionadoPaso == null || idRolSeleccionadoPaso.isBlank()) {
            throw new IllegalArgumentException("Cada paso debe tener un rol responsable");
        }
        Rol rol = rolDAO.find(UUID.fromString(idRolSeleccionadoPaso));
        if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
            throw new IllegalArgumentException("Solo se pueden asignar roles activos");
        }
        return rol;
    }

    private void validarReferenciaSecuencia(ProcedimientoPaso pasoBase) {
        if (idPasoAnteriorSeleccionadoSecuencia == null || idPasoAnteriorSeleccionadoSecuencia.isBlank()) {
            throw new IllegalArgumentException("Los pasos subsecuentes deben depender de un paso anterior");
        }
        if (pasoBase.getIdProcedimientoPaso().toString().equals(idPasoAnteriorSeleccionadoSecuencia)) {
            throw new IllegalArgumentException("Un paso no puede depender de sí mismo");
        }
        ProcedimientoPaso anterior = buscarPasoEnMemoria(idPasoAnteriorSeleccionadoSecuencia);
        if (anterior == null) {
            throw new IllegalArgumentException("El paso anterior no pertenece al procedimiento");
        }
    }

    public List<Rol> getRoles() {
        return rolDAO.findActivos();
    }

    public List<Examen> getExamenes() {
        return examenDAO.findActivos();
    }

    public TreeNode<ProcedimientoPaso> getArbolPasos() {
        if (arbolPasos != null) {
            return arbolPasos;
        }
        TreeNode<ProcedimientoPaso> raiz = new DefaultTreeNode<>(null, null);
        List<ProcedimientoPaso> pasos = getPasosDelProcedimiento();
        Map<UUID, ProcedimientoPaso> porId = new HashMap<>();
        Map<UUID, UUID> padrePorHijo = new HashMap<>();
        for (ProcedimientoPaso paso : pasos) {
            porId.put(paso.getIdProcedimientoPaso(), paso);
            if (paso.getProcedimientoPasoSecuenciaList() != null) {
                for (ProcedimientoPasoSecuencia secuencia : paso.getProcedimientoPasoSecuenciaList()) {
                    if (secuencia.getIdProcedimientoPasoReferencia() != null) {
                        padrePorHijo.put(paso.getIdProcedimientoPaso(), secuencia.getIdProcedimientoPasoReferencia());
                        break;
                    }
                }
            }
        }
        Set<UUID> agregados = new HashSet<>();
        for (ProcedimientoPaso paso : pasos) {
            if (!padrePorHijo.containsKey(paso.getIdProcedimientoPaso())
                    || !porId.containsKey(padrePorHijo.get(paso.getIdProcedimientoPaso()))) {
                agregarRama(raiz, paso, pasos, padrePorHijo, agregados);
            }
        }
        for (ProcedimientoPaso paso : pasos) {
            if (!agregados.contains(paso.getIdProcedimientoPaso())) {
                agregarRama(raiz, paso, pasos, padrePorHijo, agregados);
            }
        }
        arbolPasos = raiz;
        return arbolPasos;
    }

    private void agregarRama(TreeNode<ProcedimientoPaso> padre, ProcedimientoPaso paso,
            List<ProcedimientoPaso> pasos, Map<UUID, UUID> padrePorHijo, Set<UUID> agregados) {
        if (!agregados.add(paso.getIdProcedimientoPaso())) {
            return;
        }
        TreeNode<ProcedimientoPaso> nodo = new DefaultTreeNode<>(paso, padre);
        nodo.setExpanded(true);
        for (ProcedimientoPaso candidato : pasos) {
            if (paso.getIdProcedimientoPaso().equals(padrePorHijo.get(candidato.getIdProcedimientoPaso()))) {
                agregarRama(nodo, candidato, pasos, padrePorHijo, agregados);
            }
        }
    }

    private void seleccionarPasoEnArbol(UUID idPaso) {
        TreeNode<ProcedimientoPaso> nodo = buscarNodo(getArbolPasos(), idPaso);
        if (nodo != null) {
            nodo.setSelected(true);
            pasoNodoSeleccionado = nodo;
            TreeNode<ProcedimientoPaso> padre = nodo.getParent();
            while (padre != null) {
                padre.setExpanded(true);
                padre = padre.getParent();
            }
        }
    }

    private TreeNode<ProcedimientoPaso> buscarNodo(TreeNode<ProcedimientoPaso> nodo, UUID idPaso) {
        if (nodo == null || idPaso == null) {
            return null;
        }
        if (nodo.getData() != null && idPaso.equals(nodo.getData().getIdProcedimientoPaso())) {
            return nodo;
        }
        for (TreeNode<ProcedimientoPaso> hijo : nodo.getChildren()) {
            TreeNode<ProcedimientoPaso> encontrado = buscarNodo(hijo, idPaso);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
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
    public boolean isCapturandoPaso() { return capturandoPaso; }
    public void setCapturandoPaso(boolean capturandoPaso) { this.capturandoPaso = capturandoPaso; }
    public List<String> getIdsExamenesPaso() { return idsExamenesPaso; }
    public void setIdsExamenesPaso(List<String> idsExamenesPaso) { this.idsExamenesPaso = idsExamenesPaso; }
    public Examen getExamenPasoSeleccionadoTemporal() { return examenPasoSeleccionadoTemporal; }
    public void setExamenPasoSeleccionadoTemporal(Examen examenPasoSeleccionadoTemporal) { this.examenPasoSeleccionadoTemporal = examenPasoSeleccionadoTemporal; }
    public TreeNode<ProcedimientoPaso> getPasoNodoSeleccionado() { return pasoNodoSeleccionado; }
    public void setPasoNodoSeleccionado(TreeNode<ProcedimientoPaso> pasoNodoSeleccionado) { this.pasoNodoSeleccionado = pasoNodoSeleccionado; }

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
