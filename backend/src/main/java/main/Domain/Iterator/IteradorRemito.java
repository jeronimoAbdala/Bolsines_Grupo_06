package main.Domain.Iterator;

import main.Domain.Entities.Remito;

import java.util.Arrays;

/**
 * Patron Iterator - rol ConcreteIterator: recorre los remitos de un bolsin.
 * Lo crea y lo usa Bolsin (en crearIterador / obtenerInformacionRemito, paso 6 del CU 28).
 */
public class IteradorRemito implements IIterador {

    private Remito[] remitos;
    private int posicionActual;

    /**
     * new(elementos: Object[]) del diagrama. Bolsin pasa remitos.toArray(), que es un Object[]
     * (no un Remito[]): por eso se copia a un Remito[] en vez de castear el arreglo.
     */
    public IteradorRemito(Object[] elementos) {
        this.remitos = Arrays.copyOf(elementos, elementos.length, Remito[].class);
        this.posicionActual = 0;
    }

    @Override
    public void primero() {
        posicionActual = 0;
    }

    @Override
    public void siguiente() {
        posicionActual++;
    }

    @Override
    public boolean haFinalizado() {
        return posicionActual >= remitos.length;
    }

    @Override
    public Object elementoActual() {
        if (haFinalizado()) {
            return null;
        }
        return remitos[posicionActual];
    }
}
