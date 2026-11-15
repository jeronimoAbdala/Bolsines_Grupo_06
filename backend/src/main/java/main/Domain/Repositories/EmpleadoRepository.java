package main.Domain.Repositories;

import main.Domain.Entities.Empleado;

import java.util.List;

public interface EmpleadoRepository {

    List<Empleado> obtenerTodos();

    /** Guarda el empleado; por cascada PERSIST/MERGE tambien guarda su Usuario. */
    void guardar(Empleado empleado);
}
