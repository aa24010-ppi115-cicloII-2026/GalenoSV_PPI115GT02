/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

/**
 *
 * @author duran
 */
@Named("idiomaBean")
@SessionScoped
public class IdiomaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idioma = "es";

    public Locale getLocale() {
        return switch (idioma) {
            case "en" ->
                Locale.of("en", "US");
            case "fr" ->
                Locale.of("fr", "FR");
            case "zh" ->
                Locale.of("zh", "CN");
            default ->
                Locale.of("es", "SV");
        };
    }

    public void cambiarIdioma() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        if (contexto.getViewRoot() != null) {
            contexto.getViewRoot().setLocale(getLocale());
        }
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

}
