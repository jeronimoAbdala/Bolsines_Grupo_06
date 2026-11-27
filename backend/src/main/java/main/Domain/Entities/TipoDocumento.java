package main.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "tipo_documento")
public class TipoDocumento implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clave natural: el nombre del tipo. */
    @Id
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    /** Requerido por JPA. */
    protected TipoDocumento() {
    }

    public TipoDocumento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
