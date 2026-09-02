package main.Domain.Entities;

public class DetalleRemito {

    private final Documentation documentacion;

    public DetalleRemito(Documentation documentacion) {
        this.documentacion = documentacion;
    }

    public Documentation getDocumentacion() {
        return documentacion;
    }
}