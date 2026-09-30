package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named("tipoMedioContactoModel")
@ViewScoped
public class TipoMedioContactoModel extends AbstractModel<TipoMedioContacto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    TipoMedioContactoDAO dao;

    public TipoMedioContactoModel() {
        this.nombreBean = "TipoMedioContacto";
    }


    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected void validarUnicidad(TipoMedioContacto r, boolean esModificacion) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return;
        }
        r.setNombre(r.getNombre().trim());
        java.util.UUID excluir = esModificacion ? r.getIdTipoMedioContacto() : null;
        if (dao.existeNombre(r.getNombre(), excluir)) {
            throw new IllegalArgumentException("Ya existe un tipo de medio de contacto con ese nombre");
        }
    }

    @Override
    protected DefaultDAO<TipoMedioContacto> getDao() {
        return dao;
    }

    @Override
    protected TipoMedioContacto nuevoRegistro() {
        TipoMedioContacto r = new TipoMedioContacto();
        r.setIdTipoMedioContacto(UUID.randomUUID());
        r.setActivo(true);
        r.setExpresionRegular(".*"); // default regex como hizo en clase
        return r;
    }

    @Override
    protected TipoMedioContacto buscarRegistroPorId(Object id) {
        if (id != null && id instanceof UUID buscado && !this.modelo.getWrappedData().isEmpty()) {
            for (TipoMedioContacto e : dao.findAll()) {
                if (e.getIdTipoMedioContacto() != null && e.getIdTipoMedioContacto().equals(buscado)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(TipoMedioContacto r) {
        if (r != null && r.getIdTipoMedioContacto() != null) {
            return r.getIdTipoMedioContacto().toString();
        }
        return null;
    }

    @Override
    protected TipoMedioContacto getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                UUID buscado = UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(x -> x.getIdTipoMedioContacto()!=null && x.getIdTipoMedioContacto().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
