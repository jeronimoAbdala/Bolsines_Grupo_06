package main.Domain.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Remito: solo tiene atributo estado, sin historial (no existe CambioEstadoRemito en el diagrama).
 */
@Entity
@Table(name = "remito")
public class Remito implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clave natural asignada: la asigna el sistema (obtenerSiguienteNumeroRemito). */
    @Id
    @Column(name = "numero")
    private int numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_origen_codigo", nullable = false)
    private ComisionMedica cmOrigen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_destino_codigo", nullable = false)
    private ComisionMedica cmDestino;

    /**
     * Composicion Remito -> DetalleRemito (unidireccional). ALL: los detalles viven y mueren con el remito.
     * La FK remito_numero queda en la tabla detalle_remito. LAZY (default de @OneToMany).
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "remito_numero", nullable = false)
    @OrderBy("id")
    private List<DetalleRemito> detalles = new ArrayList<>();

    /** Requerido por JPA. */
    protected Remito() {
    }

    public Remito(int numero, LocalDate fecha, Estado estado,
                  ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        this.numero = numero;
        this.fecha = fecha;
        this.estado = estado;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
    }

    public void agregarDetalle(DetalleRemito detalle) {
        detalles.add(detalle);
    }

    public List<Documentacion> buscarDocumentacion() {
        List<Documentacion> docs = new ArrayList<>();
        for (DetalleRemito detalle : detalles) {
            docs.add(detalle.getDocumentacion());
        }
        return docs;
    }

    /**
     * Paso 11 del CU 28: primero setEstado() del remito (sin historial: no hay CambioEstadoRemito)
     * y despues *aceptarDocumentacion() en cada detalle.
     */
    public void aceptar(Estado estadoRecibidoYAceptado, Estado estadoRecibidaYAceptada,
                        Empleado empleado, LocalDateTime fechaHora) {
        setEstado(estadoRecibidoYAceptado);
        for (DetalleRemito detalle : detalles) {
            detalle.aceptarDocumentacion(estadoRecibidaYAceptada, empleado, fechaHora);
        }
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ComisionMedica getCmOrigen() {
        return cmOrigen;
    }

    public ComisionMedica getCmDestino() {
        return cmDestino;
    }

    public List<DetalleRemito> getDetalles() {
        return detalles;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
