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
@Table(name = "bolsin")
public class Bolsin implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clave natural asignada: la asigna el sistema (obtenerSiguienteNumeroBolsin). */
    @Id
    @Column(name = "numero")
    private int numero;

    @Column(name = "fecha_generacion")
    private LocalDate fechaGeneracion;

    @Column(name = "nro_precinto")
    private String nroPrecinto;

    @Column(name = "peso")
    private double peso;

    /** Estado actual (atributo). Multiplicidad 1. Sin cascada: Estado es un catalogo. */
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
     * Composicion Bolsin -> Remito (unidireccional: el remito no conoce a su bolsin).
     * ALL: los remitos viven y mueren con el bolsin. LAZY (default de @OneToMany).
     * La FK bolsin_numero queda en la tabla remito; NOT NULL porque es composicion (un remito
     * siempre pertenece a un bolsin).
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bolsin_numero", nullable = false)
    @OrderBy("numero")
    private List<Remito> remitos = new ArrayList<>();

    /**
     * Historial de cambios de estado (unidireccional: el cambio no conoce a su bolsin).
     * ALL: el historial vive y muere con el bolsin. LAZY (default de @OneToMany).
     * La FK bolsin_numero queda en la tabla cambio_estado_bolsin.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bolsin_numero", nullable = false)
    @OrderBy("fechaHoraInicio")
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();

    /** Requerido por JPA. */
    protected Bolsin() {
    }

    public Bolsin(int numero,
                  ComisionMedica cmOrigen,
                  ComisionMedica cmDestino,
                  Estado estado) {
        this.numero = numero;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.estado = estado;
        this.fechaGeneracion = LocalDate.now();
    }

    /** Igual al anterior, pero con la fecha de generacion dada (la usan los datos de prueba). */
    public Bolsin(int numero,
                  ComisionMedica cmOrigen,
                  ComisionMedica cmDestino,
                  Estado estado,
                  LocalDate fechaGeneracion) {
        this.numero = numero;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
    }

    public void agregarRemito(Remito remito) {
        remitos.add(remito);
    }

    public void agregarCambioEstado(CambioEstadoBolsin cambio) {
        cambiosEstado.add(cambio);
    }

    /**
     * Paso 3 del CU 28 (por historial): busca el cambio de estado actual (*sosActual)
     * y le pregunta sosEnviado(); ese cambio se lo pregunta a su Estado (esEnviado).
     */
    public boolean sosEnviado() {
        for (CambioEstadoBolsin cambio : cambiosEstado) {
            if (cambio.sosActual()) {
                return cambio.sosEnviado();
            }
        }
        return false;
    }

    /** Paso 3 del CU 28: compara por nombre la CM destino con la CM del empleado logueado. */
    public boolean esTuCM(String nombreCM) {
        return cmDestino != null && cmDestino.getNombre().equals(nombreCM);
    }

    public boolean esTuNumero(int numero) {
        return this.numero == numero;
    }

    /**
     * Paso 11 del CU 28 (antes mostrarRecibido). Orden del diagrama de secuencia:
     * PRIMERO *aceptar() en cada remito; DESPUES el bolsin: setEstado(),
     * *sosActual() -> setFechaHoraFin() al actual, crearNuevoCE().
     */
    public void recibir(Estado estadoRecibidoEnCMDestino, Estado estadoRecibidoYAceptado,
                        Estado estadoRecibidaYAceptada, Empleado empleado, LocalDateTime fechaHora) {
        for (Remito remito : remitos) {
            remito.aceptar(estadoRecibidoYAceptado, estadoRecibidaYAceptada, empleado, fechaHora);
        }
        setEstado(estadoRecibidoEnCMDestino);
        for (CambioEstadoBolsin cambio : cambiosEstado) {
            if (cambio.sosActual()) {
                cambio.setFechaHoraFin(fechaHora);
            }
        }
        crearNuevoCE(estadoRecibidoEnCMDestino, fechaHora, empleado);
    }

    public String obtenerInformacionRemito() {
        StringBuilder info = new StringBuilder();
        for (Remito remito : remitos) {
            info.append("Remito #").append(remito.getNumero()).append("\n");
        }
        return info.toString();
    }

    public CambioEstadoBolsin crearNuevoCE(Estado estado, LocalDateTime fechaHora, Empleado empleado) {
        CambioEstadoBolsin cambio = new CambioEstadoBolsin(estado, fechaHora, empleado);
        agregarCambioEstado(cambio);
        return cambio;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public String getNroPrecinto() {
        return nroPrecinto;
    }

    public void setNroPrecinto(String nroPrecinto) {
        this.nroPrecinto = nroPrecinto;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public ComisionMedica getCmOrigen() {
        return cmOrigen;
    }

    public ComisionMedica getCmDestino() {
        return cmDestino;
    }

    public List<Remito> getRemitos() {
        return remitos;
    }

    public List<CambioEstadoBolsin> getCambiosEstado() {
        return cambiosEstado;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
