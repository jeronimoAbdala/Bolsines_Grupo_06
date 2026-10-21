package main.Domain.Entities;

import java.io.Serializable;

public class ComisionMedica implements Serializable {

    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;

    public ComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public ComisionMedica(int codigo, String nombre, String direccion, String email) {
        this(codigo, nombre, direccion, email, "");
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
