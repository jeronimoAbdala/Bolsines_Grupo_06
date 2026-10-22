package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Bolsin implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private LocalDate fechaGeneracion;
    private String nroPrecinto;
    private int cantRemitos;
    private double peso;

    private Estado estado;

    private ComisionMedica cmOrigen;
    private ComisionMedica cmDestino;

    private ArrayList<Remito> remitos;
    private ArrayList<CambioEstadoBolsin> cambiosEstado;

    public Bolsin(int numero,
                  ComisionMedica cmOrigen,
                  ComisionMedica cmDestino,
                  Estado estado) {
        this.numero = numero;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.estado = estado;
        this.fechaGeneracion = LocalDate.now();
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

    public boolean sosEnviado() {
        return estado != null && estado.esEnviado() && estado.esAmbitoBolsin();
    }

    public boolean esTuCM(ComisionMedica cm) {
        return cmDestino != null && cmDestino.getNombre().equals(cm.getNombre());
    }

    public boolean esTuNumero(int numero) {
        return this.numero == numero;
    }

    public String mostrarRecibido() {
        return "Bolsin #" + numero + " - Estado: " + estado.getNombre();
    }

    public String obtenerInformacionRemito() {
        StringBuilder info = new StringBuilder();
        for (Remito remito : remitos) {
            info.append("Remito #").append(remito.getNumero()).append("\n");
        }
        return info.toString();
    }

    public CambioEstadoBolsin crearNuevoCEO(Estado nuevoEstado, Empleado empleado) {
        CambioEstadoBolsin cambio = new CambioEstadoBolsin(nuevoEstado, empleado);
        agregarCambioEstado(cambio);
        return cambio;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public String getNroPrecinto() {
        return nroPrecinto;
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

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
