package main.Domain.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clave natural numero del remito + numero de la documentacion (DetalleRemitoId): una documentacion figura
 * una sola vez en cada remito. Las dos son columnas FK, como en el Laboreo de la catedra.
 */
@Entity
@Table(name = "detalle_remito")
@IdClass(DetalleRemitoId.class)
public class DetalleRemito implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Columna FK hacia el remito, como columna de la clave: la escribe el detalle (no la lista del Remito). */
    @Id
    @Column(name = "remito_numero", nullable = false)
    private int remitoNumero;

    /** Columna FK hacia la documentacion, como columna de la clave: la escribe esta columna, no la @ManyToOne. */
    @Id
    @Column(name = "documentacion_numero", nullable = false)
    private int documentacionNumero;

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
    @JoinColumn(name = "documentacion_numero", nullable = false, insertable = false, updatable = false)
    private Documentacion documentacion;

    /** Requerido por JPA. */
    protected DetalleRemito() {
    }

    public DetalleRemito(int remitoNumero, Documentacion documentacion, String areaCMDestino) {
        this.remitoNumero = remitoNumero;
        this.documentacionNumero = documentacion.getNumero();
        this.documentacion = documentacion;
        this.areaCMDestino = areaCMDestino;
    }

    public DetalleRemito(int remitoNumero, Documentacion documentacion) {
        this(remitoNumero, documentacion, "");
    }

    public String getAreaCMDestino() {
        return areaCMDestino;
    }

    /**
     * Devuelve el objeto Documentacion (lo usa RemitoDto para el JSON). No es el getDocumentacion()
     * del diagrama: ese devuelve un String con los datos de la documentacion (paso 6).
     */
    public Documentacion getObjetoDocumentacion() {
        return documentacion;
    }

    /**
     * Paso 6 del CU 28 (getDocumentacion(): String del diagrama): le pide a su Documentacion
     * getAsunto() y mostrarTipoDocumentacion() (que le pide getNombre() a su TipoDocumento)
     * y devuelve esos datos. Asi el Remito nunca le habla a la Documentacion.
     */
    public String getDocumentacion() {
        return "Asunto: " + documentacion.getAsunto()
                + " | Tipo: " + documentacion.mostrarTipoDocumentacion();
    }

    /** Paso 11 del CU 28: delega en la Documentacion su transicion a RecibidaYAceptada. */
    public void aceptarDocumentacion(Estado estadoRecibidaYAceptada, Empleado empleado, LocalDateTime fechaHora) {
        documentacion.esRecibidaYAceptada(estadoRecibidaYAceptada, fechaHora, empleado);
    }
}
