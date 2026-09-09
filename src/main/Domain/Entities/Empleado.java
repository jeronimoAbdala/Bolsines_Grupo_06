package main.Domain.Entities;

import java.io.Serializable;

public class Empleado implements Serializable {

    private String nombre;
    private String apellido;
    private String mail;
    private ComisionMedica comisionMedica;

    public Empleado(String nombre, String apellido, String mail, ComisionMedica comisionMedica) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.comisionMedica = comisionMedica;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getMail() {
        return mail;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public ComisionMedica getComisionMedica() {
        return comisionMedica;
    }
}
