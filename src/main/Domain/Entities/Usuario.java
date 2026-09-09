package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;

public class Usuario implements Serializable {

    private String nombre;
    private String password;
    private LocalDate fechaAlta;
    private Empleado empleado;

    public Usuario(String nombre, String password, Empleado empleado) {
        this.nombre = nombre;
        this.password = password;
        this.empleado = empleado;
        this.fechaAlta = LocalDate.now();
    }

    public String getNombreUsuario() {
        return nombre;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}