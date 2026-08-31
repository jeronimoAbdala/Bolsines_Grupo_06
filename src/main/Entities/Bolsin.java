package main.Entities;
import java.util.ArrayList;

import java.time.LocalDate;

public class Bolsin {

    private int numero;
    private LocalDate fechaGeneracion;
    private String nroPrecinto;
    private double peso;

    private Estado estado;

    private ComisionMedica cmOrigen;
    private ComisionMedica cmDestino;

    private ArrayList<Remito> remitos;

    public Bolsin(int numero,
                  ComisionMedica cmOrigen,
                  ComisionMedica cmDestino,
                  Estado estado) {

        this.numero = numero;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.estado = estado;

        this.fechaGeneracion = LocalDate.now();

        remitos = new ArrayList<>();
    }

    public void agregarRemito(Remito remito) {

        remitos.add(remito);
    }

    public ArrayList<Remito> getRemitos() {

        return remitos;
    }

    public Estado getEstado() {

        return estado;
    }

    public void setEstado(Estado estado) {

        this.estado = estado;
    }
}