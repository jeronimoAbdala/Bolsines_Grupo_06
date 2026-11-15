package main.Presentation.Api.Dto;

import main.Domain.Entities.Remito;

import java.time.LocalDate;
import java.util.List;

public record RemitoDto(int numero, LocalDate fecha, String estado, List<DocumentacionDto> documentos) {

    public static RemitoDto de(Remito remito) {
        List<DocumentacionDto> docs = remito.buscarDocumentacion().stream().map(DocumentacionDto::de).toList();
        return new RemitoDto(remito.getNumero(), remito.getFecha(), remito.getEstado().getNombre(), docs);
    }
}
