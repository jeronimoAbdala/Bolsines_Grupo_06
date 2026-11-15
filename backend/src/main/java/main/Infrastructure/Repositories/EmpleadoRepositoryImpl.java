package main.Infrastructure.Repositories;

import main.Domain.Entities.Empleado;
import main.Domain.Repositories.EmpleadoRepository;
import main.Infrastructure.Datasources.EmpleadoJpaDatasource;

import java.util.List;

/** Sin anotaciones de Spring: se registra como @Bean en AppConfig, donde se arma la cadena de capas. */
public class EmpleadoRepositoryImpl implements EmpleadoRepository {

    private final EmpleadoJpaDatasource empleadoDatasource;

    public EmpleadoRepositoryImpl(EmpleadoJpaDatasource empleadoDatasource) {
        this.empleadoDatasource = empleadoDatasource;
    }

    @Override
    public List<Empleado> obtenerTodos() {
        return empleadoDatasource.findAll();
    }

    /** Empleado nuevo (id null) -> persist; por la cascada PERSIST tambien inserta su Usuario. */
    @Override
    public void guardar(Empleado empleado) {
        empleadoDatasource.save(empleado);
    }
}
