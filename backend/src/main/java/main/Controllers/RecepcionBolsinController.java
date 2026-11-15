package main.Controllers;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Empleado;
import main.Gestores.GestorRecepcionBolsin;

import java.util.List;

public class RecepcionBolsinController {

    private final GestorRecepcionBolsin gestor;

    public RecepcionBolsinController(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    public Empleado buscarUsuarioLogueado() {
        return gestor.buscarUsuarioLogueado();
    }

    public List<Bolsin> buscarBolsinesEnviados() {
        return gestor.buscarBolsinesEnviados();
    }

    public Bolsin buscarBolsinPorNumero(int numero) {
        return gestor.buscarBolsinPorNumero(numero);
    }

    public String obtenerInformacionRemito() {
        return gestor.obtenerInformacionRemito();
    }

    public void tomarSeleccionBolsin(int numero) {
        gestor.tomarSeleccionBolsin(numero);
    }

    /**
     * Opcion de recepcion elegida por el usuario y confirmada. Se vuelve a tomar la seleccion
     * para trabajar con el bolsin cargado en este request. Secuencia: tomarOpcionDeRecepcion ->
     * (solicitarConfirmacion en la pantalla) -> tomarConfirmacion (paso 10), que dispara el paso 11.
     */
    public void registrarRecepcion(int numeroBolsin, int opcion) {
        gestor.tomarSeleccionBolsin(numeroBolsin);
        gestor.tomarOpcionDeRecepcion(opcion);
        gestor.tomarConfirmacion(true);
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

    public int obtenerSiguienteNumeroDocumentacion() {
        return gestor.obtenerSiguienteNumeroDocumentacion();
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

    public List<Empleado> obtenerTodosEmpleados() {
        return gestor.obtenerTodosEmpleados();
    }
}
