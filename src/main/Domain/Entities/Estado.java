package main.Domain.Entities;

import java.io.Serializable;

public class Estado implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ambito;
    private String nombre;
    private String descripcion;

    public Estado(String ambito, String nombre, String descripcion) {
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getAmbito() {
        return ambito;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEnviado() {
        return "ENVIADO".equals(nombre) || "ENVIADA".equals(nombre);
    }

    public boolean esAmbitoBolsin() {
        return "BOL".equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return "REM".equals(ambito);
    }

    public boolean esAmbitoDocumentacion() {
        return "DOC".equals(ambito);
    }

    public boolean esEstadoRecibidoEnCMDestino() {
        return "RECIBIDO".equals(nombre) && esAmbitoBolsin();
    }

    public boolean esEstadoRecibidoYAceptado() {
        return "RECIBIDO_ACEPTADO".equals(nombre) && esAmbitoRemito();
    }

    public boolean esEstadoRecibidaYAceptada() {
        return "RECIBIDA_ACEPTADA".equals(nombre) && esAmbitoDocumentacion();
    }
}
