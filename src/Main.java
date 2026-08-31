import main.Controllers.GestorRecepcionBolsin;
import main.Entities.ComisionMedica;
import main.Entities.Empleado;
import main.Entities.Sesion;
import main.Entities.Usuario;



void main() {

    ComisionMedica cm = new ComisionMedica(
            1,
            "Cordoba",
            "Colon 100",
            "cm@correo.com"
    );

    Empleado empleado = new Empleado(
            "Jeronimo",
            "Abdala",
            "mail@mail.com",
            cm
    );

    Usuario usuario = new Usuario(
            "jero",
            "1234",
            empleado
    );

    Sesion sesion = new Sesion(usuario);

    GestorRecepcionBolsin gestor =
            new GestorRecepcionBolsin(sesion);

    Usuario usuarioLogueado =
            gestor.buscarUsuarioLogueado();

    System.out.println(
            usuarioLogueado.getNombreUsuario()
    );
}