package main.Dto;

import main.Domain.Entities.Documentacion;

public record DocumentacionDto(int numero, String asunto, String descripcion, String tipo, String estado) {

    public static DocumentacionDto de(Documentacion doc) {
        return new DocumentacionDto(doc.getNumero(), doc.getAsunto(), doc.getDescripcion(),
                doc.mostrarTipoDocumentacion(), doc.getEstado().getNombre());
    }
}
