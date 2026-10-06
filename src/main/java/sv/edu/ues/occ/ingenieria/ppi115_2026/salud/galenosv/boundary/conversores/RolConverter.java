package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Rol;

@RequestScoped
@FacesConverter(value = "rolConverter", managed = true)
public class RolConverter implements Converter<Rol> {

    @Inject
    RolDAO dao;

    @Override
    public Rol getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return dao.find(UUID.fromString(value.trim()));
        } catch (Exception ex) {
            Logger.getLogger(RolConverter.class.getName())
                    .log(Level.WARNING, "No se pudo convertir Rol", ex);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Rol value) {
        if (value == null || value.getIdRol() == null) {
            return "";
        }
        return value.getIdRol().toString();
    }
}
