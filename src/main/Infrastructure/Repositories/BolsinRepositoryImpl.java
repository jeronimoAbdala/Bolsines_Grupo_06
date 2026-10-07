package main.Infrastructure.Repositories;

import main.Infrastructure.Datasources.SqliteBolsinDatasource;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Remito;
import main.Domain.Repositories.BolsinRepository;

import java.util.List;

public class BolsinRepositoryImpl implements BolsinRepository {

    private final SqliteBolsinDatasource datasource;

    public BolsinRepositoryImpl(SqliteBolsinDatasource datasource) {
        this.datasource = datasource;
    }

    @Override
    public Bolsin buscarPorNumero(int numero) {
        return datasource.buscarPorNumero(numero);
    }

    @Override
    public Bolsin buscarPorNumero(String numero) {
        return buscarPorNumero(Integer.parseInt(numero));
    }

    @Override
    public void guardar(Bolsin bolsin) {
        datasource.guardar(bolsin);
    }

    @Override
    public List<Bolsin> obtenerTodos() {
        return datasource.obtenerTodos();
    }

    @Override
    public List<Bolsin> obtenerEnviadosHacia(ComisionMedica cmDestino) {
        return datasource.obtenerEnviadosHacia(cmDestino);
    }

    @Override
    public List<ComisionMedica> obtenerComisionesMedicas() {
        return datasource.obtenerComisionesMedicas();
    }

    @Override
    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return datasource.crearComisionMedica(codigo, nombre, direccion, email, telefono);
    }

    @Override
    public void crearBolsin(int numero, String precinto, double peso, ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        datasource.crearBolsin(numero, precinto, peso, cmOrigen, cmDestino);
    }

    @Override
    public void agregarRemitoABolsin(int bolsinNumero, Remito remito) {
        datasource.agregarRemitoABolsin(bolsinNumero, remito);
    }

    @Override
    public int obtenerSiguienteNumeroBolsin() {
        return datasource.obtenerSiguienteNumeroBolsin();
    }

    @Override
    public int obtenerSiguienteNumeroRemito() {
        return datasource.obtenerSiguienteNumeroRemito();
    }

    @Override
    public int obtenerSiguienteNumeroDocumentacion() {
        return datasource.obtenerSiguienteNumeroDocumentacion();
    }
}
