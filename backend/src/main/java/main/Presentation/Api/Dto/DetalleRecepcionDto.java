package main.Presentation.Api.Dto;

/**
 * Bolsin seleccionado en el CU 28 junto con la informacion de remitos del paso 6: la arma el Bolsin
 * con el patron Iterator y el Gestor la guarda en su atributo informacionRemito.
 */
public record DetalleRecepcionDto(BolsinDto bolsin, String informacionRemito) {
}
