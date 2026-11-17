package main.Domain.Iterator;

import main.Domain.Entities.Remito;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IteradorRemito solo (sin Spring ni base): orden, fin del recorrido, primero() reinicia, coleccion vacia.
 * Los remitos se crean en memoria; el iterador no mira sus datos, solo los recorre.
 */
class IteradorRemitoTest {

    private final Remito r500 = remito(500);
    private final Remito r501 = remito(501);
    private final Remito r502 = remito(502);

    @Test
    void recorreLosRemitosEnOrden() {
        IIterador iterador = new IteradorRemito(new Object[]{r500, r501, r502});

        assertThat(recorrer(iterador)).containsExactly(r500, r501, r502);
    }

    @Test
    void alTerminar_haFinalizadoYElementoActualDevuelveNull() {
        IIterador iterador = new IteradorRemito(new Object[]{r500, r501});
        iterador.primero();

        assertThat(iterador.haFinalizado()).isFalse();
        assertThat(iterador.elementoActual()).isSameAs(r500);
        iterador.siguiente();
        assertThat(iterador.haFinalizado()).isFalse();
        assertThat(iterador.elementoActual()).isSameAs(r501);
        iterador.siguiente();
        assertThat(iterador.haFinalizado()).isTrue();
        assertThat(iterador.elementoActual()).isNull();
    }

    @Test
    void primero_reiniciaElRecorrido() {
        IIterador iterador = new IteradorRemito(new Object[]{r500, r501});
        recorrer(iterador);
        assertThat(iterador.haFinalizado()).isTrue();

        iterador.primero();

        assertThat(iterador.haFinalizado()).isFalse();
        assertThat(iterador.elementoActual()).isSameAs(r500);
        assertThat(recorrer(iterador)).containsExactly(r500, r501);
    }

    @Test
    void coleccionVacia_terminaEnseguida() {
        IIterador iterador = new IteradorRemito(new Object[0]);
        iterador.primero();

        assertThat(iterador.haFinalizado()).isTrue();
        assertThat(iterador.elementoActual()).isNull();
        assertThat(recorrer(iterador)).isEmpty();
    }

    @Test
    void aceptaElObjectArrayDeToArray() {
        // Bolsin pasa remitos.toArray(): un Object[] real (castearlo a Remito[] daria ClassCastException).
        List<Remito> remitos = List.of(r500, r501);
        Object[] elementos = remitos.toArray();
        assertThat(elementos.getClass()).isEqualTo(Object[].class);

        assertThat(recorrer(new IteradorRemito(elementos))).containsExactly(r500, r501);
    }

    /** El mismo recorrido de la secuencia: primero(); while (!haFinalizado()) { elementoActual(); siguiente(); } */
    private static List<Remito> recorrer(IIterador iterador) {
        List<Remito> recorridos = new ArrayList<>();
        iterador.primero();
        while (!iterador.haFinalizado()) {
            recorridos.add((Remito) iterador.elementoActual());
            iterador.siguiente();
        }
        return recorridos;
    }

    private static Remito remito(int numero) {
        return new Remito(numero, LocalDate.now(), null, null, null);
    }
}
