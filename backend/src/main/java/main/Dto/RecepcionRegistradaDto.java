package main.Dto;

/**
 * Respuesta de tomarConfirmacion() (paso 12 del CU 28): el bolsin recibido, que muestra informarRecepcion(),
 * y si se envio la notificacion del CU 29 (llamarCU29()).
 */
public record RecepcionRegistradaDto(BolsinDto bolsin, boolean notificacionEnviada) {
}
