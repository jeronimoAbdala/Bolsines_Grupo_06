package main.Config;

import main.Domain.Entities.Sesion;
import main.Domain.Entities.Usuario;
import main.Repositories.UsuarioRepository;
import main.Gestores.GestorRecepcionBolsin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * No hay login: la Sesion actual se simula con el usuario configurado. Al iniciar (despues de CargadorDatosDePrueba) se busca el usuario
 * configurado en bolsines.usuario-logueado, se crea la Sesion y se registra en el Gestor.
 */
@Component
@Order(2)
public class InicioDeSesion implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InicioDeSesion.class);

    private final UsuarioRepository usuarioRepository;
    private final GestorRecepcionBolsin gestor;
    private final String nombreUsuario;

    public InicioDeSesion(UsuarioRepository usuarioRepository, GestorRecepcionBolsin gestor,
                          @Value("${bolsines.usuario-logueado}") String nombreUsuario) {
        this.usuarioRepository = usuarioRepository;
        this.gestor = gestor;
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public void run(ApplicationArguments args) {
        Usuario usuario = usuarioRepository.findById(nombreUsuario).orElse(null);
        if (usuario == null) {
            log.warn("Sesion: no existe el usuario '{}'; no hay usuario logueado.", nombreUsuario);
            return;
        }
        gestor.setSesionActual(new Sesion(usuario));
        log.info("Sesion iniciada para el usuario '{}'.", nombreUsuario);
    }
}
