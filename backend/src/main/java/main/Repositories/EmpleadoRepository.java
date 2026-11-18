package main.Repositories;

import main.Domain.Entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

/** save() de un empleado nuevo tambien inserta su Usuario (cascada PERSIST/MERGE). */
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}
