package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;


@Named("examenModel")
@ViewScoped
public class ExamenModel extends AbstractModel<Examen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    ExamenDAO dao;

    @Inject
    ExamenTipoExamenDAO examenTipoExamenDAO;

    @Inject
    ProcedimientoPasoExamenDAO pasoExamenDAO;

    @Inject
    ExamenTipoExamenModel examenTipoExamenModel;

    @Override
    protected void validarEliminacion(Examen r) {
        if (r.getIdExamen() == null) {
            return;
        }
        long vinc = examenTipoExamenDAO.countByIdExamen(r.getIdExamen());
        long pasos = pasoExamenDAO.countByExamen(r.getIdExamen());
        if (vinc + pasos > 0) {
            throw new IllegalArgumentException("No se puede eliminar el examen porque tiene " + vinc + " tipo(s) vinculado(s) y " + pasos + " paso(s) que lo requieren");
        }
    }

    public ExamenModel() {
        this.nombreBean = "Examen";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected void validarUnicidad(Examen r, boolean esModificacion) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return;
        }
        r.setNombre(r.getNombre().trim());
        java.util.UUID excluir = esModificacion ? r.getIdExamen() : null;
        if (dao.existeNombre(r.getNombre(), excluir)) {
            throw new IllegalArgumentException("Ya existe un examen con ese nombre");
        }
    }

    @Override
    protected DefaultDAO<Examen> getDao() {
        return dao;
    }

    @Override
    protected Examen nuevoRegistro() {
        Examen r = new Examen();
        r.setIdExamen(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    public void selectionHandler(SelectEvent<Examen> r) {
        super.selectionHandler(r);
        sincronizarDetalle();
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        sincronizarDetalle();
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        sincronizarDetalle();
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        super.btnGuardarHandler(actionEvent);
        sincronizarDetalle();
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        super.btnModificarHandler(actionEvent);
        sincronizarDetalle();
    }

    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        super.btnEliminarHandler(actionEvent);
        sincronizarDetalle();
    }

    @Override
    protected Examen buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            for (Examen e : dao.findAll()) {
                if (e.getIdExamen() != null && e.getIdExamen().equals(buscado)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Examen r) {
        if (r != null && r.getIdExamen() != null) {
            return r.getIdExamen().toString();
        }
        return null;
    }

    @Override
    protected Examen getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                UUID buscado = UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(x -> x.getIdExamen() != null && x.getIdExamen().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private void sincronizarDetalle() {
        if (this.registro != null && this.registro.getIdExamen() != null) {
            examenTipoExamenModel.setIdExamen(this.registro.getIdExamen());
        } else {
            examenTipoExamenModel.setIdExamen(null);
        }
    }

    public ExamenTipoExamenModel getExamenTipoExamenModel() {
        sincronizarDetalle();
        return examenTipoExamenModel;
    }
}
