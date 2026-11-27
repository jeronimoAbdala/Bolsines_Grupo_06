package main.Domain.Entities;

import java.io.Serializable;
import java.util.Objects;

/** Clave de Estado (@IdClass): ambito + nombre. Mismos nombres y tipos que los @Id de la entidad. */
public class EstadoId implements Serializable {

    private String ambito;
    private String nombre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstadoId that = (EstadoId) o;
        return Objects.equals(ambito, that.ambito)
                && Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ambito, nombre);
    }
}
