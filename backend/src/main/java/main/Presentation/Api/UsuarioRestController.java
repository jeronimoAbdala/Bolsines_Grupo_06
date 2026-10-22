package main.Presentation.Api;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.ComisionMedica;
import main.Presentation.Api.Dto.Requests;
import main.Presentation.Api.Dto.UsuarioDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final RecepcionBolsinController controller;

    public UsuarioRestController(RecepcionBolsinController controller) {
        this.controller = controller;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return controller.obtenerTodosUsuarios().stream().map(UsuarioDto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@RequestBody Requests.CrearUsuario req) {
        String nombreUsuario = Validar.requerido(req.nombreUsuario(), "nombreUsuario");
        String password = Validar.requerido(req.password(), "password");
        String nombre = Validar.requerido(req.nombre(), "nombre");
        String apellido = Validar.requerido(req.apellido(), "apellido");
        boolean repetido = controller.obtenerTodosUsuarios().stream()
                .anyMatch(u -> u.getNombreUsuario().equalsIgnoreCase(nombreUsuario));
        if (repetido) {
            throw ApiException.conflicto("Ya existe el usuario '" + nombreUsuario + "'.");
        }
        ComisionMedica cm = controller.obtenerComisionesMedicas().stream()
                .filter(c -> c.getCodigo() == req.comisionMedicaCodigo())
                .findFirst()
                .orElseThrow(() -> ApiException.invalido("No existe la comision medica con codigo "
                        + req.comisionMedicaCodigo() + "."));

        controller.crearUsuario(nombreUsuario, password, nombre, apellido, Validar.opcional(req.mail()), cm);
        return controller.obtenerTodosUsuarios().stream()
                .filter(u -> u.getNombreUsuario().equals(nombreUsuario))
                .findFirst()
                .map(UsuarioDto::de)
                .orElseThrow(() -> new IllegalStateException("El usuario no se guardo."));
    }
}
