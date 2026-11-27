package main;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU 28 de punta a punta, sin tocar la base de archivo: PantallaRegistrarRecepcionBolsin (REST) -> Gestor
 * -> repositorios (EntityManager) -> Hibernate -> H2 en memoria.
 * Los metodos corren EN ORDEN y comparten la misma base y el mismo Gestor (guarda el estado del CU entre
 * pedidos): primero se verifica el estado inicial, despues se recibe el 1001 y se verifica el historial;
 * al final, que el 1001 ya no este pendiente, el Iterator del 1005 (4 documentaciones) y el historial del 1007,
 * que los datos de prueba cargan ya recibido. El 12 verifica que despues del inicio el Gestor no vuelve a consultar la base.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecepcionBolsinFlujoTest {

    private static final String BASE = "/api/recepcion";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @Order(1)
    void datosDePrueba_fechasIgualesAlEnvio() {
        LocalDate envio = LocalDate.now().minusDays(2);
        assertThat(jdbc.queryForObject("select fecha_generacion from bolsin where numero = 1001", LocalDate.class))
                .isEqualTo(envio);
        assertThat(jdbc.queryForObject("select fecha from remito where numero = 500", LocalDate.class))
                .isEqualTo(envio);
        assertThat(jdbc.queryForObject("select fecha_pase from documentacion where numero = 1", LocalDate.class))
                .isEqualTo(envio);
        assertThat(jdbc.queryForObject(
                "select cast(fecha_hora_inicio as date) from cambio_estado_bolsin where bolsin_numero = 1001",
                LocalDate.class)).isEqualTo(envio);
    }

    @Test
    @Order(2)
    void paso2_sesionDelEmpleadoLogueado() throws Exception {
        mvc.perform(post(BASE + "/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombreUsuario", is("jero")))
                .andExpect(jsonPath("$.usuario.comisionMedica.nombre", is("Cordoba")));
    }

    @Test
    @Order(3)
    void paso3y4_bolsinesEnviadosALaCMDelEmpleado() throws Exception {
        // El Gestor pide getCMOrigen() y getNroPrecinto() en el loop; la Pantalla arma cada fila
        // con mostrarCMOrigen() y mostrarNroPrecinto(), y agrega el texto de solicitarSeleccionBolsin().
        // No aparecen el 1006 (enviado a Buenos Aires: esTuCM() da falso) ni el 1007 (ya recibido: sosEnviado() da falso).
        mvc.perform(post(BASE + "/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsines[*].numero", containsInAnyOrder(1001, 1002, 1003, 1004, 1005)))
                .andExpect(jsonPath("$.bolsines[*].estado", everyItem(is("Enviado"))))
                .andExpect(jsonPath("$.bolsines[*].cmOrigen",
                        containsInAnyOrder("Buenos Aires", "Buenos Aires", "Rosario", "Mendoza", "Tucumán")))
                .andExpect(jsonPath("$.bolsines[*].nroPrecinto",
                        containsInAnyOrder("PREC-001", "PREC-002", "PREC-003", "PREC-004", "PREC-005")))
                .andExpect(jsonPath("$.solicitudSeleccion", is("Seleccione un bolsín para registrar su recepción")));
    }

    @Test
    @Order(4)
    void paso5y6_remitosYDocumentacionDelBolsinSeleccionado() throws Exception {
        // informacionRemito es lo que armo el Iterator (Bolsin -> Remito -> DetalleRemito) con los datos de prueba.
        // El remito 500 tiene 3 documentaciones de tipos distintos: el loop Detalles da 3 vueltas en el 500 y 1 en el 501.
        mvc.perform(post(BASE + "/seleccion/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsin.cantRemitos", is(2)))
                .andExpect(jsonPath("$.opcionesRecepcion", hasSize(4)))
                .andExpect(jsonPath("$.informacionRemito", is(
                        "Remito #500\n"
                                + "  Asunto: Expediente-cardiológico | Tipo: EXPEDIENTE\n"
                                + "  Asunto: Informe-electrocardiograma | Tipo: INFORME\n"
                                + "  Asunto: Certificado-de-aptitud | Tipo: CERTIFICADO\n"
                                + "Remito #501\n"
                                + "  Asunto: Estudios-complementarios | Tipo: EXPEDIENTE\n")));
    }

    @Test
    @Order(5)
    void opcionNoImplementada_responde501YNoCambiaNada() throws Exception {
        // Sigue seleccionado el 1001 (paso 5 del test anterior).
        mvc.perform(opcion("{\"opcion\":2}"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.mensaje", is("Opción no implementada")));
        mvc.perform(opcion("{\"opcion\":9}"))
                .andExpect(status().isBadRequest());
        // Con la opcion 2 guardada en el Gestor, confirmar no registra nada.
        mvc.perform(post(BASE + "/confirmacion"))
                .andExpect(status().isConflict());

        assertThat(estadoDelBolsin(1001)).isEqualTo("Enviado");
        assertThat(jdbc.queryForObject(
                "select count(*) from cambio_estado_bolsin where bolsin_numero = 1001", Integer.class)).isEqualTo(1);
    }

    @Test
    @Order(6)
    void paso11_recibirConOpcion1() throws Exception {
        mvc.perform(post(BASE + "/iniciar")).andExpect(status().isOk());
        mvc.perform(post(BASE + "/seleccion/1001")).andExpect(status().isOk());
        // solicitarConfirmacion(): el numero del bolsin que se pide confirmar.
        mvc.perform(opcion("{\"opcion\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1001"));
        mvc.perform(post(BASE + "/confirmacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsin.estado", is("RecibidoEnCMDestino")))
                .andExpect(jsonPath("$.bolsin.remitos[*].estado", everyItem(is("RecibidoYAceptado"))))
                .andExpect(jsonPath("$.bolsin.remitos[*].documentos[*].estado", everyItem(is("RecibidaYAceptada"))))
                // Paso 12: llamarCU29() incluye el CU 29 y la respuesta informa que la notificacion se envio.
                .andExpect(jsonPath("$.notificacionEnviada", is(true)));
    }

    @Test
    @Order(7)
    void paso11_historialDelBolsinConElEmpleadoResponsable() {
        List<Map<String, Object>> cambios = jdbc.queryForList("""
                select ce.estado_nombre as estado, ce.fecha_hora_inicio as inicio, ce.fecha_hora_fin as fin,
                       ce.empleado_nombre || ' ' || ce.empleado_apellido as responsable
                from cambio_estado_bolsin ce
                where ce.bolsin_numero = 1001
                order by ce.fecha_hora_inicio""");

        assertThat(cambios).hasSize(2);
        Map<String, Object> enviado = cambios.get(0);
        Map<String, Object> recibido = cambios.get(1);
        assertThat(enviado.get("estado")).isEqualTo("Enviado");
        assertThat(enviado.get("responsable")).isEqualTo("María López");
        assertThat(enviado.get("fin")).isNotNull();
        assertThat(recibido.get("estado")).isEqualTo("RecibidoEnCMDestino");
        assertThat(recibido.get("responsable")).isEqualTo("Jeronimo Abdala");
        assertThat(recibido.get("fin")).isNull();
        // Misma fecha/hora del Gestor: cierra el anterior y abre el nuevo.
        assertThat(enviado.get("fin")).isEqualTo(recibido.get("inicio"));
    }

    @Test
    @Order(8)
    void paso11_historialDeLaDocumentacionYEstadoDeLosRemitos() {
        // Documentaciones del 1001: 1, 2 y 3 (remito 500) y 4 (remito 501).
        for (int doc : new int[]{1, 2, 3, 4}) {
            List<Map<String, Object>> cambios = jdbc.queryForList("""
                    select ce.estado_nombre as estado, ce.fecha_hora_fin as fin,
                           ce.empleado_nombre || ' ' || ce.empleado_apellido as responsable
                    from cambio_estado_documentacion ce
                    where ce.documentacion_numero = ?
                    order by ce.fecha_hora_inicio""", doc);
            assertThat(cambios).hasSize(2);
            assertThat(cambios.get(0).get("estado")).isEqualTo("EnBolsinEnviado");
            assertThat(cambios.get(0).get("fin")).isNotNull();
            assertThat(cambios.get(1).get("estado")).isEqualTo("RecibidaYAceptada");
            assertThat(cambios.get(1).get("responsable")).isEqualTo("Jeronimo Abdala");
            assertThat(cambios.get(1).get("fin")).isNull();
        }
        assertThat(jdbc.queryForList("""
                select r.estado_nombre from remito r
                where r.bolsin_numero = 1001""", String.class)).containsOnly("RecibidoYAceptado");
        // El 1002 no se toco (su documentacion es la 5).
        assertThat(estadoDelBolsin(1002)).isEqualTo("Enviado");
        assertThat(jdbc.queryForObject(
                "select count(*) from cambio_estado_documentacion where documentacion_numero = 5", Integer.class))
                .isEqualTo(1);
    }

    @Test
    @Order(9)
    void despuesDeRecibir_el1001YaNoEstaPendiente() throws Exception {
        mvc.perform(post(BASE + "/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsines", hasSize(4)))
                .andExpect(jsonPath("$.bolsines[*].numero", containsInAnyOrder(1002, 1003, 1004, 1005)));
        mvc.perform(post(BASE + "/seleccion/1001"))
                .andExpect(status().isConflict());
        // finCU() dejo el CU sin bolsin seleccionado.
        mvc.perform(post(BASE + "/confirmacion"))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(10)
    void paso6_iteratorDelBolsin1005ConCuatroDocumentaciones() throws Exception {
        // Un solo remito (el loop Remitos da 1 vuelta) con 4 documentaciones, una de cada tipo (el loop Detalles da 4).
        mvc.perform(post(BASE + "/iniciar")).andExpect(status().isOk());
        mvc.perform(post(BASE + "/seleccion/1005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsin.cantRemitos", is(1)))
                .andExpect(jsonPath("$.bolsin.cmOrigen.nombre", is("Tucumán")))
                .andExpect(jsonPath("$.informacionRemito", is(
                        "Remito #506\n"
                                + "  Asunto: Reclamo-enfermedad-profesional | Tipo: EXPEDIENTE\n"
                                + "  Asunto: Informe-psicológico | Tipo: INFORME\n"
                                + "  Asunto: Certificado-de-reposo | Tipo: CERTIFICADO\n"
                                + "  Asunto: Constancia-de-turno-junta-médica | Tipo: OTRO\n")));
    }

    @Test
    @Order(11)
    void datosDePrueba_bolsin1007YaRecibidoConHistorialCoherente() {
        // Lo arma CargadorDatosDePrueba con Bolsin.recibir(), la misma logica del paso 11.
        List<Map<String, Object>> cambios = jdbc.queryForList("""
                select ce.estado_nombre as estado, ce.fecha_hora_inicio as inicio, ce.fecha_hora_fin as fin,
                       ce.empleado_nombre || ' ' || ce.empleado_apellido as responsable
                from cambio_estado_bolsin ce
                where ce.bolsin_numero = 1007
                order by ce.fecha_hora_inicio""");
        assertThat(cambios).hasSize(2);
        assertThat(cambios.get(0).get("estado")).isEqualTo("Enviado");
        assertThat(cambios.get(0).get("responsable")).isEqualTo("Lucía Fernández");
        assertThat(cambios.get(1).get("estado")).isEqualTo("RecibidoEnCMDestino");
        assertThat(cambios.get(1).get("responsable")).isEqualTo("Jeronimo Abdala");
        assertThat(cambios.get(1).get("fin")).isNull();
        assertThat(cambios.get(0).get("fin")).isEqualTo(cambios.get(1).get("inicio"));
        assertThat(estadoDelBolsin(1007)).isEqualTo("RecibidoEnCMDestino");

        assertThat(jdbc.queryForObject("""
                select r.estado_nombre from remito r
                where r.numero = 508""", String.class)).isEqualTo("RecibidoYAceptado");
        List<Map<String, Object>> cambiosDoc = jdbc.queryForList("""
                select ce.estado_nombre as estado, ce.fecha_hora_fin as fin,
                       ce.empleado_nombre || ' ' || ce.empleado_apellido as responsable
                from cambio_estado_documentacion ce
                where ce.documentacion_numero = 15
                order by ce.fecha_hora_inicio""");
        assertThat(cambiosDoc).hasSize(2);
        assertThat(cambiosDoc.get(0).get("estado")).isEqualTo("EnBolsinEnviado");
        assertThat(cambiosDoc.get(0).get("fin")).isNotNull();
        assertThat(cambiosDoc.get(1).get("estado")).isEqualTo("RecibidaYAceptada");
        assertThat(cambiosDoc.get(1).get("responsable")).isEqualTo("Jeronimo Abdala");
    }

    @Test
    @Order(12)
    void sinConsultasRepetidas_seleccionYConfirmacionUsanElBolsinDelPaso3() throws Exception {
        Statistics estadisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        mvc.perform(post(BASE + "/iniciar")).andExpect(status().isOk());

        // Seleccion (con el Iterator) y opcion: trabajan con el bolsin que trajo el paso 3, sin ninguna sentencia SQL.
        estadisticas.clear();
        mvc.perform(post(BASE + "/seleccion/1003")).andExpect(status().isOk());
        mvc.perform(opcion("{\"opcion\":1}")).andExpect(status().isOk());
        assertThat(estadisticas.getPrepareStatementCount()).isZero();

        // Confirmacion: ninguna consulta del Gestor (los estados se cargaron al iniciar); solo quedan los SELECT internos del merge de guardar().
        estadisticas.clear();
        mvc.perform(post(BASE + "/confirmacion")).andExpect(status().isOk());
        assertThat(estadisticas.getQueryExecutionCount()).isZero();
    }

    private static org.springframework.test.web.servlet.RequestBuilder opcion(String cuerpo) {
        return post(BASE + "/opcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo);
    }

    private String estadoDelBolsin(int numero) {
        return jdbc.queryForObject(
                "select estado_nombre from bolsin where numero = ?",
                String.class, numero);
    }
}
