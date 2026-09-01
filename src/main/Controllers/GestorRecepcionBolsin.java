package main.Controllers;

import main.Domain.Entities.Sesion;
import main.Domain.Entities.Usuario;

public class GestorRecepcionBolsin {

    private Sesion sesionActual;

    public GestorRecepcionBolsin(
            Sesion sesionActual) {

        this.sesionActual = sesionActual;
    }

    public Usuario buscarUsuarioLogueado() {

        return sesionActual.getUsuario();
    }
}