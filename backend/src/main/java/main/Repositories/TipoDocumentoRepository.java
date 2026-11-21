package main.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.Domain.Entities.TipoDocumento;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Acceso a la base del catalogo de tipos de documento. */
@Repository
public class TipoDocumentoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /** Alta de un tipo nuevo: persist, la misma instancia queda con su id. */
    @Transactional
    public void guardar(TipoDocumento tipoDocumento) {
        entityManager.persist(tipoDocumento);
    }
}
