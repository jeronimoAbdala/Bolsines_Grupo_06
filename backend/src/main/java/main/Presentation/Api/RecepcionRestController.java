package main.Presentation.Api;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.Empleado;
import main.Gestores.GestorRecepcionBolsin;
import main.Presentation.Api.Dto.BolsinDto;
import main.Presentation.Api.Dto.DetalleRecepcionDto;
import main.Presentation.Api.Dto.UsuarioDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU 28 - Registrar recepcion de bolsin. Cumple el rol de PantallaRegistrarRecepcionBolsin:
 * cada endpoint corresponde a un mensaje del actor a la pantalla en el diagrama de secuencia.
 * El gestor guarda el bolsin seleccionado, por eso los pasos que lo usan se sincronizan.
 */
@RestController
@RequestMapping("/api/recepcion")
public class RecepcionRestController {

    private final RecepcionBolsinController controller;

    public RecepcionRestController(RecepcionBolsinController controller) {
        this.controller = controller;
    }

    /** opcRegistrarRecepcionBolsin(): habilita la ventana y muestra la CM del usuario logueado (mostrarCM). */
    @GetMapping("/sesion")
    public UsuarioDto opcRegistrarRecepcionBolsin() {
        synchronized (controller) {
            Empleado logueado = controller.opRegistrarRecepcionBolsin();
            if (logueado == null) {
                throw ApiException.noEncontrado("No se encontro el usuario logueado.");
            }
            return UsuarioDto.de(logueado.getUsuario());
        }
    }

    /** mostrarBolsinesParaSeleccion(): bolsines enviados hacia la CM del usuario. */
    @GetMapping("/bolsines")
    public List<BolsinDto> mostrarBolsinesParaSeleccion() {
        synchronized (controller) {
            return controller.buscarBolsinesEnviados().stream().map(BolsinDto::de).toList();
        }
    }

    /** tomarSeleccionBolsin(): el gestor pide la informacion de los remitos (mostrarDatosRemito). */
    @GetMapping("/bolsines/{numero}")
    public DetalleRecepcionDto tomarSeleccionBolsin(@PathVariable int numero) {
        synchronized (controller) {
            Bolsin bolsin = seleccionar(numero);
            List<String> informacion = controller.obtenerInformacionRemito();
            return new DetalleRecepcionDto(BolsinDto.de(bolsin), String.join("\n", informacion));
        }
    }

    /** tomarOpcionDeRecepcion("Recibir") + tomarConfirmacion(): registra la recepcion e informa el resultado. */
    @PostMapping("/bolsines/{numero}/recibir")
    public BolsinDto tomarConfirmacion(@PathVariable int numero) {
        synchronized (controller) {
            seleccionar(numero);
            controller.tomarOpcionDeRecepcion(GestorRecepcionBolsin.OPCION_RECIBIR);
            Bolsin recibido = controller.tomarConfirmacion(true);
            return BolsinDto.de(recibido);
        }
    }

    private Bolsin seleccionar(int numero) {
        Bolsin bolsin = controller.tomarSeleccionBolsin(numero);
        if (bolsin == null) {
            throw ApiException.conflicto(
                    "El Bolsin #" + numero + " no esta pendiente de recepcion para su comision medica.");
        }
        return bolsin;
    }
}
