package main.Gestores;

import main.Domain.Entities.*;
import main.Dto.BolsinDto;
import main.Dto.BolsinEnviadoDto;
import main.Dto.ComisionMedicaDto;
import main.Dto.UsuarioDto;
import main.Repositories.BolsinRepository;
import main.Repositories.EmpleadoRepository;
import main.Repositories.EstadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Una sola instancia (Spring la crea y le inyecta los repositorios por constructor). */
@Service
public class GestorRecepcionBolsin {

    private final BolsinRepository bolsinRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EstadoRepository estadoRepository;

    private Sesion sesionActual;
    private Empleado empleadoLogueado;

    private List<Bolsin> bolsinesSeleccionados = new ArrayList<>();
    /** Datos de cada bolsin de bolsinesSeleccionados para la Pantalla (paso 4), sin entidades. */
    private List<BolsinEnviadoDto> bolsinesEnviados = new ArrayList<>();
    private String informacionRemito;
    private int opcionDeRecepcion;
    private LocalDateTime fechaHoraActual;
    private Bolsin bolsinSeleccionado;

    /** Estados persistidos: se traen una sola vez en tomarConfirmacion() y los recorren los tres obtenerEstado…(). */
    private List<Estado> estados = new ArrayList<>();
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoYAceptado;
    private Estado estadoRecibidaYAceptada;

    public GestorRecepcionBolsin(BolsinRepository bolsinRepository,
                                 EmpleadoRepository empleadoRepository,
                                 EstadoRepository estadoRepository) {
        this.bolsinRepository = bolsinRepository;
        this.empleadoRepository = empleadoRepository;
        this.estadoRepository = estadoRepository;
    }

    /** No hay login: la Sesion actual se crea al iniciar la aplicacion y se registra aca. */
    public void setSesionActual(Sesion sesionActual) {
        this.sesionActual = sesionActual;
    }

    // opRegistrarRecepcionBolsin() — pasos 2 y 3
    public void opRegistrarRecepcionBolsin() {
        buscarUsuarioLogueado();
        if (empleadoLogueado == null) return;
        buscarBolsinesEnviados(empleadoLogueado.getCM());   // getCM() — paso 2
    }

    /**
     * Paso 2 del CU 28: le pide getUsuario() a la Sesion actual y recorre los empleados
     * preguntando *esTuUsuario(usuario). El encontrado es el empleado logueado.
     */
    public void buscarUsuarioLogueado() {
        this.empleadoLogueado = null;
        if (sesionActual == null) return;
        Usuario usuario = sesionActual.getUsuario();
        for (Empleado empleado : empleadoRepository.obtenerTodos()) {
            if (empleado.esTuUsuario(usuario)) {
                this.empleadoLogueado = empleado;
                break;
            }
        }
    }

