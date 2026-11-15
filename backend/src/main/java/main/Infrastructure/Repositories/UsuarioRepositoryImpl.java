package main.Infrastructure.Repositories;

import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Usuario;
import main.Domain.Repositories.UsuarioRepository;
import main.Infrastructure.Datasources.ComisionMedicaJpaDatasource;
import main.Infrastructure.Datasources.UsuarioJpaDatasource;

import java.util.List;

/** Sin anotaciones de Spring: se registra como @Bean en AppConfig, donde se arma la cadena de capas. */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaDatasource usuarioDatasource;
    private final ComisionMedicaJpaDatasource comisionMedicaDatasource;

    public UsuarioRepositoryImpl(UsuarioJpaDatasource usuarioDatasource,
                                 ComisionMedicaJpaDatasource comisionMedicaDatasource) {
        this.usuarioDatasource = usuarioDatasource;
        this.comisionMedicaDatasource = comisionMedicaDatasource;
    }

    /** null si no existe (el Gestor y la API ya manejan ese caso). */
    @Override
    public Usuario buscarPorNombre(String nombreUsuario) {
        return usuarioDatasource.findById(nombreUsuario).orElse(null);
    }

    @Override
    public void guardar(Usuario usuario) {
        usuarioDatasource.save(usuario);
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioDatasource.findAll();
    }

    @Override
    public List<ComisionMedica> obtenerComisionesMedicas() {
        return comisionMedicaDatasource.findAll();
    }

    /** save() con un codigo que ya existe hace UPDATE: crea la comision o actualiza sus datos. */
    @Override
    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return comisionMedicaDatasource.save(new ComisionMedica(codigo, nombre, direccion, email, telefono));
    }
}
