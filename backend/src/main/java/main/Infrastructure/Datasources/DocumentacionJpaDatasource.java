package main.Infrastructure.Datasources;

import main.Domain.Entities.Documentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Solo se usa para el siguiente numero: la documentacion se guarda por cascada desde el DetalleRemito. */
public interface DocumentacionJpaDatasource extends JpaRepository<Documentacion, Integer> {

    @Query("select coalesce(max(d.numero), 0) + 1 from Documentacion d")
    int obtenerSiguienteNumero();
}
