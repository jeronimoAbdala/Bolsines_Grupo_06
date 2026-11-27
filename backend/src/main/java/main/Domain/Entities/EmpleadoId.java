package main.Domain.Entities;

import java.io.Serializable;
import java.util.Objects;

/** Clave de Empleado (@IdClass): nombre + apellido, como en el ejemplo de la catedra. */
public class EmpleadoId implements Serializable {

    private String nombre;
    private String apellido;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpleadoId that = (EmpleadoId) o;
        return Objects.equals(nombre, that.nombre)
                && Objects.equals(apellido, that.apellido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellido);
    }
}
