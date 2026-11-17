package main.Domain.Entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Paso 6 del CU 28: recorrido anidado completo con el patron Iterator, en memoria (sin Spring ni base).
 * Bolsin (IteradorRemito) -> Remito (IteradorDetalle) -> DetalleRemito -> Documentacion -> TipoDocumento.
 */
class RecorridoIteratorTest {

    private final ComisionMedica buenosAires = new ComisionMedica(2, "Buenos Aires", "", "");
    private final ComisionMedica cordoba = new ComisionMedica(1, "Cordoba", "", "");
    private final Estado bolEnviado = new Estado("BOL", "Enviado", "");
    private final Estado remEnviado = new Estado("REM", "Enviado", "");
    private final Estado docEnBolsinEnviado = new Estado("DOC", "EnBolsinEnviado", "");
    private final TipoDocumento expediente = new TipoDocumento("EXPEDIENTE", "Expediente médico");
    private final TipoDocumento informe = new TipoDocumento("INFORME", "Informe");

    @Test
    void bolsinConRemitosYDocumentaciones_armaElTextoDelPaso6EnOrden() {
        Bolsin bolsin = bolsin(1001);
        Remito r500 = remito(500);
        r500.agregarDetalle(detalle(1, "Expediente-cardiológico", expediente));
        r500.agregarDetalle(detalle(2, "Informe de alta", informe));
        Remito r501 = remito(501);
        r501.agregarDetalle(detalle(3, "Estudios-complementarios", expediente));
        bolsin.agregarRemito(r500);
        bolsin.agregarRemito(r501);

        assertThat(bolsin.obtenerInformacionRemito()).isEqualTo(
                "Remito #500\n"
                        + "  Asunto: Expediente-cardiológico | Tipo: EXPEDIENTE\n"
                        + "  Asunto: Informe de alta | Tipo: INFORME\n"
                        + "Remito #501\n"
                        + "  Asunto: Estudios-complementarios | Tipo: EXPEDIENTE\n");
    }

    @Test
    void bolsinSinRemitos_devuelveVacio() {
        assertThat(bolsin(1003).obtenerInformacionRemito()).isEmpty();
    }

    @Test
    void remitoSinDetalles_soloPoneElNumeroDelRemito() {
        Bolsin bolsin = bolsin(1004);
        Remito r503 = remito(503);
        Remito r504 = remito(504);
        r504.agregarDetalle(detalle(4, "Turno-derivación", expediente));
        bolsin.agregarRemito(r503);
        bolsin.agregarRemito(r504);

        assertThat(r503.buscarDocumentacion()).isEmpty();
        assertThat(bolsin.obtenerInformacionRemito()).isEqualTo(
                "Remito #503\n"
                        + "Remito #504\n"
                        + "  Asunto: Turno-derivación | Tipo: EXPEDIENTE\n");
    }

    @Test
    void cadaLlamadaCreaIteradoresNuevos_mismoResultado() {
        Bolsin bolsin = bolsin(1005);
        Remito r505 = remito(505);
        r505.agregarDetalle(detalle(5, "Asunto 5", informe));
        bolsin.agregarRemito(r505);

        String primera = bolsin.obtenerInformacionRemito();
        String segunda = bolsin.obtenerInformacionRemito();

        assertThat(segunda).isEqualTo(primera).isEqualTo("Remito #505\n  Asunto: Asunto 5 | Tipo: INFORME\n");
    }

    @Test
    void detalleRemito_getDocumentacionDevuelveAsuntoYTipo() {
        assertThat(detalle(6, "Historia clínica", informe).getDocumentacion())
                .isEqualTo("Asunto: Historia clínica | Tipo: INFORME");
    }

    private Bolsin bolsin(int numero) {
        return new Bolsin(numero, buenosAires, cordoba, bolEnviado);
    }

    private Remito remito(int numero) {
        return new Remito(numero, LocalDate.now(), remEnviado, buenosAires, cordoba);
    }

    private DetalleRemito detalle(int numeroDoc, String asunto, TipoDocumento tipo) {
        return new DetalleRemito(new Documentacion(numeroDoc, asunto, "", docEnBolsinEnviado, tipo));
    }
}
