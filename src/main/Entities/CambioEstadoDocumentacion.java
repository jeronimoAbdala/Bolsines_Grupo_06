package main.Entities;

import java.time.LocalDateTime;

public class CambioEstadoDocumentacion {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    private Estado estado;
    private Empleado empleado;

    public CambioEstadoDocumentacion(Estado estado,
                                     Empleado empleado) {

        this.estado = estado;
        this.empleado = empleado;
        this.fechaHoraInicio = LocalDateTime.now();
    }
}
