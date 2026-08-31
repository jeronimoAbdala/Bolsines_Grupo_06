package main.Entities;
import java.util.ArrayList;


public class Remito {

    private int numero;

    private Estado estado;

    private ArrayList<DetalleRemito> detalles;

    public Remito(int numero, Estado estado) {

        this.numero = numero;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetalleRemito detalle) {

        detalles.add(detalle);
    }

    public ArrayList<DetalleRemito> getDetalles() {

        return detalles;
    }

    public Estado getEstado() {

        return estado;
    }

    public void setEstado(Estado estado) {

        this.estado = estado;
    }
}