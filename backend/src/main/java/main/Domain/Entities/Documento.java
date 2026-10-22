package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Documento implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private LocalDate fechaPase;
    private final String asunto;
    private String descripcion;

    private TipoDocumento tipoDocumento;
    // El estado de la documentacion no es un atributo: es el del cambio de estado actual
    private ArrayList<CambioEstadoDocumentacion> cambiosEstado;

    public Documento(int numero, String asunto, String descripcion, TipoDocumento tipoDocumento) {
        this.numero = numero;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.tipoDocumento = tipoDocumento;
        this.fechaPase = LocalDate.now();
        this.cambiosEstado = new ArrayList<>();
    }

    // Metodos del diagrama de clases que pertenecen a otros casos de uso (sin implementar en el CU 28)
    public void crear() {
    }

    public void darBaja() {
    }

    public void cancelarRemito() {
    }

    public void crearRemito() {
    }

    public void eliminarDeBolsin() {
    }

    public void incorporarEnBolsin() {
    }

    public void quitarDeBolsin() {
    }

    public void redireccionar() {
    }

    public void rechazar() {
    }

    public void marcarNoRecibida() {
    }

    public void devolverAOrigen() {
    }

    public void vincularNuevamente() {
    }

    public boolean sosEnviado() {
        Estado estado = getEstadoActual();
        return estado != null && estado.esEnviado() && estado.esAmbitoDocumentacion();
    }

    public boolean esRecibidaYAceptada() {
        Estado estado = getEstadoActual();
        return estado != null && estado.esEstadoRecibidaYAceptada();
    }

    // aceptar() -> *sosActual() -> actual.setFechaHoraFin() -> crearNuevoCE() -> new CambioEstadoDocumentacion
    public void aceptar(Estado estadoRecibidaYAceptada, LocalDateTime fechaHoraActual, Empleado empleado) {
        CambioEstadoDocumentacion actual = buscarCEActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHoraActual);
        }
        crearNuevoCE(estadoRecibidaYAceptada, fechaHoraActual, empleado);
    }

    public CambioEstadoDocumentacion crearNuevoCE(Estado estado, LocalDateTime fechaHoraInicio, Empleado empleado) {
        CambioEstadoDocumentacion nuevo = new CambioEstadoDocumentacion(estado, fechaHoraInicio, empleado);
        agregarCambioEstado(nuevo);
        return nuevo;
    }

    private CambioEstadoDocumentacion buscarCEActual() {
        for (CambioEstadoDocumentacion cambio : cambiosEstado) {
            if (cambio.sosActual()) {
                return cambio;
            }
        }
        return null;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambio) {
        cambiosEstado.add(cambio);
    }

    public Estado getEstadoActual() {
        CambioEstadoDocumentacion actual = buscarCEActual();
        return actual != null ? actual.getEstado() : null;
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

    public ArrayList<CambioEstadoDocumentacion> getCambiosEstado() {
        return cambiosEstado;
    }
}
