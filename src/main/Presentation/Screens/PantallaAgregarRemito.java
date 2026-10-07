package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;

import java.util.List;
import java.util.Scanner;

public class PantallaAgregarRemito {

    private static final String LINEA = "========================================";

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    public PantallaAgregarRemito(RecepcionBolsinController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void mostrar() {
        System.out.println(LINEA);
        System.out.println("           AGREGAR REMITO A BOLSIN");
        System.out.println(LINEA);
        System.out.println();

        List<Bolsin> bolsines = controller.obtenerTodosBolsines();
        if (bolsines.isEmpty()) {
            System.out.println("  No hay bolsines creados. Cree uno primero (opcion 2).");
            return;
        }

        System.out.println("  Bolsines disponibles:");
        for (int i = 0; i < bolsines.size(); i++) {
            Bolsin b = bolsines.get(i);
            System.out.println("    " + (i + 1) + ") Bolsin #" + b.getNumero()
                    + " (" + b.getCmOrigen().getNombre() + " -> " + b.getCmDestino().getNombre() + ")");
        }
        System.out.println();
        System.out.print("  Seleccionar Bolsin (numero): > ");
        int idxBolsin = leerIndice(bolsines);
        Bolsin bolsin = bolsines.get(idxBolsin);

        int remitoNumero = controller.obtenerSiguienteNumeroRemito();
        System.out.println();
        System.out.println("  Numero remito generado: " + remitoNumero);
        System.out.println("  Origen: " + bolsin.getCmOrigen().getNombre());
        System.out.println("  Destino: " + bolsin.getCmDestino().getNombre());

        System.out.println();
        System.out.print("  Asunto de la documentacion: > ");
        String asunto = scanner.nextLine().trim();

        System.out.print("  Descripcion: > ");
        String descripcion = scanner.nextLine().trim();

        System.out.println();
        System.out.println("  Tipos de documento:");
        System.out.println("    1) EXPEDIENTE");
        System.out.println("    2) INFORME");
        System.out.println("    3) CERTIFICADO");
        System.out.println("    4) OTRO");
        System.out.print("  Seleccionar tipo: > ");
        String tipoDoc = leerTipoDocumento();

        System.out.println();
        System.out.println("  Resumen:");
        System.out.println("    Bolsin: #" + bolsin.getNumero());
        System.out.println("    Remito: #" + remitoNumero);
        System.out.println("    Asunto: " + asunto);
        System.out.println("    Tipo: " + tipoDoc);

        System.out.println();
        System.out.print("  Confirmar? (S/N): > ");
        String confirmacion = scanner.nextLine().trim();
        if (confirmacion.equalsIgnoreCase("S")) {
            controller.crearRemitoConDocumentacion(
                    bolsin.getNumero(), remitoNumero,
                    bolsin.getCmOrigen(), bolsin.getCmDestino(),
                    asunto, descripcion, tipoDoc);
            System.out.println();
            System.out.println(LINEA);
            System.out.println("  REMITO AGREGADO EXITOSAMENTE");
            System.out.println(LINEA);
        } else {
            System.out.println("  Operacion cancelada.");
        }
        System.out.println();
    }

    private String leerTipoDocumento() {
        String entrada = scanner.nextLine().trim();
        switch (entrada) {
            case "1": return "EXPEDIENTE";
            case "2": return "INFORME";
            case "3": return "CERTIFICADO";
            default: return "OTRO";
        }
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
