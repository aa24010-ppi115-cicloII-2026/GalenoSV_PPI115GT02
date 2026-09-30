package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

@Named("tipoExamenModel")
@ViewScoped
public class TipoExamenModel extends AbstractModel<TipoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    TipoExamenDAO dao;

    @Inject
    ExamenTipoExamenDAO examenTipoExamenDAO;

    @Override
    protected void validarEliminacion(TipoExamen r) {
        long n = r.getIdTipoExamen() == null ? 0 : examenTipoExamenDAO.countByTipo(r.getIdTipoExamen());
        if (n > 0) {
            throw new IllegalArgumentException("No se puede eliminar el tipo de examen porque está vinculado a " + n + " examen(es)");
        }
    }

    public TipoExamenModel() {
        this.nombreBean = "TipoExamen";
    }
    
    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected void validarUnicidad(TipoExamen r, boolean esModificacion) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return;
        }
        r.setNombre(r.getNombre().trim());
        java.util.UUID excluir = esModificacion ? r.getIdTipoExamen() : null;
        if (dao.existeNombre(r.getNombre(), excluir)) {
            throw new IllegalArgumentException("Ya existe un tipo de examen con ese nombre");
        }
    }

    @Override
    protected DefaultDAO<TipoExamen> getDao() {
        return dao;
    }

    @Override
    protected TipoExamen nuevoRegistro() {
        TipoExamen r = new TipoExamen();
        r.setIdTipoExamen(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    protected TipoExamen buscarRegistroPorId(Object id) {
        if (id != null && id instanceof UUID buscado && !this.modelo.getWrappedData().isEmpty()) {
            for (TipoExamen e : dao.findAll()) {
                if (e.getIdTipoExamen() != null && e.getIdTipoExamen().equals(buscado)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(TipoExamen r) {
        if (r != null && r.getIdTipoExamen() != null) {
            return r.getIdTipoExamen().toString();
        }
        return null;
    }

    @Override
    protected TipoExamen getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                UUID buscado = UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(x -> x.getIdTipoExamen() != null && x.getIdTipoExamen().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
