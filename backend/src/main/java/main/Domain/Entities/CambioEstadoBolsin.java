package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CambioEstadoBolsin implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;
    private Empleado empleado;

    // new() del diagrama: lo crea el Bolsin en crearNuevoCE()
    public CambioEstadoBolsin(Estado estado, LocalDateTime fechaHoraInicio, Empleado empleado) {
        this.estado = estado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.empleado = empleado;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public boolean sosEnviado() {
        return estado != null && estado.esEnviado();
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public Estado getEstado() {
        return estado;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}
