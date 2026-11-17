package main.Domain.Iterator;

/**
 * Patron Iterator - rol Iterator: define como se recorre una coleccion, sin mostrar como
 * estan guardados sus elementos. La implementan IteradorRemito e IteradorDetalle.
 * Uso (paso 6 del CU 28): primero(); while (!haFinalizado()) { elementoActual(); ...; siguiente(); }
 */
public interface IIterador {

    /** Se ubica en el primer elemento (reinicia el recorrido). */
    void primero();

    /** Avanza al elemento siguiente. */
    void siguiente();

    /** true si ya no quedan elementos por recorrer. */
    boolean haFinalizado();

    /** Elemento en la posicion actual (null si el recorrido termino). Quien lo recibe hace el cast. */
    Object elementoActual();
}
