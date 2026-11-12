package main.Domain.Repositories;

import main.Domain.Entities.Estado;

import java.util.List;

public interface EstadoRepository {

    /** Todos los estados: el Gestor los recorre con esAmbitoX()/esEstadoY() (paso 11 del CU). */
    List<Estado> obtenerTodos();

    void guardar(Estado estado);
}
