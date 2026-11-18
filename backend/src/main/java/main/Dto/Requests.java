package main.Dto;

/**
 * Cuerpos de las peticiones POST de la API.
 */
public final class Requests {

    private Requests() {
    }

    /** Paso 8 del CU 28: opcion de recepcion elegida por el usuario (1 a 4, segun la ERS). */
    public record OpcionDeRecepcion(int opcion) {
    }
}
