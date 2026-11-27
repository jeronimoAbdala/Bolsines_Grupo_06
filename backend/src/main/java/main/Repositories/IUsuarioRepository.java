package main.Repositories;

import main.Domain.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Usuarios (clave: nombre de usuario). Solo la usa InicioDeSesion. */
@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, String> {
}
