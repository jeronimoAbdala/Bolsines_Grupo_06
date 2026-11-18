package main.Gestores;

import main.Domain.Entities.*;
import main.Repositories.BolsinRepository;
import main.Repositories.ComisionMedicaRepository;
import main.Repositories.EmpleadoRepository;
import main.Repositories.EstadoRepository;
import main.Repositories.TipoDocumentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Una sola instancia (Spring la crea y le inyecta los repositorios por constructor). */
@Service
public class GestorRecepcionBolsin {

    private final BolsinRepository bolsinRepository;
    private final ComisionMedicaRepository comisionMedicaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EstadoRepository estadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    private Sesion sesionActual;
    private Empleado empleadoLogueado;

    private List<Bolsin> bolsinesSeleccionados;
    private String informacionRemito;
    private int opcionDeRecepcion;
    private LocalDateTime fechaHoraActual;
    private int opcionSeleccionada;
    private Bolsin bolsinSeleccionado;

    /** Estados persistidos que obtiene el paso 11 recorriendo estadoRepository.findAll(). */
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoYAceptado;
    private Estado estadoRecibidaYAceptada;

    public GestorRecepcionBolsin(BolsinRepository bolsinRepository,
                                 ComisionMedicaRepository comisionMedicaRepository,
                                 EmpleadoRepository empleadoRepository,
                                 EstadoRepository estadoRepository,
                                 TipoDocumentoRepository tipoDocumentoRepository) {
        this.bolsinRepository = bolsinRepository;
        this.comisionMedicaRepository = comisionMedicaRepository;
        this.empleadoRepository = empleadoRepository;
        this.estadoRepository = estadoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    /** No hay login: la Sesion actual se crea al iniciar la aplicacion y se registra aca. */
    public void setSesionActual(Sesion sesionActual) {
        this.sesionActual = sesionActual;
    }

    public void opRegistrarRecepcionBolsin() {
        getFechaYHoraActual();
    }

    /**
     * Paso 2 del CU 28: le pide getUsuario() a la Sesion actual y recorre los empleados
     * preguntando *esTuUsuario(usuario). El encontrado es el empleado logueado.
     */
    public Empleado buscarUsuarioLogueado() {
        this.empleadoLogueado = null;
        if (sesionActual == null) return null;
        Usuario usuario = sesionActual.getUsuario();
        for (Empleado empleado : empleadoRepository.findAll()) {
            if (empleado.esTuUsuario(usuario)) {
                this.empleadoLogueado = empleado;
                break;
            }
        }
        return empleadoLogueado;
    }

    /** Paso 2 del CU 28: le pide getCM() al empleado logueado (nombre de su ComisionMedica). */
    public String getCM() {
        return buscarUsuarioLogueado().getCM();
    }

    /**
     * Paso 3 del CU 28: trae todos los bolsines y los recorre preguntando *sosEnviado()
     * y esTuCM(cm). El filtro lo hace el dominio, no una consulta a la base.
     */
    public List<Bolsin> buscarBolsinesEnviados() {
        String cm = getCM();
        this.bolsinesSeleccionados = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepository.findAll()) {
            if (bolsin.sosEnviado() && bolsin.esTuCM(cm)) {
                bolsinesSeleccionados.add(bolsin);
            }
        }
        return bolsinesSeleccionados;
    }

