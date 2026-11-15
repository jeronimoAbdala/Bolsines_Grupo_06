package main.Infrastructure.Datasources;

import main.Domain.Entities.Remito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Solo se usa para el siguiente numero: los remitos se guardan por cascada desde el Bolsin. */
public interface RemitoJpaDatasource extends JpaRepository<Remito, Integer> {

    @Query("select coalesce(max(r.numero), 500) + 1 from Remito r")
    int obtenerSiguienteNumero();
}
