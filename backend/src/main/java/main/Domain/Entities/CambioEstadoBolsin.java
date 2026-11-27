package main.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clave natural numero del bolsin + fecha/hora de inicio (CambioEstadoBolsinId): un bolsin no tiene dos
 * cambios que empiecen en el mismo instante. El cambio no conoce a su bolsin: solo guarda su numero.
 */
@Entity
@Table(name = "cambio_estado_bolsin")
@IdClass(CambioEstadoBolsinId.class)
public class CambioEstadoBolsin implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Columna FK hacia el bolsin, como columna de la clave: la escribe el cambio (no la lista del Bolsin). */
    @Id
    @Column(name = "bolsin_numero", nullable = false)
    private int bolsinNumero;

    @Id
    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    /** null mientras es el cambio de estado actual (sosActual). */
    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "estado_ambito", referencedColumnName = "ambito", nullable = false),
            @JoinColumn(name = "estado_nombre", referencedColumnName = "nombre", nullable = false)
    })
    private Estado estado;

    /** Empleado responsable del cambio (paso 11: el empleado logueado). Sin cascada. */
    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "empleado_nombre", referencedColumnName = "nombre", nullable = false),
            @JoinColumn(name = "empleado_apellido", referencedColumnName = "apellido", nullable = false)
    })
    private Empleado empleado;

    /** Requerido por JPA. */
    protected CambioEstadoBolsin() {
    }

    /** La fecha/hora la pasa el Gestor (getFechaYHoraActual): la misma que cierra el cambio anterior. */
    public CambioEstadoBolsin(int bolsinNumero, Estado estado, LocalDateTime fechaHoraInicio, Empleado empleado) {
        this.bolsinNumero = bolsinNumero;
        this.estado = estado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.empleado = empleado;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public boolean sosEnviado() {
        return estado != null && estado.esEnviado();
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public void cerrar() {
        this.fechaHoraFin = LocalDateTime.now();
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public Estado getEstado() {
        return estado;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}
