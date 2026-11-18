package main.Dto;

import main.Domain.Entities.Empleado;

/**
 * Usuario logueado / usuario del sistema, armado desde el Empleado (que conoce a su Usuario).
 * Mismo JSON que antes.
 */
public record UsuarioDto(String nombreUsuario, String nombre, String apellido, String mail,
                         ComisionMedicaDto comisionMedica) {

    public static UsuarioDto de(Empleado e) {
        return new UsuarioDto(e.getUsuario().getNombreUsuario(), e.getNombre(), e.getApellido(), e.getMail(),
                ComisionMedicaDto.de(e.getComisionMedica()));
    }
}
