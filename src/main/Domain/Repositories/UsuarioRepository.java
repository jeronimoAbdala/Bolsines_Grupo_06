package main.Domain.Repositories;

import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Empleado;
import main.Domain.Entities.Usuario;

import java.util.List;

public interface UsuarioRepository {

    Usuario buscarPorNombre(String nombreUsuario);

    void guardar(Usuario usuario);

    List<Usuario> obtenerTodos();

    void crearUsuario(String nombreUsuario, String password, Empleado empleado);

    List<ComisionMedica> obtenerComisionesMedicas();

    ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono);
}
