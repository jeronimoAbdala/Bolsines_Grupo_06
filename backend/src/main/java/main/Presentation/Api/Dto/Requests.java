package main.Presentation.Api.Dto;

/**
 * Cuerpos de las peticiones POST de la API.
 */
public final class Requests {

    private Requests() {
    }

    public record CrearBolsin(String nroPrecinto, double peso, int cmOrigenCodigo, int cmDestinoCodigo) {
    }

    public record CrearRemito(String asunto, String descripcion, String tipoDocumento) {
    }

    public record CrearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
    }

    public record CrearUsuario(String nombreUsuario, String password, String nombre, String apellido,
                               String mail, int comisionMedicaCodigo) {
    }
}
