package main.Domain.Iterator;

import main.Domain.Entities.DetalleRemito;

import java.util.Arrays;

/**
 * Patron Iterator - rol ConcreteIterator: recorre los detalles de un remito.
 * Lo crea y lo usa Remito (en crearIterador / buscarDocumentacion, paso 6 del CU 28).
 */
public class IteradorDetalle implements IIterador {

    private DetalleRemito[] detalleRemito;
    private int posicionActual;

    /**
     * new(elementos: Object[]) del diagrama. Remito pasa detalleRemito.toArray(), que es un Object[]
     * (no un DetalleRemito[]): por eso se copia a un DetalleRemito[] en vez de castear el arreglo.
     */
    public IteradorDetalle(Object[] elementos) {
        this.detalleRemito = Arrays.copyOf(elementos, elementos.length, DetalleRemito[].class);
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
        return posicionActual >= detalleRemito.length;
    }

    @Override
    public Object elementoActual() {
        if (haFinalizado()) {
            return null;
        }
        return detalleRemito[posicionActual];
    }
}
