package main.Repositories;

import main.Domain.Entities.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

/** Sin consultas por ambito/nombre: el Gestor recorre findAll() preguntando esAmbitoX()/esEstadoY() (diagrama). */
public interface EstadoRepository extends JpaRepository<Estado, Long> {
}
