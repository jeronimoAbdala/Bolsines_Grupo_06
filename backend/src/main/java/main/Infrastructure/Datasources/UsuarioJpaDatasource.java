package main.Infrastructure.Datasources;

import main.Domain.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** El @Id de Usuario es el nombre de usuario (String). */
public interface UsuarioJpaDatasource extends JpaRepository<Usuario, String> {
}
