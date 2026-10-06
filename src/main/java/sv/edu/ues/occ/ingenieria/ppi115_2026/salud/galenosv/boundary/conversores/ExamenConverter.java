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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.ExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.Examen;

@RequestScoped
@FacesConverter(value = "examenConverter", managed = true)
public class ExamenConverter implements Converter<Examen> {

    @Inject
    ExamenDAO dao;

    @Override
    public Examen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return dao.find(UUID.fromString(value.trim()));
        } catch (Exception ex) {
            Logger.getLogger(ExamenConverter.class.getName())
                    .log(Level.WARNING, "No se pudo convertir Examen", ex);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Examen value) {
        if (value == null || value.getIdExamen() == null) {
            return "";
        }
        return value.getIdExamen().toString();
    }
}
