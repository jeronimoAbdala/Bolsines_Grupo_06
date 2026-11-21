package main.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.Domain.Entities.Estado;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Sin consultas por ambito/nombre: el Gestor recorre obtenerTodos() preguntando esAmbitoX()/esEstadoY() (diagrama). */
@Repository
public class EstadoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Estado> obtenerTodos() {
        return entityManager.createQuery("select e from Estado e", Estado.class).getResultList();
    }

    /** Alta de un estado nuevo: persist, la misma instancia queda con su id. */
    @Transactional
    public void guardar(Estado estado) {
        entityManager.persist(estado);
    }
}
