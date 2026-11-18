package main.Dto;

import java.util.List;

/** Respuesta de opcRegistrarRecepcionBolsin(): lo que muestran mostrarCM() (paso 2) y mostrarBolsinesParaSeleccion() (paso 4). */
public record VentanaRecepcionDto(UsuarioDto usuario, List<BolsinParaSeleccionDto> bolsines) {
}
