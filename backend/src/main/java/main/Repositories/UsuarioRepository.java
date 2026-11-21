package main.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.Domain.Entities.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Acceso a la base de los usuarios (el @Id es el nombre de usuario). */
@Repository
public class UsuarioRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /** Devuelve el usuario con ese nombre, o null si no existe. */
    @Transactional(readOnly = true)
    public Usuario buscarPorNombre(String nombreUsuario) {
        return entityManager.find(Usuario.class, nombreUsuario);
    }
}
