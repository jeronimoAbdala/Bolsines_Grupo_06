package main.Infrastructure.Repositories;

import main.Infrastructure.Datasources.SqliteUsuarioDatasource;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Empleado;
import main.Domain.Entities.Usuario;
import main.Domain.Repositories.UsuarioRepository;

import java.util.List;

public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final SqliteUsuarioDatasource datasource;

    public UsuarioRepositoryImpl(SqliteUsuarioDatasource datasource) {
        this.datasource = datasource;
    }

    @Override
    public Usuario buscarPorNombre(String nombreUsuario) {
        return datasource.buscarPorNombre(nombreUsuario);
    }

    @Override
    public void guardar(Usuario usuario) {
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return datasource.obtenerTodos();
    }

    @Override
    public void crearUsuario(String nombreUsuario, String password, Empleado empleado) {
        datasource.crearUsuario(nombreUsuario, password, empleado);
    }

    @Override
    public List<ComisionMedica> obtenerComisionesMedicas() {
        return datasource.obtenerComisionesMedicas();
    }

    @Override
    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return datasource.crearComisionMedica(codigo, nombre, direccion, email, telefono);
    }
}
