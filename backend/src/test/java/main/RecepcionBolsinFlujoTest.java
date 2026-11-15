package main;

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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU 28 de punta a punta, sin tocar la base de archivo: API REST -> Controller -> Gestor -> repositorios -> Hibernate -> H2 en memoria.
 * Los metodos corren EN ORDEN y comparten la misma base: primero se verifica el estado inicial,
 * despues se recibe el 1001 y al final se verifica el historial.
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
        mvc.perform(get(BASE + "/sesion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario", is("jero")))
                .andExpect(jsonPath("$.comisionMedica.nombre", is("Cordoba")));
    }

    @Test
    @Order(3)
    void paso3y4_bolsinesEnviadosALaCMDelEmpleado() throws Exception {
        mvc.perform(get(BASE + "/bolsines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].numero", containsInAnyOrder(1001, 1002)))
                .andExpect(jsonPath("$[*].estado", everyItem(is("Enviado"))));
    }

    @Test
    @Order(4)
    void paso5y6_remitosYDocumentacionDelBolsinSeleccionado() throws Exception {
        mvc.perform(get(BASE + "/bolsines/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsin.cantRemitos", is(2)))
                .andExpect(jsonPath("$.informacionRemito", containsString("Remito #500")))
                .andExpect(jsonPath("$.informacionRemito", containsString("Remito #501")));
    }

    @Test
    @Order(5)
    void opcionNoImplementada_responde501YNoCambiaNada() throws Exception {
        mvc.perform(recibir(1001, "{\"opcion\":2}"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.mensaje", containsString("no implementada")));
        mvc.perform(recibir(1001, "{\"opcion\":9}"))
                .andExpect(status().isBadRequest());

        assertThat(estadoDelBolsin(1001)).isEqualTo("Enviado");
        assertThat(jdbc.queryForObject(
                "select count(*) from cambio_estado_bolsin where bolsin_numero = 1001", Integer.class)).isEqualTo(1);
    }

    @Test
    @Order(6)
    void paso11_recibirConOpcion1() throws Exception {
        mvc.perform(recibir(1001, "{\"opcion\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("RecibidoEnCMDestino")))
                .andExpect(jsonPath("$.remitos[*].estado", everyItem(is("RecibidoYAceptado"))))
                .andExpect(jsonPath("$.remitos[*].documentos[*].estado", everyItem(is("RecibidaYAceptada"))));
    }

    @Test
    @Order(7)
    void paso11_historialDelBolsinConElEmpleadoResponsable() {
        List<Map<String, Object>> cambios = jdbc.queryForList("""
                select e.nombre as estado, ce.fecha_hora_inicio as inicio, ce.fecha_hora_fin as fin,
                       emp.nombre || ' ' || emp.apellido as responsable
                from cambio_estado_bolsin ce
                join estado e on e.id = ce.estado_id
                join empleado emp on emp.id = ce.empleado_id
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
        for (int doc : new int[]{1, 2}) {
            List<Map<String, Object>> cambios = jdbc.queryForList("""
                    select e.nombre as estado, ce.fecha_hora_fin as fin,
                           emp.nombre || ' ' || emp.apellido as responsable
                    from cambio_estado_documentacion ce
                    join estado e on e.id = ce.estado_id
                    join empleado emp on emp.id = ce.empleado_id
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
                select e.nombre from remito r join estado e on e.id = r.estado_id
                where r.bolsin_numero = 1001""", String.class)).containsOnly("RecibidoYAceptado");
        // El 1002 no se toco.
        assertThat(estadoDelBolsin(1002)).isEqualTo("Enviado");
        assertThat(jdbc.queryForObject(
                "select count(*) from cambio_estado_documentacion where documentacion_numero = 3", Integer.class))
                .isEqualTo(1);
    }

    @Test
    @Order(9)
    void despuesDeRecibir_soloQuedaPendienteEl1002() throws Exception {
        mvc.perform(get(BASE + "/bolsines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numero", is(1002)));
        mvc.perform(get(BASE + "/bolsines/1001"))
                .andExpect(status().isConflict());
    }

    private static org.springframework.test.web.servlet.RequestBuilder recibir(int numero, String cuerpo) {
        return post(BASE + "/bolsines/" + numero + "/recibir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo);
    }

    private String estadoDelBolsin(int numero) {
        return jdbc.queryForObject(
                "select e.nombre from bolsin b join estado e on e.id = b.estado_id where b.numero = ?",
                String.class, numero);
    }
}
