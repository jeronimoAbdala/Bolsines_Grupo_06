package main.Presentation.Api.Dto;

import main.Domain.Entities.ComisionMedica;

public record ComisionMedicaDto(int codigo, String nombre, String direccion, String email, String telefono) {

    public static ComisionMedicaDto de(ComisionMedica cm) {
        return new ComisionMedicaDto(cm.getCodigo(), cm.getNombre(), cm.getDireccion(), cm.getEmail(), cm.getTelefono());
    }
}
