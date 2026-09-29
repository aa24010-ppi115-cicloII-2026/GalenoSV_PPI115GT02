package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ValidacionFormato {
    private ValidacionFormato() {
    }

    public static void validar(String valor, String expresion) {
        validar(valor, expresion, null);
    }

    public static void validar(String valor, String expresion, String indicaciones) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar un valor");
        }
        String limpio = valor.trim();
        if (limpio.length() < 8 && (expresion == null || expresion.isBlank())) {
            throw new IllegalArgumentException("El valor debe tener al menos 8 caracteres");
        }
        if (expresion == null || expresion.isBlank()) {
            return;
        }
        try {
            if (!Pattern.compile(expresion).matcher(limpio).matches()) {
                String base = "El valor '" + limpio + "' no cumple el formato esperado";
                if (indicaciones != null && !indicaciones.isBlank()) {
                    base += ": " + indicaciones.trim();
                } else {
                    base += " (" + expresion + ")";
                }
                throw new IllegalArgumentException(base);
            }
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("La expresion regular del tipo seleccionado no es valida", ex);
        }
    }
}