    // buscarBolsinesEnviados() — paso 3: *sosEnviado(), *esTuCM(); getCMOrigen() y getNroPrecinto() — paso 4.
    // Unica lectura de bolsines del CU: la seleccion y la confirmacion usan estos mismos objetos.
    public void buscarBolsinesEnviados(String cm) {
        this.bolsinesSeleccionados = new ArrayList<>();
        this.bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepository.obtenerTodos()) {
            if (bolsin.sosEnviado() && bolsin.esTuCM(cm)) {
                bolsinesSeleccionados.add(bolsin);
                bolsinesEnviados.add(new BolsinEnviadoDto(bolsin.getNumero(),
                        ComisionMedicaDto.de(bolsin.getCMOrigen()), bolsin.getNroPrecinto(),
                        bolsin.getRemitos().size(), bolsin.getEstado().getNombre()));
            }
        }
    }

    // tomarSeleccionBolsin() — paso 5: se elige entre los bolsines mostrados (con numero: decision P1)
    public void tomarSeleccionBolsin(int numeroBolsin) {
        this.bolsinSeleccionado = null;
        for (Bolsin bolsin : bolsinesSeleccionados) {
            if (bolsin.esTuNumero(numeroBolsin)) {
                this.bolsinSeleccionado = bolsin;
            }
        }
        obtenerInformacionRemito();
    }

    /**
     * Paso 6 del CU 28: el Gestor es cliente del agregado. Le delega al bolsin seleccionado
     * obtenerInformacionRemito() y guarda el resultado en informacionRemito; no conoce la coleccion
     * de remitos ni los iteradores (los crea y los usa el propio Bolsin, y cada Remito el suyo).
     */
    public void obtenerInformacionRemito() {
        if (bolsinSeleccionado == null) {
            this.informacionRemito = "";
            return;
        }
        this.informacionRemito = bolsinSeleccionado.obtenerInformacionRemito();
    }

    /**
     * Paso 8 del CU 28: opcion de recepcion elegida. Solo se implementa la opcion 1
     * ("el contenido del bolsin es igual al registrado"); las otras 3 son de otra tarea.
     */
    public void tomarOpcionDeRecepcion(int opcion) {
        this.opcionDeRecepcion = opcion;
    }

    // tomarConfirmacion() — pasos 10 y 11; devuelve los datos de informarRecepcion() (paso 12). Solo la opcion 1.
    public BolsinDto tomarConfirmacion() {
        if (bolsinSeleccionado == null || opcionDeRecepcion != 1) return null;
        this.estados = estadoRepository.obtenerTodos();   // una sola vez para los tres obtenerEstado…()
        obtenerEstadoRecibidoEnCMDestino();
        obtenerEstadoRecibidoYAceptado();
        obtenerEstadoRecibidaYAceptada();
        getFechaYHoraActual();
        bolsinSeleccionado.recibir(estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                estadoRecibidaYAceptada, empleadoLogueado, fechaHoraActual);
        bolsinRepository.guardar(bolsinSeleccionado);
        llamarCU29();
        BolsinDto recibido = BolsinDto.de(bolsinSeleccionado);
        finCU();
        return recibido;
    }

    /** Recorre los estados traidos en tomarConfirmacion() preguntando *esAmbitoBolsin() y esEstadoRecibidoEnCMDestino(). */
    public Estado obtenerEstadoRecibidoEnCMDestino() {
        this.estadoRecibidoEnCMDestino = null;
        for (Estado estado : estados) {
            if (estado.esAmbitoBolsin() && estado.esEstadoRecibidoEnCMDestino()) {
                this.estadoRecibidoEnCMDestino = estado;
                break;
            }
        }
        return estadoRecibidoEnCMDestino;
    }

    /** Recorre los estados traidos en tomarConfirmacion() preguntando *esAmbitoRemito() y esEstadoRecibidoYAceptado(). */
    public Estado obtenerEstadoRecibidoYAceptado() {
        this.estadoRecibidoYAceptado = null;
        for (Estado estado : estados) {
            if (estado.esAmbitoRemito() && estado.esEstadoRecibidoYAceptado()) {
                this.estadoRecibidoYAceptado = estado;
                break;
            }
        }
        return estadoRecibidoYAceptado;
    }

    /** Recorre los estados traidos en tomarConfirmacion() preguntando *esAmbitoDocumentacion() y esEstadoRecibidaYAceptada(). */
    public Estado obtenerEstadoRecibidaYAceptada() {
        this.estadoRecibidaYAceptada = null;
        for (Estado estado : estados) {
            if (estado.esAmbitoDocumentacion() && estado.esEstadoRecibidaYAceptada()) {
                this.estadoRecibidaYAceptada = estado;
                break;
            }
        }
        return estadoRecibidaYAceptada;
    }

    public LocalDateTime getFechaYHoraActual() {
        this.fechaHoraActual = LocalDateTime.now();
        return fechaHoraActual;
    }

    // llamarCU29() — paso 12: el CU 29 Notificar recepcion de bolsin no se implementa.
    public void llamarCU29() {
    }

    // finCU() — paso 12: tambien vacia las listas, asi un bolsin recibido no se puede volver a elegir.
    public void finCU() {
        this.bolsinSeleccionado = null;
        this.informacionRemito = null;
        this.bolsinesSeleccionados = new ArrayList<>();
        this.bolsinesEnviados = new ArrayList<>();
    }

    // --- Lo que la Pantalla lee del CU (siempre DTO: la Pantalla no toca entidades).

    /** Empleado logueado y su CM (paso 2); null si no hay. */
    public UsuarioDto getUsuarioLogueado() {
        return empleadoLogueado == null ? null : UsuarioDto.de(empleadoLogueado);
    }

    /** Datos de los bolsines enviados a la CM del empleado logueado (paso 4). */
    public List<BolsinEnviadoDto> getBolsinesEnviados() {
        return bolsinesEnviados;
    }

    /** Encabezado del bolsin seleccionado (paso 5); null si no hay. */
    public BolsinDto getDatosBolsinSeleccionado() {
        return bolsinSeleccionado == null ? null : BolsinDto.de(bolsinSeleccionado);
    }

    public String getInformacionRemito() {
        return informacionRemito;
    }
}
