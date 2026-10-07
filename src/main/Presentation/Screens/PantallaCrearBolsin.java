package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.ComisionMedica;

import java.util.List;
import java.util.Scanner;

public class PantallaCrearBolsin {

    private static final String LINEA = "========================================";

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    public PantallaCrearBolsin(RecepcionBolsinController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void mostrar() {
        System.out.println(LINEA);
        System.out.println("           CREAR BOLSIN");
        System.out.println(LINEA);
        System.out.println();

        int numero = controller.obtenerSiguienteNumeroBolsin();
        System.out.println("  Numero generado: " + numero);

        System.out.print("  Precinto: > ");
        String precinto = scanner.nextLine().trim();

        System.out.print("  Peso (kg): > ");
        double peso = 0;
        try {
            peso = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Peso invalido, usando 0.0");
        }

        List<ComisionMedica> cms = controller.obtenerComisionesMedicas();
        if (cms.isEmpty()) {
            System.out.println("  No hay Comisiones Medicas creadas. Cree una primero (opcion 4).");
            return;
        }

        System.out.println();
        System.out.println("  Comisiones Medicas disponibles:");
        for (int i = 0; i < cms.size(); i++) {
            ComisionMedica cm = cms.get(i);
            System.out.println("    " + (i + 1) + ") " + cm.getNombre() + " (Codigo: " + cm.getCodigo() + ")");
        }

        System.out.println();
        System.out.print("  Seleccionar Comision Origen (numero): > ");
        int idxOrigen = leerIndice(cms);

        System.out.print("  Seleccionar Comision Destino (numero): > ");
        int idxDestino = leerIndice(cms);

        ComisionMedica cmOrigen = cms.get(idxOrigen);
        ComisionMedica cmDestino = cms.get(idxDestino);

        System.out.println();
        System.out.println("  Resumen:");
        System.out.println("    Numero: " + numero);
        System.out.println("    Precinto: " + precinto);
        System.out.println("    Peso: " + peso + " kg");
        System.out.println("    Origen: " + cmOrigen.getNombre());
        System.out.println("    Destino: " + cmDestino.getNombre());

        System.out.println();
        System.out.print("  Confirmar creacion? (S/N): > ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("S")) {
            controller.crearBolsin(numero, precinto, peso, cmOrigen, cmDestino);
            System.out.println();
            System.out.println(LINEA);
            System.out.println("  BOLSIN CREADO EXITOSAMENTE");
            System.out.println(LINEA);
        } else {
            System.out.println("  Creacion cancelada.");
        }
        System.out.println();
    }

    private int leerIndice(List<ComisionMedica> cms) {
        String entrada = scanner.nextLine().trim();
        try {
            int indice = Integer.parseInt(entrada) - 1;
            if (indice < 0 || indice >= cms.size()) {
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
