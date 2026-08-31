package main.Entities;

public class ComisionMedica {

    private int codigo;
    private String nombre;
    private String direccion;
    private String email;

    public ComisionMedica(int codigo,
                          String nombre,
                          String direccion,
                          String email) {

        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }
}