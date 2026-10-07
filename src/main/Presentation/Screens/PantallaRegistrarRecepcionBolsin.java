package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.DetalleRemito;
import main.Domain.Entities.Documentation;
import main.Domain.Entities.Remito;
import main.Domain.Entities.Usuario;

import java.util.List;
import java.util.Scanner;

public class PantallaRegistrarRecepcionBolsin {

    private static final String SEPARADOR = "----------------------------------------";
    private static final String LINEA_LARGA = "========================================";

    private final RecepcionBolsinController controller;
    private final Scanner scanner;

    public PantallaRegistrarRecepcionBolsin(RecepcionBolsinController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void opcRegistrarRecepcionBolsin() {
        habilitarVentana();
    }

    public void habilitarVentana() {
        mostrarCM();
        mostrarBolsinesParaSeleccion();
    }

    public void mostrarCM() {
        Usuario usuario = controller.buscarUsuarioLogueado();
        System.out.println(LINEA_LARGA);
        System.out.println("           SISTEMA BOLSINES");
        System.out.println(LINEA_LARGA);
        System.out.println();
        System.out.println("  Usuario: " + usuario.getEmpleado().getNombreCompleto());
        System.out.println("  CM:      " + usuario.getEmpleado().getComisionMedica().getNombre());
        System.out.println();
    }

    public void mostrarBolsinesParaSeleccion() {
        List<Bolsin> bolsines = controller.buscarBolsinesEnviados();
        System.out.println(SEPARADOR);
        System.out.println("  BOLINES ENVIADOS");
        System.out.println(SEPARADOR);
        System.out.println();
        if (bolsines.isEmpty()) {
            System.out.println("  No hay bolsines para recibir.");
            return;
        }
        for (int i = 0; i < bolsines.size(); i++) {
            Bolsin b = bolsines.get(i);
            System.out.println("  " + (i + 1) + ") Bolsin #" + b.getNumero());
            mostrarCMOrigen(b);
            mostrarNroPrecinto(b);
            System.out.println("     Remitos: " + b.getCantRemitos());
            System.out.println("     Estado: " + b.getEstado().getNombre());
            System.out.println();
        }
        System.out.println(SEPARADOR);
    }

    public void mostrarCMOrigen(Bolsin b) {
        System.out.println("     " + b.getCmOrigen().getNombre() + " -> " + b.getCmDestino().getNombre());
    }

    public void mostrarNroPrecinto(Bolsin b) {
        String precinto = b.getNroPrecinto() != null ? b.getNroPrecinto() : "N/A";
        System.out.println("     Precinto: " + precinto);
    }

    public int solicitarSeleccionBolsin(List<Bolsin> bolsines) {
        System.out.print("  Seleccione un numero: > ");
        String entrada = scanner.nextLine().trim();
        try {
            int indice = Integer.parseInt(entrada) - 1;
            if (indice < 0 || indice >= bolsines.size()) {
                System.out.println("  Opcion invalida. Seleccionando la primera.");
                indice = 0;
            }
            System.out.println();
            return bolsines.get(indice).getNumero();
        } catch (NumberFormatException e) {
            System.out.println("  Entrada invalida. Seleccionando la primera.");
            System.out.println();
            return bolsines.get(0).getNumero();
        }
    }

    public void mostrarDatosRemito(String informacionRemito) {
        System.out.println(SEPARADOR);
        System.out.println("  INFORMACION DEL REMITO");
        System.out.println(SEPARADOR);
        System.out.println(informacionRemito);
    }

    public void mostrarOpcionesRecepcion() {
        System.out.println("  1) Recibir");
        System.out.println("  2) Rechazar");
        System.out.println();
    }

    public int tomarOpcionDeRecepcion() {
        System.out.print("  Seleccione opcion: > ");
        String entrada = scanner.nextLine().trim();
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public boolean solicitarConfirmacion(int numeroBolsin) {
        System.out.print("  Confirmar recepcion del Bolsin #" + numeroBolsin + "? (S/N): > ");
        String entrada = scanner.nextLine().trim();
        System.out.println();
        return entrada.equalsIgnoreCase("S");
    }

    public boolean tomarConfirmacion() {
        return solicitarConfirmacion(0);
    }

    public void informarRecepcion(int numeroBolsin) {
        System.out.println(LINEA_LARGA);
        System.out.println("  RECEPCION REGISTRADA EXITOSAMENTE");
        System.out.println(LINEA_LARGA);
        System.out.println();
        System.out.println("  Bolsin #" + numeroBolsin + ": RECIBIDO");
        System.out.println();
        Bolsin recibido = controller.buscarBolsinPorNumero(numeroBolsin);
        mostrarDetalleEstados(recibido);
        System.out.println(LINEA_LARGA);
    }

    private void mostrarDetalleEstados(Bolsin bolsin) {
        for (Remito remito : bolsin.getRemitos()) {
            System.out.println("  Remito #" + remito.getNumero() + ": RECIBIDO_ACEPTADO");
            for (DetalleRemito detalle : remito.getDetalles()) {
                Documentation doc = detalle.getDocumentacion();
                System.out.println("    Documento #" + doc.getNumero() + ": RECIBIDA_ACEPTADA");
                System.out.println("      Asunto: " + doc.getAsunto());
                System.out.println("      Tipo: " + doc.mostrarTipoDocumentacion());
            }
            System.out.println();
        }
    }
}
