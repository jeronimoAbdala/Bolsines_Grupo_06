package main.Repositories;

import main.Domain.Entities.Empleado;
import main.Domain.Entities.EmpleadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Empleados (clave: nombre + apellido). El Gestor usa findAll() al iniciar el CU. */
@Repository
public interface IEmpleadoRepository extends JpaRepository<Empleado, EmpleadoId> {
}
