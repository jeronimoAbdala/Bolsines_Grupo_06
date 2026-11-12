package main.Config;

import main.Controllers.RecepcionBolsinController;
import main.Domain.Repositories.BolsinRepository;
import main.Domain.Repositories.EmpleadoRepository;
import main.Domain.Repositories.EstadoRepository;
import main.Domain.Repositories.TipoDocumentoRepository;
import main.Domain.Repositories.UsuarioRepository;
import main.Gestores.GestorRecepcionBolsin;
import main.Infrastructure.Datasources.BolsinJpaDatasource;
import main.Infrastructure.Datasources.ComisionMedicaJpaDatasource;
import main.Infrastructure.Datasources.DocumentacionJpaDatasource;
import main.Infrastructure.Datasources.EmpleadoJpaDatasource;
import main.Infrastructure.Datasources.EstadoJpaDatasource;
import main.Infrastructure.Datasources.RemitoJpaDatasource;
import main.Infrastructure.Datasources.TipoDocumentoJpaDatasource;
import main.Infrastructure.Datasources.UsuarioJpaDatasource;
import main.Infrastructure.Repositories.BolsinRepositoryImpl;
import main.Infrastructure.Repositories.EmpleadoRepositoryImpl;
import main.Infrastructure.Repositories.EstadoRepositoryImpl;
import main.Infrastructure.Repositories.TipoDocumentoRepositoryImpl;
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

    // --- Repositorios: Spring pasa como parametro los datasources (interfaces Spring Data que el
    // mismo genera) y nosotros devolvemos la implementacion del dominio. Infrastructure -> Domain.

    @Bean
    public BolsinRepository bolsinRepository(BolsinJpaDatasource bolsinDatasource,
                                             RemitoJpaDatasource remitoDatasource,
                                             DocumentacionJpaDatasource documentacionDatasource,
                                             ComisionMedicaJpaDatasource comisionMedicaDatasource) {
        return new BolsinRepositoryImpl(bolsinDatasource, remitoDatasource,
                documentacionDatasource, comisionMedicaDatasource);
    }

    @Bean
    public UsuarioRepository usuarioRepository(UsuarioJpaDatasource usuarioDatasource,
                                               ComisionMedicaJpaDatasource comisionMedicaDatasource) {
        return new UsuarioRepositoryImpl(usuarioDatasource, comisionMedicaDatasource);
    }

    @Bean
    public EmpleadoRepository empleadoRepository(EmpleadoJpaDatasource empleadoDatasource) {
        return new EmpleadoRepositoryImpl(empleadoDatasource);
    }

    /** Lo usan CargadorDatosDePrueba y el Gestor (paso 11 del CU y pantallas auxiliares). */
    @Bean
    public EstadoRepository estadoRepository(EstadoJpaDatasource estadoDatasource) {
        return new EstadoRepositoryImpl(estadoDatasource);
    }

    /** Lo usan CargadorDatosDePrueba y las pantallas auxiliares (tipo de documento desde el catalogo). */
    @Bean
    public TipoDocumentoRepository tipoDocumentoRepository(TipoDocumentoJpaDatasource tipoDocumentoDatasource) {
        return new TipoDocumentoRepositoryImpl(tipoDocumentoDatasource);
    }

    // --- Gestor y controller: Spring crea una sola instancia de cada uno, como antes.

    /** Bean para que InicioDeSesion le pueda registrar la Sesion actual al arrancar. */
    @Bean
    public GestorRecepcionBolsin gestorRecepcionBolsin(BolsinRepository bolsinRepository,
                                                       UsuarioRepository usuarioRepository,
                                                       EmpleadoRepository empleadoRepository,
                                                       EstadoRepository estadoRepository,
                                                       TipoDocumentoRepository tipoDocumentoRepository) {
        return new GestorRecepcionBolsin(bolsinRepository, usuarioRepository,
                empleadoRepository, estadoRepository, tipoDocumentoRepository);
    }

    @Bean
    public RecepcionBolsinController recepcionBolsinController(GestorRecepcionBolsin gestor) {
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
