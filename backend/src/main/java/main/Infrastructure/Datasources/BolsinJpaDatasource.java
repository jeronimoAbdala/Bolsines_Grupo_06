package main.Infrastructure.Datasources;

import main.Domain.Entities.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Datasource de Bolsin con Spring Data JPA: la implementacion (save, findById, findAll, ...)
 * la genera Spring en tiempo de ejecucion. JpaRepository<Entidad, tipo de su @Id>.
 */
public interface BolsinJpaDatasource extends JpaRepository<Bolsin, Integer> {

    /** JPQL (sobre la entidad Bolsin y su atributo numero, no sobre la tabla). Con la tabla vacia el primer numero es 1001. */
    @Query("select coalesce(max(b.numero), 1000) + 1 from Bolsin b")
    int obtenerSiguienteNumero();
}
