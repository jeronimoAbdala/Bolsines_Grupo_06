package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/** Clave de CambioEstadoDocumentacion (@IdClass): numero de la documentacion + fecha/hora de inicio. */
public class CambioEstadoDocumentacionId implements Serializable {

    private int documentacionNumero;
    private LocalDateTime fechaHoraInicio;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CambioEstadoDocumentacionId that = (CambioEstadoDocumentacionId) o;
        return documentacionNumero == that.documentacionNumero
                && Objects.equals(fechaHoraInicio, that.fechaHoraInicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentacionNumero, fechaHoraInicio);
    }
}
