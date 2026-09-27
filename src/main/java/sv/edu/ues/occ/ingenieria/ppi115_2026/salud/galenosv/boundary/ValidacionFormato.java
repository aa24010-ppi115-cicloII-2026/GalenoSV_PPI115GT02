package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ValidacionFormato {
    private ValidacionFormato() {
    }

    public static void validar(String valor, String expresion) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar un valor");
        }
        if (expresion == null || expresion.isBlank()) {
            return;
        }
        try {
            if (!Pattern.compile(expresion).matcher(valor).matches()) {
                throw new IllegalArgumentException("El valor no cumple el formato del tipo seleccionado");
            }
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("La expresion regular del tipo seleccionado no es valida", ex);
        }
    }
}
