package main.Domain.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_remito")
public class DetalleRemito implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_cm_destino")
    private String areaCMDestino;

    /**
     * Asociacion (no composicion) con la Documentacion: la misma documentacion puede figurar en
     * detalles de distintos remitos (por ejemplo al redirigirse), por eso @ManyToOne.
     * PERSIST: al guardar un detalle nuevo con una documentacion nueva, se inserta la documentacion.
     * MERGE: al guardar (merge) el bolsin/remito, los cambios llegan a la documentacion y su historial.
     * Sin REMOVE: borrar un detalle no borra la documentacion.
     */
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "documentacion_numero", nullable = false)
    private Documentacion documentacion;

    /** Requerido por JPA. */
    protected DetalleRemito() {
    }

    public DetalleRemito(Documentacion documentacion, String areaCMDestino) {
        this.documentacion = documentacion;
        this.areaCMDestino = areaCMDestino;
    }

    public DetalleRemito(Documentacion documentacion) {
        this(documentacion, "");
    }

    public String getAreaCMDestino() {
        return areaCMDestino;
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }

    /** Paso 11 del CU 28: delega en la Documentacion su transicion a RecibidaYAceptada. */
    public void aceptarDocumentacion(Estado estadoRecibidaYAceptada, Empleado empleado, LocalDateTime fechaHora) {
        documentacion.esRecibidaYAceptada(estadoRecibidaYAceptada, fechaHora, empleado);
    }
}
