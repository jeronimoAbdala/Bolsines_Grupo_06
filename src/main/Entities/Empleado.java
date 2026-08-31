package main.Entities;

public class Empleado {

    private String nombre;
    private String apellido;
    private String mail;

    public Empleado(String nombre, String apellido, String mail, ComisionMedica cm) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
