package main.Domain.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import main.Domain.Iterator.IAgregado;
import main.Domain.Iterator.IIterador;
import main.Domain.Iterator.IteradorDetalle;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Remito: solo tiene atributo estado, sin historial (no existe CambioEstadoRemito en el diagrama).
 * Patron Iterator - rol ConcreteAggregate: crea un IteradorDetalle y con el recorre sus detalles (paso 6).
 */
@Entity
@Table(name = "remito")
public class Remito implements Serializable, IAgregado {

    private static final long serialVersionUID = 1L;

    /** Clave natural asignada (no autogenerada): la ponen los datos de prueba (CargadorDatosDePrueba). */
    @Id
    @Column(name = "numero")
    private int numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "estado_ambito", referencedColumnName = "ambito", nullable = false),
            @JoinColumn(name = "estado_nombre", referencedColumnName = "nombre", nullable = false)
    })
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
     * Es parte de la clave del detalle y la escribe el detalle, por eso la lista no la inserta ni la actualiza.
     * El atributo se llama como en el diagrama de clases (detalleRemito); la columna no depende
     * de ese nombre: la fija @JoinColumn.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "remito_numero", insertable = false, updatable = false)
    @OrderBy("documentacionNumero")
    private List<DetalleRemito> detalleRemito = new ArrayList<>();

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
        detalleRemito.add(detalle);
    }

    /** crearIterador(elementos: Object[]): IIterador del diagrama: el Remito crea su iterador concreto. */
    @Override
    public IIterador crearIterador(Object[] elementos) {
        return new IteradorDetalle(elementos);
    }

    /**
     * Paso 6 del CU 28 (loop Detalles de la secuencia): recorre sus detalles con un IteradorDetalle
     * y a cada uno le pide getDocumentacion(). Una linea por documentacion, con sangria de 2 espacios.
     * Sin detalles devuelve "".
     */
    public String buscarDocumentacion() {
        StringBuilder documentacion = new StringBuilder();
        IIterador iterador = crearIterador(detalleRemito.toArray());
        iterador.primero();
        while (!iterador.haFinalizado()) {
            DetalleRemito detalle = (DetalleRemito) iterador.elementoActual();
            documentacion.append("  ").append(detalle.getDocumentacion()).append("\n");
            iterador.siguiente();
        }
        return documentacion.toString();
    }

    /**
     * Paso 11 del CU 28: primero setEstado() del remito (sin historial: no hay CambioEstadoRemito)
     * y despues *aceptarDocumentacion() en cada detalle.
     */
    public void aceptar(Estado estadoRecibidoYAceptado, Estado estadoRecibidaYAceptada,
                        Empleado empleado, LocalDateTime fechaHora) {
        setEstado(estadoRecibidoYAceptado);
        for (DetalleRemito detalle : detalleRemito) {
            detalle.aceptarDocumentacion(estadoRecibidaYAceptada, empleado, fechaHora);
        }
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ComisionMedica getCMOrigen() {
        return cmOrigen;
    }

    public ComisionMedica getCMDestino() {
        return cmDestino;
    }

    public List<DetalleRemito> getDetalleRemito() {
        return detalleRemito;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
