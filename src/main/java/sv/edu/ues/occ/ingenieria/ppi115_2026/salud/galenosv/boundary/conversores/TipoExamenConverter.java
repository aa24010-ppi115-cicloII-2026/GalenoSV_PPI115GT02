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
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

@RequestScoped
@FacesConverter(value = "tipoExamenConverter", managed = true)
public class TipoExamenConverter implements Converter<TipoExamen> {

    @Inject
    TipoExamenDAO dao;

    @Override
    public TipoExamen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return dao.find(UUID.fromString(value.trim()));
        } catch (Exception ex) {
            Logger.getLogger(TipoExamenConverter.class.getName())
                    .log(Level.WARNING, "No se pudo convertir TipoExamen", ex);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoExamen value) {
        if (value == null || value.getIdTipoExamen() == null) {
            return "";
        }
        return value.getIdTipoExamen().toString();
    }
}
