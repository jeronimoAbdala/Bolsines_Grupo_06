package main.Presentation.Api;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Presentation.Api.Dto.BolsinDto;
import main.Presentation.Api.Dto.Requests;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bolsines")
public class BolsinRestController {

    private static final List<String> TIPOS_DOCUMENTO = List.of("EXPEDIENTE", "INFORME", "CERTIFICADO", "OTRO");

    private final RecepcionBolsinController controller;

    public BolsinRestController(RecepcionBolsinController controller) {
        this.controller = controller;
    }

    @GetMapping
    public List<BolsinDto> listar() {
        return controller.obtenerTodosBolsines().stream().map(BolsinDto::de).toList();
    }

    @GetMapping("/{numero}")
    public BolsinDto obtener(@PathVariable int numero) {
        return BolsinDto.de(buscar(numero));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BolsinDto crear(@RequestBody Requests.CrearBolsin req) {
        String precinto = Validar.requerido(req.nroPrecinto(), "nroPrecinto");
        if (req.peso() < 0) {
            throw ApiException.invalido("El peso no puede ser negativo.");
        }
        if (req.cmOrigenCodigo() == req.cmDestinoCodigo()) {
            throw ApiException.invalido("La comision origen y destino deben ser distintas.");
        }
        ComisionMedica origen = buscarComision(req.cmOrigenCodigo());
        ComisionMedica destino = buscarComision(req.cmDestinoCodigo());

        synchronized (controller) {
            int numero = controller.obtenerSiguienteNumeroBolsin();
            controller.crearBolsin(numero, precinto, req.peso(), origen, destino);
            return BolsinDto.de(buscar(numero));
        }
    }

    @PostMapping("/{numero}/remitos")
    @ResponseStatus(HttpStatus.CREATED)
    public BolsinDto agregarRemito(@PathVariable int numero, @RequestBody Requests.CrearRemito req) {
        String asunto = Validar.requerido(req.asunto(), "asunto");
        String tipo = Validar.requerido(req.tipoDocumento(), "tipoDocumento").toUpperCase();
        if (!TIPOS_DOCUMENTO.contains(tipo)) {
            throw ApiException.invalido("Tipo de documento invalido. Valores: " + TIPOS_DOCUMENTO);
        }
        Bolsin bolsin = buscar(numero);

        synchronized (controller) {
            int remitoNumero = controller.obtenerSiguienteNumeroRemito();
            controller.crearRemitoConDocumentacion(numero, remitoNumero,
                    bolsin.getCmOrigen(), bolsin.getCmDestino(),
                    asunto, Validar.opcional(req.descripcion()), tipo);
            return BolsinDto.de(buscar(numero));
        }
    }

    private Bolsin buscar(int numero) {
        Bolsin bolsin = controller.buscarBolsinPorNumero(numero);
        if (bolsin == null) {
            throw ApiException.noEncontrado("No existe el Bolsin #" + numero + ".");
        }
        return bolsin;
    }

    private ComisionMedica buscarComision(int codigo) {
        return controller.obtenerComisionesMedicas().stream()
                .filter(cm -> cm.getCodigo() == codigo)
                .findFirst()
                .orElseThrow(() -> ApiException.invalido("No existe la comision medica con codigo " + codigo + "."));
    }
}
