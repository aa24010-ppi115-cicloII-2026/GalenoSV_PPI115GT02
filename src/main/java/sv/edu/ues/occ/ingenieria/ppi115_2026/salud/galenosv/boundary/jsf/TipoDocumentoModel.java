package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DocumentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoDocumento;

@Named("tipoDocumentoModel")
@ViewScoped
public class TipoDocumentoModel extends AbstractModel<TipoDocumento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    TipoDocumentoDAO dao;

    @Inject
    DocumentoDAO documentoDAO;

    @Override
    protected void validarEliminacion(TipoDocumento r) {
        long n = r.getIdTipoDocumento() == null ? 0 : documentoDAO.countByTipo(r.getIdTipoDocumento());
        if (n > 0) {
            throw new IllegalArgumentException("No se puede eliminar el tipo de documento porque tiene " + n + " documento(s) registrado(s)");
        }
    }

    public TipoDocumentoModel() {
        this.nombreBean = "TipoDocumento";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected void validarUnicidad(TipoDocumento r, boolean esModificacion) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return;
        }
        r.setNombre(r.getNombre().trim());
        java.util.UUID excluir = esModificacion ? r.getIdTipoDocumento() : null;
        if (dao.existeNombre(r.getNombre(), excluir)) {
            throw new IllegalArgumentException("Ya existe un tipo de documento con ese nombre");
        }
    }

    @Override
    protected DefaultDAO<TipoDocumento> getDao() {
        return dao;
    }

    @Override
    protected TipoDocumento nuevoRegistro() {
        TipoDocumento r = new TipoDocumento();
        r.setIdTipoDocumento(UUID.randomUUID());
        r.setActivo(true);
        r.setExpresionRegular(".*");
        return r;
    }

    @Override
    protected TipoDocumento buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            for (TipoDocumento e : dao.findAll()) {
                if (e.getIdTipoDocumento() != null && e.getIdTipoDocumento().equals(buscado)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(TipoDocumento r) {
        if (r != null && r.getIdTipoDocumento() != null) {
            return r.getIdTipoDocumento().toString();
        }
        return null;
    }

    @Override
    protected TipoDocumento getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                UUID buscado = UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(x -> x.getIdTipoDocumento() != null && x.getIdTipoDocumento().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
