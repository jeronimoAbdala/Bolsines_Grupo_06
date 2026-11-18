package main.Dto;

/**
 * Paso 4 del CU 28: una fila de la lista de bolsines para seleccionar. La arma la Pantalla en
 * mostrarBolsinesParaSeleccion() con los datos que entrega el Gestor (BolsinEnviadoDto); las columnas
 * CM origen y precinto salen de mostrarCMOrigen() y mostrarNroPrecinto().
 */
public record BolsinParaSeleccionDto(int numero, String cmOrigen, String nroPrecinto, int cantRemitos, String estado) {
}
