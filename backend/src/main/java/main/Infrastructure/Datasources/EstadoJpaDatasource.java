package main.Infrastructure.Datasources;

import main.Domain.Entities.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

/** Sin consultas por ambito/nombre: el Gestor recorre todos los estados (diagrama de secuencia). */
public interface EstadoJpaDatasource extends JpaRepository<Estado, Long> {
}
