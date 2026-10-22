package main.Presentation.Api.Dto;

import main.Domain.Entities.Bolsin;

import java.time.LocalDate;
import java.util.List;

public record BolsinDto(int numero, LocalDate fechaGeneracion, String nroPrecinto, double peso,
                        int cantRemitos, String estado, ComisionMedicaDto cmOrigen, ComisionMedicaDto cmDestino,
                        List<RemitoDto> remitos) {

    public static BolsinDto de(Bolsin b) {
        return new BolsinDto(b.getNumero(), b.getFechaGeneracion(), b.getNroPrecinto(), b.getPeso(),
                b.getCantRemitos(), b.getEstado().getNombre(),
                ComisionMedicaDto.de(b.getCmOrigen()), ComisionMedicaDto.de(b.getCmDestino()),
                b.getRemitos().stream().map(RemitoDto::de).toList());
    }
}
