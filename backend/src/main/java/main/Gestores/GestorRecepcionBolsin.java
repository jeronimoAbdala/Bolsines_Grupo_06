package main.Gestores;

import main.Domain.Entities.*;
import main.Dto.BolsinDto;
import main.Dto.BolsinEnviadoDto;
import main.Dto.ComisionMedicaDto;
import main.Dto.UsuarioDto;
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

    private List<Bolsin> bolsinesSeleccionados = new ArrayList<>();
    /** Datos de cada bolsin de bolsinesSeleccionados para la Pantalla (paso 4), sin entidades. */
    private List<BolsinEnviadoDto> bolsinesEnviados = new ArrayList<>();
    private String informacionRemito;
    private int opcionDeRecepcion;
    private LocalDateTime fechaHoraActual;
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

    // buscarBolsinesEnviados() — paso 3: *sosEnviado(), *esTuCM(); getCMOrigen() y getNroPrecinto() — paso 4
    public void buscarBolsinesEnviados(String cm) {
        this.bolsinesSeleccionados = new ArrayList<>();
        this.bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepository.findAll()) {
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
                // Persistencia: se vuelve a leer en este pedido (el de la lista es de un pedido anterior).
                this.bolsinSeleccionado = bolsinRepository.findById(numeroBolsin).orElse(null);
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
        // Persistencia: se vuelve a leer en este pedido (la seleccion es de un pedido anterior).
        this.bolsinSeleccionado = bolsinRepository.findById(bolsinSeleccionado.getNumero()).orElse(null);
        obtenerEstadoRecibidoEnCMDestino();
        obtenerEstadoRecibidoYAceptado();
        obtenerEstadoRecibidaYAceptada();
        getFechaYHoraActual();
        bolsinSeleccionado.recibir(estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                estadoRecibidaYAceptada, empleadoLogueado, fechaHoraActual);
        bolsinRepository.save(bolsinSeleccionado);   // persistencia: un solo save() guarda todo (cascadas)
        llamarCU29();
        BolsinDto recibido = BolsinDto.de(bolsinSeleccionado);
        finCU();
        return recibido;
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
