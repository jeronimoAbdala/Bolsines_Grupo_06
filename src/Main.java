import main.Controllers.RecepcionBolsinController;
import main.Datasources.MemoryBolsinDatasource;
import main.Datasources.MemoryUsuarioDatasource;
import main.Gestores.GestorRecepcionBolsin;
import main.Presentation.Router.AppRouter;
import main.Presentation.Screens.PantallaRegistrarRecepcionBolsin;
import main.Repositories.BolsinRepositoryImpl;
import main.Repositories.UsuarioRepositoryImpl;

void main() {
    String nombreUsuarioLogueado = "jero";

    MemoryBolsinDatasource bolsinDatasource = new MemoryBolsinDatasource();
    MemoryUsuarioDatasource usuarioDatasource = new MemoryUsuarioDatasource();

    BolsinRepositoryImpl bolsinRepository = new BolsinRepositoryImpl(bolsinDatasource);
    UsuarioRepositoryImpl usuarioRepository = new UsuarioRepositoryImpl(usuarioDatasource);

    GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(
            nombreUsuarioLogueado,
            bolsinRepository,
            usuarioRepository
    );

    RecepcionBolsinController controller = new RecepcionBolsinController(gestor);

    PantallaRegistrarRecepcionBolsin pantalla =
            new PantallaRegistrarRecepcionBolsin(controller);

    AppRouter router = new AppRouter(pantalla);

    router.iniciar();
}
