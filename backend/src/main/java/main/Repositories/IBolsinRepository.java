package main.Repositories;

import main.Domain.Entities.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Bolsines (clave: numero). El Gestor usa findAll() al iniciar el CU y save() al confirmar. */
@Repository
public interface IBolsinRepository extends JpaRepository<Bolsin, Integer> {
}
