package main.Config;

import main.Controllers.AdministracionController;
import main.Controllers.RecepcionBolsinController;
import main.Domain.Entities.Sesion;
import main.Domain.Entities.Usuario;
import main.Domain.Repositories.BolsinRepository;
import main.Domain.Repositories.EstadoRepository;
import main.Domain.Repositories.UsuarioRepository;
import main.Gestores.GestorAdministracion;
import main.Gestores.GestorRecepcionBolsin;
import main.Infrastructure.Database.DatabaseManager;
import main.Infrastructure.Datasources.SqliteBolsinDatasource;
import main.Infrastructure.Datasources.SqliteEstadoDatasource;
import main.Infrastructure.Datasources.SqliteUsuarioDatasource;
import main.Infrastructure.Repositories.BolsinRepositoryImpl;
import main.Infrastructure.Repositories.EstadoRepositoryImpl;
import main.Infrastructure.Repositories.UsuarioRepositoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Composition root: arma la cadena de capas (Datasource -> Repository -> Gestor -> Controller).
 */
@Configuration
public class AppConfig {

    @Bean
    public DatabaseManager databaseManager() {
        DatabaseManager db = DatabaseManager.getInstance();
        db.inicializar();
        return db;
    }

    @Bean
    public EstadoRepository estadoRepository(DatabaseManager db) {
        SqliteEstadoDatasource datasource = new SqliteEstadoDatasource(db.getConnection());
        datasource.cargarDatosDePrueba();
        return new EstadoRepositoryImpl(datasource);
    }

    @Bean
    public UsuarioRepository usuarioRepository(DatabaseManager db) {
        return new UsuarioRepositoryImpl(new SqliteUsuarioDatasource(db.getConnection()));
    }

    // Recibe EstadoRepository para que los estados ya esten cargados al crear los datos de prueba
    @Bean
    public BolsinRepository bolsinRepository(DatabaseManager db, EstadoRepository estadoRepository) {
        SqliteBolsinDatasource datasource = new SqliteBolsinDatasource(db.getConnection());
        datasource.cargarDatosDePrueba();
        return new BolsinRepositoryImpl(datasource);
    }

    @Bean
    public RecepcionBolsinController recepcionBolsinController(
            @Value("${bolsines.usuario-logueado}") String nombreUsuarioLogueado,
            BolsinRepository bolsinRepository, UsuarioRepository usuarioRepository,
            EstadoRepository estadoRepository) {
        // Todavia no hay login: la sesion actual se arma con el usuario configurado
        Usuario usuario = usuarioRepository.buscarPorNombre(nombreUsuarioLogueado);
        Sesion actualSesion = new Sesion(usuario);
        return new RecepcionBolsinController(
                new GestorRecepcionBolsin(actualSesion, bolsinRepository, usuarioRepository, estadoRepository));
    }

    @Bean
    public AdministracionController administracionController(
            BolsinRepository bolsinRepository, UsuarioRepository usuarioRepository,
            EstadoRepository estadoRepository) {
        return new AdministracionController(
                new GestorAdministracion(bolsinRepository, usuarioRepository, estadoRepository));
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${bolsines.cors.origen}") String origen) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins(origen).allowedMethods("GET", "POST");
            }
        };
    }
}
