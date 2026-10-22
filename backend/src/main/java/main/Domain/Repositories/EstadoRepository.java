package main.Domain.Repositories;

import main.Domain.Entities.Estado;

import java.util.List;

public interface EstadoRepository {

    List<Estado> obtenerTodos();
}
