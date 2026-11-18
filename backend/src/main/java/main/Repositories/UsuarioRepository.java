package main.Repositories;

import main.Domain.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** El @Id de Usuario es el nombre de usuario (String). */
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
