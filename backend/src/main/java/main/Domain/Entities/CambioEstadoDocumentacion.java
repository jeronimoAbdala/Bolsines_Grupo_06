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
 * Clave natural numero de la documentacion + fecha/hora de inicio (CambioEstadoDocumentacionId): al recibir,
 * varias documentaciones reciben un cambio con la misma fecha/hora; las distingue el numero de su documentacion.
 * El cambio no conoce a su documentacion: solo guarda su numero.
 */
@Entity
@Table(name = "cambio_estado_documentacion")
@IdClass(CambioEstadoDocumentacionId.class)
public class CambioEstadoDocumentacion implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Columna FK hacia la documentacion, como columna de la clave: la escribe el cambio (no la lista de la Documentacion). */
    @Id
    @Column(name = "documentacion_numero", nullable = false)
    private int documentacionNumero;

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
    protected CambioEstadoDocumentacion() {
    }

    /** La fecha/hora la pasa el Gestor (getFechaYHoraActual): la misma que cierra el cambio anterior. */
    public CambioEstadoDocumentacion(int documentacionNumero, Estado estado, LocalDateTime fechaHoraInicio,
                                     Empleado empleado) {
        this.documentacionNumero = documentacionNumero;
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
