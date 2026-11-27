package main.Repositories;

import main.Domain.Entities.ComisionMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Comisiones medicas (clave: codigo). Solo la usa CargadorDatosDePrueba. */
@Repository
public interface IComisionMedicaRepository extends JpaRepository<ComisionMedica, Integer> {
}
