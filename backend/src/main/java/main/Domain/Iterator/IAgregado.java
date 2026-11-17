package main.Domain.Iterator;

/**
 * Patron Iterator - rol Aggregate: una coleccion que sabe crear el iterador que la recorre.
 * La implementan Bolsin (crea un IteradorRemito) y Remito (crea un IteradorDetalle).
 */
public interface IAgregado {

    /** Crea el iterador concreto para recorrer los elementos dados. */
    IIterador crearIterador(Object[] elementos);
}
