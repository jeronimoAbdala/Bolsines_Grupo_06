package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.ComisionMedica;

import java.util.List;
import java.util.Scanner;

public class PantallaCrearUsuario {

    private static final String LINEA = "========================================";

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    public PantallaCrearUsuario(RecepcionBolsinController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void mostrar() {
        System.out.println(LINEA);
        System.out.println("           CREAR USUARIO");
        System.out.println(LINEA);
        System.out.println();

        System.out.print("  Nombre de usuario: > ");
        String nombreUsuario = scanner.nextLine().trim();

        System.out.print("  Password: > ");
        String password = scanner.nextLine().trim();

        System.out.print("  Nombre del empleado: > ");
        String nombreEmp = scanner.nextLine().trim();

        System.out.print("  Apellido del empleado: > ");
        String apellidoEmp = scanner.nextLine().trim();

        System.out.print("  Mail del empleado: > ");
        String mailEmp = scanner.nextLine().trim();

        List<ComisionMedica> cms = controller.obtenerComisionesMedicas();
        if (cms.isEmpty()) {
            System.out.println();
            System.out.println("  No hay Comisiones Medicas. Cree una primero (opcion 4).");
            return;
        }

        System.out.println();
        System.out.println("  Comisiones Medicas disponibles:");
        for (int i = 0; i < cms.size(); i++) {
            ComisionMedica cm = cms.get(i);
            System.out.println("    " + (i + 1) + ") " + cm.getNombre() + " (Codigo: " + cm.getCodigo() + ")");
        }

        System.out.println();
        System.out.print("  Seleccionar Comision Medica (numero): > ");
        int idx = leerIndice(cms);
        ComisionMedica cm = cms.get(idx);

        System.out.println();
        System.out.println("  Resumen:");
        System.out.println("    Usuario: " + nombreUsuario);
        System.out.println("    Empleado: " + nombreEmp + " " + apellidoEmp);
        System.out.println("    Mail: " + mailEmp);
        System.out.println("    CM: " + cm.getNombre());

        System.out.println();
        System.out.print("  Confirmar creacion? (S/N): > ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("S")) {
            controller.crearUsuario(nombreUsuario, password, nombreEmp, apellidoEmp, mailEmp, cm);
            System.out.println();
            System.out.println(LINEA);
            System.out.println("  USUARIO CREADO EXITOSAMENTE");
            System.out.println(LINEA);
        } else {
            System.out.println("  Creacion cancelada.");
        }
        System.out.println();
    }

    private int leerIndice(List<?> items) {
        String entrada = scanner.nextLine().trim();
        try {
            int indice = Integer.parseInt(entrada) - 1;
            if (indice < 0 || indice >= items.size()) {
                System.out.println("  Indice invalido, usando el primero.");
                return 0;
            }
            return indice;
        } catch (NumberFormatException e) {
            System.out.println("  Entrada invalida, usando el primero.");
            return 0;
        }
    }
}
