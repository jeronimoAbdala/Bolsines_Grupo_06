package main.Presentation.Api.Dto;

import main.Domain.Entities.Empleado;
import main.Domain.Entities.Usuario;

public record UsuarioDto(String nombreUsuario, String nombre, String apellido, String mail,
                         ComisionMedicaDto comisionMedica) {

    public static UsuarioDto de(Usuario u) {
        Empleado e = u.getEmpleado();
        return new UsuarioDto(u.getNombreUsuario(), e.getNombre(), e.getApellido(), e.getMail(),
                ComisionMedicaDto.de(e.getComisionMedica()));
    }
}
