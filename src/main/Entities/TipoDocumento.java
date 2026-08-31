package main.Entities;

public class TipoDocumento {

    private String nombre;
    private String descripcion;

    public TipoDocumento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }
}
