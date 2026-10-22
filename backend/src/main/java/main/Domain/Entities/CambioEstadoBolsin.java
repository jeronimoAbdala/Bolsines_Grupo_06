package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CambioEstadoBolsin implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;
    private Empleado empleado;

    public CambioEstadoBolsin(Estado estado, Empleado empleado) {
        this.estado = estado;
        this.empleado = empleado;
        this.fechaHoraInicio = LocalDateTime.now();
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

    public void cerrar() {
        this.fechaHoraFin = LocalDateTime.now();
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
