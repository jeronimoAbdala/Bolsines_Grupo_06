package main.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.Domain.Entities.Empleado;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Acceso a la base de los empleados (cada uno con su Usuario y su ComisionMedica). */
@Repository
public class EmpleadoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /** Paso 2 del CU 28: el Gestor los recorre preguntando esTuUsuario(usuario). */
    @Transactional(readOnly = true)
    public List<Empleado> obtenerTodos() {
        return entityManager.createQuery("select e from Empleado e", Empleado.class).getResultList();
    }

    /** Alta de un empleado nuevo: persist, que por cascada inserta tambien su Usuario. */
    @Transactional
    public void guardar(Empleado empleado) {
        entityManager.persist(empleado);
    }
}
