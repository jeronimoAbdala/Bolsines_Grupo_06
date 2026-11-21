package main.Gestores;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gestor del CU 29 Notificar recepcion de bolsin: lo incluye el paso 12 del CU 28 (llamarCU29()).
 * Segun la ERS notifica por email a la CM origen; sin servidor de correo, la notificacion queda en el log.
 */
@Service
public class GestorNotificarRecepcionBolsin {

    private static final Logger log = LoggerFactory.getLogger(GestorNotificarRecepcionBolsin.class);

    /** Notifica a la CM origen que el bolsin fue recibido y devuelve true: la notificacion se envio. */
    public boolean notificarRecepcion(Bolsin bolsin) {
        ComisionMedica cmOrigen = bolsin.getCMOrigen();
        log.info("CU 29: notificacion enviada a {} ({}): el bolsin #{} fue recibido en {} ({}).",
                cmOrigen.getNombre(), cmOrigen.getEmail(), bolsin.getNumero(),
                bolsin.getCMDestino().getNombre(), bolsin.getEstado().getNombre());
        return true;
    }
}
