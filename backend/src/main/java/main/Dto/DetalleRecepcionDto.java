package main.Dto;

import java.util.List;

/**
 * Respuesta de tomarSeleccionBolsin(): encabezado del bolsin seleccionado (paso 5), la informacion de remitos
 * del paso 6 (la arma el Bolsin con el patron Iterator; mostrarDatosRemito()) y las 4 opciones del paso 7
 * (mostrarOpcionesRecepcion()).
 */
public record DetalleRecepcionDto(BolsinDto bolsin, String informacionRemito, List<String> opcionesRecepcion) {
}
