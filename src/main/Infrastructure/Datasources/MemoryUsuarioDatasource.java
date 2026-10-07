package main.Infrastructure.Datasources;

import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Empleado;
import main.Domain.Entities.Usuario;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MemoryUsuarioDatasource implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ARCHIVO_DATOS = "data/usuarios.dat";

    private final List<Usuario> usuarios;

    public MemoryUsuarioDatasource() {
        this.usuarios = new ArrayList<>();
        cargar();
    }

    public List<Usuario> obtenerTodos() {
        return usuarios;
    }

    public void guardar(Usuario usuario) {
        usuarios.add(usuario);
        persistir();
    }

    private void cargar() {
        File archivo = new File(ARCHIVO_DATOS);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                @SuppressWarnings("unchecked")
                List<Usuario> cargados = (List<Usuario>) ois.readObject();
                usuarios.addAll(cargados);
                return;
            } catch (Exception ignored) {
            }
        }
        cargarDatosDePrueba();
    }

    private void persistir() {
        try {
            File directorio = new File("data");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
                oos.writeObject(new ArrayList<>(usuarios));
            }
        } catch (IOException ignored) {
        }
    }

    private void cargarDatosDePrueba() {
        ComisionMedica cordoba = new ComisionMedica(1, "Cordoba", "Colon 100", "cm@correo.com");
        Empleado empleado = new Empleado("Jeronimo", "Abdala", "mail@mail.com", cordoba);
        Usuario usuario = new Usuario("jero", "1234", empleado);
        guardar(usuario);
    }
}
