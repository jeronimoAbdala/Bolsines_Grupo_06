package main.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.Domain.Entities.ComisionMedica;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Acceso a la base de las comisiones medicas (clave asignada: el codigo). */
@Repository
public class ComisionMedicaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /** Alta de una comision nueva: persist, la misma instancia queda guardada. */
    @Transactional
    public void guardar(ComisionMedica comisionMedica) {
        entityManager.persist(comisionMedica);
    }
}
