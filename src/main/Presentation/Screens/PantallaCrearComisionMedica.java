package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;

import java.util.Scanner;

public class PantallaCrearComisionMedica {

    private static final String LINEA = "========================================";

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    public PantallaCrearComisionMedica(RecepcionBolsinController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void mostrar() {
        System.out.println(LINEA);
        System.out.println("        CREAR COMISION MEDICA");
        System.out.println(LINEA);
        System.out.println();

        System.out.print("  Codigo (entero): > ");
        int codigo = 0;
        try {
            codigo = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Codigo invalido.");
            return;
        }

        System.out.print("  Nombre: > ");
        String nombre = scanner.nextLine().trim();

        System.out.print("  Direccion: > ");
        String direccion = scanner.nextLine().trim();

        System.out.print("  Email: > ");
        String email = scanner.nextLine().trim();

        System.out.print("  Telefono: > ");
        String telefono = scanner.nextLine().trim();

        System.out.println();
        System.out.println("  Resumen:");
        System.out.println("    Codigo: " + codigo);
        System.out.println("    Nombre: " + nombre);
        System.out.println("    Direccion: " + direccion);
        System.out.println("    Email: " + email);
        System.out.println("    Telefono: " + telefono);

        System.out.println();
        System.out.print("  Confirmar creacion? (S/N): > ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("S")) {
            controller.crearComisionMedica(codigo, nombre, direccion, email, telefono);
            System.out.println();
            System.out.println(LINEA);
            System.out.println("  COMISION MEDICA CREADA EXITOSAMENTE");
            System.out.println(LINEA);
        } else {
            System.out.println("  Creacion cancelada.");
        }
        System.out.println();
    }
}
