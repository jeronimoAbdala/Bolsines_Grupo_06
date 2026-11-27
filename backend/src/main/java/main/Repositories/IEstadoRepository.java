package main.Repositories;

import main.Domain.Entities.Estado;
import main.Domain.Entities.EstadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Estados (clave: ambito + nombre). El Gestor usa findAll() al iniciar el CU. */
@Repository
public interface IEstadoRepository extends JpaRepository<Estado, EstadoId> {
}
