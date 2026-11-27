package main.Repositories;

import main.Domain.Entities.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Tipos de documento (clave: nombre). Solo la usa CargadorDatosDePrueba. */
@Repository
public interface ITipoDocumentoRepository extends JpaRepository<TipoDocumento, String> {
}
