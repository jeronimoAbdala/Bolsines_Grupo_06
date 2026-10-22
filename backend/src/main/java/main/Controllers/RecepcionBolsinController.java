package main.Controllers;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.Empleado;
import main.Gestores.GestorRecepcionBolsin;

import java.util.List;

/**
 * Intermediario entre la pantalla (API REST) y el gestor del CU 28.
 * Expone solo los mensajes que la pantalla le envia al gestor en el diagrama de secuencia.
 */
public class RecepcionBolsinController {

    private final GestorRecepcionBolsin gestor;

    public RecepcionBolsinController(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    public Empleado opRegistrarRecepcionBolsin() {
        return gestor.opRegistrarRecepcionBolsin();
    }

    public List<Bolsin> buscarBolsinesEnviados() {
        return gestor.buscarBolsinesEnviados();
    }

    public Bolsin tomarSeleccionBolsin(int numero) {
        return gestor.tomarSeleccionBolsin(numero);
    }

    public List<String> obtenerInformacionRemito() {
        return gestor.obtenerInformacionRemito();
    }

    public void tomarOpcionDeRecepcion(int opcion) {
        gestor.tomarOpcionDeRecepcion(opcion);
    }

    public Bolsin tomarConfirmacion(boolean confirmado) {
        return gestor.tomarConfirmacion(confirmado);
    }
}
