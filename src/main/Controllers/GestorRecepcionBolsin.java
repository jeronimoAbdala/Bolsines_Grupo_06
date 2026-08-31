package main.Controllers;

import main.Entities.Sesion;
import main.Entities.Usuario;

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