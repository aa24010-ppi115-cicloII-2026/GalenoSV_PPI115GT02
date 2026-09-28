package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named("idiomaBean")
@SessionScoped
public class IdiomaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idioma = "es";
    private String pais = "SV";

    public Locale getLocale() {
        return Locale.of(idioma, pais);
    }

    public void cambiarIdioma(String nuevoIdioma) {
        this.idioma = nuevoIdioma;
        if ("en".equals(nuevoIdioma)) {
            this.pais = "US";
        } else if ("fr".equals(nuevoIdioma)) {
            this.pais = "FR";
        } else if ("zh".equals(nuevoIdioma)) {
            this.pais = "CN";
        } else {
            this.idioma = "es";
            this.pais = "SV";
        }
        FacesContext.getCurrentInstance().getViewRoot().setLocale(getLocale());
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}
