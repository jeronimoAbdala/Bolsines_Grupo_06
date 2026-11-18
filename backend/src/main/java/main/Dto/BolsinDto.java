package main.Dto;

import main.Domain.Entities.Bolsin;

import java.time.LocalDate;
import java.util.List;

public record BolsinDto(int numero, LocalDate fechaGeneracion, String nroPrecinto, double peso,
                        int cantRemitos, String estado, ComisionMedicaDto cmOrigen, ComisionMedicaDto cmDestino,
                        List<RemitoDto> remitos) {

    /** cantRemitos es derivado: se calcula con la cantidad de remitos (ya no es atributo de Bolsin). */
    public static BolsinDto de(Bolsin b) {
        return new BolsinDto(b.getNumero(), b.getFechaGeneracion(), b.getNroPrecinto(), b.getPeso(),
                b.getRemitos().size(), b.getEstado().getNombre(),
                ComisionMedicaDto.de(b.getCMOrigen()), ComisionMedicaDto.de(b.getCMDestino()),
                b.getRemitos().stream().map(RemitoDto::de).toList());
    }
}
