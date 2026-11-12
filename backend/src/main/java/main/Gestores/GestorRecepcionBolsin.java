package main.Gestores;

import main.Domain.Entities.*;
import main.Domain.Repositories.BolsinRepository;
import main.Domain.Repositories.EmpleadoRepository;
import main.Domain.Repositories.EstadoRepository;
import main.Domain.Repositories.TipoDocumentoRepository;
import main.Domain.Repositories.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestorRecepcionBolsin {

    private final BolsinRepository bolsinRepository;
    private final UsuarioRepository usuarioRepository;
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

    /** Estados persistidos que obtiene el paso 11 recorriendo EstadoRepository.obtenerTodos(). */
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoYAceptado;
    private Estado estadoRecibidaYAceptada;

    public GestorRecepcionBolsin(BolsinRepository bolsinRepository,
                                 UsuarioRepository usuarioRepository,
                                 EmpleadoRepository empleadoRepository,
                                 EstadoRepository estadoRepository,
                                 TipoDocumentoRepository tipoDocumentoRepository) {
        this.bolsinRepository = bolsinRepository;
        this.usuarioRepository = usuarioRepository;
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
        for (Empleado empleado : empleadoRepository.obtenerTodos()) {
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
        for (Bolsin bolsin : bolsinRepository.obtenerTodos()) {
            if (bolsin.sosEnviado() && bolsin.esTuCM(cm)) {
                bolsinesSeleccionados.add(bolsin);
            }
        }
        return bolsinesSeleccionados;
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
                Documentacion doc = detalle.getDocumentacion();
                info.append("  Doc #").append(doc.getNumero()).append(": ").append(doc.getAsunto()).append("\n");
            }
        }
        this.informacionRemito = info.toString();
        return informacionRemito;
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
        for (Estado estado : estadoRepository.obtenerTodos()) {
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
        for (Estado estado : estadoRepository.obtenerTodos()) {
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
        for (Estado estado : estadoRepository.obtenerTodos()) {
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
     * logueado y la fecha/hora del Gestor; despues un solo guardar() persiste todo (cascadas).
     */
    public void registrarRecepcionBolsin() {
        if (bolsinSeleccionado == null) return;
        bolsinSeleccionado.recibir(estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                estadoRecibidaYAceptada, buscarUsuarioLogueado(), fechaHoraActual);
        bolsinRepository.guardar(bolsinSeleccionado);
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
        return bolsinRepository.buscarPorNumero(numero);
    }

    public String getInformacionRemito() {
        return informacionRemito;
    }

    public List<ComisionMedica> obtenerComisionesMedicas() {
        return bolsinRepository.obtenerComisionesMedicas();
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
        bolsinRepository.guardar(bolsin);
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
        bolsinRepository.agregarRemitoABolsin(bolsinNumero, remito);
    }

    // --- Pantallas auxiliares: estados y tipo persistidos (mismo estilo de recorrido que el paso 11).

    private Estado obtenerEstadoBolsinEnviado() {
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoBolsin() && estado.esEnviado()) {
                return estado;
            }
        }
        return null;
    }

    private Estado obtenerEstadoRemitoEnviado() {
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoRemito() && estado.esEnviado()) {
                return estado;
            }
        }
        return null;
    }

    /** esEnviado() en el ambito DOC es EnBolsinEnviado (DME). */
    private Estado obtenerEstadoDocumentacionEnBolsinEnviado() {
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoDocumentacion() && estado.esEnviado()) {
                return estado;
            }
        }
        return null;
    }

    private TipoDocumento buscarTipoDocumento(String nombre) {
        for (TipoDocumento tipo : tipoDocumentoRepository.obtenerTodos()) {
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
        empleadoRepository.guardar(emp);
    }

    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return usuarioRepository.crearComisionMedica(codigo, nombre, direccion, email, telefono);
    }

    public List<Bolsin> obtenerTodosBolsines() {
        return bolsinRepository.obtenerTodos();
    }

    /** Cada Empleado conoce a su Usuario; la pantalla de usuarios lista empleados con su usuario. */
    public List<Empleado> obtenerTodosEmpleados() {
        return empleadoRepository.obtenerTodos();
    }
}
