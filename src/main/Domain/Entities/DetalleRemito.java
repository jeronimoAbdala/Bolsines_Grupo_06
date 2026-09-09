package main.Domain.Entities;

import java.io.Serializable;

public class DetalleRemito implements Serializable {

    private static final long serialVersionUID = 1L;

    private String areaCMDestino;
    private final Documentation documentacion;

    public DetalleRemito(Documentation documentacion, String areaCMDestino) {
        this.documentacion = documentacion;
        this.areaCMDestino = areaCMDestino;
    }

    public DetalleRemito(Documentation documentacion) {
        this(documentacion, "");
    }

    public String getAreaCMDestino() {
        return areaCMDestino;
    }

    public Documentation getDocumentacion() {
        return documentacion;
    }

    public void aceptarDocumentacion(Empleado empleado) {
        documentacion.setEstado(
                new Estado("DOC", "RECIBIDA_ACEPTADA", "Documentación recibida y aceptada")
        );
    }
}
