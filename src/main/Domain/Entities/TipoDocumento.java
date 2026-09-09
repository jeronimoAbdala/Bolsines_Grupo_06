package main.Domain.Entities;

import java.io.Serializable;

public class TipoDocumento implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String descripcion;

    public TipoDocumento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
