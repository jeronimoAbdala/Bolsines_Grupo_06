package main.Presentation.Api;

import main.Presentation.Api.Dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorDto> manejarApi(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> manejarCuerpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorDto("El cuerpo de la peticion no es valido."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> manejarInesperado(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error inesperado: " + e.getMessage()));
    }
}
