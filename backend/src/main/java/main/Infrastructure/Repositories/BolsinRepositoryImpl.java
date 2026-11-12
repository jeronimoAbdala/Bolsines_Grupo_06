package main.Infrastructure.Repositories;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Remito;
import main.Domain.Repositories.BolsinRepository;
import main.Infrastructure.Datasources.BolsinJpaDatasource;
import main.Infrastructure.Datasources.ComisionMedicaJpaDatasource;
import main.Infrastructure.Datasources.DocumentacionJpaDatasource;
import main.Infrastructure.Datasources.RemitoJpaDatasource;

import java.util.List;

/**
 * Capa de repositorio que separa el dominio de la tecnologia de persistencia:
 * implementa BolsinRepository (dominio) usando los datasources de Spring Data JPA.
 * No lleva anotaciones de Spring: se registra como @Bean en AppConfig, donde se arma la cadena de capas.
 */
public class BolsinRepositoryImpl implements BolsinRepository {

    private final BolsinJpaDatasource bolsinDatasource;
    private final RemitoJpaDatasource remitoDatasource;
    private final DocumentacionJpaDatasource documentacionDatasource;
    private final ComisionMedicaJpaDatasource comisionMedicaDatasource;

    public BolsinRepositoryImpl(BolsinJpaDatasource bolsinDatasource,
                                RemitoJpaDatasource remitoDatasource,
                                DocumentacionJpaDatasource documentacionDatasource,
                                ComisionMedicaJpaDatasource comisionMedicaDatasource) {
        this.bolsinDatasource = bolsinDatasource;
        this.remitoDatasource = remitoDatasource;
        this.documentacionDatasource = documentacionDatasource;
        this.comisionMedicaDatasource = comisionMedicaDatasource;
    }

    /** null si no existe (el Gestor y la API ya manejan ese caso). */
    @Override
    public Bolsin buscarPorNumero(int numero) {
        return bolsinDatasource.findById(numero).orElse(null);
    }

    @Override
    public Bolsin buscarPorNumero(String numero) {
        return buscarPorNumero(Integer.parseInt(numero));
    }

    /**
     * save(): como el numero es una clave asignada, Spring Data hace merge
     * (SELECT y despues INSERT o UPDATE). Por las cascadas guarda remitos, detalles,
     * documentacion e historiales en la misma transaccion.
     */
    @Override
    public void guardar(Bolsin bolsin) {
        bolsinDatasource.save(bolsin);
    }

    @Override
    public List<Bolsin> obtenerTodos() {
        return bolsinDatasource.findAll();
    }

    @Override
    public List<ComisionMedica> obtenerComisionesMedicas() {
        return comisionMedicaDatasource.findAll();
    }

    /** save() con un codigo que ya existe hace UPDATE: crea la comision o actualiza sus datos. */
    @Override
    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return comisionMedicaDatasource.save(new ComisionMedica(codigo, nombre, direccion, email, telefono));
    }

    /** El remito nuevo se inserta por la cascada ALL de Bolsin.remitos (y sus detalles/documentacion). */
    @Override
    public void agregarRemitoABolsin(int bolsinNumero, Remito remito) {
        Bolsin bolsin = buscarPorNumero(bolsinNumero);
        if (bolsin != null) {
            bolsin.agregarRemito(remito);
            guardar(bolsin);
        }
    }

    @Override
    public int obtenerSiguienteNumeroBolsin() {
        return bolsinDatasource.obtenerSiguienteNumero();
    }

    @Override
    public int obtenerSiguienteNumeroRemito() {
        return remitoDatasource.obtenerSiguienteNumero();
    }

    @Override
    public int obtenerSiguienteNumeroDocumentacion() {
        return documentacionDatasource.obtenerSiguienteNumero();
    }
}
