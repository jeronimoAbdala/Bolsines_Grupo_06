package main.Dto;

import main.Domain.Entities.Remito;

import java.time.LocalDate;
import java.util.List;

public record RemitoDto(int numero, LocalDate fecha, String estado, List<DocumentacionDto> documentos) {

    /** La lista de documentos sale de los detalles del remito (cada detalle conoce su Documentacion). */
    public static RemitoDto de(Remito remito) {
        List<DocumentacionDto> docs = remito.getDetalleRemito().stream()
                .map(detalle -> DocumentacionDto.de(detalle.getObjetoDocumentacion()))
                .toList();
        return new RemitoDto(remito.getNumero(), remito.getFecha(), remito.getEstado().getNombre(), docs);
    }
}
