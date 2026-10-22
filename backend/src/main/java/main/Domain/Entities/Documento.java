package main.Domain.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Documento implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private LocalDate fechaPase;
    private final String asunto;
    private String descripcion;

    private Estado estado;
    private TipoDocumento tipoDocumento;
    private ArrayList<CambioEstadoDocumentacion> cambiosEstado;

    public Documento(int numero, String asunto, String descripcion,
                     Estado estado, TipoDocumento tipoDocumento) {
        this.numero = numero;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.estado = estado;
        this.tipoDocumento = tipoDocumento;
        this.fechaPase = LocalDate.now();
        this.cambiosEstado = new ArrayList<>();
    }

    public void crear() {
        this.fechaPase = LocalDate.now();
    }

    public void darBaja() {
        this.estado = new Estado("DOC", "BAJA", "Documentación dada de baja");
    }

    public void cancelarRemito() {
        this.estado = new Estado("DOC", "CANCELADA", "Remito cancelado");
    }

    public void crearRemito() {
    }

    public void eliminarDeBolsin() {
    }

    public void incorporarEnBolsin() {
    }

    public void quitarDeBolsin() {
    }

    public boolean sosEnviado() {
        return estado != null && estado.esEnviado() && estado.esAmbitoDocumentacion();
    }

    public void redireccionar() {
    }

    public boolean esRecibidaYAceptada() {
        return estado != null && estado.esEstadoRecibidaYAceptada();
    }

    public void rechazar() {
        this.estado = new Estado("DOC", "RECHAZADA", "Documentación rechazada");
    }

    public void marcarNoRecibida() {
        this.estado = new Estado("DOC", "NO_RECIBIDA", "Documentación no recibida");
    }

    public void devolverAOrigen() {
        this.estado = new Estado("DOC", "DEVUELTA", "Documentación devuelta a origen");
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

    public ArrayList<CambioEstadoDocumentacion> getCambiosEstado() {
        return cambiosEstado;
    }
}
