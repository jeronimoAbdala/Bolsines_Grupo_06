package main.Infrastructure.Datasources;

import main.Domain.Entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoJpaDatasource extends JpaRepository<Empleado, Long> {
}
