package main.Presentation.Api.Dto;

import main.Domain.Entities.Remito;

import java.time.LocalDate;
import java.util.List;

public record RemitoDto(int numero, LocalDate fecha, String estado, List<DocumentoDto> documentos) {

    public static RemitoDto de(Remito remito) {
        List<DocumentoDto> docs = remito.getDetalles().stream().map(d -> DocumentoDto.de(d.getDocumentacion())).toList();
        return new RemitoDto(remito.getNumero(), remito.getFecha(), BolsinDto.nombre(remito.getEstado()), docs);
    }
}
