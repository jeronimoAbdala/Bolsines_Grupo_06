package main.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "comision_medica")
public class ComisionMedica implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clave natural asignada (no generada): el codigo lo elige quien da de alta la comision. */
    @Id
    @Column(name = "codigo")
    private int codigo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "email")
    private String email;

    @Column(name = "telefono")
    private String telefono;

    /** Requerido por JPA. */
    protected ComisionMedica() {
    }

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
