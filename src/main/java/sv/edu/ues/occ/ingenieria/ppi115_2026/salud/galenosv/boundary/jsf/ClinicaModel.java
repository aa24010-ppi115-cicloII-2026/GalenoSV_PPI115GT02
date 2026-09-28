package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.AbstractModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ClinicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Clinica;

@Named("clinicaModel")
@ViewScoped
public class ClinicaModel extends AbstractModel<Clinica> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    FacesContext facesContext;

    @Inject
    ClinicaDAO dao;

    @Inject
    PersonaRolModel personaRolModel;

    public PersonaRolModel getPersonaRolModel() {
        personaRolModel.establecerClinicaMaestro(
                estado == sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.ESTADO_CRUD.MODIFICAR
                        && registro != null ? registro.getIdClinica() : null);
        return personaRolModel;
    }

    public ClinicaModel() {
        this.nombreBean = "Clinica";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected DefaultDAO<Clinica> getDao() {
        return dao;
    }

    @Override
    protected Clinica nuevoRegistro() {
        Clinica r = new Clinica();
        r.setIdClinica(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    protected Clinica buscarRegistroPorId(Object id) {
        if (id instanceof UUID buscado) {
            return dao.find(buscado);
        }
        return null;
    }

    @Override
    protected String getIdAsText(Clinica r) {
        if (r != null && r.getIdClinica() != null) {
            return r.getIdClinica().toString();
        }
        return null;
    }

    @Override
    protected Clinica getIdByText(String id) {
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
