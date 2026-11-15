package main.Infrastructure.Repositories;

import main.Domain.Entities.Estado;
import main.Domain.Repositories.EstadoRepository;
import main.Infrastructure.Datasources.EstadoJpaDatasource;

import java.util.List;

/** Sin anotaciones de Spring: se registra como @Bean en AppConfig, donde se arma la cadena de capas. */
public class EstadoRepositoryImpl implements EstadoRepository {

    private final EstadoJpaDatasource estadoDatasource;

    public EstadoRepositoryImpl(EstadoJpaDatasource estadoDatasource) {
        this.estadoDatasource = estadoDatasource;
    }

    @Override
    public List<Estado> obtenerTodos() {
        return estadoDatasource.findAll();
    }

    @Override
    public void guardar(Estado estado) {
        estadoDatasource.save(estado);
    }
}
