package main.Presentation.Router;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Presentation.Screens.PantallaAgregarRemito;
import main.Presentation.Screens.PantallaCrearBolsin;
import main.Presentation.Screens.PantallaCrearComisionMedica;
import main.Presentation.Screens.PantallaCrearUsuario;
import main.Presentation.Screens.PantallaListar;
import main.Presentation.Screens.PantallaMenuPrincipal;
import main.Presentation.Screens.PantallaRegistrarRecepcionBolsin;

import java.util.List;
import java.util.Scanner;

public class AppRouter {

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    private final PantallaMenuPrincipal pantallaMenu;
    private final PantallaRegistrarRecepcionBolsin pantallaRecepcion;
    private final PantallaCrearBolsin pantallaCrearBolsin;
    private final PantallaAgregarRemito pantallaAgregarRemito;
    private final PantallaCrearComisionMedica pantallaCrearCM;
    private final PantallaCrearUsuario pantallaCrearUsuario;
    private final PantallaListar pantallaListar;

    public AppRouter(RecepcionBolsinController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
        this.pantallaMenu = new PantallaMenuPrincipal(controller, scanner);
        this.pantallaRecepcion = new PantallaRegistrarRecepcionBolsin(controller, scanner);
        this.pantallaCrearBolsin = new PantallaCrearBolsin(controller, scanner);
        this.pantallaAgregarRemito = new PantallaAgregarRemito(controller, scanner);
        this.pantallaCrearCM = new PantallaCrearComisionMedica(controller, scanner);
        this.pantallaCrearUsuario = new PantallaCrearUsuario(controller, scanner);
        this.pantallaListar = new PantallaListar(controller);
    }

    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            pantallaMenu.mostrar();
            int opcion = pantallaMenu.leerOpcion();

            switch (opcion) {
                case 1:
                    navegarARecepcion();
                    break;
                case 2:
                    pantallaCrearBolsin.mostrar();
                    break;
                case 3:
                    pantallaAgregarRemito.mostrar();
                    break;
                case 4:
                    pantallaCrearCM.mostrar();
                    break;
                case 5:
                    pantallaCrearUsuario.mostrar();
                    break;
                case 6:
                    pantallaListar.listarBolsines();
                    break;
                case 7:
                    pantallaListar.listarUsuarios();
                    break;
                case 0:
                    continuar = false;
                    break;
                default:
                    System.out.println("  Opcion invalida. Intente de nuevo.");
                    System.out.println();
            }
        }
        System.out.println("=== FIN ===");
    }

    public void navegarARecepcion() {
        pantallaRecepcion.opcRegistrarRecepcionBolsin();

        List<Bolsin> bolsines = controller.buscarBolsinesEnviados();
        if (bolsines.isEmpty()) {
            return;
        }

        int numeroSeleccion = pantallaRecepcion.solicitarSeleccionBolsin(bolsines);
        controller.tomarSeleccionBolsin(numeroSeleccion);

        String infoRemito = controller.obtenerInformacionRemito();
        pantallaRecepcion.mostrarDatosRemito(infoRemito);

        pantallaRecepcion.mostrarOpcionesRecepcion();
        int opcion = pantallaRecepcion.tomarOpcionDeRecepcion();

        if (pantallaRecepcion.solicitarConfirmacion(numeroSeleccion)) {
            controller.registrarRecepcion(numeroSeleccion);
            pantallaRecepcion.informarRecepcion(numeroSeleccion);
        } else {
            System.out.println("  Recepcion cancelada por el usuario.");
        }
    }
}
