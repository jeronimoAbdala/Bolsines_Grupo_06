package main.Domain.Iterator;

import main.Domain.Entities.DetalleRemito;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IteradorDetalle solo (sin Spring ni base): orden, fin del recorrido, primero() reinicia, coleccion vacia.
 * Los detalles se crean en memoria sin documentacion: el iterador no mira sus datos, solo los recorre.
 */
class IteradorDetalleTest {

    private final DetalleRemito d1 = new DetalleRemito(null, "Area 1");
    private final DetalleRemito d2 = new DetalleRemito(null, "Area 2");
    private final DetalleRemito d3 = new DetalleRemito(null, "Area 3");

    @Test
    void recorreLosDetallesEnOrden() {
        IIterador iterador = new IteradorDetalle(new Object[]{d1, d2, d3});

        assertThat(recorrer(iterador)).containsExactly(d1, d2, d3);
    }

    @Test
    void alTerminar_haFinalizadoYElementoActualDevuelveNull() {
        IIterador iterador = new IteradorDetalle(new Object[]{d1});
        iterador.primero();

        assertThat(iterador.haFinalizado()).isFalse();
        assertThat(iterador.elementoActual()).isSameAs(d1);
        iterador.siguiente();
        assertThat(iterador.haFinalizado()).isTrue();
        assertThat(iterador.elementoActual()).isNull();
    }

    @Test
    void primero_reiniciaElRecorrido() {
        IIterador iterador = new IteradorDetalle(new Object[]{d1, d2});
        recorrer(iterador);
        assertThat(iterador.haFinalizado()).isTrue();

        iterador.primero();

        assertThat(iterador.haFinalizado()).isFalse();
        assertThat(iterador.elementoActual()).isSameAs(d1);
        assertThat(recorrer(iterador)).containsExactly(d1, d2);
    }

    @Test
    void coleccionVacia_terminaEnseguida() {
        IIterador iterador = new IteradorDetalle(new Object[0]);
        iterador.primero();

        assertThat(iterador.haFinalizado()).isTrue();
        assertThat(iterador.elementoActual()).isNull();
        assertThat(recorrer(iterador)).isEmpty();
    }

    @Test
    void aceptaElObjectArrayDeToArray() {
        // Remito pasa detalleRemito.toArray(): un Object[] real (castearlo a DetalleRemito[] daria ClassCastException).
        List<DetalleRemito> detalles = List.of(d1, d2);
        Object[] elementos = detalles.toArray();
        assertThat(elementos.getClass()).isEqualTo(Object[].class);

        assertThat(recorrer(new IteradorDetalle(elementos))).containsExactly(d1, d2);
    }

    /** El mismo recorrido de la secuencia: primero(); while (!haFinalizado()) { elementoActual(); siguiente(); } */
    private static List<DetalleRemito> recorrer(IIterador iterador) {
        List<DetalleRemito> recorridos = new ArrayList<>();
        iterador.primero();
        while (!iterador.haFinalizado()) {
            recorridos.add((DetalleRemito) iterador.elementoActual());
            iterador.siguiente();
        }
        return recorridos;
    }
}
