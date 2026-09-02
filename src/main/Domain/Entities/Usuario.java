package main.Domain.Entities;

import java.time.LocalDate;

public class Usuario {

    private String nombreUsuario;
    private String password;
    private LocalDate fechaAlta;
    private Empleado empleado;

    public Usuario(String nombreUsuario, String password, Empleado empleado) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.empleado = empleado;
        this.fechaAlta = LocalDate.now();
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}