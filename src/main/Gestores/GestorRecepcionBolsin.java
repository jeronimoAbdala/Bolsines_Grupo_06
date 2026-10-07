package main.Gestores;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.DetalleRemito;
import main.Domain.Entities.Documentation;
import main.Domain.Entities.Empleado;
import main.Domain.Entities.Estado;
import main.Domain.Entities.Remito;
import main.Domain.Entities.TipoDocumento;
import main.Domain.Entities.Usuario;
import main.Domain.Repositories.BolsinRepository;
import main.Domain.Repositories.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestorRecepcionBolsin {

    private final String nombreUsuarioLogueado;
    private final BolsinRepository bolsinRepository;
    private final UsuarioRepository usuarioRepository;

    private ArrayList<Bolsin> bolsinesSeleccionados;
    private String informacionRemito;
    private int opcionDeRecepcion;
    private LocalDateTime fechaHoraActual;
    private int opcionSeleccionada;
    private Bolsin bolsinSeleccionado;

    private final Estado estadoRecibidoEnCMDestino = new Estado("BOL", "RECIBIDO", "Bolsín recibido en comisión destino");
    private final Estado estadoRecibidoYAceptado = new Estado("REM", "RECIBIDO_ACEPTADO", "Remito recibido y aceptado");
    private final Estado estadoRecibidaYAceptada = new Estado("DOC", "RECIBIDA_ACEPTADA", "Documentación recibida y aceptada");

    public GestorRecepcionBolsin(String nombreUsuarioLogueado,
                                 BolsinRepository bolsinRepository,
                                 UsuarioRepository usuarioRepository) {
        this.nombreUsuarioLogueado = nombreUsuarioLogueado;
        this.bolsinRepository = bolsinRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void opRegistrarRecepcionBolsin() {
        getFechaYHoraActual();
    }

    public Usuario buscarUsuarioLogueado() {
        return usuarioRepository.buscarPorNombre(nombreUsuarioLogueado);
    }

    public ComisionMedica getCM() {
        return buscarUsuarioLogueado().getEmpleado().getComisionMedica();
    }

    public List<Bolsin> buscarBolsinesEnviados() {
        return bolsinRepository.obtenerEnviadosHacia(getCM());
    }

    public void tomarSeleccionBolsin(int numeroBolsin) {
        this.bolsinSeleccionado = bolsinRepository.buscarPorNumero(numeroBolsin);
    }

    public String obtenerInformacionRemito() {
        if (bolsinSeleccionado == null) return "";
        StringBuilder info = new StringBuilder();
        info.append("Origen: ").append(bolsinSeleccionado.getCmOrigen().getNombre()).append("\n");
        info.append("Precinto: ").append(bolsinSeleccionado.getNroPrecinto() != null
                ? bolsinSeleccionado.getNroPrecinto() : "N/A").append("\n");
        for (Remito remito : bolsinSeleccionado.getRemitos()) {
            info.append("Remito #").append(remito.getNumero()).append(" - ");
            info.append("Fecha: ").append(remito.getFecha()).append("\n");
            for (DetalleRemito detalle : remito.getDetalles()) {
                Documentation doc = detalle.getDocumentacion();
                info.append("  Doc #").append(doc.getNumero()).append(": ").append(doc.getAsunto()).append("\n");
            }
        }
        this.informacionRemito = info.toString();
        return informacionRemito;
    }

    public void tomarOpcionDeRecepcion(int opcion) {
        this.opcionDeRecepcion = opcion;
    }

    public void tomarConfirmacion(boolean confirmado) {
        this.opcionSeleccionada = confirmado ? 1 : 0;
    }

    public Estado obtenerEstadoRecibidoEnCMDestino() {
        return estadoRecibidoEnCMDestino;
    }

    public Estado obtenerEstadoRecibidoYAceptado() {
        return estadoRecibidoYAceptado;
    }

    public Estado obtenerEstadoRecibidaYAceptada() {
        return estadoRecibidaYAceptada;
    }

    public LocalDateTime getFechaYHoraActual() {
        this.fechaHoraActual = LocalDateTime.now();
        return fechaHoraActual;
    }

    public void registrarRecepcionBolsin() {
        if (bolsinSeleccionado == null) return;
        bolsinSeleccionado.setEstado(estadoRecibidoEnCMDestino);
        bolsinSeleccionado.crearNuevoCEO(estadoRecibidoEnCMDestino, null);
        aceptarRemitos(bolsinSeleccionado);
        bolsinRepository.guardar(bolsinSeleccionado);
    }

    public void llamarCU290() {
    }

    public void finCU() {
        this.bolsinSeleccionado = null;
        this.informacionRemito = null;
    }

    public Bolsin getBolsinSeleccionado() {
        return bolsinSeleccionado;
    }

    public Bolsin buscarBolsinPorNumero(int numero) {
        return bolsinRepository.buscarPorNumero(numero);
    }

    public String getInformacionRemito() {
        return informacionRemito;
    }

    public List<ComisionMedica> obtenerComisionesMedicas() {
        return bolsinRepository.obtenerComisionesMedicas();
    }

    public void crearBolsin(int numero, String precinto, double peso, ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        bolsinRepository.crearBolsin(numero, precinto, peso, cmOrigen, cmDestino);
    }

    public int obtenerSiguienteNumeroBolsin() {
        return bolsinRepository.obtenerSiguienteNumeroBolsin();
    }

    public int obtenerSiguienteNumeroRemito() {
        return bolsinRepository.obtenerSiguienteNumeroRemito();
    }

    public int obtenerSiguienteNumeroDocumentacion() {
        return bolsinRepository.obtenerSiguienteNumeroDocumentacion();
    }

    public void crearRemitoConDocumentacion(int bolsinNumero, int remitoNumero,
                                            ComisionMedica cmOrigen, ComisionMedica cmDestino,
                                            String asunto, String descripcion, String tipoDoc) {
        Estado estadoRem = new Estado("REM", "ENVIADO", "Remito enviado");
        Estado estadoDoc = new Estado("DOC", "ENVIADA", "Documentación enviada");

        Remito remito = new Remito(remitoNumero, LocalDate.now(), estadoRem, cmOrigen, cmDestino);

        int docNumero = obtenerSiguienteNumeroDocumentacion();
        TipoDocumento tipo = new TipoDocumento(tipoDoc, "Tipo de documento: " + tipoDoc);
        Documentation doc = new Documentation(docNumero, asunto, descripcion, estadoDoc, tipo);

        remito.agregarDetalle(new DetalleRemito(doc));
        bolsinRepository.agregarRemitoABolsin(bolsinNumero, remito);
    }

    public void crearUsuario(String nombreUsuario, String password,
                             String nombreEmp, String apellidoEmp, String mailEmp, ComisionMedica cm) {
        Empleado emp = new Empleado(nombreEmp, apellidoEmp, mailEmp, cm);
        usuarioRepository.crearUsuario(nombreUsuario, password, emp);
    }

    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return usuarioRepository.crearComisionMedica(codigo, nombre, direccion, email, telefono);
    }

    public List<Bolsin> obtenerTodosBolsines() {
        return bolsinRepository.obtenerTodos();
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.obtenerTodos();
    }

    private void aceptarRemitos(Bolsin bolsin) {
        for (Remito remito : bolsin.getRemitos()) {
            remito.aceptar(null);
            aceptarDocumentacion(remito);
        }
    }

    private void aceptarDocumentacion(Remito remito) {
        for (DetalleRemito detalle : remito.getDetalles()) {
            detalle.aceptarDocumentacion(null);
        }
    }
}
