import main.Controllers.RecepcionBolsinController;
import main.Infrastructure.Database.DatabaseManager;
import main.Infrastructure.Datasources.SqliteBolsinDatasource;
import main.Infrastructure.Datasources.SqliteUsuarioDatasource;
import main.Infrastructure.Repositories.BolsinRepositoryImpl;
import main.Infrastructure.Repositories.UsuarioRepositoryImpl;
import main.Gestores.GestorRecepcionBolsin;
import main.Presentation.Router.AppRouter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

void main() {
    String nombreUsuarioLogueado = "jero";

    try (Connection seedConn = DriverManager.getConnection("jdbc:sqlite:bolsines.db")) {
        seedConn.createStatement().execute("PRAGMA foreign_keys = ON");
        SqliteBolsinDatasource seedDatasource = new SqliteBolsinDatasource(seedConn);
        seedDatasource.cargarDatosDePrueba();
    } catch (Exception e) {
        e.printStackTrace();
    }

    DatabaseManager.getInstance().inicializar();

    SqliteBolsinDatasource bolsinDatasource = new SqliteBolsinDatasource();
    SqliteUsuarioDatasource usuarioDatasource = new SqliteUsuarioDatasource();

    BolsinRepositoryImpl bolsinRepository = new BolsinRepositoryImpl(bolsinDatasource);
    UsuarioRepositoryImpl usuarioRepository = new UsuarioRepositoryImpl(usuarioDatasource);

    GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(
            nombreUsuarioLogueado,
            bolsinRepository,
            usuarioRepository
    );

    RecepcionBolsinController controller = new RecepcionBolsinController(gestor);

    AppRouter router = new AppRouter(controller);
    router.iniciar();
}
