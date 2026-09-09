package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Sesion implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Usuario usuario;
    private ComisionMedica comisionMedica;

    public Sesion(Usuario usuario, ComisionMedica comisionMedica) {
        this.usuario = usuario;
        this.comisionMedica = comisionMedica;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    public Sesion(Usuario usuario) {
        this(usuario, null);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public ComisionMedica getComisionMedica() {
        return comisionMedica;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
}
