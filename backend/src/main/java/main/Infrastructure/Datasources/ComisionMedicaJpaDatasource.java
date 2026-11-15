package main.Infrastructure.Datasources;

import main.Domain.Entities.ComisionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

/** Compartido por BolsinRepositoryImpl y UsuarioRepositoryImpl: los dos repositorios del dominio
 * ofrecen las comisiones medicas y ambos las leen de la misma tabla. */
public interface ComisionMedicaJpaDatasource extends JpaRepository<ComisionMedica, Integer> {
}
