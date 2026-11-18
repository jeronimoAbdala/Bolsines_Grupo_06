package main.Presentation.Api;

import main.Dto.BolsinDto;
import main.Dto.BolsinEnviadoDto;
import main.Dto.BolsinParaSeleccionDto;
import main.Dto.ComisionMedicaDto;
import main.Dto.DetalleRecepcionDto;
import main.Dto.Requests;
import main.Dto.UsuarioDto;
import main.Dto.VentanaRecepcionDto;
import main.Gestores.GestorRecepcionBolsin;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * CU 28 - Registrar recepcion de bolsin: la Pantalla del diagrama de secuencia.
 * Cada mensaje del actor es un endpoint y le habla directo al Gestor; los mensajes Gestor -> Pantalla
 * son metodos privados que arman la respuesta con los DTO que entrega el Gestor (la Pantalla no toca entidades).
 * El Gestor guarda el estado del CU entre pedidos, por eso cada endpoint se sincroniza con el.
 */
@RestController
@RequestMapping("/api/recepcion")
public class PantallaRegistrarRecepcionBolsin {

    /** Paso 7: opciones de recepcion de la ERS. Solo la 1 esta implementada. */
    private static final List<String> OPCIONES_RECEPCION = List.of(
            "El contenido del bolsín es igual al registrado",
            "No se recibe toda la documentación asociada a los remitos que contiene el bolsín",
            "Existe documentación que no corresponde al destino (CM del usuario logueado)",
            "La documentación se debe redirigir a otra área");

    private final GestorRecepcionBolsin gestor;

    public PantallaRegistrarRecepcionBolsin(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    // opcRegistrarRecepcionBolsin() — paso 1
    @PostMapping("/iniciar")
    public VentanaRecepcionDto opcRegistrarRecepcionBolsin() {
        synchronized (gestor) {
            return habilitarVentana();
        }
    }

    // habilitarVentana() — paso 1
    private VentanaRecepcionDto habilitarVentana() {
        gestor.opRegistrarRecepcionBolsin();
        return new VentanaRecepcionDto(mostrarCM(), mostrarBolsinesParaSeleccion(), solicitarSeleccionBolsin());
    }

    // mostrarCM() — paso 2
    private UsuarioDto mostrarCM() {
        UsuarioDto usuario = gestor.getUsuarioLogueado();
        if (usuario == null) {
            throw ApiException.noEncontrado("No se encontro el usuario logueado.");
        }
        return usuario;
    }

    // mostrarBolsinesParaSeleccion() — paso 4: una fila por cada bolsin que entrega el Gestor
    private List<BolsinParaSeleccionDto> mostrarBolsinesParaSeleccion() {
        List<BolsinParaSeleccionDto> filas = new ArrayList<>();
        for (BolsinEnviadoDto bolsin : gestor.getBolsinesEnviados()) {
            filas.add(new BolsinParaSeleccionDto(bolsin.numero(), mostrarCMOrigen(bolsin.cmOrigen()),
                    mostrarNroPrecinto(bolsin.nroPrecinto()), bolsin.cantRemitos(), bolsin.estado()));
        }
        return filas;
    }

    // mostrarCMOrigen() — paso 4: nombre de la CM origen
    private String mostrarCMOrigen(ComisionMedicaDto cmOrigen) {
        return cmOrigen.nombre();
    }

    // mostrarNroPrecinto() — paso 4: 'N/A' si el bolsin no tiene precinto
    private String mostrarNroPrecinto(String nroPrecinto) {
        return nroPrecinto == null ? "N/A" : nroPrecinto;
    }

    // solicitarSeleccionBolsin() — paso 4
    private String solicitarSeleccionBolsin() {
        return "Seleccione un bolsín para registrar su recepción";
    }

    // tomarSeleccionBolsin() — paso 5 (con numero: decision P1)
    @PostMapping("/seleccion/{numero}")
    public DetalleRecepcionDto tomarSeleccionBolsin(@PathVariable int numero) {
        synchronized (gestor) {
            gestor.tomarSeleccionBolsin(numero);
            BolsinDto bolsin = gestor.getDatosBolsinSeleccionado();
            if (bolsin == null) {
                throw ApiException.conflicto(
                        "El Bolsin #" + numero + " no esta pendiente de recepcion para su comision medica.");
            }
            return new DetalleRecepcionDto(bolsin, mostrarDatosRemito(), mostrarOpcionesRecepcion());
        }
    }

    // mostrarDatosRemito() — paso 6: lo que armo el Iterator
    private String mostrarDatosRemito() {
        return gestor.getInformacionRemito();
    }

    // mostrarOpcionesRecepcion() — paso 7
    private List<String> mostrarOpcionesRecepcion() {
        return OPCIONES_RECEPCION;
    }

    // tomarOpcionDeRecepcion() — paso 8: las opciones 2, 3 y 4 (A3, A4, A5) no estan implementadas
    @PostMapping("/opcion")
    public int tomarOpcionDeRecepcion(@RequestBody Requests.OpcionDeRecepcion req) {
        int opcion = req.opcion();
        if (opcion < 1 || opcion > OPCIONES_RECEPCION.size()) {
            throw ApiException.invalido("Opcion de recepcion invalida: " + opcion + ". Valores: 1 a 4.");
        }
        synchronized (gestor) {
            if (gestor.getDatosBolsinSeleccionado() == null) {
                throw ApiException.conflicto("No hay un bolsin seleccionado.");
            }
            gestor.tomarOpcionDeRecepcion(opcion);
            if (opcion != 1) {
                throw ApiException.noImplementado("Opción no implementada");
            }
            return solicitarConfirmacion();
        }
    }

    // solicitarConfirmacion() — paso 9: numero del bolsin que se pide confirmar
    private int solicitarConfirmacion() {
        return gestor.getDatosBolsinSeleccionado().numero();
    }

    // tomarConfirmacion() — paso 10
    @PostMapping("/confirmacion")
    public BolsinDto tomarConfirmacion() {
        synchronized (gestor) {
            return informarRecepcion(gestor.tomarConfirmacion());
        }
    }

    // informarRecepcion() — paso 12
    private BolsinDto informarRecepcion(BolsinDto recibido) {
        if (recibido == null) {
            throw ApiException.conflicto("No hay un bolsin seleccionado con la opcion 1 para recibir.");
        }
        return recibido;
    }
}
