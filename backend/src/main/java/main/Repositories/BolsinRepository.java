package main.Repositories;

import main.Domain.Entities.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio de Bolsin: findAll, findById y save los genera Spring Data. JpaRepository<Entidad, tipo de su @Id>.
 * Las 3 consultas de numeracion son JPQL (sobre las entidades, no sobre las tablas).
 */
public interface BolsinRepository extends JpaRepository<Bolsin, Integer> {

    /** Con la tabla vacia el primer numero es 1001. */
    @Query("select coalesce(max(b.numero), 1000) + 1 from Bolsin b")
    int obtenerSiguienteNumeroBolsin();

    /** Los remitos se guardan por cascada desde el Bolsin: solo hace falta su siguiente numero. */
    @Query("select coalesce(max(r.numero), 500) + 1 from Remito r")
    int obtenerSiguienteNumeroRemito();

    /** La documentacion se guarda por cascada desde el DetalleRemito: solo hace falta su siguiente numero. */
    @Query("select coalesce(max(d.numero), 0) + 1 from Documentacion d")
    int obtenerSiguienteNumeroDocumentacion();
}
