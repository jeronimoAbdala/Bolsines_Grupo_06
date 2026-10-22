package main.Controllers;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Usuario;
import main.Gestores.GestorAdministracion;

import java.util.List;

public class AdministracionController {

    private final GestorAdministracion gestor;

    public AdministracionController(GestorAdministracion gestor) {
        this.gestor = gestor;
    }

    public Bolsin buscarBolsinPorNumero(int numero) {
        return gestor.buscarBolsinPorNumero(numero);
    }

    public List<ComisionMedica> obtenerComisionesMedicas() {
        return gestor.obtenerComisionesMedicas();
    }

    public void crearBolsin(int numero, String precinto, double peso, ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        gestor.crearBolsin(numero, precinto, peso, cmOrigen, cmDestino);
    }

    public int obtenerSiguienteNumeroBolsin() {
        return gestor.obtenerSiguienteNumeroBolsin();
    }

    public int obtenerSiguienteNumeroRemito() {
        return gestor.obtenerSiguienteNumeroRemito();
    }

    public void crearRemitoConDocumentacion(int bolsinNumero, int remitoNumero,
                                            ComisionMedica cmOrigen, ComisionMedica cmDestino,
                                            String asunto, String descripcion, String tipoDoc) {
        gestor.crearRemitoConDocumentacion(bolsinNumero, remitoNumero, cmOrigen, cmDestino, asunto, descripcion, tipoDoc);
    }

    public void crearUsuario(String nombreUsuario, String password, String nombreEmp, String apellidoEmp, String mailEmp, ComisionMedica cm) {
        gestor.crearUsuario(nombreUsuario, password, nombreEmp, apellidoEmp, mailEmp, cm);
    }

    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return gestor.crearComisionMedica(codigo, nombre, direccion, email, telefono);
    }

    public List<Bolsin> obtenerTodosBolsines() {
        return gestor.obtenerTodosBolsines();
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return gestor.obtenerTodosUsuarios();
    }
}
