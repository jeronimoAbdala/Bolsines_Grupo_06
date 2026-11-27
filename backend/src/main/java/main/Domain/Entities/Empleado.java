package main.Domain.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

/** Clave natural nombre + apellido (EmpleadoId), como en el ejemplo de la catedra. */
@Entity
@Table(name = "empleado")
@IdClass(EmpleadoId.class)
public class Empleado implements Serializable {

    @Id
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Id
    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "mail")
    private String mail;

    /**
     * Muchos empleados trabajan en una comision medica (multiplicidad 1 en el diagrama).
     * Sin cascada: la CM es un dato maestro.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "comision_medica_codigo", nullable = false)
    private ComisionMedica comisionMedica;

    /**
     * El Empleado conoce a su Usuario (Empleado -> Usuario, multiplicidad 1 en el diagrama).
     * PERSIST: al guardar un empleado nuevo se inserta tambien su usuario.
     * MERGE: al guardar un empleado existente se propagan los cambios a su usuario.
     */
    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "usuario_nombre", nullable = false, unique = true)
    private Usuario usuario;

    /** Requerido por JPA. */
    protected Empleado() {
    }

    public Empleado(String nombre, String apellido, String mail,
                    ComisionMedica comisionMedica, Usuario usuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.comisionMedica = comisionMedica;
        this.usuario = usuario;
    }

    /** Paso 2 del CU 28: el gestor recorre los empleados preguntando *esTuUsuario(usuario). */
    public boolean esTuUsuario(Usuario usuario) {
        return this.usuario != null && usuario != null
                && this.usuario.getNombreUsuario().equals(usuario.getNombreUsuario());
    }

    /** Paso 2 del CU 28: devuelve el nombre de la CM del empleado (le pide getNombre() a su ComisionMedica). */
    public String getCM() {
        return comisionMedica.getNombre();
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

    public Usuario getUsuario() {
        return usuario;
    }
}
