package main.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.Documentacion;
import main.Domain.Entities.Remito;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Acceso a la base de los bolsines: el Gestor no conoce JPA, las consultas ni las cascadas. */
@Repository
public class BolsinRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Paso 3 del CU 28: todos los bolsines, con lo que despues usan el Iterator (paso 6) y recibir() (paso 11),
     * asi el Gestor no vuelve a leerlos. Un JOIN FETCH por cada List (dos List en la misma consulta dan
     * MultipleBagFetchException); en la misma transaccion Hibernate tiene un solo objeto por fila, entonces
     * cada consulta completa las colecciones de los mismos objetos.
     */
    @Transactional(readOnly = true)
    public List<Bolsin> obtenerTodos() {
        List<Bolsin> bolsines = entityManager.createQuery(
                "select b from Bolsin b left join fetch b.cambiosEstado order by b.numero", Bolsin.class)
                .getResultList();
        // Completa los remitos de esos mismos bolsines.
        entityManager.createQuery("select b from Bolsin b left join fetch b.remitos", Bolsin.class)
                .getResultList();
        // Completa el historial de la documentacion (antes que los detalles, que la referencian).
        entityManager.createQuery("select d from Documentacion d left join fetch d.cambiosEstado", Documentacion.class)
                .getResultList();
        // Completa los detalles de cada remito.
        entityManager.createQuery("select r from Remito r left join fetch r.detalleRemito", Remito.class)
                .getResultList();
        return bolsines;
    }

    /** Paso 11 del CU 28: merge del bolsin; las cascadas guardan remitos, documentacion e historiales. */
    @Transactional
    public void guardar(Bolsin bolsin) {
        entityManager.merge(bolsin);
    }
}
