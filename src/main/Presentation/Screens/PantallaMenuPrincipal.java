package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;

import java.util.Scanner;

public class PantallaMenuPrincipal {

    private static final String LINEA = "========================================";

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    public PantallaMenuPrincipal(RecepcionBolsinController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void mostrar() {
        System.out.println(LINEA);
        System.out.println("           SISTEMA BOLSINES");
        System.out.println(LINEA);
        System.out.println();
        System.out.println("  1) Registrar Recepcion de Bolsin");
        System.out.println("  2) Crear Bolsin");
        System.out.println("  3) Agregar Remito a Bolsin");
        System.out.println("  4) Crear Comision Medica");
        System.out.println("  5) Crear Usuario");
        System.out.println("  6) Listar Bolsines");
        System.out.println("  7) Listar Usuarios");
        System.out.println("  0) Salir");
        System.out.println();
        System.out.print("  Seleccione una opcion: > ");
    }

    public int leerOpcion() {
        String entrada = scanner.nextLine().trim();
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
