package main.Presentation.Api;

import main.Gestores.GestorRecepcionBolsin;
import main.Domain.Entities.ComisionMedica;
import main.Dto.Requests;
import main.Dto.UsuarioDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final GestorRecepcionBolsin gestor;

    public UsuarioRestController(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return gestor.obtenerTodosEmpleados().stream().map(UsuarioDto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@RequestBody Requests.CrearUsuario req) {
        String nombreUsuario = Validar.requerido(req.nombreUsuario(), "nombreUsuario");
        String password = Validar.requerido(req.password(), "password");
        String nombre = Validar.requerido(req.nombre(), "nombre");
        String apellido = Validar.requerido(req.apellido(), "apellido");
        boolean repetido = gestor.obtenerTodosEmpleados().stream()
                .anyMatch(e -> e.getUsuario().getNombreUsuario().equalsIgnoreCase(nombreUsuario));
        if (repetido) {
            throw ApiException.conflicto("Ya existe el usuario '" + nombreUsuario + "'.");
        }
        ComisionMedica cm = gestor.obtenerComisionesMedicas().stream()
                .filter(c -> c.getCodigo() == req.comisionMedicaCodigo())
                .findFirst()
                .orElseThrow(() -> ApiException.invalido("No existe la comision medica con codigo "
                        + req.comisionMedicaCodigo() + "."));

        gestor.crearUsuario(nombreUsuario, password, nombre, apellido, Validar.opcional(req.mail()), cm);
        return gestor.obtenerTodosEmpleados().stream()
                .filter(e -> e.getUsuario().getNombreUsuario().equals(nombreUsuario))
                .findFirst()
                .map(UsuarioDto::de)
                .orElseThrow(() -> new IllegalStateException("El usuario no se guardo."));
    }
}
