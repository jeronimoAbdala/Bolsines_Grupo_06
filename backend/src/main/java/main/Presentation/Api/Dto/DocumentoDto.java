package main.Presentation.Api.Dto;

import main.Domain.Entities.Documento;

public record DocumentoDto(int numero, String asunto, String descripcion, String tipo, String estado) {

    public static DocumentoDto de(Documento doc) {
        return new DocumentoDto(doc.getNumero(), doc.getAsunto(), doc.getDescripcion(),
                doc.mostrarTipoDocumentacion(), BolsinDto.nombre(doc.getEstadoActual()));
    }
}
