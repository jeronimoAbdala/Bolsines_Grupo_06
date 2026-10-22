package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Por cada detalle: getDocumentacion() -> getAsunto() y mostrarTipoDocumentacion()
    public List<String> buscarDocumentacion() {
        List<String> documentacion = new ArrayList<>();
        for (DetalleRemito detalle : detalles) {
            Documento documento = detalle.getDocumentacion();
            documentacion.add(documento.getAsunto() + " (" + documento.mostrarTipoDocumentacion() + ")");
        }
        return documentacion;
    }

    // El remito pasa a Recibido y Aceptado y le pide a cada detalle que acepte su documentacion
    public void aceptar(Estado estadoRecibidoYAceptado, Estado estadoRecibidaYAceptada,
                        LocalDateTime fechaHoraActual, Empleado empleado) {
        this.estado = estadoRecibidoYAceptado;
        for (DetalleRemito detalle : detalles) {
            detalle.aceptarDocumentacion(estadoRecibidaYAceptada, fechaHoraActual, empleado);
        }
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
}
