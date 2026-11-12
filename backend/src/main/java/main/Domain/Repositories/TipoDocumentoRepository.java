package main.Domain.Repositories;

import main.Domain.Entities.TipoDocumento;

import java.util.List;

public interface TipoDocumentoRepository {

    List<TipoDocumento> obtenerTodos();

    void guardar(TipoDocumento tipoDocumento);
}
