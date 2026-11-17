package main.Presentation.Api;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.Empleado;
import main.Presentation.Api.Dto.BolsinDto;
import main.Presentation.Api.Dto.DetalleRecepcionDto;
import main.Presentation.Api.Dto.Requests;
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
        Empleado empleadoLogueado = controller.buscarUsuarioLogueado();
        if (empleadoLogueado == null) {
            throw ApiException.noEncontrado("No se encontro el usuario logueado.");
        }
        return UsuarioDto.de(empleadoLogueado);
    }

    /** Bolsines en estado Enviado hacia la CM del usuario (mostrarBolsinesParaSeleccion). */
    @GetMapping("/bolsines")
    public List<BolsinDto> bolsinesEnviados() {
        return controller.buscarBolsinesEnviados().stream().map(BolsinDto::de).toList();
    }

    /**
     * Pasos 5 y 6: seleccion de un bolsin (tomarSeleccionBolsin) y la informacion de sus remitos que
     * arma el Iterator (informacionRemito). El encabezado (origen, destino, precinto, peso) va en el DTO.
     */
    @GetMapping("/bolsines/{numero}")
    public DetalleRecepcionDto seleccionarBolsin(@PathVariable int numero) {
        synchronized (controller) {
            Bolsin bolsin = buscarPendiente(numero);
            controller.tomarSeleccionBolsin(numero);
            return new DetalleRecepcionDto(BolsinDto.de(bolsin), controller.getInformacionRemito());
        }
    }

    /**
     * Opcion de recepcion elegida (paso 8) y confirmada (paso 10): cambia estados en cascada y
     * persiste. Solo la opcion 1 esta implementada; con otra opcion responde un error sin cambiar nada.
     */
    @PostMapping("/bolsines/{numero}/recibir")
    public BolsinDto recibir(@PathVariable int numero, @RequestBody Requests.RecibirBolsin req) {
        int opcion = req.opcion();
        if (opcion < 1 || opcion > 4) {
            throw ApiException.invalido("Opcion de recepcion invalida: " + opcion + ". Valores: 1 a 4.");
        }
        if (opcion != 1) {
            throw ApiException.noImplementado("Opcion de recepcion no implementada: " + opcion + ".");
        }
        synchronized (controller) {
            buscarPendiente(numero);
            controller.registrarRecepcion(numero, opcion);
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
