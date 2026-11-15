package main.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Usuario del sistema. No conoce a su Empleado: la navegacion es Empleado -> Usuario.
 */
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    /** Clave natural: el nombre de usuario. */
    @Id
    @Column(name = "nombre_usuario")
    private String nombre;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;

    /** Requerido por JPA. */
    protected Usuario() {
    }

    public Usuario(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
        this.fechaAlta = LocalDate.now();
    }

    public String getNombreUsuario() {
        return nombre;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }
}
