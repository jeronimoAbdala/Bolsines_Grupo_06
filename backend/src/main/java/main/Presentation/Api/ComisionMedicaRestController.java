package main.Presentation.Api;

import main.Controllers.RecepcionBolsinController;
import main.Presentation.Api.Dto.ComisionMedicaDto;
import main.Presentation.Api.Dto.Requests;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comisiones")
public class ComisionMedicaRestController {

    private final RecepcionBolsinController controller;

    public ComisionMedicaRestController(RecepcionBolsinController controller) {
        this.controller = controller;
    }

    @GetMapping
    public List<ComisionMedicaDto> listar() {
        return controller.obtenerComisionesMedicas().stream().map(ComisionMedicaDto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComisionMedicaDto crear(@RequestBody Requests.CrearComisionMedica req) {
        if (req.codigo() <= 0) {
            throw ApiException.invalido("El codigo debe ser un entero positivo.");
        }
        String nombre = Validar.requerido(req.nombre(), "nombre");
        boolean repetido = controller.obtenerComisionesMedicas().stream().anyMatch(cm -> cm.getCodigo() == req.codigo());
        if (repetido) {
            throw ApiException.conflicto("Ya existe una comision medica con el codigo " + req.codigo() + ".");
        }
        return ComisionMedicaDto.de(controller.crearComisionMedica(req.codigo(), nombre,
                Validar.opcional(req.direccion()), Validar.opcional(req.email()), Validar.opcional(req.telefono())));
    }
}
