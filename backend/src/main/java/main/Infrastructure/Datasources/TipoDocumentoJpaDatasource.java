package main.Infrastructure.Datasources;

import main.Domain.Entities.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoDocumentoJpaDatasource extends JpaRepository<TipoDocumento, Long> {
}
