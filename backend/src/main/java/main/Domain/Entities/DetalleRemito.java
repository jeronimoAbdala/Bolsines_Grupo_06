package main.Domain.Entities;

import java.io.Serializable;

public class DetalleRemito implements Serializable {

    private static final long serialVersionUID = 1L;

    private String areaCMDestino;
    private final Documento documento;

    public DetalleRemito(Documento documento, String areaCMDestino) {
        this.documento = documento;
        this.areaCMDestino = areaCMDestino;
    }

    public DetalleRemito(Documento documento) {
        this(documento, "");
    }

    public String getAreaCMDestino() {
        return areaCMDestino;
    }

    public Documento getDocumentacion() {
        return documento;
    }

    public void aceptarDocumentacion(Empleado empleado) {
        documento.setEstado(
                new Estado("DOC", "RECIBIDA_ACEPTADA", "Documentación recibida y aceptada")
        );
    }
}
