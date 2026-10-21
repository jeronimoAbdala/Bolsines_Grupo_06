package main.Presentation.Api;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.Usuario;
import main.Presentation.Api.Dto.BolsinDto;
import main.Presentation.Api.Dto.DetalleRecepcionDto;
import main.Presentation.Api.Dto.UsuarioDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU 28 - Registrar recepcion de bolsin.
 * Reemplaza a la pantalla de consola: cada endpoint corresponde a un paso del caso de uso.
 * El gestor guarda el bolsin seleccionado, por eso los pasos que lo usan se sincronizan.
 */
@RestController
@RequestMapping("/api/recepcion")
public class RecepcionRestController {

    private final RecepcionBolsinController controller;

    public RecepcionRestController(RecepcionBolsinController controller) {
        this.controller = controller;
    }

    /** Usuario logueado y su comision medica (mostrarCM). */
    @GetMapping("/sesion")
    public UsuarioDto sesion() {
        Usuario usuario = controller.buscarUsuarioLogueado();
        if (usuario == null) {
            throw ApiException.noEncontrado("No se encontro el usuario logueado.");
        }
        return UsuarioDto.de(usuario);
    }

    /** Bolsines en estado ENVIADO hacia la CM del usuario (mostrarBolsinesParaSeleccion). */
    @GetMapping("/bolsines")
    public List<BolsinDto> bolsinesEnviados() {
        return controller.buscarBolsinesEnviados().stream().map(BolsinDto::de).toList();
    }

    /** Seleccion de un bolsin y datos de sus remitos (tomarSeleccionBolsin + obtenerInformacionRemito). */
    @GetMapping("/bolsines/{numero}")
    public DetalleRecepcionDto seleccionarBolsin(@PathVariable int numero) {
        synchronized (controller) {
            Bolsin bolsin = buscarPendiente(numero);
            controller.tomarSeleccionBolsin(numero);
            return new DetalleRecepcionDto(BolsinDto.de(bolsin), controller.obtenerInformacionRemito());
        }
    }

    /** Opcion "Recibir" confirmada: cambia estados en cascada y persiste (registrarRecepcion). */
    @PostMapping("/bolsines/{numero}/recibir")
    public BolsinDto recibir(@PathVariable int numero) {
        synchronized (controller) {
            buscarPendiente(numero);
            controller.registrarRecepcion(numero);
            return BolsinDto.de(controller.buscarBolsinPorNumero(numero));
        }
    }

    private Bolsin buscarPendiente(int numero) {
        return controller.buscarBolsinesEnviados().stream()
                .filter(b -> b.esTuNumero(numero))
                .findFirst()
                .orElseThrow(() -> ApiException.conflicto(
                        "El Bolsin #" + numero + " no esta pendiente de recepcion para su comision medica."));
    }
}
