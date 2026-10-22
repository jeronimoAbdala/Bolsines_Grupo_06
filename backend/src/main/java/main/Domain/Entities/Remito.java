package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Remito implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private LocalDate fecha;
    private Estado estado;

    private ComisionMedica cmOrigen;
    private ComisionMedica cmDestino;

    private ArrayList<DetalleRemito> detalles;

    public Remito(int numero, LocalDate fecha, Estado estado,
                  ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        this.numero = numero;
        this.fecha = fecha;
        this.estado = estado;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetalleRemito detalle) {
        detalles.add(detalle);
    }

    public ArrayList<Documento> buscarDocumentacion() {
        ArrayList<Documento> docs = new ArrayList<>();
        for (DetalleRemito detalle : detalles) {
            docs.add(detalle.getDocumentacion());
        }
        return docs;
    }

    public void aceptar(Empleado empleado) {
        Estado estadoAceptado = new Estado("REM", "RECIBIDO_ACEPTADO", "Remito recibido y aceptado");
        this.estado = estadoAceptado;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ComisionMedica getCmOrigen() {
        return cmOrigen;
    }

    public ComisionMedica getCmDestino() {
        return cmDestino;
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
