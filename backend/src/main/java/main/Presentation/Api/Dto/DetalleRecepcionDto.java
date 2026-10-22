package main.Presentation.Api.Dto;

/**
 * Bolsin seleccionado en el CU 28 junto con la informacion de remitos que arma el gestor.
 */
public record DetalleRecepcionDto(BolsinDto bolsin, String informacionRemito) {
}
