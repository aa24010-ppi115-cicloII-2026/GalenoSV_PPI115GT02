package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

@Named("rolModel")
@ViewScoped
public class RolModel extends AbstractModel<Rol> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    RolDAO dao;

    public RolModel() {
        this.nombreBean = "Rol";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<Rol> getDao() {
        return dao;
    }

    @Override
    protected Rol nuevoRegistro() {
        Rol r = new Rol();
        r.setIdRol(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    protected Rol buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado) {
            return dao.find(buscado);
        }
        return null;
    }

    @Override
    protected String getIdAsText(Rol r) {
        if (r != null && r.getIdRol() != null) {
            return r.getIdRol().toString();
        }
        return null;
    }

    @Override
    protected Rol getIdByText(String id) {
        if (id != null) {
            try {
                return dao.find(UUID.fromString(id));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }
}
