package main.Entities;

import java.time.LocalDateTime;

public class Sesion {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Usuario usuario;

    public Sesion(Usuario usuario) {
        this.usuario = usuario;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    public Usuario getUsuario() {
        return usuario;
    }
}