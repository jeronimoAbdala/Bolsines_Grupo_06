package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Sesion implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
}
