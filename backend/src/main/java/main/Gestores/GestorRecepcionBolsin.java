package main.Gestores;

import main.Domain.Entities.*;
import main.Domain.Repositories.BolsinRepository;
import main.Domain.Repositories.EstadoRepository;
import main.Domain.Repositories.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Control del CU 28 - Registrar recepcion de bolsin.
 * Cada metodo corresponde a un mensaje del diagrama de secuencia; el gestor coordina
 * y delega en las entidades (Sesion, Empleado, Bolsin, Estado).
 */
public class GestorRecepcionBolsin {

    public static final int OPCION_RECIBIR = 1;

    private final Sesion actualSesion;
    private final BolsinRepository bolsinRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoRepository estadoRepository;

    private Empleado empleadoLogueado;
    private ComisionMedica cmLogueado;
    private List<Bolsin> bolsinesSeleccionados;
    private Bolsin bolsinSeleccionado;
    private List<String> informacionRemito;
    private int opcionDeRecepcion;
    private boolean opcionSeleccionada;
    private LocalDateTime fechaHoraActual;

    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoYAceptado;
    private Estado estadoRecibidaYAceptada;

    public GestorRecepcionBolsin(Sesion actualSesion,
                                 BolsinRepository bolsinRepository,
                                 UsuarioRepository usuarioRepository,
                                 EstadoRepository estadoRepository) {
        this.actualSesion = actualSesion;
        this.bolsinRepository = bolsinRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoRepository = estadoRepository;
    }

    public Empleado opRegistrarRecepcionBolsin() {
        buscarUsuarioLogueado();
        getCM();
        return empleadoLogueado;
    }

    // getUsuario() a la sesion y *esTuUsuario() a cada empleado
    private void buscarUsuarioLogueado() {
        Usuario usuario = actualSesion.getUsuario();
        this.empleadoLogueado = null;
        for (Empleado empleado : usuarioRepository.obtenerEmpleados()) {
            if (empleado.esTuUsuario(usuario)) {
                this.empleadoLogueado = empleado;
                return;
            }
        }
    }

    private ComisionMedica getCM() {
        this.cmLogueado = empleadoLogueado != null ? empleadoLogueado.getCM() : null;
        return cmLogueado;
    }

    // loop mientras haya bolsines: sosEnviado() y esTuCM()
    public List<Bolsin> buscarBolsinesEnviados() {
        if (cmLogueado == null) {
            opRegistrarRecepcionBolsin();
        }
        this.bolsinesSeleccionados = new ArrayList<>();
        if (cmLogueado == null) {
            return bolsinesSeleccionados;
        }
        for (Bolsin bolsin : bolsinRepository.obtenerTodos()) {
            if (bolsin.sosEnviado() && bolsin.esTuCM(cmLogueado)) {
                bolsinesSeleccionados.add(bolsin);
            }
        }
        return bolsinesSeleccionados;
    }

    /** Toma el bolsin elegido entre los enviados. Devuelve null si no esta pendiente. */
    public Bolsin tomarSeleccionBolsin(int numeroBolsin) {
        this.bolsinSeleccionado = null;
        for (Bolsin bolsin : buscarBolsinesEnviados()) {
            if (bolsin.esTuNumero(numeroBolsin)) {
                this.bolsinSeleccionado = bolsin;
            }
        }
        return bolsinSeleccionado;
    }

    public List<String> obtenerInformacionRemito() {
        if (bolsinSeleccionado == null) return List.of();
        this.informacionRemito = bolsinSeleccionado.obtenerInformacionRemito();
        return informacionRemito;
    }

    public void tomarOpcionDeRecepcion(int opcion) {
        this.opcionDeRecepcion = opcion;
    }

    /** Con la confirmacion busca los estados, toma la fecha y registra la recepcion. Devuelve el bolsin recibido. */
    public Bolsin tomarConfirmacion(boolean confirmado) {
        this.opcionSeleccionada = confirmado;
        Bolsin recibido = bolsinSeleccionado;
        if (!opcionSeleccionada || opcionDeRecepcion != OPCION_RECIBIR || recibido == null) {
            return null;
        }

        obtenerEstadoRecibidoEnCMDestino();
        obtenerEstadoRecibidoYAceptado();
        obtenerEstadoRecibidaYAceptada();
        getFechaYHoraActual();
        registrarRecepcionBolsin();
        llamarCU29();
        finCU();
        return recibido;
    }

    private void obtenerEstadoRecibidoEnCMDestino() {
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoBolsin() && estado.esEstadoRecibidoEnCMDestino()) {
                this.estadoRecibidoEnCMDestino = estado;
            }
        }
    }

    private void obtenerEstadoRecibidoYAceptado() {
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoRemito() && estado.esEstadoRecibidoYAceptado()) {
                this.estadoRecibidoYAceptado = estado;
            }
        }
    }

    private void obtenerEstadoRecibidaYAceptada() {
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoDocumentacion() && estado.esEstadoRecibidaYAceptada()) {
                this.estadoRecibidaYAceptada = estado;
            }
        }
    }

    private void getFechaYHoraActual() {
        this.fechaHoraActual = LocalDateTime.now();
    }

    // El bolsin cambia su estado y delega en sus remitos, que delegan en su documentacion
    private void registrarRecepcionBolsin() {
        bolsinSeleccionado.mostrarRecibido(estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                estadoRecibidaYAceptada, fechaHoraActual, empleadoLogueado);
        bolsinRepository.guardar(bolsinSeleccionado);
    }

    // <<include>> CU 29 - Notificar recepcion de bolsin (fuera del alcance de esta entrega)
    private void llamarCU29() {
    }

    private void finCU() {
        this.bolsinSeleccionado = null;
        this.informacionRemito = null;
        this.opcionDeRecepcion = 0;
        this.opcionSeleccionada = false;
    }
}