    /**
     * Paso 5 del CU 28: busca el bolsin seleccionado y se pide a si mismo obtenerInformacionRemito()
     * (paso 6). El numero llega de la pantalla (en el diagrama la seleccion no lleva parametro).
     */
    public void tomarSeleccionBolsin(int numeroBolsin) {
        this.bolsinSeleccionado = bolsinRepository.findById(numeroBolsin).orElse(null);
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

    /**
     * Paso 11 del CU 28 (orden del diagrama de secuencia): con la confirmacion (paso 10), el Gestor
     * obtiene los 3 estados, la fecha/hora actual y registra la recepcion.
     * Solo sigue con la opcion 1: las otras opciones de recepcion no estan implementadas.
     */
    public void tomarConfirmacion(boolean confirmado) {
        this.opcionSeleccionada = confirmado ? 1 : 0;
        if (!confirmado || opcionDeRecepcion != 1) return;
        obtenerEstadoRecibidoEnCMDestino();
        obtenerEstadoRecibidoYAceptado();
        obtenerEstadoRecibidaYAceptada();
        getFechaYHoraActual();
        registrarRecepcionBolsin();
    }

    /** Recorre todos los estados preguntando *esAmbitoBolsin() y esEstadoRecibidoEnCMDestino(). */
    public Estado obtenerEstadoRecibidoEnCMDestino() {
        this.estadoRecibidoEnCMDestino = null;
        for (Estado estado : estadoRepository.findAll()) {
            if (estado.esAmbitoBolsin() && estado.esEstadoRecibidoEnCMDestino()) {
                this.estadoRecibidoEnCMDestino = estado;
                break;
            }
        }
        return estadoRecibidoEnCMDestino;
    }

    /** Recorre todos los estados preguntando *esAmbitoRemito() y esEstadoRecibidoYAceptado(). */
    public Estado obtenerEstadoRecibidoYAceptado() {
        this.estadoRecibidoYAceptado = null;
        for (Estado estado : estadoRepository.findAll()) {
            if (estado.esAmbitoRemito() && estado.esEstadoRecibidoYAceptado()) {
                this.estadoRecibidoYAceptado = estado;
                break;
            }
        }
        return estadoRecibidoYAceptado;
    }

    /** Recorre todos los estados preguntando *esAmbitoDocumentacion() y esEstadoRecibidaYAceptada(). */
    public Estado obtenerEstadoRecibidaYAceptada() {
        this.estadoRecibidaYAceptada = null;
        for (Estado estado : estadoRepository.findAll()) {
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

    /**
     * El Bolsin recibe (y en cascada sus remitos y documentacion) con los estados, el empleado
     * logueado y la fecha/hora del Gestor; despues un solo save() persiste todo (cascadas).
     */
    public void registrarRecepcionBolsin() {
        if (bolsinSeleccionado == null) return;
        bolsinSeleccionado.recibir(estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                estadoRecibidaYAceptada, buscarUsuarioLogueado(), fechaHoraActual);
        bolsinRepository.save(bolsinSeleccionado);
    }

    public void llamarCU29() {
    }

    public void finCU() {
        this.bolsinSeleccionado = null;
        this.informacionRemito = null;
    }

    public Bolsin getBolsinSeleccionado() {
        return bolsinSeleccionado;
    }

    public Bolsin buscarBolsinPorNumero(int numero) {
        return bolsinRepository.findById(numero).orElse(null);
    }

    public String getInformacionRemito() {
        return informacionRemito;
    }

    public List<ComisionMedica> obtenerComisionesMedicas() {
        return comisionMedicaRepository.findAll();
    }

    /**
     * Pantalla auxiliar: el bolsin nace en Bolsin/Enviado con su cambio de estado inicial,
     * a cargo del empleado logueado.
     */
    public void crearBolsin(int numero, String precinto, double peso, ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        Estado estadoEnviado = obtenerEstadoBolsinEnviado();
        LocalDateTime fechaHora = getFechaYHoraActual();
        Bolsin bolsin = new Bolsin(numero, cmOrigen, cmDestino, estadoEnviado);
        bolsin.setNroPrecinto(precinto);
        bolsin.setPeso(peso);
        bolsin.crearNuevoCE(estadoEnviado, fechaHora, buscarUsuarioLogueado());
        bolsinRepository.save(bolsin);
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
        // Pantalla auxiliar: Remito/Enviado (sin historial) y Documentacion/EnBolsinEnviado
        // con su cambio de estado inicial a cargo del empleado logueado; tipo desde el catalogo.
        Estado estadoRem = obtenerEstadoRemitoEnviado();
        Estado estadoDoc = obtenerEstadoDocumentacionEnBolsinEnviado();
        LocalDateTime fechaHora = getFechaYHoraActual();

        Remito remito = new Remito(remitoNumero, LocalDate.now(), estadoRem, cmOrigen, cmDestino);

        int docNumero = obtenerSiguienteNumeroDocumentacion();
        Documentacion doc = new Documentacion(docNumero, asunto, descripcion, estadoDoc,
                buscarTipoDocumento(tipoDoc));
        doc.crearNuevoCE(estadoDoc, fechaHora, buscarUsuarioLogueado());

        remito.agregarDetalle(new DetalleRemito(doc));
        // El remito nuevo se inserta por la cascada ALL de Bolsin.remitos (y sus detalles y documentacion).
        Bolsin bolsin = bolsinRepository.findById(bolsinNumero).orElse(null);
        if (bolsin != null) {
            bolsin.agregarRemito(remito);
            bolsinRepository.save(bolsin);
        }
    }

    // --- Pantallas auxiliares: estados y tipo persistidos (mismo estilo de recorrido que el paso 11).

    private Estado obtenerEstadoBolsinEnviado() {
        for (Estado estado : estadoRepository.findAll()) {
            if (estado.esAmbitoBolsin() && estado.esEnviado()) {
                return estado;
            }
        }
        return null;
    }

    private Estado obtenerEstadoRemitoEnviado() {
        for (Estado estado : estadoRepository.findAll()) {
            if (estado.esAmbitoRemito() && estado.esEnviado()) {
                return estado;
            }
        }
        return null;
    }

    /** esEnviado() en el ambito DOC es EnBolsinEnviado (DME). */
    private Estado obtenerEstadoDocumentacionEnBolsinEnviado() {
        for (Estado estado : estadoRepository.findAll()) {
            if (estado.esAmbitoDocumentacion() && estado.esEnviado()) {
                return estado;
            }
        }
        return null;
    }

    private TipoDocumento buscarTipoDocumento(String nombre) {
        for (TipoDocumento tipo : tipoDocumentoRepository.findAll()) {
            if (tipo.getNombre().equals(nombre)) {
                return tipo;
            }
        }
        return null;
    }

    public void crearUsuario(String nombreUsuario, String password,
                             String nombreEmp, String apellidoEmp, String mailEmp, ComisionMedica cm) {
        Usuario usuario = new Usuario(nombreUsuario, password);
        Empleado emp = new Empleado(nombreEmp, apellidoEmp, mailEmp, cm, usuario);
        empleadoRepository.save(emp);
    }

    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return comisionMedicaRepository.save(new ComisionMedica(codigo, nombre, direccion, email, telefono));
    }

    public List<Bolsin> obtenerTodosBolsines() {
        return bolsinRepository.findAll();
    }

    /** Cada Empleado conoce a su Usuario; la pantalla de usuarios lista empleados con su usuario. */
    public List<Empleado> obtenerTodosEmpleados() {
        return empleadoRepository.findAll();
    }
}
