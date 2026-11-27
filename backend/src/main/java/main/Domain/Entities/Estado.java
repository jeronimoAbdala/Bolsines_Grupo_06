package main.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

/** Clave natural ambito + nombre (EstadoId): el nombre solo no alcanza ("Enviado" es de bolsin y de remito). */
@Entity
@Table(name = "estado")
@IdClass(EstadoId.class)
public class Estado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ambito", nullable = false)
    private String ambito;

    @Id
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    /** Requerido por JPA: Hibernate crea la instancia vacia y despues le carga los atributos. */
    protected Estado() {
    }

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

    /**
     * Paso 3 del CU 28. "Enviado" es el nombre para Bolsin y Remito; para Documentacion,
     * segun la DME, el estado equivalente es "EnBolsinEnviado".
     */
    public boolean esEnviado() {
        return "Enviado".equals(nombre) || "EnBolsinEnviado".equals(nombre);
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

    // Paso 11 del CU 28: los esEstadoX() comparan SOLO el nombre; el ambito el Gestor lo
    // pregunta aparte con esAmbitoX(), asi no se repite esa responsabilidad.

    public boolean esEstadoRecibidoEnCMDestino() {
        return "RecibidoEnCMDestino".equals(nombre);
    }

    public boolean esEstadoRecibidoYAceptado() {
        return "RecibidoYAceptado".equals(nombre);
    }

    public boolean esEstadoRecibidaYAceptada() {
        return "RecibidaYAceptada".equals(nombre);
    }
}
