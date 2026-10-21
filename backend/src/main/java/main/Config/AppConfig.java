package main.Config;

import main.Controllers.RecepcionBolsinController;
import main.Gestores.GestorRecepcionBolsin;
import main.Infrastructure.Database.DatabaseManager;
import main.Infrastructure.Datasources.SqliteBolsinDatasource;
import main.Infrastructure.Datasources.SqliteUsuarioDatasource;
import main.Infrastructure.Repositories.BolsinRepositoryImpl;
import main.Infrastructure.Repositories.UsuarioRepositoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Composition root: arma la misma cadena de capas que antes armaba Main.
 */
@Configuration
public class AppConfig {

    @Bean
    public RecepcionBolsinController recepcionBolsinController(
            @Value("${bolsines.usuario-logueado}") String nombreUsuarioLogueado) {
        DatabaseManager.getInstance().inicializar();

        SqliteBolsinDatasource bolsinDatasource = new SqliteBolsinDatasource();
        SqliteUsuarioDatasource usuarioDatasource = new SqliteUsuarioDatasource();
        bolsinDatasource.cargarDatosDePrueba();

        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(
                nombreUsuarioLogueado,
                new BolsinRepositoryImpl(bolsinDatasource),
                new UsuarioRepositoryImpl(usuarioDatasource)
        );
        return new RecepcionBolsinController(gestor);
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
