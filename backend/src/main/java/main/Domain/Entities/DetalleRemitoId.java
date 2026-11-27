package main.Domain.Entities;

import java.io.Serializable;
import java.util.Objects;

/** Clave de DetalleRemito (@IdClass): numero del remito + numero de la documentacion. */
public class DetalleRemitoId implements Serializable {

    private int remitoNumero;
    private int documentacionNumero;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleRemitoId that = (DetalleRemitoId) o;
        return remitoNumero == that.remitoNumero
                && documentacionNumero == that.documentacionNumero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(remitoNumero, documentacionNumero);
    }
}
