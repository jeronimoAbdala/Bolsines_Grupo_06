package main.Repositories;

import main.Domain.Entities.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Bolsin: findAll, findById y save los genera Spring Data. JpaRepository<Entidad, tipo de su @Id>.
 * Los remitos, sus detalles y la documentacion se guardan por cascada desde el Bolsin.
 */
public interface BolsinRepository extends JpaRepository<Bolsin, Integer> {
}
