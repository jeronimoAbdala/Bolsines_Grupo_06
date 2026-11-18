package main.Dto;

/**
 * Paso 4 del CU 28: una fila de la lista de bolsines para seleccionar. La arma el Gestor en el loop de
 * buscarBolsinesEnviados() con getCmOrigen() (nombre de la CM origen) y getNroPrecinto() de cada bolsin.
 */
public record BolsinParaSeleccionDto(int numero, String cmOrigen, String nroPrecinto, int cantRemitos, String estado) {
}
