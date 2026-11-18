package main.Dto;

/**
 * Paso 4 del CU 28: datos de un bolsin enviado a la CM del empleado logueado, tal como los obtiene el Gestor
 * en el loop de buscarBolsinesEnviados() (getCMOrigen() y getNroPrecinto(), sin formatear). Con ellos la
 * Pantalla arma cada fila (BolsinParaSeleccionDto) en mostrarCMOrigen() y mostrarNroPrecinto().
 */
public record BolsinEnviadoDto(int numero, ComisionMedicaDto cmOrigen, String nroPrecinto, int cantRemitos, String estado) {
}
