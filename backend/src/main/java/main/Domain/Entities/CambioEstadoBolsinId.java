package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/** Clave de CambioEstadoBolsin (@IdClass): numero del bolsin + fecha/hora de inicio. */
public class CambioEstadoBolsinId implements Serializable {

    private int bolsinNumero;
    private LocalDateTime fechaHoraInicio;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CambioEstadoBolsinId that = (CambioEstadoBolsinId) o;
        return bolsinNumero == that.bolsinNumero
                && Objects.equals(fechaHoraInicio, that.fechaHoraInicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bolsinNumero, fechaHoraInicio);
    }
}
