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

@Entity
@Table(name = "documentacion")
public class Documentacion implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clave natural asignada: la asigna el sistema (obtenerSiguienteNumeroDocumentacion). */
    @Id
    @Column(name = "numero")
    private int numero;

    @Column(name = "fecha_pase")
    private LocalDate fechaPase;

    @Column(name = "asunto", nullable = false)
    private String asunto;

    @Column(name = "descripcion")
    private String descripcion;

    /** Estado actual (atributo). Multiplicidad 1. Sin cascada: Estado es un catalogo. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    /** Multiplicidad 1. Sin cascada: TipoDocumento es un catalogo. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipoDocumento;

    /**
     * Historial de cambios de estado (unidireccional: el cambio no conoce a su documentacion).
     * ALL: el historial vive y muere con la documentacion. LAZY (default de @OneToMany).
     * La FK documentacion_numero queda en la tabla cambio_estado_documentacion.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "documentacion_numero", nullable = false)
    @OrderBy("fechaHoraInicio")
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    /** Requerido por JPA. */
    protected Documentacion() {
    }

    public Documentacion(int numero, String asunto, String descripcion,
                         Estado estado, TipoDocumento tipoDocumento) {
        this.numero = numero;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.estado = estado;
        this.tipoDocumento = tipoDocumento;
        this.fechaPase = LocalDate.now();
    }

    /** Igual al anterior, pero con la fecha de pase dada (la usan los datos de prueba). */
    public Documentacion(int numero, String asunto, String descripcion,
                         Estado estado, TipoDocumento tipoDocumento, LocalDate fechaPase) {
        this.numero = numero;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.estado = estado;
        this.tipoDocumento = tipoDocumento;
        this.fechaPase = fechaPase;
    }

    public void crear() {
        this.fechaPase = LocalDate.now();
    }

    // darBaja, cancelarRemito, rechazar, marcarNoRecibida, devolverAOrigen: de otros CU, no se usan.
    // Solo reciben el Estado por parametro en vez de crearlo: las entidades no crean estados.

    public void darBaja(Estado estado) {
        this.estado = estado;
    }

    public void cancelarRemito(Estado estado) {
        this.estado = estado;
    }

    public void crearRemito() {
    }

    public void eliminarDeBolsin() {
    }

    public void incorporarEnBolsin() {
    }

    public void quitarDeBolsin() {
    }

    /** Por historial, igual que Bolsin: el cambio de estado actual le pregunta a su Estado. */
    public boolean sosEnviado() {
        for (CambioEstadoDocumentacion cambio : cambiosEstado) {
            if (cambio.sosActual()) {
                return cambio.sosEnviado();
            }
        }
        return false;
    }

    public void redireccionar() {
    }

    /**
     * Paso 11 del CU 28: transicion a RecibidaYAceptada (nombre de la transicion en la DME).
     * setEstado(), *sosActual() -> setFechaHoraFin() al actual, crearNuevoCE().
     */
    public void esRecibidaYAceptada(Estado estadoRecibidaYAceptada, LocalDateTime fechaHora, Empleado empleado) {
        setEstado(estadoRecibidaYAceptada);
        for (CambioEstadoDocumentacion cambio : cambiosEstado) {
            if (cambio.sosActual()) {
                cambio.setFechaHoraFin(fechaHora);
            }
        }
        crearNuevoCE(estadoRecibidaYAceptada, fechaHora, empleado);
    }

    public CambioEstadoDocumentacion crearNuevoCE(Estado estado, LocalDateTime fechaHora, Empleado empleado) {
        CambioEstadoDocumentacion cambio = new CambioEstadoDocumentacion(estado, fechaHora, empleado);
        agregarCambioEstado(cambio);
        return cambio;
    }

    public void rechazar(Estado estado) {
        this.estado = estado;
    }

    public void marcarNoRecibida(Estado estado) {
        this.estado = estado;
    }

    public void devolverAOrigen(Estado estado) {
        this.estado = estado;
    }

    public void vincularNuevamente() {
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambio) {
        cambiosEstado.add(cambio);
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaPase() {
        return fechaPase;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String mostrarTipoDocumentacion() {
        return tipoDocumento != null ? tipoDocumento.getNombre() : "";
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public List<CambioEstadoDocumentacion> getCambiosEstado() {
        return cambiosEstado;
    }
}
