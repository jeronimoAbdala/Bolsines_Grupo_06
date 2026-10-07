package main.Presentation.Screens;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.DetalleRemito;
import main.Domain.Entities.Documentation;
import main.Domain.Entities.Remito;
import main.Domain.Entities.Usuario;

import java.util.List;

public class PantallaListar {

    private static final String LINEA = "========================================";
    private static final String SEPARADOR = "----------------------------------------";

    private final RecepcionBolsinController controller;

    public PantallaListar(RecepcionBolsinController controller) {
        this.controller = controller;
    }

    public void listarBolsines() {
        System.out.println(LINEA);
        System.out.println("           LISTA DE BOLSINES");
        System.out.println(LINEA);
        System.out.println();

        List<Bolsin> bolsines = controller.obtenerTodosBolsines();
        if (bolsines.isEmpty()) {
            System.out.println("  No hay bolsines registrados.");
        } else {
            for (Bolsin b : bolsines) {
                System.out.println("  Bolsin #" + b.getNumero());
                System.out.println("    " + b.getCmOrigen().getNombre() + " -> " + b.getCmDestino().getNombre());
                System.out.println("    Precinto: " + (b.getNroPrecinto() != null ? b.getNroPrecinto() : "N/A"));
                System.out.println("    Peso: " + b.getPeso() + " kg");
                System.out.println("    Estado: " + b.getEstado().getNombre());
                System.out.println("    Remitos: " + b.getCantRemitos());

                for (Remito r : b.getRemitos()) {
                    System.out.println("      Remito #" + r.getNumero() + " (" + r.getEstado().getNombre() + ")");
                    for (DetalleRemito d : r.getDetalles()) {
                        Documentation doc = d.getDocumentacion();
                        System.out.println("        Doc #" + doc.getNumero() + ": " + doc.getAsunto()
                                + " [" + doc.getEstado().getNombre() + "]");
                    }
                }
                System.out.println();
            }
        }
        System.out.println(LINEA);
        System.out.println();
    }

    public void listarUsuarios() {
        System.out.println(LINEA);
        System.out.println("           LISTA DE USUARIOS");
        System.out.println(LINEA);
        System.out.println();

        List<Usuario> usuarios = controller.obtenerTodosUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("  No hay usuarios registrados.");
        } else {
            for (Usuario u : usuarios) {
                System.out.println("  Usuario: " + u.getNombreUsuario());
                System.out.println("    Empleado: " + u.getEmpleado().getNombreCompleto());
                System.out.println("    Mail: " + u.getEmpleado().getMail());
                System.out.println("    CM: " + u.getEmpleado().getComisionMedica().getNombre());
                System.out.println();
            }
        }
        System.out.println(LINEA);
        System.out.println();
    }
}
