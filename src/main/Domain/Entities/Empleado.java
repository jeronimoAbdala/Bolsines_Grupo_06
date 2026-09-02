package main.Domain.Entities;

public class Empleado {

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

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public ComisionMedica getComisionMedica() {
        return comisionMedica;
    }
}
