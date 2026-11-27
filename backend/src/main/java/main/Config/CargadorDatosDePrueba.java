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
import main.Repositories.IBolsinRepository;
import main.Repositories.IComisionMedicaRepository;
import main.Repositories.IEmpleadoRepository;
import main.Repositories.IEstadoRepository;
import main.Repositories.ITipoDocumentoRepository;
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

    private final IEstadoRepository estadoRepository;
    private final ITipoDocumentoRepository tipoDocumentoRepository;
    private final IComisionMedicaRepository comisionMedicaRepository;
    private final IEmpleadoRepository empleadoRepository;
    private final IBolsinRepository bolsinRepository;

    public CargadorDatosDePrueba(IEstadoRepository estadoRepository,
                                 ITipoDocumentoRepository tipoDocumentoRepository,
                                 IComisionMedicaRepository comisionMedicaRepository,
                                 IEmpleadoRepository empleadoRepository,
                                 IBolsinRepository bolsinRepository) {
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
        Estado bolRecibidoEnCMDestino = estado("BOL", "RecibidoEnCMDestino", "Bolsín recibido en comisión destino");
        Estado remEnviado = estado("REM", "Enviado", "Remito enviado");
        Estado remRecibidoYAceptado = estado("REM", "RecibidoYAceptado", "Remito recibido y aceptado");
        estado("DOC", "Registrado", "Documentación registrada");
        estado("DOC", "EnRemito", "Documentación incluida en un remito");
        estado("DOC", "EnBolsinSaliente", "Documentación en bolsín saliente");
        Estado docEnBolsinEnviado = estado("DOC", "EnBolsinEnviado", "Documentación en bolsín enviado");
        estado("DOC", "NoRecibida", "Documentación no recibida");
        estado("DOC", "RecibidaYRechazada", "Documentación recibida y rechazada");
        estado("DOC", "ParaRedirigir", "Documentación para redirigir");
        Estado docRecibidaYAceptada = estado("DOC", "RecibidaYAceptada", "Documentación recibida y aceptada");
        estado("DOC", "Eliminado", "Documentación eliminada");

        // 2. Tipos de documento del catalogo (los muestra el paso 6 por cada documentacion).
        TipoDocumento expediente = tipo("EXPEDIENTE", "Expediente médico");
        TipoDocumento informe = tipo("INFORME", "Informe");
        TipoDocumento certificado = tipo("CERTIFICADO", "Certificado");
        TipoDocumento otro = tipo("OTRO", "Otro");

        // 3. Comisiones medicas (clave asignada): se guardan y se siguen usando como referencia.
        ComisionMedica cordoba = new ComisionMedica(
                1, "Cordoba", "Colon 100", "cm@correo.com", "351-123456");
        comisionMedicaRepository.save(cordoba);
        ComisionMedica buenosAires = new ComisionMedica(
                2, "Buenos Aires", "Av. 9 de Julio 100", "cmba@correo.com", "11-654321");
        comisionMedicaRepository.save(buenosAires);
        ComisionMedica rosario = new ComisionMedica(
                3, "Rosario", "Bv. Oroño 1200", "cmrosario@correo.com", "341-456789");
        comisionMedicaRepository.save(rosario);
        ComisionMedica mendoza = new ComisionMedica(
                4, "Mendoza", "Av. San Martín 1500", "cmmendoza@correo.com", "261-345678");
        comisionMedicaRepository.save(mendoza);
        ComisionMedica tucuman = new ComisionMedica(
                5, "Tucumán", "Av. Sarmiento 800", "cmtucuman@correo.com", "381-234567");
        comisionMedicaRepository.save(tucuman);

        // 4. Empleados con su Usuario (cascada PERSIST Empleado -> Usuario). Jeronimo (jero) es el usuario logueado;
        //    los demas son los responsables de los envios de su CM.
        Empleado jero = new Empleado("Jeronimo", "Abdala", "mail@mail.com",
                cordoba, new Usuario("jero", "1234"));
        empleadoRepository.save(jero);
        Empleado maria = new Empleado("María", "López", "mlopez@mail.com",
                buenosAires, new Usuario("mlopez", "1234"));
        empleadoRepository.save(maria);
        Empleado lucia = new Empleado("Lucía", "Fernández", "lfernandez@mail.com",
                rosario, new Usuario("lfernandez", "1234"));
        empleadoRepository.save(lucia);
        Empleado martin = new Empleado("Martín", "Gómez", "mgomez@mail.com",
                mendoza, new Usuario("mgomez", "1234"));
        empleadoRepository.save(martin);
        Empleado sofia = new Empleado("Sofía", "Ruiz", "sruiz@mail.com",
                tucuman, new Usuario("sruiz", "1234"));
        empleadoRepository.save(sofia);

        // 5. Bolsines BA -> Cordoba. Envio 2 dias antes de la carga, a cargo de Maria Lopez (empleada de la CM origen),
        //    para que el historial de cada bolsin y documentacion empiece con un cambio de estado.
        //    Fecha de generacion del bolsin, fecha del remito y fecha de pase de la documentacion:
        //    el mismo dia del envio (iguales o anteriores al cambio de estado inicial).
        LocalDateTime envio = LocalDateTime.now().minusDays(2);

        //    Un DetalleRemito por cada documentacion (Remito 1..* DetalleRemito, DetalleRemito 1 Documentacion).
        //    El remito 500 tiene 3 documentaciones de tipos distintos: en el paso 6 el loop Detalles del Iterator da 3 vueltas.
        Bolsin b1001 = bolsin(1001, "PREC-001", 2.5, buenosAires, cordoba, bolEnviado, envio, maria);
        Remito r500 = remito(500, buenosAires, cordoba, remEnviado, envio);
        r500.agregarDetalle(new DetalleRemito(r500.getNumero(), documentacion(1, "Expediente-cardiológico",
                "Historia clínica del paciente X", expediente, docEnBolsinEnviado, envio, maria)));
        r500.agregarDetalle(new DetalleRemito(r500.getNumero(), documentacion(2, "Informe-electrocardiograma",
                "Electrocardiograma y ecocardiograma", informe, docEnBolsinEnviado, envio, maria)));
        r500.agregarDetalle(new DetalleRemito(r500.getNumero(), documentacion(3, "Certificado-de-aptitud",
                "Certificado de aptitud laboral", certificado, docEnBolsinEnviado, envio, maria)));
        b1001.agregarRemito(r500);
        Remito r501 = remito(501, buenosAires, cordoba, remEnviado, envio);
        r501.agregarDetalle(new DetalleRemito(r501.getNumero(), documentacion(4, "Estudios-complementarios",
                "Radiografías y análisis", expediente, docEnBolsinEnviado, envio, maria)));
        b1001.agregarRemito(r501);
        bolsinRepository.save(b1001);

        Bolsin b1002 = bolsin(1002, "PREC-002", 1.2, buenosAires, cordoba, bolEnviado, envio, maria);
        Remito r502 = remito(502, buenosAires, cordoba, remEnviado, envio);
        r502.agregarDetalle(new DetalleRemito(r502.getNumero(), documentacion(5, "Turno-derivación",
                "Documentación para derivación", expediente, docEnBolsinEnviado, envio, maria)));
        b1002.agregarRemito(r502);
        bolsinRepository.save(b1002);

        // 6. Mas bolsines pendientes para Cordoba, cada uno enviado otro dia y con distinto contenido
        //    (el Iterator del paso 6 da distinta cantidad de vueltas).
        LocalDateTime envio1003 = LocalDateTime.now().minusDays(3);
        Bolsin b1003 = bolsin(1003, "PREC-003", 0.8, rosario, cordoba, bolEnviado, envio1003, lucia);
        Remito r503 = remito(503, rosario, cordoba, remEnviado, envio1003);
        r503.agregarDetalle(new DetalleRemito(r503.getNumero(), documentacion(6, "Dictamen-de-incapacidad",
                "Dictamen de la junta médica por incapacidad laboral", expediente, docEnBolsinEnviado, envio1003, lucia)));
        b1003.agregarRemito(r503);
        bolsinRepository.save(b1003);

        //    1004: 2 remitos (2 y 1 documentaciones).
        LocalDateTime envio1004 = LocalDateTime.now().minusDays(4);
        Bolsin b1004 = bolsin(1004, "PREC-004", 3.1, mendoza, cordoba, bolEnviado, envio1004, martin);
        Remito r504 = remito(504, mendoza, cordoba, remEnviado, envio1004);
        r504.agregarDetalle(new DetalleRemito(r504.getNumero(), documentacion(7, "Informe-traumatológico",
                "Evaluación traumatológica posterior a accidente laboral", informe, docEnBolsinEnviado, envio1004, martin)));
        r504.agregarDetalle(new DetalleRemito(r504.getNumero(), documentacion(8, "Certificado-de-alta-médica",
                "Alta médica con reincorporación al puesto de trabajo", certificado, docEnBolsinEnviado, envio1004, martin)));
        b1004.agregarRemito(r504);
        Remito r505 = remito(505, mendoza, cordoba, remEnviado, envio1004);
        r505.agregarDetalle(new DetalleRemito(r505.getNumero(), documentacion(9, "Historia-clínica-laboral",
                "Antecedentes médicos y laborales del trabajador", expediente, docEnBolsinEnviado, envio1004, martin)));
        b1004.agregarRemito(r505);
        bolsinRepository.save(b1004);

        //    1005: 1 remito con 4 documentaciones, una de cada tipo.
        LocalDateTime envio1005 = LocalDateTime.now().minusDays(5);
        Bolsin b1005 = bolsin(1005, "PREC-005", 2.0, tucuman, cordoba, bolEnviado, envio1005, sofia);
        Remito r506 = remito(506, tucuman, cordoba, remEnviado, envio1005);
        r506.agregarDetalle(new DetalleRemito(r506.getNumero(), documentacion(10, "Reclamo-enfermedad-profesional",
                "Reclamo ante la ART por enfermedad profesional", expediente, docEnBolsinEnviado, envio1005, sofia)));
        r506.agregarDetalle(new DetalleRemito(r506.getNumero(), documentacion(11, "Informe-psicológico",
                "Evaluación psicológica del trabajador", informe, docEnBolsinEnviado, envio1005, sofia)));
        r506.agregarDetalle(new DetalleRemito(r506.getNumero(), documentacion(12, "Certificado-de-reposo",
                "Reposo laboral indicado por el médico tratante", certificado, docEnBolsinEnviado, envio1005, sofia)));
        r506.agregarDetalle(new DetalleRemito(r506.getNumero(), documentacion(13, "Constancia-de-turno-junta-médica",
                "Constancia del turno asignado para la junta médica", otro, docEnBolsinEnviado, envio1005, sofia)));
        b1005.agregarRemito(r506);
        bolsinRepository.save(b1005);

        // 7. Bolsines que NO aparecen en la lista del paso 3.
        //    1006: enviado a OTRA CM (Buenos Aires): esTuCM("Cordoba") da falso.
        LocalDateTime envio1006 = LocalDateTime.now().minusDays(6);
        Bolsin b1006 = bolsin(1006, "PREC-006", 1.5, mendoza, buenosAires, bolEnviado, envio1006, martin);
        Remito r507 = remito(507, mendoza, buenosAires, remEnviado, envio1006);
        r507.agregarDetalle(new DetalleRemito(r507.getNumero(), documentacion(14, "Informe-audiométrico",
                "Audiometría por hipoacusia laboral", informe, docEnBolsinEnviado, envio1006, martin)));
        b1006.agregarRemito(r507);
        bolsinRepository.save(b1006);

        //    1007: YA RECIBIDO en Cordoba: sosEnviado() da falso. El historial lo arma la misma logica
        //    del paso 11 del CU (Bolsin.recibir), a cargo de jero, un dia despues del envio.
        LocalDateTime envio1007 = LocalDateTime.now().minusDays(8);
        Bolsin b1007 = bolsin(1007, "PREC-007", 0.9, rosario, cordoba, bolEnviado, envio1007, lucia);
        Remito r508 = remito(508, rosario, cordoba, remEnviado, envio1007);
        r508.agregarDetalle(new DetalleRemito(r508.getNumero(), documentacion(15, "Certificado-de-discapacidad",
                "Certificado único de discapacidad", certificado, docEnBolsinEnviado, envio1007, lucia)));
        b1007.agregarRemito(r508);
        b1007.recibir(bolRecibidoEnCMDestino, remRecibidoYAceptado, docRecibidaYAceptada, jero, envio1007.plusDays(1));
        bolsinRepository.save(b1007);

        log.info("Datos de prueba cargados: 13 estados, 4 tipos, 5 CM, 5 empleados, "
                + "bolsines 1001 a 1007 (9 remitos, 15 documentaciones).");
    }

    /**
     * save() hace merge (la clave natural ya viene puesta): la instancia original se sigue usando como
     * referencia en los bolsines, y el merge de cada bolsin la encuentra por su clave.
     */
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
