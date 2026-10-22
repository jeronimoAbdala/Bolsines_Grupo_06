package main.Presentation.Api.Dto;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.Estado;

import java.time.LocalDate;
import java.util.List;

public record BolsinDto(int numero, LocalDate fechaGeneracion, String nroPrecinto, double peso,
                        int cantRemitos, String estado, ComisionMedicaDto cmOrigen, ComisionMedicaDto cmDestino,
                        List<RemitoDto> remitos) {

    public static BolsinDto de(Bolsin b) {
        return new BolsinDto(b.getNumero(), b.getFechaGeneracion(), b.getNroPrecinto(), b.getPeso(),
                b.getCantRemitos(), nombre(b.getEstadoActual()),
                ComisionMedicaDto.de(b.getCmOrigen()), ComisionMedicaDto.de(b.getCmDestino()),
                b.getRemitos().stream().map(RemitoDto::de).toList());
    }

    static String nombre(Estado estado) {
        return estado != null ? estado.getNombre() : "";
    }
}
