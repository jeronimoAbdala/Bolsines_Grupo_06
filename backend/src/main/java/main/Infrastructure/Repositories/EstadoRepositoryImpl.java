package main.Infrastructure.Repositories;

import main.Domain.Entities.Estado;
import main.Domain.Repositories.EstadoRepository;
import main.Infrastructure.Datasources.SqliteEstadoDatasource;

import java.util.List;

public class EstadoRepositoryImpl implements EstadoRepository {

    private final SqliteEstadoDatasource datasource;

    public EstadoRepositoryImpl(SqliteEstadoDatasource datasource) {
        this.datasource = datasource;
    }

    @Override
    public List<Estado> obtenerTodos() {
        return datasource.obtenerTodos();
    }
}
