package main.Gestores;

import main.Domain.Entities.*;
import main.Domain.Repositories.BolsinRepository;
import main.Domain.Repositories.EstadoRepository;
import main.Domain.Repositories.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Altas y consultas de apoyo (bolsines, remitos, usuarios y comisiones medicas)
 * para tener datos con los que probar el CU 28. No forma parte del diagrama del CU.
 */
public class GestorAdministracion {

    private final BolsinRepository bolsinRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoRepository estadoRepository;

    public GestorAdministracion(BolsinRepository bolsinRepository,
                                UsuarioRepository usuarioRepository,
                                EstadoRepository estadoRepository) {
        this.bolsinRepository = bolsinRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoRepository = estadoRepository;
    }

    public Bolsin buscarBolsinPorNumero(int numero) {
        return bolsinRepository.buscarPorNumero(numero);
    }

    public List<ComisionMedica> obtenerComisionesMedicas() {
        return bolsinRepository.obtenerComisionesMedicas();
    }

    public void crearBolsin(int numero, String precinto, double peso, ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        bolsinRepository.crearBolsin(numero, precinto, peso, cmOrigen, cmDestino);
    }

    public int obtenerSiguienteNumeroBolsin() {
        return bolsinRepository.obtenerSiguienteNumeroBolsin();
    }

    public int obtenerSiguienteNumeroRemito() {
        return bolsinRepository.obtenerSiguienteNumeroRemito();
    }

    public void crearRemitoConDocumentacion(int bolsinNumero, int remitoNumero,
                                            ComisionMedica cmOrigen, ComisionMedica cmDestino,
                                            String asunto, String descripcion, String tipoDoc) {
        Estado estadoRemitoEnviado = null;
        Estado estadoDocEnviada = null;
        for (Estado estado : estadoRepository.obtenerTodos()) {
            if (estado.esAmbitoRemito() && estado.esEnviado()) estadoRemitoEnviado = estado;
            if (estado.esAmbitoDocumentacion() && estado.esEnviado()) estadoDocEnviada = estado;
        }

        Remito remito = new Remito(remitoNumero, LocalDate.now(), estadoRemitoEnviado, cmOrigen, cmDestino);

        int docNumero = bolsinRepository.obtenerSiguienteNumeroDocumentacion();
        TipoDocumento tipo = new TipoDocumento(tipoDoc, "Tipo de documento: " + tipoDoc);
        Documento doc = new Documento(docNumero, asunto, descripcion, tipo);
        doc.crearNuevoCE(estadoDocEnviada, LocalDateTime.now(), null);

        remito.agregarDetalle(new DetalleRemito(doc));
        bolsinRepository.agregarRemitoABolsin(bolsinNumero, remito);
    }

    public void crearUsuario(String nombreUsuario, String password,
                             String nombreEmp, String apellidoEmp, String mailEmp, ComisionMedica cm) {
        Empleado emp = new Empleado(nombreEmp, apellidoEmp, mailEmp, cm);
        usuarioRepository.crearUsuario(nombreUsuario, password, emp);
    }

    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        return usuarioRepository.crearComisionMedica(codigo, nombre, direccion, email, telefono);
    }

    public List<Bolsin> obtenerTodosBolsines() {
        return bolsinRepository.obtenerTodos();
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.obtenerTodos();
    }
}
