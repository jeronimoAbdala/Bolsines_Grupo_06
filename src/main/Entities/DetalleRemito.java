package main.Entities;

public class DetalleRemito {

    private Documentacion documentacion;

    public DetalleRemito(Documentacion documentacion) {
        this.documentacion = documentacion;
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }
}