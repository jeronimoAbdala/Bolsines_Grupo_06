package main.Gestores;

import main.Domain.Entities.*;
import main.Dto.BolsinDto;
import main.Dto.BolsinEnviadoDto;
import main.Dto.ComisionMedicaDto;
import main.Dto.RecepcionRegistradaDto;
import main.Dto.UsuarioDto;
import main.Repositories.BolsinRepository;
import main.Repositories.EmpleadoRepository;
import main.Repositories.EstadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Una sola instancia (Spring la crea y le inyecta los repositorios y el Gestor del CU 29 por constructor). */
@Service
public class GestorRecepcionBolsin {

    private final BolsinRepository bolsinRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EstadoRepository estadoRepository;
    private final GestorNotificarRecepcionBolsin gestorNotificarRecepcionBolsin;

    private Sesion sesionActual;
    private Empleado empleadoLogueado;

    /** Datos que el CU lee de la base, una sola vez al iniciar (opRegistrarRecepcionBolsin); despues todo es en memoria. */
    private List<Empleado> empleados = new ArrayList<>();
    private List<Bolsin> bolsines = new ArrayList<>();
    private List<Estado> estados = new ArrayList<>();

    private List<Bolsin> bolsinesSeleccionados = new ArrayList<>();
    /** Datos de cada bolsin de bolsinesSeleccionados para la Pantalla (paso 4), sin entidades. */
    private List<BolsinEnviadoDto> bolsinesEnviados = new ArrayList<>();
    private String informacionRemito;
    private int opcionDeRecepcion;
    private LocalDateTime fechaHoraActual;
    private Bolsin bolsinSeleccionado;

    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoYAceptado;
    private Estado estadoRecibidaYAceptada;
    /** Resultado del CU 29 (llamarCU29()). */
    private boolean notificacionEnviada;

    public GestorRecepcionBolsin(BolsinRepository bolsinRepository,
                                 EmpleadoRepository empleadoRepository,
                                 EstadoRepository estadoRepository,
                                 GestorNotificarRecepcionBolsin gestorNotificarRecepcionBolsin) {
        this.bolsinRepository = bolsinRepository;
        this.empleadoRepository = empleadoRepository;
        this.estadoRepository = estadoRepository;
        this.gestorNotificarRecepcionBolsin = gestorNotificarRecepcionBolsin;
    }

    /** No hay login: la Sesion actual se crea al iniciar la aplicacion y se registra aca. */
    public void setSesionActual(Sesion sesionActual) {
        this.sesionActual = sesionActual;
    }

    // opRegistrarRecepcionBolsin() — pasos 2 y 3
    public void opRegistrarRecepcionBolsin() {
        cargarDatos();
        buscarUsuarioLogueado();
        if (empleadoLogueado == null) return;
        buscarBolsinesEnviados(empleadoLogueado.getCM());   // getCM() — paso 2
    }

    // Unicas lecturas de la base del CU: empleados, bolsines y estados; despues todo es en memoria.
    private void cargarDatos() {
        this.empleados = empleadoRepository.obtenerTodos();
        this.bolsines = bolsinRepository.obtenerTodos();
        this.estados = estadoRepository.obtenerTodos();
    }

    /**
     * Paso 2 del CU 28: le pide getUsuario() a la Sesion actual y recorre los empleados
     * preguntando *esTuUsuario(usuario). El encontrado es el empleado logueado.
     */
    public void buscarUsuarioLogueado() {
        this.empleadoLogueado = null;
        if (sesionActual == null) return;
        Usuario usuario = sesionActual.getUsuario();
        for (Empleado empleado : empleados) {
            if (empleado.esTuUsuario(usuario)) {
                this.empleadoLogueado = empleado;
                break;
            }
        }
    }

    // buscarBolsinesEnviados() — paso 3: *sosEnviado(), *esTuCM(); getCMOrigen() y getNroPrecinto() — paso 4.
    public void buscarBolsinesEnviados(String cm) {
        this.bolsinesSeleccionados = new ArrayList<>();
        this.bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : bolsines) {
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
    public RecepcionRegistradaDto tomarConfirmacion() {
        if (bolsinSeleccionado == null || opcionDeRecepcion != 1) return null;
        obtenerEstadoRecibidoEnCMDestino();
        obtenerEstadoRecibidoYAceptado();
        obtenerEstadoRecibidaYAceptada();
        getFechaYHoraActual();
        bolsinSeleccionado.recibir(estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                estadoRecibidaYAceptada, empleadoLogueado, fechaHoraActual);
        bolsinRepository.guardar(bolsinSeleccionado);   // unica escritura del CU
        llamarCU29();
        RecepcionRegistradaDto recepcion = new RecepcionRegistradaDto(BolsinDto.de(bolsinSeleccionado), notificacionEnviada);
        finCU();
        return recepcion;
    }

    /** Recorre los estados cargados al iniciar el CU preguntando *esAmbitoBolsin() y esEstadoRecibidoEnCMDestino(). */
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

    /** Recorre los estados cargados al iniciar el CU preguntando *esAmbitoRemito() y esEstadoRecibidoYAceptado(). */
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

    /** Recorre los estados cargados al iniciar el CU preguntando *esAmbitoDocumentacion() y esEstadoRecibidaYAceptada(). */
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

    // llamarCU29() — paso 12: <<include>> del CU 29 Notificar recepcion de bolsin.
    public void llamarCU29() {
        this.notificacionEnviada = gestorNotificarRecepcionBolsin.notificarRecepcion(bolsinSeleccionado);
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
