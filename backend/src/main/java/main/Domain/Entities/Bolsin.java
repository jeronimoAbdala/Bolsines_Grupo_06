package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bolsin implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private LocalDate fechaGeneracion;
    private String nroPrecinto;
    private int cantRemitos;
    private double peso;

    private ComisionMedica cmOrigen;
    private ComisionMedica cmDestino;

    private ArrayList<Remito> remitos;
    // El estado del bolsin no es un atributo: es el del cambio de estado actual
    private ArrayList<CambioEstadoBolsin> cambiosEstado;

    public Bolsin(int numero, LocalDate fechaGeneracion,
                  ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        this.numero = numero;
        this.fechaGeneracion = fechaGeneracion;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.remitos = new ArrayList<>();
        this.cambiosEstado = new ArrayList<>();
        this.cantRemitos = 0;
    }

    public void agregarRemito(Remito remito) {
        remitos.add(remito);
        cantRemitos = remitos.size();
    }

    public void agregarCambioEstado(CambioEstadoBolsin cambio) {
        cambiosEstado.add(cambio);
    }

    // sosEnviado() -> *sosActual() -> actual.sosEnviado() -> estado.esEnviado()
    public boolean sosEnviado() {
        CambioEstadoBolsin actual = buscarCEActual();
        return actual != null && actual.sosEnviado();
    }

    public boolean esTuCM(ComisionMedica cm) {
        return cmDestino != null && cmDestino.getNombre().equals(cm.getNombre());
    }

    public boolean esTuNumero(int numero) {
        return this.numero == numero;
    }

    public ComisionMedica getCMOrigen() {
        return cmOrigen;
    }

    public String getNroPrecinto() {
        return nroPrecinto;
    }

    /**
     * Por cada remito pide su numero y su documentacion (buscarDocumentacion),
     * que a su vez se la pide a cada DetalleRemito.
     */
    public List<String> obtenerInformacionRemito() {
        List<String> informacion = new ArrayList<>();
        for (Remito remito : remitos) {
            informacion.add("Remito #" + remito.getNumero() + ": " + String.join(", ", remito.buscarDocumentacion()));
        }
        return informacion;
    }

    /**
     * Registra la recepcion: cierra el cambio de estado actual, crea el nuevo
     * (Recibido en CM destino) y le pide a cada remito que se acepte.
     * Se llama mostrarRecibido() porque asi figura en el diagrama de secuencia.
     */
    public void mostrarRecibido(Estado estadoRecibidoEnCMDestino, Estado estadoRecibidoYAceptado,
                                Estado estadoRecibidaYAceptada, LocalDateTime fechaHoraActual,
                                Empleado empleado) {
        CambioEstadoBolsin actual = buscarCEActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHoraActual);
        }
        crearNuevoCE(estadoRecibidoEnCMDestino, fechaHoraActual, empleado);

        for (Remito remito : remitos) {
            remito.aceptar(estadoRecibidoYAceptado, estadoRecibidaYAceptada, fechaHoraActual, empleado);
        }
    }

    public CambioEstadoBolsin crearNuevoCE(Estado estado, LocalDateTime fechaHoraInicio, Empleado empleado) {
        CambioEstadoBolsin nuevo = new CambioEstadoBolsin(estado, fechaHoraInicio, empleado);
        agregarCambioEstado(nuevo);
        return nuevo;
    }

    private CambioEstadoBolsin buscarCEActual() {
        for (CambioEstadoBolsin cambio : cambiosEstado) {
            if (cambio.sosActual()) {
                return cambio;
            }
        }
        return null;
    }

    public Estado getEstadoActual() {
        CambioEstadoBolsin actual = buscarCEActual();
        return actual != null ? actual.getEstado() : null;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setNroPrecinto(String nroPrecinto) {
        this.nroPrecinto = nroPrecinto;
    }

    public int getCantRemitos() {
        return cantRemitos;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public ComisionMedica getCmOrigen() {
        return cmOrigen;
    }

    public ComisionMedica getCmDestino() {
        return cmDestino;
    }

    public ArrayList<Remito> getRemitos() {
        return remitos;
    }

    public ArrayList<CambioEstadoBolsin> getCambiosEstado() {
        return cambiosEstado;
    }
}
