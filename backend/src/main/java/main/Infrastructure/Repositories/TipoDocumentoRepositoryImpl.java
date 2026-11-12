package main.Infrastructure.Repositories;

import main.Domain.Entities.TipoDocumento;
import main.Domain.Repositories.TipoDocumentoRepository;
import main.Infrastructure.Datasources.TipoDocumentoJpaDatasource;

import java.util.List;

/** Sin anotaciones de Spring: se registra como @Bean en AppConfig, donde se arma la cadena de capas. */
public class TipoDocumentoRepositoryImpl implements TipoDocumentoRepository {

    private final TipoDocumentoJpaDatasource tipoDocumentoDatasource;

    public TipoDocumentoRepositoryImpl(TipoDocumentoJpaDatasource tipoDocumentoDatasource) {
        this.tipoDocumentoDatasource = tipoDocumentoDatasource;
    }

    @Override
    public List<TipoDocumento> obtenerTodos() {
        return tipoDocumentoDatasource.findAll();
    }

    @Override
    public void guardar(TipoDocumento tipoDocumento) {
        tipoDocumentoDatasource.save(tipoDocumento);
    }
}
