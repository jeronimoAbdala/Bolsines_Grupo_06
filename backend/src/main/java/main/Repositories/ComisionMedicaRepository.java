package main.Repositories;

import main.Domain.Entities.ComisionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

/** save() con un codigo que ya existe hace UPDATE (clave asignada): crea la comision o actualiza sus datos. */
public interface ComisionMedicaRepository extends JpaRepository<ComisionMedica, Integer> {
}
