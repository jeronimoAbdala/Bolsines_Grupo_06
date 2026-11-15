package main.Domain.Repositories;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Remito;

import java.util.List;

public interface BolsinRepository {

    Bolsin buscarPorNumero(int numero);

    Bolsin buscarPorNumero(String numero);

    void guardar(Bolsin bolsin);

    List<Bolsin> obtenerTodos();

    List<ComisionMedica> obtenerComisionesMedicas();

    ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono);

    void agregarRemitoABolsin(int bolsinNumero, Remito remito);

    int obtenerSiguienteNumeroBolsin();

    int obtenerSiguienteNumeroRemito();

    int obtenerSiguienteNumeroDocumentacion();
}
