package main.Config;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.DetalleRemito;
import main.Domain.Entities.Documentacion;
import main.Domain.Entities.Empleado;
import main.Domain.Entities.Estado;
import main.Domain.Entities.Remito;
import main.Domain.Entities.TipoDocumento;
import main.Domain.Entities.Usuario;
import main.Repositories.BolsinRepository;
import main.Repositories.ComisionMedicaRepository;
import main.Repositories.EmpleadoRepository;
import main.Repositories.EstadoRepository;
import main.Repositories.TipoDocumentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Datos de prueba: se cargan al iniciar, SOLO si la base no tiene ningun Estado,
 * usando los repositorios (sin SQL a mano). Corre antes que InicioDeSesion.
 */
@Component
@Order(1)
public class CargadorDatosDePrueba implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargadorDatosDePrueba.class);

    private final EstadoRepository estadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ComisionMedicaRepository comisionMedicaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final BolsinRepository bolsinRepository;

    public CargadorDatosDePrueba(EstadoRepository estadoRepository,
                                 TipoDocumentoRepository tipoDocumentoRepository,
                                 ComisionMedicaRepository comisionMedicaRepository,
                                 EmpleadoRepository empleadoRepository,
                                 BolsinRepository bolsinRepository) {
        this.estadoRepository = estadoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.comisionMedicaRepository = comisionMedicaRepository;
        this.empleadoRepository = empleadoRepository;
        this.bolsinRepository = bolsinRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!estadoRepository.findAll().isEmpty()) {
            log.info("Datos de prueba: la base ya tiene datos, no se carga nada.");
            return;
        }
        log.info("Datos de prueba: base vacia, cargando...");

        // 1. Estados con los nombres de los diagramas: son los textos exactos que compara la clase Estado.
        Estado bolEnviado = estado("BOL", "Enviado", "Bolsín enviado desde comisión de origen");
        estado("BOL", "RecibidoEnCMDestino", "Bolsín recibido en comisión destino");
        Estado remEnviado = estado("REM", "Enviado", "Remito enviado");
        estado("REM", "RecibidoYAceptado", "Remito recibido y aceptado");
        estado("DOC", "Registrado", "Documentación registrada");
        estado("DOC", "EnRemito", "Documentación incluida en un remito");
        estado("DOC", "EnBolsinSaliente", "Documentación en bolsín saliente");
        Estado docEnBolsinEnviado = estado("DOC", "EnBolsinEnviado", "Documentación en bolsín enviado");
        estado("DOC", "NoRecibida", "Documentación no recibida");
        estado("DOC", "RecibidaYRechazada", "Documentación recibida y rechazada");
        estado("DOC", "ParaRedirigir", "Documentación para redirigir");
        estado("DOC", "RecibidaYAceptada", "Documentación recibida y aceptada");
        estado("DOC", "Eliminado", "Documentación eliminada");

        // 2. Tipos de documento del catalogo (los muestra el paso 6 por cada documentacion).
        TipoDocumento expediente = tipo("EXPEDIENTE", "Expediente médico");
        TipoDocumento informe = tipo("INFORME", "Informe");
        TipoDocumento certificado = tipo("CERTIFICADO", "Certificado");
        tipo("OTRO", "Otro");

        // 3. Comisiones medicas: se usa la instancia que devuelve save() (merge, clave asignada).
        ComisionMedica cordoba = comisionMedicaRepository.save(new ComisionMedica(
                1, "Cordoba", "Colon 100", "cm@correo.com", "351-123456"));
        ComisionMedica buenosAires = comisionMedicaRepository.save(new ComisionMedica(
                2, "Buenos Aires", "Av. 9 de Julio 100", "cmba@correo.com", "11-654321"));

        // 4. Empleados con su Usuario (cascada PERSIST Empleado -> Usuario).
        empleadoRepository.save(new Empleado("Jeronimo", "Abdala", "mail@mail.com",
                cordoba, new Usuario("jero", "1234")));
        Empleado maria = new Empleado("María", "López", "mlopez@mail.com",
                buenosAires, new Usuario("mlopez", "1234"));
        empleadoRepository.save(maria);

        // 5. Bolsines BA -> Cordoba. Envio 2 dias antes de la carga, a cargo de Maria Lopez (empleada de la CM origen),
        //    para que el historial de cada bolsin y documentacion empiece con un cambio de estado.
        //    Fecha de generacion del bolsin, fecha del remito y fecha de pase de la documentacion:
        //    el mismo dia del envio (iguales o anteriores al cambio de estado inicial).
        LocalDateTime envio = LocalDateTime.now().minusDays(2);

        //    Un DetalleRemito por cada documentacion (Remito 1..* DetalleRemito, DetalleRemito 1 Documentacion).
        //    El remito 500 tiene 3 documentaciones de tipos distintos: en el paso 6 el loop Detalles del Iterator da 3 vueltas.
        Bolsin b1001 = bolsin(1001, "PREC-001", 2.5, buenosAires, cordoba, bolEnviado, envio, maria);
        Remito r500 = remito(500, buenosAires, cordoba, remEnviado, envio);
        r500.agregarDetalle(new DetalleRemito(documentacion(1, "Expediente-cardiológico",
                "Historia clínica del paciente X", expediente, docEnBolsinEnviado, envio, maria)));
        r500.agregarDetalle(new DetalleRemito(documentacion(2, "Informe-electrocardiograma",
                "Electrocardiograma y ecocardiograma", informe, docEnBolsinEnviado, envio, maria)));
        r500.agregarDetalle(new DetalleRemito(documentacion(3, "Certificado-de-aptitud",
                "Certificado de aptitud laboral", certificado, docEnBolsinEnviado, envio, maria)));
        b1001.agregarRemito(r500);
        Remito r501 = remito(501, buenosAires, cordoba, remEnviado, envio);
        r501.agregarDetalle(new DetalleRemito(documentacion(4, "Estudios-complementarios",
                "Radiografías y análisis", expediente, docEnBolsinEnviado, envio, maria)));
        b1001.agregarRemito(r501);
        bolsinRepository.save(b1001);

        Bolsin b1002 = bolsin(1002, "PREC-002", 1.2, buenosAires, cordoba, bolEnviado, envio, maria);
        Remito r502 = remito(502, buenosAires, cordoba, remEnviado, envio);
        r502.agregarDetalle(new DetalleRemito(documentacion(5, "Turno-derivación",
                "Documentación para derivación", expediente, docEnBolsinEnviado, envio, maria)));
        b1002.agregarRemito(r502);
        bolsinRepository.save(b1002);

        log.info("Datos de prueba cargados: 13 estados, 4 tipos, 2 CM, 2 empleados, "
                + "bolsines 1001 y 1002 (3 remitos, 5 documentaciones).");
    }

    /** save() con id null hace persist: la misma instancia queda con su id y se puede referenciar. */
    private Estado estado(String ambito, String nombre, String descripcion) {
        Estado estado = new Estado(ambito, nombre, descripcion);
        estadoRepository.save(estado);
        return estado;
    }

    private TipoDocumento tipo(String nombre, String descripcion) {
        TipoDocumento tipo = new TipoDocumento(nombre, descripcion);
        tipoDocumentoRepository.save(tipo);
        return tipo;
    }

    private Bolsin bolsin(int numero, String precinto, double peso, ComisionMedica origen,
                          ComisionMedica destino, Estado enviado, LocalDateTime fechaHora, Empleado responsable) {
        Bolsin bolsin = new Bolsin(numero, origen, destino, enviado, fechaHora.toLocalDate());
        bolsin.setNroPrecinto(precinto);
        bolsin.setPeso(peso);
        bolsin.crearNuevoCE(enviado, fechaHora, responsable);
        return bolsin;
    }

    /** Remito sin documentacion: cada documentacion se agrega despues con su propio DetalleRemito. */
    private Remito remito(int numero, ComisionMedica origen, ComisionMedica destino,
                          Estado remEnviado, LocalDateTime fechaHora) {
        return new Remito(numero, fechaHora.toLocalDate(), remEnviado, origen, destino);
    }

    /** Documentacion con fecha de pase el dia del envio y su cambio de estado inicial (EnBolsinEnviado). */
    private Documentacion documentacion(int numero, String asunto, String descripcion, TipoDocumento tipo,
                                        Estado docEnBolsinEnviado, LocalDateTime fechaHora, Empleado responsable) {
        LocalDate fecha = fechaHora.toLocalDate();
        Documentacion doc = new Documentacion(numero, asunto, descripcion, docEnBolsinEnviado, tipo, fecha);
        doc.crearNuevoCE(docEnBolsinEnviado, fechaHora, responsable);
        return doc;
    }
}
