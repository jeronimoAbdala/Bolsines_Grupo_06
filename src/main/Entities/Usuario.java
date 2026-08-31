package main.Entities;

import java.time.LocalDate;

public class Usuario {

    private String nombreUsuario;
    private String password;
    private LocalDate fechaAlta;

    public Usuario(String nombreUsuario, String password, Empleado empleado) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.fechaAlta = LocalDate.now();
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}