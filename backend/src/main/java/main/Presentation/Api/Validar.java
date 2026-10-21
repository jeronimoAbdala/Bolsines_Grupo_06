package main.Presentation.Api;

final class Validar {

    private Validar() {
    }

    static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalido("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    static String opcional(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
