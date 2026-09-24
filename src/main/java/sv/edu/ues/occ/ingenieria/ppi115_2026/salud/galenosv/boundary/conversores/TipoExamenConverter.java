package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity.TipoExamen;

@FacesConverter(value = "tipoExamenConverter", managed = true)
public class TipoExamenConverter implements Converter<TipoExamen> {

    @Override
    public TipoExamen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        TipoExamen tipoExamen = new TipoExamen();
        try {
            tipoExamen.setIdTipoExamen(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
        return tipoExamen;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoExamen value) {
        if (value == null || value.getIdTipoExamen() == null) {
            return "";
        }
        return value.getIdTipoExamen().toString();
    }
}
